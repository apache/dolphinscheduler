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

package org.apache.dolphinscheduler.rpc.client;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RpcRequestTable
 */
public class RpcRequestTable {

    private RpcRequestTable() {
        throw new IllegalStateException("Utility class");
    }

    private static AtomicLong requestIdGen = new AtomicLong(0);

    private static ConcurrentHashMap<Long, RpcRequestCache> requestMap = new ConcurrentHashMap<>();

    public static void put(long requestId, RpcRequestCache rpcRequestCache) {
        requestMap.put(requestId, rpcRequestCache);
    }

    public static RpcRequestCache get(Long requestId) {
        return requestMap.get(requestId);
    }

    public static void remove(Long requestId) {
        requestMap.remove(requestId);
    }

    public static long getRequestId() {
        return requestIdGen.incrementAndGet();
    }

}
