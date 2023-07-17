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

package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.server.master.runner.execute.MasterDelayTaskExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.execute.MasterTaskExecuteRunnable;

import java.util.concurrent.DelayQueue;

import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class MasterDelayTaskExecuteRunnableDelayQueue {

    private final DelayQueue<MasterDelayTaskExecuteRunnable> masterDelayTaskExecuteRunnableDelayQueue =
            new DelayQueue<>();

    public boolean submitMasterDelayTaskExecuteRunnable(MasterDelayTaskExecuteRunnable masterDelayTaskExecuteRunnable) {
        return masterDelayTaskExecuteRunnableDelayQueue.offer(masterDelayTaskExecuteRunnable);
    }

    public MasterDelayTaskExecuteRunnable takeMasterDelayTaskExecuteRunnable() throws InterruptedException {
        return masterDelayTaskExecuteRunnableDelayQueue.take();
    }

    // todo: if we move the delay process to master, than we don't need this method, since dispatchProcess can directly
    // submit to thread pool
    public boolean removeMasterDelayTaskExecuteRunnable(MasterTaskExecuteRunnable masterTaskExecuteRunnable) {
        return masterDelayTaskExecuteRunnableDelayQueue.remove(masterTaskExecuteRunnable);
    }

    public int size() {
        return masterDelayTaskExecuteRunnableDelayQueue.size();
    }

}
