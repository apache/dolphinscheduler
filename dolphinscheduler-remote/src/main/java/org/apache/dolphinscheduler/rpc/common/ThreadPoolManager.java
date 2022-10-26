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

package org.apache.dolphinscheduler.rpc.common;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.TimeUnit;

public enum ThreadPoolManager {

    INSTANCE;

    ExecutorService executorService;

    private static final int WORK_QUEUE_SIZE = 200;
    private static final long KEEP_ALIVE_TIME = 60;

    ThreadPoolManager() {
        executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2,
                Runtime.getRuntime().availableProcessors() * 4, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(WORK_QUEUE_SIZE),
                new DiscardPolicy());
    }

    public void addExecuteTask(Runnable task) {
        executorService.submit(task);
    }
}
