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
 * process task relation service
 */
public interface ProcessTaskRelationService {

    /**
     * create process task relation
     *
     * @param loginUser login user
     * @param name relation name
     * @param projectName process name
     * @param processDefinitionCode process definition code
     * @param preTaskCode pre task code
     * @param postTaskCode post task code
     * @param conditionType condition type
     * @param conditionParams condition params
     * @return create result code
     */
    Map<String, Object> createProcessTaskRelation(User loginUser,
                                                  String name,
                                                  String projectName,
                                                  Long processDefinitionCode,
                                                  Long preTaskCode,
                                                  Long postTaskCode,
                                                  String conditionType,
                                                  String conditionParams);


    /**
     * query process task relation
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionCode process definition code
     */
    Map<String, Object> queryProcessTaskRelation(User loginUser,
                                                 String projectName,
                                                 Long processDefinitionCode);

    /**
     * delete process task relation
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionCode process definition code
     */
    Map<String, Object> deleteTaskDefinitionByProcess(User loginUser,
                                                      String projectName,
                                                      Long processDefinitionCode);

    /**
     * delete process task relation
     *
     * @param loginUser login user
     * @param projectName project name
     * @param preTaskCode pre task code
     */
    Map<String, Object> deleteTaskDefinitionByTask(User loginUser,
                                                   String projectName,
                                                   Long preTaskCode);


    /**
     * update process task relation
     *
     * @param loginUser login user
     * @param id process task relation id
     * @param name relation name
     * @param projectName process name
     * @param processDefinitionCode process definition code
     * @param preTaskCode pre task code
     * @param postTaskCode post task code
     * @param conditionType condition type
     * @param conditionParams condition params
     */
    Map<String, Object> updateTaskDefinition(User loginUser,
                                             int id,
                                             String name,
                                             String projectName,
                                             Long processDefinitionCode,
                                             Long preTaskCode,
                                             Long postTaskCode,
                                             String conditionType,
                                             String conditionParams);


    /**
     * switch process task relation version
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processTaskRelationId process task relation id
     * @param version the version user want to switch
     * @return switch process task relation version result code
     */
    Map<String, Object> switchVersion(User loginUser,
                                      String projectName,
                                      int processTaskRelationId,
                                      int version);
}

