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

    ThreadPoolManager() {
        int SIZE_WORK_QUEUE = 200;
        long KEEP_ALIVE_TIME = 60;
        int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
        int MAXI_MUM_POOL_SIZE = CORE_POOL_SIZE * 4;
        executorService = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXI_MUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(SIZE_WORK_QUEUE),
                new DiscardPolicy());
    }

    public void addExecuteTask(Runnable task) {
        executorService.submit(task);
    }
}
