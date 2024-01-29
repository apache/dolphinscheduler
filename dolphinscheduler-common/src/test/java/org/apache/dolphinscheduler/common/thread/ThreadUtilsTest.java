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

package org.apache.dolphinscheduler.common.thread;

import java.util.concurrent.ThreadPoolExecutor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ThreadUtilsTest {

    @Test
    void newDaemonFixedThreadExecutor() throws InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = ThreadUtils.newDaemonFixedThreadExecutor("DemonThread", 1);
        threadPoolExecutor.execute(() -> {
            throw new IllegalArgumentException("I am an exception");
        });
        Thread.sleep(1_000);
        Assertions.assertEquals(1, DefaultUncaughtExceptionHandler.getUncaughtExceptionCount());

    }
}
