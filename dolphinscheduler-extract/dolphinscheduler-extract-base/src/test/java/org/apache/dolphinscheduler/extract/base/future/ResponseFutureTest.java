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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.extract.base.IRpcResponse;
import org.apache.dolphinscheduler.extract.base.exception.RemoteException;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.netty.channel.Channel;

class ResponseFutureTest {

    /**
     * The failure cause of a future is terminal state: once set, a racing failure notification
     * must not overwrite it. Two failure paths can hit the same future concurrently — the
     * write-failure listener (caller thread) and the channel-error drain (event loop thread).
     * Whichever lands first defines the cause the caller sees; a later notification must be
     * ignored, otherwise the reported error depends on thread interleaving.
     */
    @Test
    void setCause_afterFirstCause_isIgnored() throws Exception {
        final ResponseFuture future = new ResponseFuture(999001L, 1_000, Mockito.mock(Channel.class));
        final Throwable firstCause = new RuntimeException("first: channel decoder error");
        final Throwable lateCause = new RuntimeException("late: write failure");

        future.setCause(firstCause);
        future.putResponse(null); // complete the future on the failure path

        // Late failure notification arrives after the future is already completed — e.g. the
        // write-failure listener racing the channel-error drain.
        future.setCause(lateCause);

        assertSame(firstCause, future.getCause(),
                "Terminal cause must not be overwritten by a racing failure notification");
    }

    /**
     * A cause arriving after a SUCCESSFUL completion must not resurrect the future into an error
     * state — the response field itself must stay intact.
     */
    @Test
    void setCause_afterSuccessfulCompletion_responseStaysIntact() throws Exception {
        final ResponseFuture future = new ResponseFuture(999002L, 1_000, Mockito.mock(Channel.class));
        final IRpcResponse realResponse = Mockito.mock(IRpcResponse.class);

        future.putResponse(realResponse);

        // A late channel-error drain fires after the real response already completed the future.
        future.setCause(new RuntimeException("late cause"));

        assertSame(realResponse, future.waitResponse(),
                "A late failure notification must not clobber a successful response");
    }

    /**
     * A stale future completing late must not evict the REPLACEMENT future registered under the
     * same opaque (the retry path reuses the same transporter/opaque). Removing by key alone
     * would drop the live replacement from FUTURE_TABLE, forcing its caller to wait out the full
     * timeout even after the real response arrives — removal must match the instance identity.
     */
    @Test
    void putResponse_onStaleFuture_doesNotEvictReplacementUnderSameOpaque() {
        final long opaque = 999003L;
        final ResponseFuture staleFuture = new ResponseFuture(opaque, 100, Mockito.mock(Channel.class));
        final ResponseFuture replacement = new ResponseFuture(opaque, 5_000, Mockito.mock(Channel.class));

        // The first attempt completes late — after the retry already registered its own future.
        staleFuture.putResponse(null);

        assertSame(replacement, ResponseFuture.getFuture(opaque),
                "A stale future must not evict the live replacement from FUTURE_TABLE");
    }

    /**
     * FUTURE_TABLE is the single source of truth for in-flight requests; a channel's death must
     * fail every future sent on THAT channel (identity match) and leave futures of other
     * channels completely untouched.
     */
    @Test
    void failAllForChannel_failsOnlyFuturesOfThatChannel() throws Exception {
        final Channel channelA = Mockito.mock(Channel.class);
        final Channel channelB = Mockito.mock(Channel.class);
        final ResponseFuture futureA1 = new ResponseFuture(999101L, 10_000, channelA);
        final ResponseFuture futureA2 = new ResponseFuture(999102L, 10_000, channelA);
        final ResponseFuture futureB = new ResponseFuture(999103L, 10_000, channelB);

        final RemoteException cause = new RemoteException("channel A died");

        ResponseFuture.failAllForChannel(channelA, cause);

        // Futures of channel A: completed with the cause, removed from the table, woken
        // immediately (elapsed far below their 10 s timeout).
        final long start = System.nanoTime();
        assertNull(futureA1.waitResponse(), "Drained future must not carry a response");
        assertNull(futureA2.waitResponse(), "Drained future must not carry a response");
        assertTrue(System.nanoTime() - start < 5_000_000_000L,
                "Drained futures must wake immediately, not wait out their timeout");
        assertSame(cause, futureA1.getCause(), "Future A1 must carry the channel-failure cause");
        assertSame(cause, futureA2.getCause(), "Future A2 must carry the channel-failure cause");
        assertNull(ResponseFuture.getFuture(999101L), "Drained future must be removed from FUTURE_TABLE");
        assertNull(ResponseFuture.getFuture(999102L), "Drained future must be removed from FUTURE_TABLE");

        // Futures of channel B: untouched — still in flight, no cause, still in the table.
        assertNull(futureB.getCause(), "A future of another channel must not be failed");
        assertSame(futureB, ResponseFuture.getFuture(999103L),
                "A future of another channel must stay in FUTURE_TABLE");
        futureB.cancel(); // tidy up: do not leak the untouched future into other tests
    }

    /**
     * A channel-failure drain racing a late response must not clobber the response: the future
     * was already completed successfully, so failAllForChannel is a no-op on it (done-guard), and
     * the caller keeps its real response.
     */
    @Test
    void failAllForChannel_alreadyCompletedFuture_keepsItsResponse() throws Exception {
        final Channel channel = Mockito.mock(Channel.class);
        final ResponseFuture future = new ResponseFuture(999104L, 10_000, channel);
        final IRpcResponse realResponse = Mockito.mock(IRpcResponse.class);
        future.putResponse(realResponse);

        ResponseFuture.failAllForChannel(channel, new RemoteException("late drain"));

        assertSame(realResponse, future.waitResponse(),
                "A late channel drain must not clobber an already-completed response");
        assertNull(ResponseFuture.getFuture(999104L), "Completed future must be removed from FUTURE_TABLE");
    }

    /**
     * The self-fail path used when the channel died between registration and the write: the
     * future is completed with the cause immediately and leaves FUTURE_TABLE, so the caller
     * wakes at once instead of waiting out its timeout.
     */
    @Test
    void fail_completesFutureWithCauseAndRemovesFromTable() throws Exception {
        final ResponseFuture future = new ResponseFuture(999105L, 10_000, Mockito.mock(Channel.class));
        final RemoteException cause = new RemoteException("channel not active");

        final long start = System.nanoTime();
        future.fail(cause);

        assertNull(future.waitResponse(), "Failed future must not carry a response");
        assertTrue(System.nanoTime() - start < 5_000_000_000L,
                "A failed future must wake its caller immediately");
        assertSame(cause, future.getCause(), "The failure cause must be preserved");
        assertNull(ResponseFuture.getFuture(999105L), "Failed future must be removed from FUTURE_TABLE");
    }
}
