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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.dto.taskInstance.TaskInstanceRemoveCacheResponse;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

/**
 * task instance service
 */
public interface TaskInstanceService {

    /**
     * query task list by project, process instance, task name, task start time, task end time, task status, keyword paging
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processInstanceId process instance id
     * @param searchVal search value
     * @param taskName task name
     * @param stateType state type
     * @param host host
     * @param startDate start time
     * @param endDate end time
     * @param taskExecuteType task execute type
     * @param pageNo page number
     * @param pageSize page size
     * @return task list page
     */
    Result queryTaskListPaging(User loginUser,
                               long projectCode,
                               Integer processInstanceId,
                               String processInstanceName,
                               String processDefinitionName,
                               String taskName,
                               String executorName,
                               String startDate,
                               String endDate,
                               String searchVal,
                               TaskExecutionStatus stateType,
                               String host,
                               TaskExecuteType taskExecuteType,
                               Integer pageNo,
                               Integer pageSize);

    /**
     * change one task instance's state from failure to forced success
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskInstanceId task instance id
     * @return the result code and msg
     */
    Result forceTaskSuccess(User loginUser,
                            long projectCode,
                            Integer taskInstanceId);

    /**
     * task savepoint
     * @param loginUser
     * @param projectCode
     * @param taskInstanceId
     * @return
     */
    Result taskSavePoint(User loginUser, long projectCode, Integer taskInstanceId);

    /**
     * stop task
     * @param loginUser
     * @param projectCode
     * @param taskInstanceId
     * @return
     */
    Result stopTask(User loginUser, long projectCode, Integer taskInstanceId);

    /**
     * query taskInstance by taskInstanceCode
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param taskInstanceId taskInstance id
     * @return the result code and msg
     */
    TaskInstance queryTaskInstanceById(User loginUser, long projectCode, Long taskInstanceId);

    /**
     * remove task instance cache
     * @param loginUser
     * @param projectCode
     * @param taskInstanceId
     * @return
     */
    TaskInstanceRemoveCacheResponse removeTaskInstanceCache(User loginUser, long projectCode, Integer taskInstanceId);

    void deleteByWorkflowInstanceId(Integer workflowInstanceId);
}
