package com.lorne.tx;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lorne on 2017/6/8.
 */
public class Constants {




    public static ExecutorService threadPool = null;



    /**
     * 最大事务时间（秒）
     */
    public static long MAX_TIMEOUT = 10;

    static {
        threadPool = Executors.newCachedThreadPool();
    }
}
