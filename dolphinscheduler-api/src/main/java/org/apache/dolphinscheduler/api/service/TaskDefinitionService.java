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
 * task definition service
 */
public interface TaskDefinitionService {

    /**
     * create task definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param taskDefinitionJson task definition json
     */
    Map<String, Object> createTaskDefinition(User loginUser,
                                             String projectName,
                                             String taskDefinitionJson);

    /**
     * query task definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param taskName task name
     */
    Map<String, Object> queryTaskDefinitionByName(User loginUser,
                                                  String projectName,
                                                  String taskName);

    /**
     * delete task definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param taskCode task code
     */
    Map<String, Object> deleteTaskDefinitionByCode(User loginUser,
                                                   String projectName,
                                                   long taskCode);

    /**
     * update task definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param taskCode task code
     * @param taskDefinitionJson task definition json
     */
    Map<String, Object> updateTaskDefinition(User loginUser,
                                             String projectName,
                                             long taskCode,
                                             String taskDefinitionJson);

    /**
     * update task definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param taskCode task code
     * @param version the version user want to switch
     */
    Map<String, Object> switchVersion(User loginUser,
                                      String projectName,
                                      long taskCode,
                                      int version);

    /**
     * query the pagination versions info by one certain task definition code
     *
     * @param loginUser login user info to check auth
     * @param projectName project name
     * @param pageNo page number
     * @param pageSize page size
     * @param taskCode task definition code
     * @return the pagination task definition versions info of the certain task definition
     */
    Map<String, Object> queryTaskDefinitionVersions(User loginUser,
                                                    String projectName,
                                                    int pageNo,
                                                    int pageSize,
                                                    long taskCode);

    /**
     * delete the certain task definition version by version and code
     *
     * @param loginUser login user info
     * @param projectName the task definition project name
     * @param taskCode the task definition code
     * @param version the task definition version user want to delete
     * @return delete version result code
     */
    Map<String, Object> deleteByCodeAndVersion(User loginUser,
                                               String projectName,
                                               long taskCode,
                                               int version);

    /**
     * query detail of task definition by code
     *
     * @param loginUser login user
     * @param projectName project name
     * @param taskCode the task definition code
     * @return task definition detail
     */
    Map<String, Object> queryTaskDefinitionDetail(User loginUser,
                                                  String projectName,
                                                  long taskCode);

    /**
     * query task definition list paging
     *
     * @param loginUser login user
     * @param projectName project name
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @param userId user id
     * @return task definition page
     */
    Map<String, Object> queryTaskDefinitionListPaging(User loginUser,
                                                      String projectName,
                                                      String searchVal,
                                                      Integer pageNo,
                                                      Integer pageSize,
                                                      Integer userId);
}

