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

package org.apache.dolphinscheduler.api.log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class LogPromise {

    private static final ConcurrentHashMap<Long, LogPromise> PROMISES = new ConcurrentHashMap<>();

    private long opaque;

    private final long start;

    private final long timeout;

    private final CountDownLatch latch;

    private Object result;

    public LogPromise(long opaque, long timeout){
        this.opaque = opaque;
        this.timeout = timeout;
        this.start = System.currentTimeMillis();
        this.latch = new CountDownLatch(1);
        PROMISES.put(opaque, this);
    }


    public static void notify(long opaque, Object result){
        LogPromise promise = PROMISES.remove(opaque);
        if(promise != null){
            promise.doCountDown(result);
        }
    }

    private void doCountDown(Object result){
        this.result = result;
        this.latch.countDown();
    }

    public boolean isTimeout(){
        return System.currentTimeMillis() - start > timeout;
    }

    public Object getResult(){
        try {
            latch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignore) {
        }
        PROMISES.remove(opaque);
        return this.result;
    }


}
