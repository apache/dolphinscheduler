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

import org.apache.dolphinscheduler.api.dto.workflow.WorkflowCreateRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowFilterRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowUpdateRequest;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionTypeEnum;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

public interface WorkflowDefinitionService {

    /**
     * create workflow definition
     *
     * @param loginUser          login user
     * @param projectCode        project code
     * @param name               workflow definition name
     * @param description        description
     * @param globalParams       global params
     * @param locations          locations for nodes
     * @param timeout            timeout
     * @param taskRelationJson   relation json for nodes
     * @param taskDefinitionJson taskDefinitionJson
     * @param otherParamsJson    otherParamsJson handle other params
     * @return create result code
     */
    Map<String, Object> createWorkflowDefinition(User loginUser,
                                                 long projectCode,
                                                 String name,
                                                 String description,
                                                 String globalParams,
                                                 String locations,
                                                 int timeout,
                                                 String taskRelationJson,
                                                 String taskDefinitionJson,
                                                 String otherParamsJson,
                                                 WorkflowExecutionTypeEnum executionType);

    /**
     * create workflow definition V2
     *
     * @param loginUser             login user
     * @param workflowCreateRequest the new workflow object will be created
     * @return New WorkflowDefinition object created just now
     */
    WorkflowDefinition createSingleWorkflowDefinition(User loginUser, WorkflowCreateRequest workflowCreateRequest);

    /**
     * query workflow definition list
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return definition list
     */
    Map<String, Object> queryWorkflowDefinitionList(User loginUser,
                                                    long projectCode);

    /**
     * query workflow definition simple list
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return definition simple list
     */
    Map<String, Object> queryWorkflowDefinitionSimpleList(User loginUser,
                                                          long projectCode);

    /**
     * query workflow definition list paging
     *
     * @param loginUser       login user
     * @param projectCode     project code
     * @param searchVal       search value
     * @param otherParamsJson otherParamsJson handle other params
     * @param pageNo          page number
     * @param pageSize        page size
     * @param userId          user id
     * @return workflow definition page
     */
    PageInfo<WorkflowDefinition> queryWorkflowDefinitionListPaging(User loginUser,
                                                                   long projectCode,
                                                                   String searchVal,
                                                                   String otherParamsJson,
                                                                   Integer userId,
                                                                   Integer pageNo,
                                                                   Integer pageSize);

    /**
     * Filter resource workflow definitions
     *
     * @param loginUser             login user
     * @param workflowFilterRequest workflow filter requests
     * @return List workflow definition
     */
    PageInfo<WorkflowDefinition> filterWorkflowDefinition(User loginUser,
                                                          WorkflowFilterRequest workflowFilterRequest);

    /**
     * query detail of workflow definition
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param code        workflow definition code
     * @return workflow definition detail
     */

    Map<String, Object> queryWorkflowDefinitionByCode(User loginUser,
                                                      long projectCode,
                                                      long code);

    /**
     * Get resource workflow
     *
     * @param loginUser login user
     * @param code      workflow definition code
     * @return workflow definition Object
     */
    WorkflowDefinition getWorkflowDefinition(User loginUser,
                                             long code);

    Optional<WorkflowDefinition> queryWorkflowDefinition(long workflowDefinitionCode, int workflowDefinitionVersion);

    WorkflowDefinition queryWorkflowDefinitionThrowExceptionIfNotFound(long workflowDefinitionCode,
                                                                       int workflowDefinitionVersion);

    /**
     * query detail of workflow definition
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param name        workflow definition name
     * @return workflow definition detail
     */

    Map<String, Object> queryWorkflowDefinitionByName(User loginUser,
                                                      long projectCode,
                                                      String name);

    /**
     * batch copy workflow definition
     *
     * @param loginUser         loginUser
     * @param projectCode       projectCode
     * @param codes             workflowDefinitionCodes
     * @param targetProjectCode targetProjectCode
     */
    Map<String, Object> batchCopyWorkflowDefinition(User loginUser,
                                                    long projectCode,
                                                    String codes,
                                                    long targetProjectCode);

    /**
     * batch move workflow definition
     *
     * @param loginUser         loginUser
     * @param projectCode       projectCode
     * @param codes             workflowDefinitionCodes
     * @param targetProjectCode targetProjectCode
     */
    Map<String, Object> batchMoveWorkflowDefinition(User loginUser,
                                                    long projectCode,
                                                    String codes,
                                                    long targetProjectCode);

    /**
     * update workflow definition, with whole workflow definition object including task definition, task relation and location.
     *
     * @param loginUser          login user
     * @param projectCode        project code
     * @param name               workflow definition name
     * @param code               workflow definition code
     * @param description        description
     * @param globalParams       global params
     * @param locations          locations for nodes
     * @param timeout            timeout
     * @param taskRelationJson   relation json for nodes
     * @param taskDefinitionJson taskDefinitionJson
     * @return update result code
     */
    Map<String, Object> updateWorkflowDefinition(User loginUser,
                                                 long projectCode,
                                                 String name,
                                                 long code,
                                                 String description,
                                                 String globalParams,
                                                 String locations,
                                                 int timeout,
                                                 String taskRelationJson,
                                                 String taskDefinitionJson,
                                                 WorkflowExecutionTypeEnum executionType);

    /**
     * verify workflow definition name unique
     *
     * @param loginUser             login user
     * @param projectCode           project code
     * @param name                  name
     * @param workflowDefinitionCode workflowDefinitionCode
     * @return true if workflow definition name not exists, otherwise false
     */
    Map<String, Object> verifyWorkflowDefinitionName(User loginUser,
                                                     long projectCode,
                                                     String name,
                                                     long workflowDefinitionCode);

