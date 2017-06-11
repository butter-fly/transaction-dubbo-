package com.lorne.tx.annotation;

/**
 * Created by lorne on 2017/6/7.
 */

import java.lang.annotation.*;

/**
 * 分布式事务注解方法
 * Created by yuliang on 2017/1/19.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TxTransaction {

}
