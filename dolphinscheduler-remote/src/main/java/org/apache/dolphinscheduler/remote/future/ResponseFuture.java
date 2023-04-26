/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.remote.future;

import org.apache.dolphinscheduler.remote.command.Message;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

/**
 * response future
 */
@Slf4j
public class ResponseFuture {

    private static final ConcurrentHashMap<Long, ResponseFuture> FUTURE_TABLE = new ConcurrentHashMap<>(256);

    /**
     * request unique identification
     */
    private final long opaque;

    /**
     * timeout
     */
    private final long timeoutMillis;

    /**
     * invokeCallback function
     */
    private final InvokeCallback invokeCallback;

    /**
     * releaseSemaphore
     */
    private final ReleaseSemaphore releaseSemaphore;

    private final CountDownLatch latch = new CountDownLatch(1);

    private final long beginTimestamp = System.currentTimeMillis();

    /**
     * response command
     */
    private Message responseMessage;

    private volatile boolean sendOk = true;

    private Throwable cause;

    public ResponseFuture(long opaque, long timeoutMillis, InvokeCallback invokeCallback,
                          ReleaseSemaphore releaseSemaphore) {
        this.opaque = opaque;
        this.timeoutMillis = timeoutMillis;
        this.invokeCallback = invokeCallback;
        this.releaseSemaphore = releaseSemaphore;
        FUTURE_TABLE.put(opaque, this);
    }

    /**
     * wait for response
     *
     * @return command
     */
    public Message waitResponse() throws InterruptedException {
        if (!latch.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
            log.warn("Wait response timeout, request id {}", opaque);
        }
        return this.responseMessage;
    }

    /**
     * put response
     *
     * @param responseMessage responseCommand
     */
    public void putResponse(final Message responseMessage) {
        this.responseMessage = responseMessage;
        this.latch.countDown();
        FUTURE_TABLE.remove(opaque);
    }

    public static ResponseFuture getFuture(long opaque) {
        return FUTURE_TABLE.get(opaque);
    }

    public void removeFuture() {
        FUTURE_TABLE.remove(opaque);
    }

    /**
     * whether timeout
     *
     * @return timeout
     */
    public boolean isTimeout() {
        long diff = System.currentTimeMillis() - this.beginTimestamp;
        return diff > this.timeoutMillis;
    }

    /**
     * execute invoke callback
     */
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

    public Message getResponseCommand() {
        return responseMessage;
    }

    public void setResponseCommand(Message responseMessage) {
        this.responseMessage = responseMessage;
    }

    public InvokeCallback getInvokeCallback() {
        return invokeCallback;
    }

    /**
     * release
     */
    public void release() {
        if (this.releaseSemaphore != null) {
            this.releaseSemaphore.release();
        }
    }

    /**
     * scan future table
     */
    public static void scanFutureTable() {
        Iterator<Map.Entry<Long, ResponseFuture>> it = FUTURE_TABLE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, ResponseFuture> next = it.next();
            ResponseFuture future = next.getValue();
            if ((future.getBeginTimestamp() + future.getTimeoutMillis() + 1000) > System.currentTimeMillis()) {
                continue;
            }
            try {
                // todo: use thread pool to execute the async callback, otherwise will block the scan thread
                future.release();
                future.executeInvokeCallback();
            } catch (Exception ex) {
                log.error("ScanFutureTable, execute callback error, requestId: {}", future.getOpaque(), ex);
            }
            it.remove();
            log.debug("Remove timeout request: {}", future);
        }
    }

    @Override
    public String toString() {
        return "ResponseFuture{"
                + "opaque=" + opaque
                + ", timeoutMillis=" + timeoutMillis
                + ", invokeCallback=" + invokeCallback
                + ", releaseSemaphore=" + releaseSemaphore
                + ", latch=" + latch
                + ", beginTimestamp=" + beginTimestamp
                + ", responseCommand=" + responseMessage
                + ", sendOk=" + sendOk
                + ", cause=" + cause
                + '}';
    }
}
