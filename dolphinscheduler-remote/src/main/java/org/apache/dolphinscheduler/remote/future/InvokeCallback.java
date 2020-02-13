package org.apache.dolphinscheduler.remote.future;

/**
 * @Author: Tboy
 */
public interface InvokeCallback {

    void operationComplete(final ResponseFuture responseFuture);

}
