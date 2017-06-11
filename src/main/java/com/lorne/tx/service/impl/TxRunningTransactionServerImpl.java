package com.lorne.tx.service.impl;

import com.lorne.tx.Constants;
import com.lorne.tx.bean.TxTransactionInfo;
import com.lorne.tx.bean.TxTransactionLocal;
import com.lorne.tx.service.TransactionRunningService;
import com.lorne.tx.service.TransactionServer;
import com.lorne.tx.service.model.ServiceThreadModel;
import com.lorne.tx.utils.DubboUtils;
import com.lorne.core.framework.utils.KidUtils;
import com.lorne.core.framework.utils.task.ConditionUtils;
import com.lorne.core.framework.utils.task.Task;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 分布式事务启动开始时的业务处理
 * Created by lorne on 2017/6/8.
 */
@Service(value = "txRunningTransactionServer")
public class TxRunningTransactionServerImpl implements TransactionServer {




    @Autowired
    private TransactionRunningService transactionRunningService;


    @Override
    public Object execute(final MethodInvocation methodInvocation,final TxTransactionInfo info)throws Throwable {
        //分布式事务开始执行
        System.out.println("tx-running-start");

        final String groupId =  info.getTxTransactionLocal()==null?null:info.getTxTransactionLocal().getGroupId();

        final String taskId = KidUtils.generateShortUuid();
        final Task task = ConditionUtils.getInstance().createTask(taskId);
        final String url = DubboUtils.getProviderUrl();

        Constants.threadPool.execute(new Runnable() {
            @Override
            public void run() {
                String _groupId = "";
                if(StringUtils.isEmpty(groupId)){
                    String txGroupId = info.getTxGroupId();
                    _groupId = txGroupId;
                    if(StringUtils.isNotEmpty(txGroupId)){
                        TxTransactionLocal txTransactionLocal = new TxTransactionLocal();
                        txTransactionLocal.setGroupId(txGroupId);
                        TxTransactionLocal.setCurrent(txTransactionLocal);
                    }
                }else{
                     _groupId = groupId;
                }

                System.out.println("taskId-id-tx-running:"+taskId);

                ServiceThreadModel model =  transactionRunningService.serviceInThread(true,_groupId,url,task,methodInvocation);

                transactionRunningService.serviceWait(true,task,model);
            }
        });

        task.awaitTask();

        System.out.println("tx-running-end");
        //分布式事务执行完毕
        try{
            return task.getBack().doing();
        }finally {
            task.remove();
        }
    }
}
