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

package org.apache.dolphinscheduler.extract.base;

import org.apache.dolphinscheduler.extract.base.serialize.JsonSerializer;

import lombok.Data;

@Data
public class StandardRpcResponse implements IRpcResponse {

    /**
     * The message both OLD and CURRENT servers attach when the requested method does not exist
     * on the receiving side (see {@code JdkDynamicServerHandler}). A current server also sets
     * {@link #methodNotFound}; an old one sends the bare failure, so this prefix is the legacy
     * discriminator. Shared between the server (which produces the message) and the client
     * (which recognizes it) — change both sides together or not at all.
     */
    public static final String METHOD_NOT_FOUND_MESSAGE_PREFIX = "Cannot find the ServerMethodInvoker of ";

    private boolean success;

    private String message;

    private byte[] body;

    private Class<?> bodyType;

    /**
     * True only when the failure is specifically "the server does not have this method" — the
     * old-server signal during a rolling upgrade. Every other failure (the method threw, the
     * server's invocation pool is full) leaves it false, so a caller must never mistake a
     * saturated or broken server for an outdated one. Older peers ignore the field.
     */
    private boolean methodNotFound;

    public static StandardRpcResponse success(byte[] body, Class<?> bodyType) {
        StandardRpcResponse rpcResponse = new StandardRpcResponse();
        rpcResponse.setSuccess(true);
        rpcResponse.setBody(body);
        rpcResponse.setBodyType(bodyType);
        return rpcResponse;
    }

    public static StandardRpcResponse fail(String message) {
        StandardRpcResponse rpcResponse = new StandardRpcResponse();
        rpcResponse.setSuccess(false);
        rpcResponse.setMessage(message);
        return rpcResponse;
    }

    /**
     * The server answered that it does not know the requested method (see {@link #isMethodNotFound()}).
     */
    public static StandardRpcResponse methodNotFound(String message) {
        StandardRpcResponse rpcResponse = fail(message);
        rpcResponse.setMethodNotFound(true);
        return rpcResponse;
    }

    /**
     * The typed flag from a CURRENT peer, or — for an OLD peer whose wire JSON predates the
     * flag — the exact legacy failure message both server generations have always used for a
     * missing method. Other failures keep false: their messages never start with this prefix.
     */
    @Override
    public boolean isMethodNotFound() {
        if (methodNotFound) {
            return true;
        }
        return message != null && message.startsWith(METHOD_NOT_FOUND_MESSAGE_PREFIX);
    }

    @Override
    public byte[] toBytes() {
        return JsonSerializer.serialize(this);
    }
}
