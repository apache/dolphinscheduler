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

import java.util.Map;

/**
 * task group service
 */
public interface TaskGroupService {

    /**
     * create a Task group
     *
     * @param loginUser   login user
     * @param name        task group name
     * @param description task group description
     * @param groupSize   task group total size
     * @return the result code and msg
     */
    Map<String, Object> createTaskGroup(User loginUser, Long projectCode, String name,
                                        String description, int groupSize);

    /**
     * update the task group
     *
     * @param loginUser   login user
     * @param name        task group name
     * @param description task group description
     * @param groupSize   task group total size
     * @return the result code and msg
     */
    Map<String, Object> updateTaskGroup(User loginUser, int id, String name,
                                        String description, int groupSize);

    /**
     * get task group status
     *
     * @param id task group id
     * @return the result code and msg
     */
    boolean isTheTaskGroupAvailable(int id);

    /**
     * query all task group by user id
     *
     * @param loginUser login user
     * @param pageNo    page no
     * @param pageSize  page size
     * @return the result code and msg
     */
    Map<String, Object> queryAllTaskGroup(User loginUser, String name, Integer status, int pageNo, int pageSize);

    /**
     * query all task group by status
     *
     * @param loginUser login user
     * @param pageNo    page no
     * @param pageSize  page size
     * @param status    status
     * @return the result code and msg
     */
    Map<String, Object> queryTaskGroupByStatus(User loginUser, int pageNo, int pageSize, int status);

    /**
     * query all task group by name
     *
     * @param loginUser login user
     * @param pageNo    page no
     * @param pageSize  page size
     * @param projectCode  project code
     * @return the result code and msg
     */
    Map<String, Object> queryTaskGroupByProjectCode(User loginUser, int pageNo, int pageSize, Long projectCode);

    /**
     * query all task group by id
     *
     * @param loginUser login user
     * @param id        id
     * @return the result code and msg
     */
    Map<String, Object> queryTaskGroupById(User loginUser, int id);

    /**
     * query
     *
     * @param loginUser login user
     * @param pageNo    page no
     * @param pageSize  page size
     * @param userId    user id
     * @param name      name
     * @param status    status
     * @return the result code and msg
     */
    Map<String, Object> doQuery(User loginUser, int pageNo, int pageSize, int userId, String name, Integer status);

    /**
     * close a task group
     *
     * @param loginUser login user
     * @param id        task group id
     * @return the result code and msg
     */
    Map<String, Object> closeTaskGroup(User loginUser, int id);

    /**
     * start a task group
     *
     * @param loginUser login user
     * @param id        task group id
     * @return the result code and msg
     */
    Map<String, Object> startTaskGroup(User loginUser, int id);

    /**
     * wake a task manually
     *
     * @param taskId task id
     * @return result
     */
    Map<String, Object> forceStartTask(User loginUser, int taskId);

    Map<String, Object> modifyPriority(User loginUser, Integer queueId, Integer priority);

    void deleteTaskGroupByProjectCode(long projectCode);
}
