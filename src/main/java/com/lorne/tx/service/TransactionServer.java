package com.lorne.tx.service;

import com.lorne.tx.bean.TxTransactionInfo;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Created by lorne on 2017/6/8.
 */
public interface TransactionServer {


   // void execute();

  Object  execute(MethodInvocation methodInvocation, TxTransactionInfo info)throws Throwable;

}