    /**
     * batch delete workflow definition by code
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param codes       workflow definition codes
     * @return delete result code
     */
    Map<String, Object> batchDeleteWorkflowDefinitionByCodes(User loginUser,
                                                             long projectCode,
                                                             String codes);

    void deleteWorkflowDefinitionByCode(User loginUser, long workflowDefinitionCode);

    /**
     * batch export workflow definition by codes
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param codes       workflow definition codes
     * @param response    http servlet response
     */
    void batchExportWorkflowDefinitionByCodes(User loginUser,
                                              long projectCode,
                                              String codes,
                                              HttpServletResponse response);

    /**
     * import workflow definition
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param file        workflow metadata json file
     * @return import workflow
     */
    Map<String, Object> importWorkflowDefinition(User loginUser,
                                                 long projectCode,
                                                 MultipartFile file);

    /**
     * import sql workflow definition
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param file        sql file, zip
     * @return import workflow
     */
    Map<String, Object> importSqlWorkflowDefinition(User loginUser,
                                                    long projectCode,
                                                    MultipartFile file);

    /**
     * check the workflow task relation json
     *
     * @param workflowTaskRelationJson workflow task relation json
     * @return check result code
     */
    Map<String, Object> checkWorkflowNodeList(String workflowTaskRelationJson,
                                              List<TaskDefinitionLog> taskDefinitionLogs);

    /**
     * get task node details based on workflow definition
     *
     * @param loginUser   loginUser
     * @param projectCode project code
     * @param code        workflowDefinition code
     * @return task node list
     */
    Map<String, Object> getTaskNodeListByDefinitionCode(User loginUser,
                                                        long projectCode,
                                                        long code);

    /**
     * get task node details map based on workflow definition
     *
     * @param loginUser   loginUser
     * @param projectCode project code
     * @param codes       define code list
     * @return task node list
     */
    Map<String, Object> getNodeListMapByDefinitionCodes(User loginUser,
                                                        long projectCode,
                                                        String codes);

    /**
     * query workflow definition all by project code
     *
     * @param projectCode project code
     * @return workflow definitions in the project
     */
    Map<String, Object> queryAllWorkflowDefinitionByProjectCode(User loginUser, long projectCode);

    /**
     * query workflow definition list by project code
     *
     * @param projectCode project code
     * @return workflow definitions in the project
     */
    Map<String, Object> queryWorkflowDefinitionListByProjectCode(long projectCode);

    /**
     * query workflow definition list by project code
     *
     * @param projectCode           project code
     * @param workflowDefinitionCode workflow definition code
     * @return workflow definitions in the project
     */
    Map<String, Object> queryTaskDefinitionListByWorkflowDefinitionCode(long projectCode, Long workflowDefinitionCode);

    /**
     * Encapsulates the TreeView structure
     *
     * @param projectCode project code
     * @param code        workflow definition code
     * @param limit       limit
     * @return tree view json data
     */
    Map<String, Object> viewTree(User loginUser, long projectCode, long code, Integer limit);

    /**
     * switch the defined workflow definition version
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param code        workflow definition code
     * @param version     the version user want to switch
     * @return switch workflow definition version result code
     */
    Map<String, Object> switchWorkflowDefinitionVersion(User loginUser,
                                                        long projectCode,
                                                        long code,
                                                        int version);

    /**
     * query the pagination versions info by one certain workflow definition code
     *
     * @param loginUser   login user info to check auth
     * @param projectCode project code
     * @param pageNo      page number
     * @param pageSize    page size
     * @param code        workflow definition code
     * @return the pagination workflow definition versions info of the certain workflow definition
     */
    Result queryWorkflowDefinitionVersions(User loginUser,
                                           long projectCode,
                                           int pageNo,
                                           int pageSize,
                                           long code);

    /**
     * delete one certain workflow definition by version number and workflow definition code
     *
     * @param loginUser                 login user info to check auth
     * @param projectCode               project code
     * @param workflowDefinitionCode    workflow definition code
     * @param workflowDefinitionVersion version number
     */
    void deleteWorkflowDefinitionVersion(User loginUser,
                                         long projectCode,
                                         long workflowDefinitionCode,
                                         int workflowDefinitionVersion);

    /**
     * update workflow definition basic info, not including task definition, task relation and location.
     *
     * @param loginUser             login user
     * @param workflowCode          workflow resource code you want to update
     * @param workflowUpdateRequest workflow update requests
     * @return WorkflowDefinition instance
     */
    WorkflowDefinition updateSingleWorkflowDefinition(User loginUser,
                                                      long workflowCode,
                                                      WorkflowUpdateRequest workflowUpdateRequest);

    /**
     * Online the workflow definition, it will check all sub workflow is online.
     */
    void onlineWorkflowDefinition(User loginUser, Long projectCode, Long workflowDefinitionCode);

    /**
     * Offline the workflow definition. It will auto offline the scheduler.
     */
    void offlineWorkflowDefinition(User loginUser, Long projectCode, Long workflowDefinitionCode);

    /**
     * view workflow variables
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param code        workflow definition code
     * @return variables data
     */
    Map<String, Object> viewVariables(User loginUser, long projectCode, long code);

    void saveWorkflowLineage(long projectCode,
                             long workflowDefinitionCode,
                             int workflowDefinitionVersion,
                             List<TaskDefinitionLog> taskDefinitionLogList);
}
