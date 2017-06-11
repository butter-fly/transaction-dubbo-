package com.lorne.tx.service.impl;

import com.alibaba.dubbo.rpc.RpcContext;
import com.lorne.tx.mq.model.TxGroup;
import com.lorne.tx.mq.service.MQTxManagerService;
import com.lorne.tx.service.TransactionRunningService;
import com.lorne.tx.service.model.ServiceThreadModel;
import com.lorne.core.framework.Constant;
import com.lorne.core.framework.utils.KidUtils;
import com.lorne.core.framework.utils.task.ConditionUtils;
import com.lorne.core.framework.utils.task.IBack;
import com.lorne.core.framework.utils.task.Task;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.concurrent.TimeUnit;

/**
 * Created by lorne on 2017/6/9.
 */
@Service
public class TransactionRunningServiceImpl implements TransactionRunningService {




    @Autowired
    private PlatformTransactionManager txManager;



    @Autowired
    private MQTxManagerService txManagerService;


    @Override
    public ServiceThreadModel serviceInThread(boolean signTask, String _groupId, String url, Task task, MethodInvocation methodInvocation) {


        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);

        TransactionStatus status = txManager.getTransaction(def);

        String kid = KidUtils.generateShortUuid();

        Task waitTask = ConditionUtils.getInstance().createTask(kid);

        TxGroup txGroup =  txManagerService.addTransactionGroup(_groupId,kid,url);

        try {
            RpcContext.getContext().setAttachment("tx-group",_groupId);
            final Object res = methodInvocation.proceed();
            task.setBack(new IBack() {
                @Override
                public Object doing(Object... objects) throws Throwable {
                    return res;
                }
            });
            //通知TxManager调用成功
            txManagerService.notifyTransactionInfo(_groupId,kid,true);
        } catch (final Throwable throwable) {
            task.setBack(new IBack() {
                @Override
                public Object doing(Object... objects) throws Throwable{
                    throw new Throwable(throwable);
                }
            });
            //通知TxManager调用失败
            txManagerService.notifyTransactionInfo(_groupId,kid,false);
        }

        if(signTask)
            task.signalTask();


        ServiceThreadModel model = new ServiceThreadModel();
        model.setStatus(status);
        model.setWaitTask(waitTask);
        model.setTxGroup(txGroup);

        return model;

    }


    @Override
    public void serviceWait(boolean signTask,Task task,ServiceThreadModel model) {
        Task waitTask = model.getWaitTask();
        final String taskId = waitTask.getKey();
        TransactionStatus status = model.getStatus();

        Constant.scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                Task task = ConditionUtils.getInstance().getTask(taskId);
                if(task.getState()==0) {
                    task.setBack(new IBack() {
                        @Override
                        public Object doing(Object... objects) throws Throwable {
                            return false;
                        }
                    });
                    task.signalTask();
                }
            }
        },model.getTxGroup().getWaitTime(), TimeUnit.SECONDS);

        waitTask.awaitTask();

        try {
            Boolean state =  (Boolean) waitTask.getBack().doing();
            if(state){
                txManager.commit(status);
                if(!signTask){
                    task.signalTask();
                }
            }else{
                txManager.rollback(status);
                if(!signTask){
                    task.setBack(new IBack() {
                        @Override
                        public Object doing(Object... objs) throws Throwable {
                             throw new Throwable("分布式事务已回归.");
                        }
                    });
                    task.signalTask();
                }
            }
        } catch (Throwable throwable) {
            txManager.rollback(status);
        }finally {
            waitTask.remove();
        }
    }


}
