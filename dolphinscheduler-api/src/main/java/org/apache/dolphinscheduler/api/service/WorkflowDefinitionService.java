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

import org.apache.dolphinscheduler.api.dto.treeview.TreeViewDto;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowDefinitionVariablesDTO;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionTypeEnum;
import org.apache.dolphinscheduler.dao.entity.DagData;
import org.apache.dolphinscheduler.dao.entity.DependentSimplifyDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.node.ArrayNode;

public interface WorkflowDefinitionService {

    /**
     * create workflow definition
     */
    WorkflowDefinition createWorkflowDefinition(User loginUser,
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
     * query workflow definition list (full DAG data)
     */
    List<DagData> queryWorkflowDefinitionList(User loginUser,
                                              long projectCode);

    /**
     * query workflow definition simple list (id / code / name / projectCode)
     */
    ArrayNode queryWorkflowDefinitionSimpleList(User loginUser,
                                                long projectCode);

    /**
     * query workflow definition list paging
     */
    PageInfo<WorkflowDefinition> queryWorkflowDefinitionListPaging(User loginUser,
                                                                   long projectCode,
                                                                   String searchVal,
                                                                   String otherParamsJson,
                                                                   Integer userId,
                                                                   Integer pageNo,
                                                                   Integer pageSize);

    /**
     * query detail of workflow definition
     */
    DagData queryWorkflowDefinitionByCode(User loginUser,
                                          long projectCode,
                                          long code);

    Optional<WorkflowDefinition> queryWorkflowDefinition(long workflowDefinitionCode, int workflowDefinitionVersion);

    WorkflowDefinition queryWorkflowDefinitionThrowExceptionIfNotFound(long workflowDefinitionCode,
                                                                       int workflowDefinitionVersion);

    /**
     * query detail of workflow definition by name
     */
    DagData queryWorkflowDefinitionByName(User loginUser,
                                          long projectCode,
                                          String name);

    /**
     * batch copy workflow definition
     */
    void batchCopyWorkflowDefinition(User loginUser,
                                     long projectCode,
                                     String codes,
                                     long targetProjectCode);

    /**
     * batch move workflow definition
     */
    void batchMoveWorkflowDefinition(User loginUser,
                                     long projectCode,
                                     String codes,
                                     long targetProjectCode);

    /**
     * update workflow definition, with whole workflow definition object including task definition, task relation and location.
     */
    WorkflowDefinition updateWorkflowDefinition(User loginUser,
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
     * verify workflow definition name unique. Throws {@link org.apache.dolphinscheduler.api.exceptions.ServiceException}
     * with {@code WORKFLOW_DEFINITION_NAME_EXIST} when the name is already taken.
     */
    void verifyWorkflowDefinitionName(User loginUser,
                                      long projectCode,
                                      String name,
                                      long workflowDefinitionCode);

    /**
     * batch delete workflow definition by codes
     */
    void batchDeleteWorkflowDefinitionByCodes(User loginUser,
                                              long projectCode,
                                              String codes);

    void deleteWorkflowDefinitionByCode(User loginUser, long workflowDefinitionCode);

    /**
     * check the workflow task relation json (no return value: throws on validation failures)
     */
    void checkWorkflowNodeList(String workflowTaskRelationJson,
                               List<TaskDefinitionLog> taskDefinitionLogs);

    /**
     * get task node details based on workflow definition
     */
    List<TaskDefinition> getTaskNodeListByDefinitionCode(User loginUser,
                                                         long projectCode,
                                                         long code);

    /**
     * get task node details map based on workflow definition codes (workflow code -> task definition list)
     */
    Map<Long, List<TaskDefinition>> getNodeListMapByDefinitionCodes(User loginUser,
                                                                    long projectCode,
                                                                    String codes);

    /**
     * query workflow definition all by project code
     */
    List<DagData> queryAllWorkflowDefinitionByProjectCode(User loginUser, long projectCode);

    /**
     * query workflow definition list by project code (simplified records used for dependent task picker)
     */
    List<DependentSimplifyDefinition> queryWorkflowDefinitionListByProjectCode(long projectCode);

    /**
     * query task definition list (simplified records) by workflow definition code
     */
    List<DependentSimplifyDefinition> queryTaskDefinitionListByWorkflowDefinitionCode(long projectCode,
                                                                                      Long workflowDefinitionCode);

    /**
     * Encapsulates the TreeView structure
     */
    TreeViewDto viewTree(User loginUser, long projectCode, long code, Integer limit);

    /**
     * switch the defined workflow definition version
     */
    void switchWorkflowDefinitionVersion(User loginUser,
                                         long projectCode,
                                         long code,
                                         int version);

    /**
     * query the pagination versions info by one certain workflow definition code
     */
    Result queryWorkflowDefinitionVersions(User loginUser,
                                           long projectCode,
                                           int pageNo,
                                           int pageSize,
                                           long code);

    /**
     * delete one certain workflow definition by version number and workflow definition code
     */
    void deleteWorkflowDefinitionVersion(User loginUser,
                                         long projectCode,
                                         long workflowDefinitionCode,
                                         int workflowDefinitionVersion);

    /**
     * Online the workflow definition, it will check all sub workflow is online.
     */
    void onlineWorkflowDefinition(User loginUser, Long projectCode, Long workflowDefinitionCode);

    /**
     * Offline the workflow definition. It will auto offline the scheduler.
     */
    void offlineWorkflowDefinition(User loginUser, Long projectCode, Long workflowDefinitionCode);

    /**
     * view workflow variables (globalParams + localParams)
     */
    WorkflowDefinitionVariablesDTO viewVariables(User loginUser, long projectCode, long code);

    void saveWorkflowLineage(long projectCode,
                             long workflowDefinitionCode,
                             int workflowDefinitionVersion,
                             List<TaskDefinitionLog> taskDefinitionLogList);
}
