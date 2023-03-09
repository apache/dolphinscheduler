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

import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;

import java.util.Collection;

import lombok.NonNull;

/**
 * cache of process instance id and WorkflowExecuteThread
 */
public interface ProcessInstanceExecCacheManager {

    /**
     * get WorkflowExecuteThread by process instance id
     *
     * @param processInstanceId processInstanceId
     * @return WorkflowExecuteThread
     */
    WorkflowExecuteRunnable getByProcessInstanceId(int processInstanceId);

    /**
     * judge the process instance does it exist
     *
     * @param processInstanceId processInstanceId
     * @return true - if process instance id exists in cache
     */
    boolean contains(int processInstanceId);

    /**
     * remove cache by process instance id
     *
     * @param processInstanceId processInstanceId
     */
    void removeByProcessInstanceId(int processInstanceId);

    /**
     * cache
     *
     * @param processInstanceId     processInstanceId
     * @param workflowExecuteThread if it is null, will not be cached
     */
    void cache(int processInstanceId, @NonNull WorkflowExecuteRunnable workflowExecuteThread);

    /**
     * get all WorkflowExecuteThread from cache
     *
     * @return all WorkflowExecuteThread in cache
     */
    Collection<WorkflowExecuteRunnable> getAll();

    void clearCache();
}
