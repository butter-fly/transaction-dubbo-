package com.lorne.tx.interceptor;


import com.alibaba.dubbo.rpc.RpcContext;
import com.lorne.tx.annotation.TxTransaction;
import com.lorne.tx.bean.TransactionLocal;
import com.lorne.tx.bean.TxTransactionInfo;
import com.lorne.tx.bean.TxTransactionLocal;
import com.lorne.tx.service.TransactionServer;
import com.lorne.tx.service.TransactionServerFactoryService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Created by lorne on 2017/6/7.
 */

@Component
public class TxManagerInterceptor implements MethodInterceptor{



    @Autowired
    private TransactionServerFactoryService transactionServerFactoryService;

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {

        Method method = methodInvocation.getMethod();
        Class<?> clazz = methodInvocation.getThis().getClass();
        Method thisMethod =  clazz.getMethod(method.getName(),method.getParameterTypes());

        TxTransaction transaction =  thisMethod.getAnnotation(TxTransaction.class);

        TxTransactionLocal txTransactionLocal =  TxTransactionLocal.current();

        String groupId = RpcContext.getContext().getAttachment("tx-group");

        TransactionLocal transactionLocal = TransactionLocal.current();

        TxTransactionInfo state = new TxTransactionInfo(transaction, txTransactionLocal,groupId,transactionLocal);

        TransactionServer server =  transactionServerFactoryService.createTransactionServer(state);

        return server.execute(methodInvocation,state);

    }
}
