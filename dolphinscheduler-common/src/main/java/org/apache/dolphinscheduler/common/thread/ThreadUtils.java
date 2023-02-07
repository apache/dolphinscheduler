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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

@UtilityClass
@Slf4j
public class ThreadUtils {

    /**
     * Wrapper over newDaemonFixedThreadExecutor.
     *
     * @param threadName threadName
     * @param threadsNum threadsNum
     * @return ExecutorService
     */
    public static ExecutorService newDaemonFixedThreadExecutor(String threadName, int threadsNum) {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat(threadName).build();
        return Executors.newFixedThreadPool(threadsNum, threadFactory);
    }

    public static ScheduledExecutorService newSingleDaemonScheduledExecutorService(String threadName) {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat(threadName)
                .setDaemon(true)
                .build();
        return Executors.newSingleThreadScheduledExecutor(threadFactory);
    }

    /**
     * Sleep in given mills, this is not accuracy.
     */
    public static void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            log.error("Current thread sleep error", interruptedException);
        }
    }
}
