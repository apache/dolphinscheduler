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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.extract.base.RpcMethod;
import org.apache.dolphinscheduler.extract.base.StandardRpcResponse;
import org.apache.dolphinscheduler.extract.base.exception.MethodInvocationException;
import org.apache.dolphinscheduler.extract.base.exception.MethodNotFoundException;
import org.apache.dolphinscheduler.extract.base.serialize.JsonSerializer;
import org.apache.dolphinscheduler.extract.base.utils.Host;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SyncClientMethodInvokerTest {

    private interface SampleService {

        @RpcMethod
        String call(String arg);
    }

    private static Method method() throws NoSuchMethodException {
        return SampleService.class.getDeclaredMethod("call", String.class);
    }

    private static SyncClientMethodInvoker invokerReturning(StandardRpcResponse response) throws Exception {
        final NettyRemotingClient nettyRemotingClient = mock(NettyRemotingClient.class);
        when(nettyRemotingClient.sendSync(any())).thenReturn(response);
        return new SyncClientMethodInvoker(Host.of("server:1234"), method(), nettyRemotingClient);
    }

    /**
     * The server reporting "I do not have this method" is the only signal that identifies an old
     * peer during a rolling upgrade; it must map to the typed exception so callers can give
     * upgrade guidance without parsing message strings.
     */
    @Test
    void invoke_serverWithoutTheMethod_throwsTypedMethodNotFoundException() throws Throwable {
        final SyncClientMethodInvoker invoker =
                invokerReturning(StandardRpcResponse.methodNotFound("Cannot find the ServerMethodInvoker of x"));

        final MethodNotFoundException thrown = assertThrows(MethodNotFoundException.class,
                () -> invoke(invoker));

        assertEquals("Cannot find the ServerMethodInvoker of x", thrown.getMessage());
    }

    /**
     * Every other server-side failure — the invocation pool is full, the method itself threw —
     * is reported the same way over the wire. It must stay a plain MethodInvocationException so
     * a saturated current server is never mistaken for an outdated one.
     */
    @Test
    void invoke_serverAnsweredWithAFailure_staysPlainMethodInvocationException() throws Throwable {
        final SyncClientMethodInvoker invoker =
                invokerReturning(StandardRpcResponse.fail("NettyRemotingServer's thread pool is full"));

        final MethodInvocationException thrown = assertThrows(MethodInvocationException.class,
                () -> invoke(invoker));

        assertFalse(thrown instanceof MethodNotFoundException,
                "only the explicit not-found signal may be typed as MethodNotFoundException");
        assertEquals("NettyRemotingServer's thread pool is full", thrown.getMessage());
    }

    /**
     * LEGACY peer regression: an OLD server (pre typed-signal build) answers a missing method
     * with a plain {@code fail("Cannot find the ServerMethodInvoker of ...")} — its wire JSON has
     * no {@code methodNotFound} field, so the deserialized flag is false. The client must still
     * recognize this exact legacy message as "the server does not have the method" and throw the
     * typed exception; otherwise a rolling upgrade misclassifies the old worker as a broken
     * current one and gives the wrong upgrade guidance (and the log-download fallback decision
     * degrades).
     */
    @Test
    void invoke_legacyServerWithoutTheMethod_throwsTypedMethodNotFoundException() throws Throwable {
        final SyncClientMethodInvoker invoker =
                invokerReturning(StandardRpcResponse.fail(
                        "Cannot find the ServerMethodInvoker of "
                                + "org.apache.dolphinscheduler.extract.common.ILogService:getTaskInstanceLogFileChunk"));

        final MethodNotFoundException thrown = assertThrows(MethodNotFoundException.class,
                () -> invoke(invoker));

        assertTrue(thrown.getMessage().startsWith("Cannot find the ServerMethodInvoker of"),
                "the legacy message must survive into the typed exception");
    }

    /** The happy path is untouched: a success response still deserializes to the return value. */
    @Test
    void invoke_successResponse_returnsTheDeserializedBody() throws Throwable {
        final SyncClientMethodInvoker invoker = invokerReturning(
                StandardRpcResponse.success(JsonSerializer.serialize("result-body"), String.class));

        assertEquals("result-body", invoke(invoker));
    }

    /**
     * The exact wire shape an OLD server produces: its JSON has NO {@code methodNotFound} field
     * (the field postdates it), so round-tripping it must NOT lose the not-found signal — the
     * legacy message prefix carries it instead.
     */
    @Test
    void legacyWireResponseWithoutFlagField_isStillRecognizedAsMethodNotFound() {
        final String legacyJson =
                "{\"success\":false,\"message\":\"Cannot find the ServerMethodInvoker of "
                        + "org.apache.dolphinscheduler.extract.common.ILogService:getTaskInstanceLogFileChunk\"}";

        final StandardRpcResponse deserialized =
                JsonSerializer.deserialize(legacyJson.getBytes(StandardCharsets.UTF_8), StandardRpcResponse.class);

        // The raw flag field is absent on the old wire (it deserializes to false); only the
        // message prefix can carry the signal.
        assertTrue(deserialized.isMethodNotFound(),
                "the legacy message must carry the not-found signal");
    }

    private static Object invoke(final SyncClientMethodInvoker invoker) throws Throwable {
        final Method method = method();
        return invoker.invoke(Mockito.mock(SampleService.class), method, new Object[]{"arg"});
    }
}
