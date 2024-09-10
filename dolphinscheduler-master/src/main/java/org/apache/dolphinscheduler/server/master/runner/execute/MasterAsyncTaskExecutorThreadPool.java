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

package org.apache.dolphinscheduler.server.master.runner.execute;

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;

import java.util.concurrent.ThreadPoolExecutor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MasterAsyncTaskExecutorThreadPool implements IMasterTaskExecutorThreadPool<AsyncMasterTaskExecutor> {

    private final ThreadPoolExecutor threadPoolExecutor;

    public MasterAsyncTaskExecutorThreadPool(MasterConfig masterConfig) {
        this.threadPoolExecutor = ThreadUtils.newDaemonFixedThreadExecutor("MasterAsyncTaskExecutorThreadPool",
                masterConfig.getMasterSyncTaskExecutorThreadPoolSize());
    }

    @Override
    public boolean submitMasterTaskExecutor(AsyncMasterTaskExecutor asyncMasterTaskExecutor) {
        synchronized (MasterAsyncTaskExecutorThreadPool.class) {
            // todo: check if the thread pool is overload
            threadPoolExecutor.execute(asyncMasterTaskExecutor);
            return true;
        }
    }

    @Override
    public boolean removeMasterTaskExecutor(AsyncMasterTaskExecutor asyncMasterTaskExecutor) {
        return threadPoolExecutor.remove(asyncMasterTaskExecutor);
    }

    // todo: remove this method, it's not a good idea to expose the ThreadPoolExecutor to out side.
    ThreadPoolExecutor getThreadPool() {
        return threadPoolExecutor;
    }
}
