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

package org.apache.dolphinscheduler.server.master.cache;

import org.apache.dolphinscheduler.server.master.runner.StreamTaskExecuteRunnable;

import java.util.Collection;

import lombok.NonNull;

/**
 * cache of stream task instance
 */
public interface StreamTaskInstanceExecCacheManager {

    StreamTaskExecuteRunnable getByTaskInstanceId(int taskInstanceId);

    boolean contains(int taskInstanceId);

    void removeByTaskInstanceId(int taskInstanceId);

    /**
     * cache
     *
     * @param taskInstanceId     taskInstanceId
     * @param streamTaskExecuteRunnable if it is null, will not be cached
     */
    void cache(int taskInstanceId, @NonNull StreamTaskExecuteRunnable streamTaskExecuteRunnable);

    /**
     * get all streamTaskExecuteRunnable from cache
     *
     * @return all streamTaskExecuteRunnable in cache
     */
    Collection<StreamTaskExecuteRunnable> getAll();
}
