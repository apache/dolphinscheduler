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
     * @param loginUser login user
     * @param name task group name
     * @param description task group description
     * @param groupSize task group total size
     * @return the result code and msg
     */
    Map<String, Object> createTaskGroup(User loginUser, String name,
                                        String description, Integer groupSize);


    /**
     * update the task group
     * @param loginUser login user
     * @param name task group name
     * @param description task group description
     * @param groupSize task group total size
     * @return the result code and msg
     */
    Map<String, Object> updateTaskGroup(User loginUser, Integer id, String name,
                                        String description, Integer groupSize);

    /**
     * get task group status
     * @param id task group id
     * @return the result code and msg
     */
    boolean isTheTaskGroupAvailable(Integer id);


    /**
     * query all task group by user id
     * @param loginUser login user
     * @param pageNo page no
     * @param pageSize page size
     * @return the result code and msg
     */
    Map<String, Object> queryAllTaskGroup(User loginUser, Integer pageNo, Integer pageSize);

    /**
     * query all task group by status
     * @param loginUser login user
     * @param pageNo page no
     * @param pageSize page size
     * @param status status
     * @return the result code and msg
     */
    Map<String, Object> queryTaskGroupByStatus(User loginUser, Integer pageNo, Integer pageSize, Integer status);

    /**
     * query all task group by name
     * @param loginUser login user
     * @param pageNo page no
     * @param pageSize page size
     * @param name name
     * @return the result code and msg
     */
    Map<String, Object> queryTaskGroupByName(User loginUser, Integer pageNo, Integer pageSize, String name);

    /**
     * query all task group by id
     * @param loginUser login user

     * @param id id
     * @return the result code and msg
     */
    Map<String, Object> queryTaskGroupById(User loginUser, Integer id);

    /**
     * query
     * @param loginUser login user
     * @param pageNo    page no
     * @param pageSize  page size
     * @param userId user id
     * @param name name
     * @param status status
     * @return the result code and msg
     */
    Map<String, Object> doQuery(User loginUser, Integer pageNo, Integer pageSize, Integer userId, String name, Integer status);


    /**
     * close a task group
     * @param loginUser login user
     * @param id task group id
     * @return the result code and msg
     */
    Map<String, Object> closeTaskGroup(User loginUser, Integer id);

    /**
     * start a task group
     * @param loginUser login user
     * @param id task group id
     * @return the result code and msg
     */
    Map<String, Object> startTaskGroup(User loginUser, Integer id);


    /**
     * wake a task manually
     * @param taskId task id
     * @return result
     */
    Map<String, Object> wakeTaskcompulsively(User loginUser, Integer taskId);
}
