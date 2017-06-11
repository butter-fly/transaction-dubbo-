package com.lorne.tx.service.impl;

import com.lorne.tx.bean.TransactionLocal;
import com.lorne.tx.bean.TxTransactionInfo;
import com.lorne.tx.service.TransactionServer;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.stereotype.Service;

/**
 * Created by lorne on 2017/6/8.
 */
@Service(value = "txDefaultTransactionServer")
public class TxDefaultTransactionServerImpl implements TransactionServer {


    @Override
    public Object execute(MethodInvocation methodInvocation, TxTransactionInfo info) throws Throwable {

        boolean hasStart = false;
        TransactionLocal transactionLocal =  info.getTransactionLocal();
        if(transactionLocal==null){
            transactionLocal = new TransactionLocal();
            TransactionLocal.setCurrent(transactionLocal);
            hasStart = true;
        }

        Object obj =  methodInvocation.proceed();

        if(hasStart){
            TransactionLocal.setCurrent(null);
        }
        return obj;
    }
}
