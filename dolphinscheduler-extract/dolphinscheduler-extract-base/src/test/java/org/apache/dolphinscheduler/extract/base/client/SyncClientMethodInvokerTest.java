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

    /** The happy path is untouched: a success response still deserializes to the return value. */
    @Test
    void invoke_successResponse_returnsTheDeserializedBody() throws Throwable {
        final SyncClientMethodInvoker invoker = invokerReturning(
                StandardRpcResponse.success(JsonSerializer.serialize("result-body"), String.class));

        assertEquals("result-body", invoke(invoker));
    }

    private static Object invoke(final SyncClientMethodInvoker invoker) throws Throwable {
        final Method method = method();
        return invoker.invoke(Mockito.mock(SampleService.class), method, new Object[]{"arg"});
    }
}
