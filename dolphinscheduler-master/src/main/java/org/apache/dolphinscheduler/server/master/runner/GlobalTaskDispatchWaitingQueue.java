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

import java.util.concurrent.DelayQueue;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * The class is used to store {@link TaskExecuteRunnable} which needs to be dispatched. The {@link TaskExecuteRunnable} will be stored in a {@link DelayQueue},
 * if the {@link TaskExecuteRunnable}'s delay time is 0, then it will be consumed by {@link GlobalTaskDispatchWaitingQueueLooper}.
 */
@Slf4j
@Component
public class GlobalTaskDispatchWaitingQueue {

    private final DelayQueue<DefaultTaskExecuteRunnable> queue = new DelayQueue<>();

    public void submitTaskExecuteRunnable(DefaultTaskExecuteRunnable priorityTaskExecuteRunnable) {
        queue.put(priorityTaskExecuteRunnable);
    }

    @SneakyThrows
    public DefaultTaskExecuteRunnable takeTaskExecuteRunnable() {
        return queue.take();
    }

    public int getWaitingDispatchTaskNumber() {
        return queue.size();
    }

}
