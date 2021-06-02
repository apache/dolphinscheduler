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

package org.apache.dolphinscheduler.api.controller;

import static org.apache.dolphinscheduler.api.enums.Status.BATCH_COPY_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.BATCH_DELETE_PROCESS_DEFINE_BY_IDS_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.BATCH_MOVE_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.CREATE_PROCESS_DEFINITION;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_PROCESS_DEFINE_BY_ID_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_PROCESS_DEFINITION_VERSION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.ENCAPSULATION_TREEVIEW_STRUCTURE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_DATAIL_OF_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROCESS_DEFINITION_LIST;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROCESS_DEFINITION_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROCESS_DEFINITION_VERSIONS_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.RELEASE_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.SWITCH_PROCESS_DEFINITION_VERSION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

/**
 * process definition controller
 */
@Api(tags = "PROCESS_DEFINITION_TAG")
@RestController
@RequestMapping("projects/{projectName}/process")
public class ProcessDefinitionController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionController.class);

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    /**
     * create process definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param name process definition name
     * @param json process definition json
     * @param description description
     * @param locations locations for nodes
     * @param connects connects for nodes
     * @return create result code
     */
    @ApiOperation(value = "save", notes = "CREATE_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, type = "String"),
            @ApiImplicitParam(name = "processDefinitionJson", value = "PROCESS_DEFINITION_JSON", required = true, type = "String"),
            @ApiImplicitParam(name = "locations", value = "PROCESS_DEFINITION_LOCATIONS", required = true, type = "String"),
            @ApiImplicitParam(name = "connects", value = "PROCESS_DEFINITION_CONNECTS", required = true, type = "String"),
            @ApiImplicitParam(name = "description", value = "PROCESS_DEFINITION_DESC", required = false, type = "String"),
    })
    @PostMapping(value = "/save")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_PROCESS_DEFINITION)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createProcessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                          @RequestParam(value = "name", required = true) String name,
                                          @RequestParam(value = "processDefinitionJson", required = true) String json,
                                          @RequestParam(value = "locations", required = true) String locations,
                                          @RequestParam(value = "connects", required = true) String connects,
                                          @RequestParam(value = "description", required = false) String description) throws JsonProcessingException {

        Map<String, Object> result = processDefinitionService.createProcessDefinition(loginUser, projectName, name, json,
                description, locations, connects);
        return returnDataList(result);
    }

    /**
     * copy  process definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionIds process definition ids
     * @param targetProjectId target project id
     * @return copy result code
     */
    @ApiOperation(value = "copyProcessDefinition", notes = "COPY_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionIds", value = "PROCESS_DEFINITION_IDS", required = true, dataType = "String", example = "3,4"),
            @ApiImplicitParam(name = "targetProjectId", value = "TARGET_PROJECT_ID", required = true, dataType = "Int", example = "10")
    })
    @PostMapping(value = "/copy")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_COPY_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result copyProcessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                        @RequestParam(value = "processDefinitionIds", required = true) String processDefinitionIds,
                                        @RequestParam(value = "targetProjectId", required = true) int targetProjectId) {
        return returnDataList(
                processDefinitionService.batchCopyProcessDefinition(loginUser, projectName, processDefinitionIds, targetProjectId));
    }

    /**
     * move process definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionIds process definition ids
     * @param targetProjectId target project id
     * @return move result code
     */
    @ApiOperation(value = "moveProcessDefinition", notes = "MOVE_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionIds", value = "PROCESS_DEFINITION_IDS", required = true, dataType = "String", example = "3,4"),
            @ApiImplicitParam(name = "targetProjectId", value = "TARGET_PROJECT_ID", required = true, dataType = "Int", example = "10")
    })
    @PostMapping(value = "/move")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_MOVE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result moveProcessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                        @RequestParam(value = "processDefinitionIds", required = true) String processDefinitionIds,
                                        @RequestParam(value = "targetProjectId", required = true) int targetProjectId) {
        return returnDataList(
                processDefinitionService.batchMoveProcessDefinition(loginUser, projectName, processDefinitionIds, targetProjectId));
    }

    /**
     * verify process definition name unique
     *
     * @param loginUser login user
     * @param projectName project name
     * @param name name
     * @return true if process definition name not exists, otherwise false
     */
    @ApiOperation(value = "verify-name", notes = "VERIFY_PROCESS_DEFINITION_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, type = "String")
    })
    @GetMapping(value = "/verify-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result verifyProcessDefinitionName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                              @RequestParam(value = "name", required = true) String name) {
        Map<String, Object> result = processDefinitionService.verifyProcessDefinitionName(loginUser, projectName, name);
        return returnDataList(result);
    }

    /**
     * update process definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param name process definition name
     * @param id process definition id
     * @param processDefinitionJson process definition json
     * @param description description
     * @param locations locations for nodes
     * @param connects connects for nodes
     * @return update result code
     */

    @ApiOperation(value = "updateProcessDefinition", notes = "UPDATE_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, type = "String"),
            @ApiImplicitParam(name = "id", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "processDefinitionJson", value = "PROCESS_DEFINITION_JSON", required = true, type = "String"),
            @ApiImplicitParam(name = "locations", value = "PROCESS_DEFINITION_LOCATIONS", required = true, type = "String"),
            @ApiImplicitParam(name = "connects", value = "PROCESS_DEFINITION_CONNECTS", required = true, type = "String"),
            @ApiImplicitParam(name = "description", value = "PROCESS_DEFINITION_DESC", required = false, type = "String"),
            @ApiImplicitParam(name = "releaseState", value = "RELEASE_PROCESS_DEFINITION_NOTES", required = false, dataType = "ReleaseState")
    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateProcessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                          @RequestParam(value = "name", required = true) String name,
                                          @RequestParam(value = "id", required = true) int id,
                                          @RequestParam(value = "processDefinitionJson", required = true) String processDefinitionJson,
                                          @RequestParam(value = "locations", required = false) String locations,
                                          @RequestParam(value = "connects", required = false) String connects,
                                          @RequestParam(value = "description", required = false) String description,
                                          @RequestParam(value = "releaseState", required = false, defaultValue = "OFFLINE") ReleaseState releaseState) {

        Map<String, Object> result = processDefinitionService.updateProcessDefinition(loginUser, projectName, id, name,
                processDefinitionJson, description, locations, connects);
        //  If the update fails, the result will be returned directly
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataList(result);
        }

        //  Judge whether to go online after editing,0 means offline, 1 means online
        if (releaseState == ReleaseState.ONLINE) {
            result = processDefinitionService.releaseProcessDefinition(loginUser, projectName, id, releaseState);
        }
        return returnDataList(result);
    }

    /**
     * query process definition version paging list info
     *
     * @param loginUser login user info
     * @param projectName the process definition project name
     * @param pageNo the process definition version list current page number
     * @param pageSize the process definition version list page size
     * @param processDefinitionCode the process definition code
     * @return the process definition version list
     */
    @ApiOperation(value = "queryProcessDefinitionVersions", notes = "QUERY_PROCESS_DEFINITION_VERSIONS_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "processDefinitionCode", value = "PROCESS_DEFINITION_CODE", required = true, dataType = "Long", example = "1")
    })
    @GetMapping(value = "/versions")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_VERSIONS_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProcessDefinitionVersions(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                 @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                 @RequestParam(value = "pageNo") int pageNo,
                                                 @RequestParam(value = "pageSize") int pageSize,
                                                 @RequestParam(value = "processDefinitionCode") long processDefinitionCode) {
        Map<String, Object> result = processDefinitionService.queryProcessDefinitionVersions(loginUser
                , projectName, pageNo, pageSize, processDefinitionCode);

        return returnDataList(result);
    }

    /**
     * switch certain process definition version
     *
     * @param loginUser login user info
     * @param projectName the process definition project name
     * @param processDefinitionId the process definition id
     * @param version the version user want to switch
     * @return switch version result code
     */
    @ApiOperation(value = "switchProcessDefinitionVersion", notes = "SWITCH_PROCESS_DEFINITION_VERSION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "version", value = "VERSION", required = true, dataType = "Long", example = "100")
    })
    @GetMapping(value = "/version/switch")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(SWITCH_PROCESS_DEFINITION_VERSION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result switchProcessDefinitionVersion(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                 @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                 @RequestParam(value = "processDefinitionId") int processDefinitionId,
                                                 @RequestParam(value = "version") long version) {
        Map<String, Object> result = processDefinitionService.switchProcessDefinitionVersion(loginUser, projectName
                , processDefinitionId, version);
        return returnDataList(result);
    }

    /**
     * delete the certain process definition version by version and process definition id
     *
     * @param loginUser login user info
     * @param projectName the process definition project name
     * @param processDefinitionId process definition id
     * @param version the process definition version user want to delete
     * @return delete version result code
     */
    @ApiOperation(value = "deleteProcessDefinitionVersion", notes = "DELETE_PROCESS_DEFINITION_VERSION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "version", value = "VERSION", required = true, dataType = "Long", example = "100")
    })
    @GetMapping(value = "/version/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_PROCESS_DEFINITION_VERSION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteProcessDefinitionVersion(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                 @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                 @RequestParam(value = "processDefinitionId") int processDefinitionId,
                                                 @RequestParam(value = "version") long version) {
        Map<String, Object> result = processDefinitionService.deleteByProcessDefinitionIdAndVersion(loginUser, projectName, processDefinitionId, version);
        return returnDataList(result);
    }

    /**
     * release process definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processId process definition id
     * @param releaseState release state
     * @return release result code
     */
    @ApiOperation(value = "releaseProcessDefinition", notes = "RELEASE_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, type = "String"),
            @ApiImplicitParam(name = "processId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "releaseState", value = "PROCESS_DEFINITION_CONNECTS", required = true, dataType = "ReleaseState"),
    })
    @PostMapping(value = "/release")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(RELEASE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result releaseProcessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                           @RequestParam(value = "processId", required = true) int processId,
                                           @RequestParam(value = "releaseState", required = true) ReleaseState releaseState) {

        Map<String, Object> result = processDefinitionService.releaseProcessDefinition(loginUser, projectName, processId, releaseState);
        return returnDataList(result);
    }

    /**
     * query datail of process definition by id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processId process definition id
     * @return process definition detail
     */
    @ApiOperation(value = "queryProcessDefinitionById", notes = "QUERY_PROCESS_DEFINITION_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/select-by-id")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DATAIL_OF_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProcessDefinitionById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                             @RequestParam("processId") Integer processId
    ) {
        Map<String, Object> result = processDefinitionService.queryProcessDefinitionById(loginUser, projectName, processId);
        return returnDataList(result);
    }

    /**
     * query datail of process definition by name
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionName process definition name
     * @return process definition detail
     */
    @ApiOperation(value = "queryProcessDefinitionByName", notes = "QUERY_PROCESS_DEFINITION_BY_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionName", value = "PROCESS_DEFINITION_ID", required = true, dataType = "String")
    })
    @GetMapping(value = "/select-by-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DATAIL_OF_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<ProcessDefinition> queryProcessDefinitionByName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                  @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                                  @RequestParam("processDefinitionName") String processDefinitionName
    ) {
        Map<String, Object> result = processDefinitionService.queryProcessDefinitionByName(loginUser, projectName, processDefinitionName);
        return returnDataList(result);
    }

    /**
     * query Process definition list
     *
     * @param loginUser login user
     * @param projectName project name
     * @return process definition list
     */
    @ApiOperation(value = "queryProcessDefinitionList", notes = "QUERY_PROCESS_DEFINITION_LIST_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_LIST)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProcessDefinitionList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName
    ) {
        Map<String, Object> result = processDefinitionService.queryProcessDefinitionList(loginUser, projectName);
        return returnDataList(result);
    }

    /**
     * query process definition list paging
     *
     * @param loginUser login user
     * @param projectName project name
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @param userId user id
     * @return process definition page
     */
    @ApiOperation(value = "queryProcessDefinitionListPaging", notes = "QUERY_PROCESS_DEFINITION_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", required = false, type = "String"),
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = false, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProcessDefinitionListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                   @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                   @RequestParam("pageNo") Integer pageNo,
                                                   @RequestParam(value = "searchVal", required = false) String searchVal,
                                                   @RequestParam(value = "userId", required = false, defaultValue = "0") Integer userId,
                                                   @RequestParam("pageSize") Integer pageSize) {
        Map<String, Object> result = checkPageParams(pageNo, pageSize);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataListPaging(result);
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = processDefinitionService.queryProcessDefinitionListPaging(loginUser, projectName, searchVal, pageNo, pageSize, userId);
        return returnDataListPaging(result);
    }

    /**
     * encapsulation treeview structure
     *
     * @param loginUser login user
     * @param projectName project name
     * @param id process definition id
     * @param limit limit
     * @return tree view json data
     */
    @ApiOperation(value = "viewTree", notes = "VIEW_TREE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "limit", value = "LIMIT", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/view-tree")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(ENCAPSULATION_TREEVIEW_STRUCTURE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result viewTree(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                           @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                           @RequestParam("processId") Integer id,
                           @RequestParam("limit") Integer limit) throws Exception {
        Map<String, Object> result = processDefinitionService.viewTree(id, limit);
        return returnDataList(result);
    }

    /**
     * get tasks list by process definition code
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionCode process definition code
     * @return task list
     */
    @ApiOperation(value = "getNodeListByDefinitionCode", notes = "GET_NODE_LIST_BY_DEFINITION_CODE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionCode", value = "PROCESS_DEFINITION_CODE", required = true, dataType = "Long", example = "100")
    })
    @GetMapping(value = "gen-task-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR)
    public Result getNodeListByDefinitionCode(
            @ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
            @RequestParam("processDefinitionCode") Long processDefinitionCode) throws Exception {
        logger.info("query task node name list by definitionCode, login user:{}, project name:{}, code : {}",
                loginUser.getUserName(), projectName, processDefinitionCode);
        Map<String, Object> result = processDefinitionService.getTaskNodeListByDefinitionCode(processDefinitionCode);
        return returnDataList(result);
    }

    /**
     * get tasks list by process definition code list
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionCodeList process definition code list
     * @return node list data
     */
    @ApiOperation(value = "getNodeListByDefinitionCodeList", notes = "GET_NODE_LIST_BY_DEFINITION_CODE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionCodeList", value = "PROCESS_DEFINITION_CODE_LIST", required = true, type = "String")
    })
    @GetMapping(value = "get-task-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR)
    public Result getNodeListByDefinitionCodeList(
            @ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
            @RequestParam("processDefinitionCodeList") String processDefinitionCodeList) {
        Map<String, Object> result = processDefinitionService.getTaskNodeListByDefinitionCodeList(processDefinitionCodeList);
        return returnDataList(result);
    }

    /**
     * delete process definition by id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionId process definition id
     * @return delete result code
     */
    @ApiOperation(value = "deleteProcessDefinitionById", notes = "DELETE_PROCESS_DEFINITION_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_PROCESS_DEFINE_BY_ID_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteProcessDefinitionById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                              @RequestParam("processDefinitionId") Integer processDefinitionId
    ) {
        Map<String, Object> result = processDefinitionService.deleteProcessDefinitionById(loginUser, projectName, processDefinitionId);
        return returnDataList(result);
    }

    /**
     * batch delete process definition by ids
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionIds process definition id list
     * @return delete result code
     */
    @ApiOperation(value = "batchDeleteProcessDefinitionByIds", notes = "BATCH_DELETE_PROCESS_DEFINITION_BY_IDS_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionIds", value = "PROCESS_DEFINITION_IDS", type = "String")
    })
    @GetMapping(value = "/batch-delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_DELETE_PROCESS_DEFINE_BY_IDS_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result batchDeleteProcessDefinitionByIds(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                    @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                    @RequestParam("processDefinitionIds") String processDefinitionIds
    ) {
        Map<String, Object> result = new HashMap<>();
        List<String> deleteFailedIdList = new ArrayList<>();
        if (StringUtils.isNotEmpty(processDefinitionIds)) {
            String[] processDefinitionIdArray = processDefinitionIds.split(",");
            for (String strProcessDefinitionId : processDefinitionIdArray) {
                int processDefinitionId = Integer.parseInt(strProcessDefinitionId);
                try {
                    Map<String, Object> deleteResult = processDefinitionService.deleteProcessDefinitionById(loginUser, projectName, processDefinitionId);
                    if (!Status.SUCCESS.equals(deleteResult.get(Constants.STATUS))) {
                        deleteFailedIdList.add(strProcessDefinitionId);
                        logger.error((String) deleteResult.get(Constants.MSG));
                    }
                } catch (Exception e) {
                    deleteFailedIdList.add(strProcessDefinitionId);
                }
            }
        }

        if (!deleteFailedIdList.isEmpty()) {
            putMsg(result, Status.BATCH_DELETE_PROCESS_DEFINE_BY_IDS_ERROR, String.join(",", deleteFailedIdList));
        } else {
            putMsg(result, Status.SUCCESS);
        }

        return returnDataList(result);
    }

    /**
     * batch export process definition by ids
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionIds process definition ids
     * @param response response
     */

    @ApiOperation(value = "batchExportProcessDefinitionByIds", notes = "BATCH_EXPORT_PROCESS_DEFINITION_BY_IDS_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionIds", value = "PROCESS_DEFINITION_ID", required = true, dataType = "String")
    })
    @GetMapping(value = "/export")
    @ResponseBody
    @AccessLogAnnotation(ignoreRequestArgs = {"loginUser", "response"})
    public void batchExportProcessDefinitionByIds(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                  @RequestParam("processDefinitionIds") String processDefinitionIds,
                                                  HttpServletResponse response) {
        try {
            processDefinitionService.batchExportProcessDefinitionByIds(loginUser, projectName, processDefinitionIds, response);
        } catch (Exception e) {
            logger.error(Status.BATCH_EXPORT_PROCESS_DEFINE_BY_IDS_ERROR.getMsg(), e);
        }
    }

    /**
     * query process definition all by project id
     *
     * @param loginUser login user
     * @param projectId project id
     * @return process definition list
     */
    @ApiOperation(value = "queryProcessDefinitionAllByProjectId", notes = "QUERY_PROCESS_DEFINITION_All_BY_PROJECT_ID_NOTES")
    @GetMapping(value = "/queryProcessDefinitionAllByProjectId")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_LIST)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProcessDefinitionAllByProjectId(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                       @RequestParam("projectId") Integer projectId) {
        Map<String, Object> result = processDefinitionService.queryProcessDefinitionAllByProjectId(projectId);
        return returnDataList(result);
    }

}
