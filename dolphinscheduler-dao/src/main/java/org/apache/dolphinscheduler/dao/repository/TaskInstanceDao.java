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

package org.apache.dolphinscheduler.dao.repository;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import java.util.List;
import java.util.Set;

/**
 * Task Instance DAO
 */
public interface TaskInstanceDao extends IDao<TaskInstance> {

    /**
     * Update or Insert task instance to DB.
     * ID is null -> Insert
     * ID is not null -> Update
     *
     * @param taskInstance task instance
     * @return result
     */
    boolean upsertTaskInstance(TaskInstance taskInstance);

    /**
     * Submit a task instance to DB.
     *
     * @param taskInstance    task instance
     * @param workflowInstance workflow instance
     * @return task instance
     */
    boolean submitTaskInstanceToDB(TaskInstance taskInstance, WorkflowInstance workflowInstance);

    /**
     * Mark the task instance as invalid
     */
    void markTaskInstanceInvalid(List<TaskInstance> taskInstances);

    /**
     * Query list of valid task instance by workflow instance id
     *
     * @param workflowInstanceId workflowInstanceId
     * @param testFlag          test flag
     * @return list of valid task instance
     */
    List<TaskInstance> queryValidTaskListByWorkflowInstanceId(Integer workflowInstanceId, int testFlag);

    /**
     * Query list of task instance by workflow instance id and task code
     *
     * @param workflowInstanceId workflowInstanceId
     * @param taskCode          task code
     * @return list of valid task instance
     */
    TaskInstance queryByWorkflowInstanceIdAndTaskCode(Integer workflowInstanceId, Long taskCode);

    /**
     * find previous task list by work workflow id
     *
     * @param workflowInstanceId workflowInstanceId
     * @return task instance list
     */
    List<TaskInstance> queryPreviousTaskListByWorkflowInstanceId(Integer workflowInstanceId);

    /**
     * find task instance by cache_key
     *
     * @param cacheKey cache key
     * @return task instance
     */
    TaskInstance queryByCacheKey(String cacheKey);

    /**
     * clear task instance cache by cache_key
     *
     * @param cacheKey cache key
     * @return task instance
     */
    Boolean clearCacheByCacheKey(String cacheKey);

    void deleteByWorkflowInstanceId(int workflowInstanceId);

    List<TaskInstance> queryByWorkflowInstanceId(Integer workflowInstanceId);

    /**
     * find last task instance list corresponding to taskCodes in the date interval
     *
     * @param workflowInstanceId Task's parent workflow instance id
     * @param taskCodes         taskCodes
     * @param testFlag          test flag
     * @return task instance list
     */
    List<TaskInstance> queryLastTaskInstanceListIntervalInWorkflowInstance(Integer workflowInstanceId,
                                                                           Set<Long> taskCodes, int testFlag);

    /**
     * find last task instance corresponding to taskCode in the date interval
     *
     * @param workflowInstanceId Task's parent workflow instance id
     * @param depTaskCode       taskCode
     * @param testFlag          test flag
     * @return task instance
     */
    TaskInstance queryLastTaskInstanceIntervalInWorkflowInstance(Integer workflowInstanceId,
                                                                 long depTaskCode, int testFlag);

    void updateTaskInstanceState(Integer taskInstanceId, TaskExecutionStatus originState,
                                 TaskExecutionStatus targetState);
}
