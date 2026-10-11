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

package org.apache.dolphinscheduler.extract.base.client;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.extract.base.exception.RemoteException;
import org.apache.dolphinscheduler.extract.base.exception.RemoteTimeoutException;
import org.apache.dolphinscheduler.extract.base.future.ResponseFuture;
import org.apache.dolphinscheduler.extract.base.utils.Host;

import org.junit.jupiter.api.Test;

class NettyRemotingClientTest {

    private static final Host HOST = Host.of("worker-1:1234");

    /**
     * A request whose write failed must report the recorded transport cause. Reporting a bare
     * host string instead makes "worker died mid-RPC" indistinguishable from a slow response.
     */
    @Test
    void buildEmptyResponseException_sendFailed_carriesTheRealTransportCause() {
        final ResponseFuture responseFuture = new ResponseFuture(1L, 1000L, null);
        try {
            responseFuture.setSendOk(false);
            final RuntimeException cause = new RuntimeException("connection reset by peer");
            responseFuture.setCause(cause);

            final RemoteException thrown = NettyRemotingClient.buildEmptyResponseException(HOST, responseFuture, 1000L);

            assertSame(cause, thrown.getCause(), "a failed write must not degrade to a bare host string");
        } finally {
            responseFuture.putResponse(null);
        }
    }

    /**
     * The write-failure listener can record its cause between the caller's cause check and this
     * classification (the drain races the timeout). Re-reading the state here is what keeps the
     * only diagnostic information from being dropped; the no-race case stays a plain timeout.
     */
    @Test
    void buildEmptyResponseException_timedOut_reportsTimeoutAndKeepsALateCause() {
        final ResponseFuture responseFuture = new ResponseFuture(2L, 1000L, null);
        try {
            responseFuture.setSendOk(true);

            assertTrue(NettyRemotingClient.buildEmptyResponseException(HOST, responseFuture,
                    1000L) instanceof RemoteTimeoutException);
            assertNull(NettyRemotingClient.buildEmptyResponseException(HOST, responseFuture, 1000L).getCause());

            final RuntimeException lateCause = new RuntimeException("channel closed");
            responseFuture.setCause(lateCause);

            assertSame(lateCause,
                    NettyRemotingClient.buildEmptyResponseException(HOST, responseFuture, 1000L).getCause(),
                    "a cause recorded after the caller's check must still be surfaced");
        } finally {
            responseFuture.putResponse(null);
        }
    }
}
