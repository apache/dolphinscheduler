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
import static org.apache.dolphinscheduler.api.enums.Status.IMPORT_PROCESS_DEFINE_ERROR;
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
import org.springframework.web.multipart.MultipartFile;

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
@RequestMapping("projects/{projectCode}/process")
public class ProcessDefinitionController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionController.class);

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    /**
     * create process definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param name process definition name
     * @param description description
     * @param globalParams globalParams
     * @param locations locations for nodes
     * @param timeout timeout
     * @param tenantCode tenantCode
     * @param taskRelationJson relation json for nodes
     * @return create result code
     */
    @ApiOperation(value = "save", notes = "CREATE_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, type = "String"),
        @ApiImplicitParam(name = "locations", value = "PROCESS_DEFINITION_LOCATIONS", required = true, type = "String"),
        @ApiImplicitParam(name = "description", value = "PROCESS_DEFINITION_DESC", required = false, type = "String")
    })
    @PostMapping(value = "/save")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_PROCESS_DEFINITION)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createProcessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                          @RequestParam(value = "name", required = true) String name,
                                          @RequestParam(value = "description", required = false) String description,
                                          @RequestParam(value = "globalParams", required = false, defaultValue = "[]") String globalParams,
                                          @RequestParam(value = "locations", required = false) String locations,
                                          @RequestParam(value = "timeout", required = false, defaultValue = "0") int timeout,
                                          @RequestParam(value = "tenantCode", required = true) String tenantCode,
                                          @RequestParam(value = "taskRelationJson", required = true) String taskRelationJson) throws JsonProcessingException {

        Map<String, Object> result = processDefinitionService.createProcessDefinition(loginUser, projectCode, name, description, globalParams,
            locations, timeout, tenantCode, taskRelationJson);
        return returnDataList(result);
    }

    /**
     * copy  process definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionCodes process definition codes
     * @param targetProjectCode target project code
     * @return copy result code
     */
    @ApiOperation(value = "copy", notes = "COPY_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "processDefinitionCodes", value = "PROCESS_DEFINITION_CODES", required = true, dataType = "String", example = "3,4"),
        @ApiImplicitParam(name = "targetProjectCode", value = "TARGET_PROJECT_CODE", required = true, dataType = "Long", example = "123")
    })
    @PostMapping(value = "/copy")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_COPY_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result copyProcessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                        @RequestParam(value = "processDefinitionCodes", required = true) String processDefinitionCodes,
                                        @RequestParam(value = "targetProjectCode", required = true) long targetProjectCode) {
        return returnDataList(processDefinitionService.batchCopyProcessDefinition(loginUser, projectCode, processDefinitionCodes, targetProjectCode));
    }

    /**
     * move process definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionCodes process definition codes
     * @param targetProjectCode target project code
     * @return move result code
     */
    @ApiOperation(value = "moveProcessDefinition", notes = "MOVE_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "processDefinitionCodes", value = "PROCESS_DEFINITION_CODES", required = true, dataType = "String", example = "3,4"),
        @ApiImplicitParam(name = "targetProjectCode", value = "TARGET_PROJECT_CODE", required = true, dataType = "Long", example = "123")
    })
    @PostMapping(value = "/move")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_MOVE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result moveProcessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                        @RequestParam(value = "processDefinitionCodes", required = true) String processDefinitionCodes,
                                        @RequestParam(value = "targetProjectCode", required = true) long targetProjectCode) {
        return returnDataList(processDefinitionService.batchMoveProcessDefinition(loginUser, projectCode, processDefinitionCodes, targetProjectCode));
    }

    /**
     * verify process definition name unique
     *
     * @param loginUser login user
     * @param projectCode project code
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
                                              @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                              @RequestParam(value = "name", required = true) String name) {
        Map<String, Object> result = processDefinitionService.verifyProcessDefinitionName(loginUser, projectCode, name);
        return returnDataList(result);
    }

    /**
     * update process definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param name process definition name
     * @param code process definition code
     * @param description description
     * @param globalParams globalParams
     * @param locations locations for nodes
     * @param timeout timeout
     * @param tenantCode tenantCode
     * @param taskRelationJson relation json for nodes
     * @return update result code
     */

    @ApiOperation(value = "update", notes = "UPDATE_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, type = "String"),
        @ApiImplicitParam(name = "code", value = "PROCESS_DEFINITION_CODE", required = true, dataType = "Long", example = "123456789"),
        @ApiImplicitParam(name = "locations", value = "PROCESS_DEFINITION_LOCATIONS", required = true, type = "String"),
        @ApiImplicitParam(name = "description", value = "PROCESS_DEFINITION_DESC", required = false, type = "String"),
        @ApiImplicitParam(name = "releaseState", value = "RELEASE_PROCESS_DEFINITION_NOTES", required = false, dataType = "ReleaseState")
    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateProcessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                          @RequestParam(value = "name", required = true) String name,
                                          @RequestParam(value = "code", required = true) long code,
                                          @RequestParam(value = "description", required = false) String description,
                                          @RequestParam(value = "globalParams", required = false, defaultValue = "[]") String globalParams,
                                          @RequestParam(value = "locations", required = false) String locations,
                                          @RequestParam(value = "timeout", required = false, defaultValue = "0") int timeout,
                                          @RequestParam(value = "tenantCode", required = true) String tenantCode,
                                          @RequestParam(value = "taskRelationJson", required = true) String taskRelationJson,
                                          @RequestParam(value = "releaseState", required = false, defaultValue = "OFFLINE") ReleaseState releaseState) {

        Map<String, Object> result = processDefinitionService.updateProcessDefinition(loginUser, projectCode, name, code, description, globalParams,
            locations, timeout, tenantCode, taskRelationJson);
        //  If the update fails, the result will be returned directly
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataList(result);
        }

        //  Judge whether to go online after editing,0 means offline, 1 means online
        if (releaseState == ReleaseState.ONLINE) {
            result = processDefinitionService.releaseProcessDefinition(loginUser, projectCode, code, releaseState);
        }
        return returnDataList(result);
    }

    /**
     * query process definition version paging list info
     *
     * @param loginUser login user info
     * @param projectCode project code
     * @param pageNo the process definition version list current page number
     * @param pageSize the process definition version list page size
     * @param processDefinitionCode the process definition code
     * @return the process definition version list
     */
    @ApiOperation(value = "queryVersions", notes = "QUERY_PROCESS_DEFINITION_VERSIONS_NOTES")
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
                                                 @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                 @RequestParam(value = "pageNo") int pageNo,
                                                 @RequestParam(value = "pageSize") int pageSize,
                                                 @RequestParam(value = "processDefinitionCode") long processDefinitionCode) {

        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        result = processDefinitionService.queryProcessDefinitionVersions(loginUser, projectCode, pageNo, pageSize, processDefinitionCode);

        return result;
    }

    /**
     * switch certain process definition version
     *
     * @param loginUser login user info
     * @param projectCode project code
     * @param processDefinitionId the process definition id
     * @param version the version user want to switch
     * @return switch version result code
     */
    @ApiOperation(value = "switchVersion", notes = "SWITCH_PROCESS_DEFINITION_VERSION_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100"),
        @ApiImplicitParam(name = "version", value = "VERSION", required = true, dataType = "Long", example = "100")
    })
    @GetMapping(value = "/version/switch")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(SWITCH_PROCESS_DEFINITION_VERSION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result switchProcessDefinitionVersion(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                 @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                 @RequestParam(value = "processDefinitionId") int processDefinitionId,
                                                 @RequestParam(value = "version") int version) {
        Map<String, Object> result = processDefinitionService.switchProcessDefinitionVersion(loginUser, projectCode, processDefinitionId, version);
        return returnDataList(result);
    }

    /**
     * delete the certain process definition version by version and process definition id
     *
     * @param loginUser login user info
     * @param projectCode project code
     * @param processDefinitionId process definition id
     * @param version the process definition version user want to delete
     * @return delete version result code
     */
    @ApiOperation(value = "deleteVersion", notes = "DELETE_PROCESS_DEFINITION_VERSION_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100"),
        @ApiImplicitParam(name = "version", value = "VERSION", required = true, dataType = "Long", example = "100")
    })
    @GetMapping(value = "/version/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_PROCESS_DEFINITION_VERSION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteProcessDefinitionVersion(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                 @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                 @RequestParam(value = "processDefinitionId") int processDefinitionId,
                                                 @RequestParam(value = "version") int version) {
        Map<String, Object> result = processDefinitionService.deleteByProcessDefinitionIdAndVersion(loginUser, projectCode, processDefinitionId, version);
        return returnDataList(result);
    }

    /**
     * release process definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param code process definition code
     * @param releaseState release state
     * @return release result code
     */
    @ApiOperation(value = "release", notes = "RELEASE_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, type = "String"),
        @ApiImplicitParam(name = "code", value = "PROCESS_DEFINITION_CODE", required = true, dataType = "Long", example = "123456789"),
        @ApiImplicitParam(name = "releaseState", value = "PROCESS_DEFINITION_RELEASE", required = true, dataType = "ReleaseState"),
    })
    @PostMapping(value = "/release")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(RELEASE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result releaseProcessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                           @RequestParam(value = "code", required = true) long code,
                                           @RequestParam(value = "releaseState", required = true) ReleaseState releaseState) {
        Map<String, Object> result = processDefinitionService.releaseProcessDefinition(loginUser, projectCode, code, releaseState);
        return returnDataList(result);
    }

    /**
     * query detail of process definition by code
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param code process definition code
     * @return process definition detail
     */
    @ApiOperation(value = "queryProcessDefinitionByCode", notes = "QUERY_PROCESS_DEFINITION_BY_CODE_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "code", value = "PROCESS_DEFINITION_CODE", required = true, dataType = "Long", example = "123456789")
    })
    @GetMapping(value = "/select-by-code")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DATAIL_OF_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProcessDefinitionByCode(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                               @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                               @RequestParam(value = "code", required = true) long code) {
        Map<String, Object> result = processDefinitionService.queryProcessDefinitionByCode(loginUser, projectCode, code);
        return returnDataList(result);
    }

    /**
     * query detail of process definition by name
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionName process definition name
     * @return process definition detail
     */
    @ApiOperation(value = "queryProcessDefinitionByName", notes = "QUERY_PROCESS_DEFINITION_BY_NAME_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "processDefinitionName", value = "PROCESS_DEFINITION_NAME", required = true, dataType = "String")
    })
    @GetMapping(value = "/select-by-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DATAIL_OF_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<ProcessDefinition> queryProcessDefinitionByName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                  @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                  @RequestParam("processDefinitionName") String processDefinitionName) {
        Map<String, Object> result = processDefinitionService.queryProcessDefinitionByName(loginUser, projectCode, processDefinitionName);
        return returnDataList(result);
    }

    /**
     * query Process definition list
     *
     * @param loginUser login user
     * @param projectCode project code
     * @return process definition list
     */
    @ApiOperation(value = "queryList", notes = "QUERY_PROCESS_DEFINITION_LIST_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_LIST)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProcessDefinitionList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode) {
        Map<String, Object> result = processDefinitionService.queryProcessDefinitionList(loginUser, projectCode);
        return returnDataList(result);
    }

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
    @ApiOperation(value = "queryListPaging", notes = "QUERY_PROCESS_DEFINITION_LIST_PAGING_NOTES")
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
                                                   @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                   @RequestParam("pageNo") Integer pageNo,
                                                   @RequestParam(value = "searchVal", required = false) String searchVal,
                                                   @RequestParam(value = "userId", required = false, defaultValue = "0") Integer userId,
                                                   @RequestParam("pageSize") Integer pageSize) {
        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);

        return processDefinitionService.queryProcessDefinitionListPaging(loginUser, projectCode, searchVal, pageNo, pageSize, userId);
    }

    /**
     * encapsulation tree view structure
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param code process definition code
     * @param limit limit
     * @return tree view json data
     */
    @ApiOperation(value = "viewTree", notes = "VIEW_TREE_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "code", value = "PROCESS_DEFINITION_CODE", required = true, dataType = "Long", example = "100"),
        @ApiImplicitParam(name = "limit", value = "LIMIT", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/view-tree")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(ENCAPSULATION_TREEVIEW_STRUCTURE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result viewTree(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                           @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                           @RequestParam("code") long code,
                           @RequestParam("limit") Integer limit) throws Exception {
        Map<String, Object> result = processDefinitionService.viewTree(code, limit);
        return returnDataList(result);
    }

    /**
     * get tasks list by process definition code
     *
     * @param loginUser login user
     * @param projectCode project code
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
        @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
        @RequestParam("processDefinitionCode") long processDefinitionCode) {
        logger.info("query task node name list by definitionCode, login user:{}, project name:{}, code : {}",
            loginUser.getUserName(), projectCode, processDefinitionCode);
        Map<String, Object> result = processDefinitionService.getTaskNodeListByDefinitionCode(loginUser, projectCode, processDefinitionCode);
        return returnDataList(result);
    }

    /**
     * get tasks list map by process definition multiple code
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionCodes process definition codes
     * @return node list data
     */
    @ApiOperation(value = "getNodeListByDefinitionCodes", notes = "GET_NODE_LIST_BY_DEFINITION_CODE_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "processDefinitionCodes", value = "PROCESS_DEFINITION_CODES", required = true, type = "String", example = "100,200,300")
    })
    @GetMapping(value = "gen-task-list-map")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR)
    public Result getNodeListMapByDefinitionCodes(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                  @RequestParam("processDefinitionCodes") String processDefinitionCodes) {
        Map<String, Object> result = processDefinitionService.getNodeListMapByDefinitionCodes(loginUser, projectCode, processDefinitionCodes);
        return returnDataList(result);
    }

    /**
     * delete process definition by id
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionId process definition id
     * @return delete result code
     */
    @ApiOperation(value = "deleteByCode", notes = "DELETE_PROCESS_DEFINITION_BY_ID_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_PROCESS_DEFINE_BY_ID_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteProcessDefinitionById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                              @RequestParam("processDefinitionId") Integer processDefinitionId) {
        Map<String, Object> result = processDefinitionService.deleteProcessDefinitionById(loginUser, projectCode, processDefinitionId);
        return returnDataList(result);
    }

    /**
     * batch delete process definition by codes
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionIds process definition id list
     * @return delete result code
     */
    @ApiOperation(value = "batchDeleteByCodes", notes = "BATCH_DELETE_PROCESS_DEFINITION_BY_IDS_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "processDefinitionIds", value = "PROCESS_DEFINITION_IDS", type = "String")
    })
    @GetMapping(value = "/batch-delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_DELETE_PROCESS_DEFINE_BY_IDS_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result batchDeleteProcessDefinitionByIds(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                    @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                    @RequestParam("processDefinitionIds") String processDefinitionIds
    ) {
        Map<String, Object> result = new HashMap<>();
        List<String> deleteFailedIdList = new ArrayList<>();
        if (StringUtils.isNotEmpty(processDefinitionIds)) {
            String[] processDefinitionIdArray = processDefinitionIds.split(",");
            for (String strProcessDefinitionId : processDefinitionIdArray) {
                int processDefinitionId = Integer.parseInt(strProcessDefinitionId);
                try {
                    Map<String, Object> deleteResult = processDefinitionService.deleteProcessDefinitionById(loginUser, projectCode, processDefinitionId);
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
     * batch export process definition by codes
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionCodes process definition codes
     * @param response response
     */
    @ApiOperation(value = "batchExportByCodes", notes = "BATCH_EXPORT_PROCESS_DEFINITION_BY_CODES_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "processDefinitionCodes", value = "PROCESS_DEFINITION_CODE", required = true, dataType = "String")
    })
    @GetMapping(value = "/export")
    @ResponseBody
    @AccessLogAnnotation(ignoreRequestArgs = {"loginUser", "response"})
    public void batchExportProcessDefinitionByCodes(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                    @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                    @RequestParam("processDefinitionCodes") String processDefinitionCodes,
                                                    HttpServletResponse response) {
        try {
            processDefinitionService.batchExportProcessDefinitionByCodes(loginUser, projectCode, processDefinitionCodes, response);
        } catch (Exception e) {
            logger.error(Status.BATCH_EXPORT_PROCESS_DEFINE_BY_IDS_ERROR.getMsg(), e);
        }
    }

    /**
     * query all process definition by project code
     *
     * @param loginUser login user
     * @param projectCode project code
     * @return process definition list
     */
    @ApiOperation(value = "queryAllByProjectCode", notes = "QUERY_PROCESS_DEFINITION_All_BY_PROJECT_CODE_NOTES")
    @GetMapping(value = "/queryAllByProjectCode")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_LIST)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAllProcessDefinitionByProjectCode(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                         @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode) {
        Map<String, Object> result = processDefinitionService.queryAllProcessDefinitionByProjectCode(loginUser, projectCode);
        return returnDataList(result);
    }

    /**
     * import process definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param file resource file
     * @return import result code
     */
    @ApiOperation(value = "importProcessDefinition", notes = "IMPORT_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "file", value = "RESOURCE_FILE", required = true, dataType = "MultipartFile")
    })
    @PostMapping(value = "/import")
    @ApiException(IMPORT_PROCESS_DEFINE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = {"loginUser", "file"})
    public Result importProcessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                          @RequestParam("file") MultipartFile file) {
        Map<String, Object> result = processDefinitionService.importProcessDefinition(loginUser, projectCode, file);
        return returnDataList(result);
    }

}
