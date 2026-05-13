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

import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.dao.entity.TaskGroup;
import org.apache.dolphinscheduler.dao.entity.User;

public interface TaskGroupService {

    /**
     * create a Task group
     */
    TaskGroup createTaskGroup(User loginUser, Long projectCode, String name, String description, int groupSize);

    /**
     * update the task group
     */
    TaskGroup updateTaskGroup(User loginUser, int id, String name, String description, int groupSize);

    /**
     * query all task group by user id
     */
    PageInfo<TaskGroup> queryAllTaskGroup(User loginUser, String name, Integer status, int pageNo, int pageSize);

    /**
     * query all task group by status
     */
    PageInfo<TaskGroup> queryTaskGroupByStatus(User loginUser, int pageNo, int pageSize, int status);

    /**
     * query all task group by project code
     */
    PageInfo<TaskGroup> queryTaskGroupByProjectCode(User loginUser, int pageNo, int pageSize, Long projectCode);

    /**
     * query task group by id (returns null if not found)
     */
    TaskGroup queryTaskGroupById(User loginUser, int id);

    /**
     * paginated query of task groups
     */
    PageInfo<TaskGroup> doQuery(User loginUser, int pageNo, int pageSize, int userId, String name, Integer status);

    /**
     * close a task group
     */
    void closeTaskGroup(User loginUser, int id);

    /**
     * start a task group
     */
    void startTaskGroup(User loginUser, int id);

    /**
     * wake a task manually
     */
    void forceStartTask(User loginUser, int taskId);

    void modifyPriority(User loginUser, Integer queueId, Integer priority);

    void deleteTaskGroupByProjectCode(long projectCode);
}
