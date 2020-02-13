package org.apache.dolphinscheduler.remote.future;

import org.apache.dolphinscheduler.remote.command.Command;

import java.util.concurrent.CountDownLatch;

/**
 * @Author: Tboy
 */
public class ResponseFuture {

    private final int opaque;

    private final long timeoutMillis;

    private final InvokeCallback invokeCallback;

    private final long beginTimestamp = System.currentTimeMillis();

    private final CountDownLatch latch = new CountDownLatch(1);

    public ResponseFuture(int opaque, long timeoutMillis, InvokeCallback invokeCallback) {
        this.opaque = opaque;
        this.timeoutMillis = timeoutMillis;
        this.invokeCallback = invokeCallback;
    }


}
