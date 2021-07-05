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

import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.dao.entity.ProcessData;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

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
     * @param connects connects for nodes
     * @param locations locations for nodes
     * @param timeout timeout
     * @param tenantCode tenantCode
     * @param taskRelationJson relation json for nodes
     * @return create result code
     * @throws JsonProcessingException JsonProcessingException
     */
    Map<String, Object> createProcessDefinition(User loginUser,
                                                long projectCode,
                                                String name,
                                                String description,
                                                String globalParams,
                                                String connects,
                                                String locations,
                                                int timeout,
                                                String tenantCode,
                                                String taskRelationJson) throws JsonProcessingException;

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
     * query process definition list paging
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @param userId user id
     * @return process definition page
     */
    Map<String, Object> queryProcessDefinitionListPaging(User loginUser,
                                                         long projectCode,
                                                         String searchVal,
                                                         Integer pageNo,
                                                         Integer pageSize,
                                                         Integer userId);

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
     * query datail of process definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionName process definition name
     * @return process definition detail
     */

    Map<String, Object> queryProcessDefinitionByName(User loginUser,
                                                     long projectCode,
                                                     String processDefinitionName);

    /**
     * batch copy process definition
     *
     * @param loginUser loginUser
     * @param projectCode projectCode
     * @param processDefinitionCodes processDefinitionCodes
     * @param targetProjectCode targetProjectCode
     */
    Map<String, Object> batchCopyProcessDefinition(User loginUser,
                                                   long projectCode,
                                                   String processDefinitionCodes,
                                                   long targetProjectCode);

    /**
     * batch move process definition
     *
     * @param loginUser loginUser
     * @param projectCode projectCode
     * @param processDefinitionCodes processDefinitionCodes
     * @param targetProjectCode targetProjectCode
     */
    Map<String, Object> batchMoveProcessDefinition(User loginUser,
                                                   long projectCode,
                                                   String processDefinitionCodes,
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
     * @param connects connects for nodes
     * @param locations locations for nodes
     * @param timeout timeout
     * @param tenantCode tenantCode
     * @param taskRelationJson relation json for nodes
     * @return update result code
     */
    Map<String, Object> updateProcessDefinition(User loginUser,
                                                long projectCode,
                                                String name,
                                                long code,
                                                String description,
                                                String globalParams,
                                                String connects,
                                                String locations,
                                                int timeout,
                                                String tenantCode,
                                                String taskRelationJson);

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
     * delete process definition by id
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionId process definition id
     * @return delete result code
     */
    Map<String, Object> deleteProcessDefinitionById(User loginUser,
                                                    long projectCode,
                                                    Integer processDefinitionId);

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
     * batch export process definition by ids
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionIds process definition ids
     * @param response http servlet response
     */
    void batchExportProcessDefinitionByIds(User loginUser,
                                           long projectCode,
                                           String processDefinitionIds,
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
     * check the process definition node meets the specifications
     *
     * @param processData process data
     * @param processDefinitionJson process definition json
     * @return check result code
     */
    Map<String, Object> checkProcessNodeList(ProcessData processData,
                                             String processDefinitionJson);

    /**
     * get task node details based on process definition
     *
     * @param defineCode define code
     * @return task node list
     */
    Map<String, Object> getTaskNodeListByDefinitionCode(Long defineCode);

    /**
     * get task node details based on process definition
     *
     * @param defineCodeList define code list
     * @return task node list
     */
    Map<String, Object> getTaskNodeListByDefinitionCodeList(String defineCodeList);

    /**
     * query process definition all by project code
     *
     * @param projectCode project code
     * @return process definitions in the project
     */
    Map<String, Object> queryAllProcessDefinitionByProjectCode(User loginUser, long projectCode);

    /**
     * Encapsulates the TreeView structure
     *
     * @param processId process definition id
     * @param limit limit
     * @return tree view json data
     * @throws Exception exception
     */
    Map<String, Object> viewTree(Integer processId,
                                 Integer limit) throws Exception;

    /**
     * switch the defined process definition verison
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionId process definition id
     * @param version the version user want to switch
     * @return switch process definition version result code
     */
    Map<String, Object> switchProcessDefinitionVersion(User loginUser,
                                                       long projectCode,
                                                       int processDefinitionId,
                                                       long version);

    /**
     * query the pagination versions info by one certain process definition code
     *
     * @param loginUser login user info to check auth
     * @param projectCode project code
     * @param pageNo page number
     * @param pageSize page size
     * @param processDefinitionCode process definition code
     * @return the pagination process definition versions info of the certain process definition
     */
    Map<String, Object> queryProcessDefinitionVersions(User loginUser,
                                                       long projectCode,
                                                       int pageNo,
                                                       int pageSize,
                                                       long processDefinitionCode);

    /**
     * delete one certain process definition by version number and process definition id
     *
     * @param loginUser login user info to check auth
     * @param projectCode project code
     * @param processDefinitionId process definition id
     * @param version version number
     * @return delele result code
     */
    Map<String, Object> deleteByProcessDefinitionIdAndVersion(User loginUser,
                                                              long projectCode,
                                                              int processDefinitionId,
                                                              long version);

    /**
     * check has associated process definition
     *
     * @param processDefinitionId process definition id
     * @param version version
     * @return The query result has a specific process definition return true
     */
    boolean checkHasAssociatedProcessDefinition(int processDefinitionId, long version);
}

