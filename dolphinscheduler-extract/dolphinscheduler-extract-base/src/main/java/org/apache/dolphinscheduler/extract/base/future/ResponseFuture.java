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

package org.apache.dolphinscheduler.extract.base.future;

import org.apache.dolphinscheduler.extract.base.IRpcResponse;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import io.netty.channel.Channel;

@ToString
@Slf4j
public class ResponseFuture {

    private static final ConcurrentHashMap<Long, ResponseFuture> FUTURE_TABLE = new ConcurrentHashMap<>();

    private final long opaque;

    private final Channel channel;

    private final long timeoutMillis;

    private final CountDownLatch latch = new CountDownLatch(1);

    private final long beginTimestamp = System.currentTimeMillis();

    @Getter
    @Setter
    // volatile: the TIMEOUT path in waitResponse returns this field after an await that timed
    // out (not after being countDown-woken) — without volatile that read has no happens-before
    // edge against a racing putResponse on the event loop and may see a partially-published
    // response object.
    private volatile IRpcResponse iRpcResponse;

    private volatile boolean sendOk = true;

    /**
     * The failure cause, set-once: the first cause wins and later notifications are ignored.
     * Two failure paths can hit the same future concurrently — the write-failure listener
     * (caller thread) and the channel-error drain (event loop thread) — so a plain field would
     * let whichever lands last overwrite the terminal state, and the caller would see an error
     * that depends on thread interleaving.
     */
    private final AtomicReference<Throwable> cause = new AtomicReference<>();

    /**
     * Guards that the future is completed exactly once. A channel failure may drain several
     * pending futures concurrently with a late response arrival; first completion wins so a
     * real response cannot be clobbered by a null error completion (or vice versa).
     */
    private final AtomicBoolean done = new AtomicBoolean(false);

    public ResponseFuture(long opaque, long timeoutMillis, Channel channel) {
        this.opaque = opaque;
        this.timeoutMillis = timeoutMillis;
        this.channel = channel;
        FUTURE_TABLE.put(opaque, this);
    }

    /**
     * wait for response
     *
     * @return command
     */
    public IRpcResponse waitResponse() throws InterruptedException {
        if (!latch.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
            log.warn("Wait response in {}/ms timeout, request id {}", timeoutMillis, opaque);
        }
        return this.iRpcResponse;
    }

    public void putResponse(final IRpcResponse iRpcResponse) {
        if (done.compareAndSet(false, true)) {
            this.iRpcResponse = iRpcResponse;
            this.latch.countDown();
        }
        // Remove by INSTANCE identity: the retry path registers a replacement future under the
        // same opaque, and a stale future completing late must not evict the live replacement
        // from the table (that would force the replacement's caller to wait out its full
        // timeout even after the real response arrives).
        FUTURE_TABLE.remove(opaque, this);
    }

    /**
     * The caller stopped waiting (timeout). Completes the future and drops it from the global
     * table so a timed-out request leaks nothing — no response, failure notification or channel
     * death will ever arrive to clean it up, because the channel stays healthy (heartbeats).
     * The done-guard makes this a no-op if a response or failure already completed the future,
     * and a late response afterwards finds no future and is dropped.
     */
    public void cancel() {
        putResponse(null);
    }

    /**
     * Fail this future with {@code cause}: complete it (null response) and drop it from the table,
     * waking the caller immediately. Used by the channel-death drain and by the send path when the
     * channel died between registration and the write. No-op if the future already completed
     * successfully — a real response is never clobbered by a failure notification.
     */
    public void fail(Throwable cause) {
        setCause(cause);
        putResponse(null);
    }

    /**
     * FUTURE_TABLE is the single source of truth for in-flight requests: a future is in the table
     * from construction until it completes, and every completion path (response arrival, write
     * failure, timeout, interrupt, drain) removes it. When a channel dies no response can ever
     * arrive on it again, so fail every future sent on THAT channel (identity match — other
     * channels' futures are untouched) instead of letting each caller wait out its RPC timeout.
     *
     * <p>Iterating while {@code fail} removes entries is safe on a ConcurrentHashMap (weakly
     * consistent iterator); channel death is a rare event so the O(in-flight) scan is negligible.
     */
    public static void failAllForChannel(final Channel channel, final Throwable cause) {
        for (final ResponseFuture future : FUTURE_TABLE.values()) {
            if (future.channel == channel) {
                future.fail(cause);
            }
        }
    }

    public static ResponseFuture getFuture(long opaque) {
        return FUTURE_TABLE.get(opaque);
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

    public boolean isSendOK() {
        return sendOk;
    }

    public void setSendOk(boolean sendOk) {
        this.sendOk = sendOk;
    }

    public void setCause(Throwable cause) {
        // First cause wins — this is terminal state, a racing later notification is dropped.
        this.cause.compareAndSet(null, cause);
    }

    public Throwable getCause() {
        return this.cause.get();
    }

}
