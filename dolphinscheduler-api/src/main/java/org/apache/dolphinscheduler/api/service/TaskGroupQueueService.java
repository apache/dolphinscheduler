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

import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;
import java.util.Map;

/**
 * task group queue service
 */
public interface TaskGroupQueueService {

    /**
     * query tasks in task group queue by group id
     * @param loginUser   login user
     * @param groupId     group id
     * @param taskName    Task Name
     * @param processName Process instance name
     * @param status      Task queue status
     * @param pageNo      page no
     * @param pageSize    page size
    
     * @return tasks list
     */
    Map<String, Object> queryTasksByGroupId(User loginUser, String taskName, String processName, Integer status,
                                            int groupId, int pageNo, int pageSize);

    /**
     * query tasks in task group queue by project id
     * @param loginUser login user
     * @param pageNo page no
     * @param pageSize page size
     * @param processId process id
     * @return tasks list
     */
    Map<String, Object> queryTasksByProcessId(User loginUser, int pageNo,
                                              int pageSize, int processId);

    /**
     * query all tasks in task group queue
     * @param loginUser login user
     * @param pageNo page no
     * @param pageSize page size
     * @return tasks list
     */
    Map<String, Object> queryAllTasks(User loginUser, int pageNo, int pageSize);

    /**
     * delete by task id
     * @param taskId task id
     * @return TaskGroupQueue entity
     */
    boolean deleteByTaskId(int taskId);

    void deleteByTaskInstanceIds(List<Integer> taskInstanceIds);

    void deleteByWorkflowInstanceId(Integer workflowInstanceId);

    void forceStartTask(int queueId, int forceStart);

    void modifyPriority(Integer queueId, Integer priority);

    void deleteByTaskGroupIds(List<Integer> taskGroupIds);
}
