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

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.springframework.web.multipart.MultipartFile;

/**
 * process definition service
 */
public interface ProcessDefinitionService {

    /**
     * create process definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param name process definition name
     * @param description description
     * @param globalParams global params
     * @param locations locations for nodes
     * @param timeout timeout
     * @param tenantCode tenantCode
     * @param taskRelationJson relation json for nodes
     * @param taskDefinitionJson taskDefinitionJson
     * @param otherParamsJson otherParamsJson handle other params
     * @return create result code
     */
    Map<String, Object> createProcessDefinition(User loginUser,
                                                long projectCode,
                                                String name,
                                                String description,
                                                String globalParams,
                                                String locations,
                                                int timeout,
                                                String tenantCode,
                                                String taskRelationJson,
                                                String taskDefinitionJson,
                                                String otherParamsJson,
                                                ProcessExecutionTypeEnum executionType);

    /**
     * query process definition list
     *
     * @param loginUser login user
     * @param projectCode project code
     * @return definition list
     */
    Map<String, Object> queryProcessDefinitionList(User loginUser,
                                                   long projectCode);

    /**
     * query process definition simple list
     *
     * @param loginUser login user
     * @param projectCode project code
     * @return definition simple list
     */
    Map<String, Object> queryProcessDefinitionSimpleList(User loginUser,
                                                         long projectCode);

    /**
     * query process definition list paging
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param searchVal search value
     * @param otherParamsJson otherParamsJson handle other params
     * @param pageNo page number
     * @param pageSize page size
     * @param userId user id
     * @return process definition page
     */
    Result queryProcessDefinitionListPaging(User loginUser,
                                            long projectCode,
                                            String searchVal,
                                            String otherParamsJson,
                                            Integer userId,
                                            Integer pageNo,
                                            Integer pageSize);

    /**
     * query detail of process definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param code process definition code
     * @return process definition detail
     */

    Map<String, Object> queryProcessDefinitionByCode(User loginUser,
                                                     long projectCode,
                                                     long code);

    /**
     * query detail of process definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param name process definition name
     * @return process definition detail
     */

    Map<String, Object> queryProcessDefinitionByName(User loginUser,
                                                     long projectCode,
                                                     String name);

    /**
     * batch copy process definition
     *
     * @param loginUser loginUser
     * @param projectCode projectCode
     * @param codes processDefinitionCodes
     * @param targetProjectCode targetProjectCode
     */
    Map<String, Object> batchCopyProcessDefinition(User loginUser,
                                                   long projectCode,
                                                   String codes,
                                                   long targetProjectCode);

    /**
     * batch move process definition
     *
     * @param loginUser loginUser
     * @param projectCode projectCode
     * @param codes processDefinitionCodes
     * @param targetProjectCode targetProjectCode
     */
    Map<String, Object> batchMoveProcessDefinition(User loginUser,
                                                   long projectCode,
                                                   String codes,
                                                   long targetProjectCode);

    /**
     * update  process definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param name process definition name
     * @param code process definition code
     * @param description description
     * @param globalParams global params
     * @param locations locations for nodes
     * @param timeout timeout
     * @param tenantCode tenantCode
     * @param taskRelationJson relation json for nodes
     * @param taskDefinitionJson taskDefinitionJson
     * @param otherParamsJson otherParamsJson handle other params
     * @return update result code
     */
    Map<String, Object> updateProcessDefinition(User loginUser,
                                                long projectCode,
                                                String name,
                                                long code,
                                                String description,
                                                String globalParams,
                                                String locations,
                                                int timeout,
                                                String tenantCode,
                                                String taskRelationJson,
                                                String taskDefinitionJson,
                                                String otherParamsJson,
                                                ProcessExecutionTypeEnum executionType);

    /**
     * verify process definition name unique
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param name name
     * @return true if process definition name not exists, otherwise false
     */
    Map<String, Object> verifyProcessDefinitionName(User loginUser,
                                                    long projectCode,
                                                    String name);

    /**
     * delete process definition by code
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param code process definition code
     * @return delete result code
     */
    Map<String, Object> deleteProcessDefinitionByCode(User loginUser,
                                                      long projectCode,
                                                      long code);

    /**
     * release process definition: online / offline
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param code process definition code
     * @param releaseState release state
     * @return release result code
     */
    Map<String, Object> releaseProcessDefinition(User loginUser,
                                                 long projectCode,
                                                 long code,
                                                 ReleaseState releaseState);

    /**
     * batch export process definition by codes
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param codes process definition codes
     * @param response http servlet response
     */
    void batchExportProcessDefinitionByCodes(User loginUser,
                                             long projectCode,
                                             String codes,
                                             HttpServletResponse response);

    /**
     * import process definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param file process metadata json file
     * @return import process
     */
    Map<String, Object> importProcessDefinition(User loginUser,
                                                long projectCode,
                                                MultipartFile file);

    /**
     * import sql process definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param file sql file, zip
     * @return import process
     */
    Map<String, Object> importSqlProcessDefinition(User loginUser,
                                                   long projectCode,
                                                   MultipartFile file);

