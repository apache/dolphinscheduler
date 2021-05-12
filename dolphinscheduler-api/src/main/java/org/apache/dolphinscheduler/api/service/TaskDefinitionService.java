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
                                                   Long taskCode);

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
                                             Long taskCode,
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
                                      Long taskCode,
                                      int version);
}

