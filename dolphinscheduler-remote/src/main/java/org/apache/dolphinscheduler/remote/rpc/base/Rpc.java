package org.apache.dolphinscheduler.remote.rpc.base;

import org.apache.dolphinscheduler.remote.rpc.common.AbstractRpcCallBack;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Rpc
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Rpc {

    int retries() default 3;

    boolean async() default false;

    Class<? extends AbstractRpcCallBack> callback() default AbstractRpcCallBack.class;

}
