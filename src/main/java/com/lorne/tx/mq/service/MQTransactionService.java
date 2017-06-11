package com.lorne.tx.mq.service;

/**
 * Created by lorne on 2017/6/7.
 */
public interface MQTransactionService {


    boolean notify(String kid,boolean state);

    boolean checkRollback(String kid);
}