    /**
     * check the process task relation json
     *
     * @param processTaskRelationJson process task relation json
     * @return check result code
     */
    Map<String, Object> checkProcessNodeList(String processTaskRelationJson, List<TaskDefinitionLog> taskDefinitionLogs);

    /**
     * get task node details based on process definition
     *
     * @param loginUser loginUser
     * @param projectCode project code
     * @param code processDefinition code
     * @return task node list
     */
    Map<String, Object> getTaskNodeListByDefinitionCode(User loginUser,
                                                        long projectCode,
                                                        long code);

    /**
     * get task node details map based on process definition
     *
     * @param loginUser loginUser
     * @param projectCode project code
     * @param codes define code list
     * @return task node list
     */
    Map<String, Object> getNodeListMapByDefinitionCodes(User loginUser,
                                                        long projectCode,
                                                        String codes);

    /**
     * query process definition all by project code
     *
     * @param projectCode project code
     * @return process definitions in the project
     */
    Map<String, Object> queryAllProcessDefinitionByProjectCode(User loginUser, long projectCode);

    /**
     * query process definition list by project code
     *
     * @param projectCode project code
     * @return process definitions in the project
     */
    Map<String, Object> queryProcessDefinitionListByProjectCode(long projectCode);

    /**
     * query process definition list by project code
     *
     * @param projectCode project code
     * @param processDefinitionCode process definition code
     * @return process definitions in the project
     */
    Map<String, Object> queryTaskDefinitionListByProcessDefinitionCode(long projectCode, Long processDefinitionCode);

    /**
     * Encapsulates the TreeView structure
     *
     * @param projectCode project code
     * @param code process definition code
     * @param limit limit
     * @return tree view json data
     */
    Map<String, Object> viewTree(User loginUser,long projectCode, long code, Integer limit);

    /**
     * switch the defined process definition version
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param code process definition code
     * @param version the version user want to switch
     * @return switch process definition version result code
     */
    Map<String, Object> switchProcessDefinitionVersion(User loginUser,
                                                       long projectCode,
                                                       long code,
                                                       int version);

    /**
     * query the pagination versions info by one certain process definition code
     *
     * @param loginUser login user info to check auth
     * @param projectCode project code
     * @param pageNo page number
     * @param pageSize page size
     * @param code process definition code
     * @return the pagination process definition versions info of the certain process definition
     */
    Result queryProcessDefinitionVersions(User loginUser,
                                          long projectCode,
                                          int pageNo,
                                          int pageSize,
                                          long code);

    /**
     * delete one certain process definition by version number and process definition code
     *
     * @param loginUser login user info to check auth
     * @param projectCode project code
     * @param code process definition code
     * @param version version number
     * @return delele result code
     */
    Map<String, Object> deleteProcessDefinitionVersion(User loginUser,
                                                       long projectCode,
                                                       long code,
                                                       int version);

    /**
     * create empty process definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param name process definition name
     * @param description description
     * @param globalParams globalParams
     * @param timeout timeout
     * @param tenantCode tenantCode
     * @param scheduleJson scheduleJson
     * @return process definition code
     */
    Map<String, Object> createEmptyProcessDefinition(User loginUser,
                                                     long projectCode,
                                                     String name,
                                                     String description,
                                                     String globalParams,
                                                     int timeout,
                                                     String tenantCode,
                                                     String scheduleJson,
                                                     ProcessExecutionTypeEnum executionType);

    /**
     * update process definition basic info
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param name process definition name
     * @param code process definition code
     * @param description description
     * @param globalParams globalParams
     * @param timeout timeout
     * @param tenantCode tenantCode
     * @param scheduleJson scheduleJson
     * @param otherParamsJson otherParamsJson handle other params
     * @param executionType executionType
     * @return update result code
     */
    Map<String, Object> updateProcessDefinitionBasicInfo(User loginUser,
                                                         long projectCode,
                                                         String name,
                                                         long code,
                                                         String description,
                                                         String globalParams,
                                                         int timeout,
                                                         String tenantCode,
                                                         String scheduleJson,
                                                         String otherParamsJson,
                                                         ProcessExecutionTypeEnum executionType);

    /**
     * release process definition and schedule
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param code process definition code
     * @param releaseState releaseState
     * @return update result code
     */
    Map<String, Object> releaseWorkflowAndSchedule(User loginUser,
                                                   long projectCode,
                                                   long code,
                                                   ReleaseState releaseState);

    /**
     * delete other relation
     * @param project
     * @param result
     * @param processDefinition
     */
    void deleteOtherRelation(Project project, Map<String, Object> result, ProcessDefinition processDefinition);

    /**
     * save other relation
     * @param loginUser
     * @param processDefinition
     * @param result
     * @param otherParamsJson
     */
    void saveOtherRelation(User loginUser, ProcessDefinition processDefinition, Map<String, Object> result, String otherParamsJson);

    /**
     * get Json String
     * @param loginUser
     * @param processDefinition
     * @return Json String
     */
    String doOtherOperateProcess(User loginUser, ProcessDefinition processDefinition);
}

