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

package org.apache.dolphinscheduler.task.executor;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TaskExecutorRepository implements ITaskExecutorRepository {

    private final Map<Integer, ITaskExecutor> taskExecutorMap = new ConcurrentHashMap<>();

    @Override
    public void put(final ITaskExecutor taskExecutor) {
        checkNotNull(taskExecutor);
        taskExecutorMap.put(taskExecutor.getId(), taskExecutor);
    }

    @Override
    public Optional<ITaskExecutor> get(final Integer taskExecutorId) {
        return Optional.ofNullable(taskExecutorMap.get(taskExecutorId));
    }

    @Override
    public Collection<ITaskExecutor> getAll() {
        return taskExecutorMap.values();
    }

    @Override
    public boolean contains(final Integer taskExecutorId) {
        return taskExecutorMap.containsKey(taskExecutorId);
    }

    @Override
    public void remove(final Integer taskExecutorId) {
        taskExecutorMap.remove(taskExecutorId);
    }

    @Override
    public void clear() {
        taskExecutorMap.clear();
    }

}
