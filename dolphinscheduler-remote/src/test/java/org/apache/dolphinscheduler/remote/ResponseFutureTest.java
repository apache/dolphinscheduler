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

package org.apache.dolphinscheduler.remote;


import org.apache.dolphinscheduler.remote.future.InvokeCallback;
import org.apache.dolphinscheduler.remote.future.ResponseFuture;
import org.apache.dolphinscheduler.remote.utils.NamedThreadFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ResponseFutureTest {

    @Test
    public void testScanFutureTable(){
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("executor-service"));
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                ResponseFuture.scanFutureTable();
            }
        }, 3000, 1000, TimeUnit.MILLISECONDS);

        CountDownLatch latch = new CountDownLatch(1);
        InvokeCallback invokeCallback = new InvokeCallback() {
            @Override
            public void operationComplete(ResponseFuture responseFuture) {
                latch.countDown();
            }
        };
        ResponseFuture future = new ResponseFuture(1, 2000, invokeCallback, null);
        try {
            latch.await(5000, TimeUnit.MILLISECONDS);
            Assert.assertTrue(ResponseFuture.getFuture(1) == null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdownNow();
    }
}
