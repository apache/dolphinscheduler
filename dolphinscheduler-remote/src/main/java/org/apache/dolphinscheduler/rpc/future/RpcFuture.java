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

package org.apache.dolphinscheduler.rpc.future;

import org.apache.dolphinscheduler.rpc.common.RpcRequest;
import org.apache.dolphinscheduler.rpc.common.RpcResponse;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * RpcFuture
 */
public class RpcFuture implements Future<Object> {

    private CountDownLatch latch = new CountDownLatch(1);

    private RpcResponse response;

    private RpcRequest request;

    private long requestId;

    public RpcFuture(RpcRequest rpcRequest, long requestId) {
        this.request = rpcRequest;
        this.requestId = requestId;
    }

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
    public RpcResponse get() throws InterruptedException {
        // the timeout period should be defined by the business party
        boolean success = latch.await(5, TimeUnit.SECONDS);
        if (!success) {
            throw new RuntimeException("Timeout exception. Request id: " + this.requestId
                    + ". Request class name: " + this.request.getClassName()
                    + ". Request method: " + this.request.getMethodName());
        }
        return response;
    }

    @Override
    public RpcResponse get(long timeout, TimeUnit unit) throws InterruptedException {
        boolean success = latch.await(timeout, unit);
        if (!success) {
            throw new RuntimeException("Timeout exception. Request id: " + requestId
                    + ". Request class name: " + this.request.getClassName()
                    + ". Request method: " + this.request.getMethodName());
        }
        return response;
    }

    public void done(RpcResponse response) {
        this.response = response;
        latch.countDown();
    }
}
