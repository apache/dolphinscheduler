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

package org.apache.dolphinscheduler.service.log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 *  log asyc callback
 */
public class LogPromise {

    private static final ConcurrentHashMap<Long, LogPromise> PROMISES = new ConcurrentHashMap<>();

    /**
     *  request unique identification
     */
    private long opaque;

    /**
     *  start timemillis
     */
    private final long start;

    /**
     *  timeout
     */
    private final long timeout;

    /**
     *  latch
     */
    private final CountDownLatch latch;

    /**
     *  result
     */
    private Object result;

    public LogPromise(long opaque, long timeout){
        this.opaque = opaque;
        this.timeout = timeout;
        this.start = System.currentTimeMillis();
        this.latch = new CountDownLatch(1);
        PROMISES.put(opaque, this);
    }


    /**
     *  notify client finish
     * @param opaque unique identification
     * @param result result
     */
    public static void notify(long opaque, Object result){
        LogPromise promise = PROMISES.remove(opaque);
        if(promise != null){
            promise.doCountDown(result);
        }
    }

    /**
     *  countdown
     *
     * @param result result
     */
    private void doCountDown(Object result){
        this.result = result;
        this.latch.countDown();
    }

    /**
     *  whether timeout
     * @return timeout
     */
    public boolean isTimeout(){
        return System.currentTimeMillis() - start > timeout;
    }

    /**
     *  get result
     * @return
     */
    public Object getResult(){
        try {
            latch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignore) {
        }
        PROMISES.remove(opaque);
        return this.result;
    }


}
