package org.apache.dolphinscheduler.remote.future;

import org.apache.dolphinscheduler.remote.command.Command;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Tboy
 */
public class ResponseFuture {

    private final static ConcurrentHashMap<Long,ResponseFuture> FUTURE_TABLE = new ConcurrentHashMap<>(256);

    private final long opaque;

    private final long timeoutMillis;

    private final InvokeCallback invokeCallback;

    private final CountDownLatch latch = new CountDownLatch(1);

    private final long beginTimestamp = System.currentTimeMillis();

    private volatile Command responseCommand;

    private volatile boolean sendOk = true;

    private volatile Throwable cause;


    public ResponseFuture(long opaque, long timeoutMillis, InvokeCallback invokeCallback) {
        this.opaque = opaque;
        this.timeoutMillis = timeoutMillis;
        this.invokeCallback = invokeCallback;
        FUTURE_TABLE.put(opaque, this);
    }

    public Command waitResponse() throws InterruptedException {
        this.latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        return this.responseCommand;
    }

    public void putResponse(final Command responseCommand) {
        this.responseCommand = responseCommand;
        this.latch.countDown();
        FUTURE_TABLE.remove(opaque);
    }

    public static ResponseFuture getFuture(long opaque){
        return FUTURE_TABLE.get(opaque);
    }

    public boolean isTimeout() {
        long diff = System.currentTimeMillis() - this.beginTimestamp;
        return diff > this.timeoutMillis;
    }

    public void executeInvokeCallback() {
        if (invokeCallback != null) {
            invokeCallback.operationComplete(this);
        }
    }

    public boolean isSendOK() {
        return sendOk;
    }

    public void setSendOk(boolean sendOk) {
        this.sendOk = sendOk;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public Throwable getCause() {
        return cause;
    }

    public long getOpaque() {
        return opaque;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public long getBeginTimestamp() {
        return beginTimestamp;
    }

    public Command getResponseCommand() {
        return responseCommand;
    }

    public InvokeCallback getInvokeCallback() {
        return invokeCallback;
    }
}
