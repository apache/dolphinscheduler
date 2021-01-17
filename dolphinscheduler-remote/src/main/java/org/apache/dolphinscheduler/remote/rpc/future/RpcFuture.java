package org.apache.dolphinscheduler.remote.rpc.future;

import org.apache.dolphinscheduler.remote.rpc.common.RpcResponse;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * RpcFuture
 */
public class RpcFuture implements Future<Object> {

    private CountDownLatch latch = new CountDownLatch(1);

    private RpcResponse response;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public RpcResponse get() throws InterruptedException, ExecutionException {
        boolean b = latch.await(5,TimeUnit.SECONDS);
        return response;
    }

    @Override
    public RpcResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean b = latch.await(timeout,unit);
        return response;
    }

    public void done(RpcResponse response){
        this.response = response;
        latch.countDown();
    }
}
