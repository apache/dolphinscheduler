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
import static org.apache.dolphinscheduler.api.enums.Status.BATCH_DELETE_PROCESS_DEFINE_BY_CODES_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.BATCH_MOVE_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.CREATE_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_PROCESS_DEFINE_BY_CODE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_PROCESS_DEFINITION_VERSION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.ENCAPSULATION_TREEVIEW_STRUCTURE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.IMPORT_PROCESS_DEFINE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_DETAIL_OF_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROCESS_DEFINITION_ALL_VARIABLES_ERROR;
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
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * process definition controller
 */
@Tag(name = "PROCESS_DEFINITION_TAG")
@RestController
@RequestMapping("projects/{projectCode}/process-definition")
@Slf4j
public class ProcessDefinitionController extends BaseController {

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
     * @param taskRelationJson relation json for nodes
     * @param taskDefinitionJson taskDefinitionJson
     * @param otherParamsJson otherParamsJson handle other params
     * @return create result code
     */
    @Operation(summary = "createProcessDefinition", description = "CREATE_PROCESS_DEFINITION_NOTES")
    @Parameters({
            @Parameter(name = "name", description = "PROCESS_DEFINITION_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "locations", description = "PROCESS_DEFINITION_LOCATIONS", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "description", description = "PROCESS_DEFINITION_DESC", required = false, schema = @Schema(implementation = String.class)),
            @Parameter(name = "otherParamsJson", description = "OTHER_PARAMS_JSON", required = false, schema = @Schema(implementation = String.class))
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createProcessDefinition(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                          @RequestParam(value = "name", required = true) String name,
                                          @RequestParam(value = "description", required = false) String description,
                                          @RequestParam(value = "globalParams", required = false, defaultValue = "[]") String globalParams,
                                          @RequestParam(value = "locations", required = false) String locations,
                                          @RequestParam(value = "timeout", required = false, defaultValue = "0") int timeout,
                                          @RequestParam(value = "taskRelationJson", required = true) String taskRelationJson,
                                          @RequestParam(value = "taskDefinitionJson", required = true) String taskDefinitionJson,
                                          @RequestParam(value = "otherParamsJson", required = false) String otherParamsJson,
                                          @RequestParam(value = "executionType", defaultValue = "PARALLEL") ProcessExecutionTypeEnum executionType) {
        Map<String, Object> result = processDefinitionService.createProcessDefinition(loginUser, projectCode, name,
                description, globalParams,
                locations, timeout, taskRelationJson, taskDefinitionJson, otherParamsJson, executionType);
        return returnDataList(result);
    }

    /**
     * copy process definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param codes process definition codes
     * @param targetProjectCode target project code
     * @return copy result code
     */
    @Operation(summary = "batchCopyByCodes", description = "COPY_PROCESS_DEFINITION_NOTES")
    @Parameters({
            @Parameter(name = "codes", description = "PROCESS_DEFINITION_CODES", required = true, schema = @Schema(implementation = String.class, example = "3,4")),
            @Parameter(name = "targetProjectCode", description = "TARGET_PROJECT_CODE", required = true, schema = @Schema(implementation = long.class, example = "123"))
    })
    @PostMapping(value = "/batch-copy")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_COPY_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result copyProcessDefinition(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                        @RequestParam(value = "codes", required = true) String codes,
                                        @RequestParam(value = "targetProjectCode", required = true) long targetProjectCode) {
        return returnDataList(
                processDefinitionService.batchCopyProcessDefinition(loginUser, projectCode, codes, targetProjectCode));
    }

    /**
     * move process definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param codes process definition codes
     * @param targetProjectCode target project code
     * @return move result code
     */
    @Operation(summary = "batchMoveByCodes", description = "MOVE_PROCESS_DEFINITION_NOTES")
    @Parameters({
            @Parameter(name = "codes", description = "PROCESS_DEFINITION_CODES", required = true, schema = @Schema(implementation = String.class, example = "3,4")),
            @Parameter(name = "targetProjectCode", description = "TARGET_PROJECT_CODE", required = true, schema = @Schema(implementation = long.class, example = "123"))
    })
    @PostMapping(value = "/batch-move")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_MOVE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result moveProcessDefinition(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                        @RequestParam(value = "codes", required = true) String codes,
                                        @RequestParam(value = "targetProjectCode", required = true) long targetProjectCode) {
        return returnDataList(
                processDefinitionService.batchMoveProcessDefinition(loginUser, projectCode, codes, targetProjectCode));
    }

    /**
     * verify process definition name unique
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param name name
     * @return true if process definition name not exists, otherwise false
     */
    @Operation(summary = "verify-name", description = "VERIFY_PROCESS_DEFINITION_NAME_NOTES")
    @Parameters({
            @Parameter(name = "name", description = "PROCESS_DEFINITION_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "code", description = "PROCESS_DEFINITION_CODE", required = false, schema = @Schema(implementation = Long.class)),
    })
    @GetMapping(value = "/verify-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result verifyProcessDefinitionName(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                              @RequestParam(value = "name", required = true) String name,
                                              @RequestParam(value = "code", required = false, defaultValue = "0") long processDefinitionCode) {
        Map<String, Object> result = processDefinitionService.verifyProcessDefinitionName(loginUser, projectCode, name,
                processDefinitionCode);
        return returnDataList(result);
    }

    /**
     * update process definition, with whole process definition object including task definition, task relation and location.
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param name process definition name
     * @param code process definition code
     * @param description description
     * @param globalParams globalParams
     * @param locations locations for nodes
     * @param timeout timeout
     * @param taskRelationJson relation json for nodes
     * @param taskDefinitionJson taskDefinitionJson
     * @param otherParamsJson otherParamsJson handle other params
     * @return update result code
     */
    @Operation(summary = "update", description = "UPDATE_PROCESS_DEFINITION_NOTES")
    @Parameters({
            @Parameter(name = "name", description = "PROCESS_DEFINITION_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "code", description = "PROCESS_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "123456789")),
            @Parameter(name = "locations", description = "PROCESS_DEFINITION_LOCATIONS", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "description", description = "PROCESS_DEFINITION_DESC", required = false, schema = @Schema(implementation = String.class)),
            @Parameter(name = "releaseState", description = "RELEASE_PROCESS_DEFINITION_NOTES", required = false, schema = @Schema(implementation = ReleaseState.class)),
            @Parameter(name = "otherParamsJson", description = "OTHER_PARAMS_JSON", required = false, schema = @Schema(implementation = String.class))
    })
    @PutMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateProcessDefinition(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                          @RequestParam(value = "name", required = true) String name,
                                          @PathVariable(value = "code", required = true) long code,
                                          @RequestParam(value = "description", required = false) String description,
                                          @RequestParam(value = "globalParams", required = false, defaultValue = "[]") String globalParams,
                                          @RequestParam(value = "locations", required = false) String locations,
                                          @RequestParam(value = "timeout", required = false, defaultValue = "0") int timeout,
                                          @RequestParam(value = "taskRelationJson", required = true) String taskRelationJson,
                                          @RequestParam(value = "taskDefinitionJson", required = true) String taskDefinitionJson,
                                          @RequestParam(value = "otherParamsJson", required = false) String otherParamsJson,
                                          @RequestParam(value = "executionType", defaultValue = "PARALLEL") ProcessExecutionTypeEnum executionType,
                                          @RequestParam(value = "releaseState", required = false, defaultValue = "OFFLINE") ReleaseState releaseState) {

        Map<String, Object> result = processDefinitionService.updateProcessDefinition(loginUser, projectCode, name,
                code, description, globalParams,
                locations, timeout, taskRelationJson, taskDefinitionJson, otherParamsJson, executionType);
        // If the update fails, the result will be returned directly
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataList(result);
        }

        // Judge whether to go online after editing,0 means offline, 1 means online
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
     * @param code the process definition code
     * @return the process definition version list
     */
    @Operation(summary = "queryVersions", description = "QUERY_PROCESS_DEFINITION_VERSIONS_NOTES")
    @Parameters({
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "10")),
            @Parameter(name = "code", description = "PROCESS_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "1"))
    })
    @GetMapping(value = "/{code}/versions")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_VERSIONS_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProcessDefinitionVersions(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                 @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                 @RequestParam(value = "pageNo") int pageNo,
                                                 @RequestParam(value = "pageSize") int pageSize,
                                                 @PathVariable(value = "code") long code) {

        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        result = processDefinitionService.queryProcessDefinitionVersions(loginUser, projectCode, pageNo, pageSize,
                code);

        return result;
    }

    /**
     * switch certain process definition version
     *
     * @param loginUser login user info
     * @param projectCode project code
     * @param code the process definition code
     * @param version the version user want to switch
     * @return switch version result code
     */
    @Operation(summary = "switchVersion", description = "SWITCH_PROCESS_DEFINITION_VERSION_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "PROCESS_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "1")),
            @Parameter(name = "version", description = "VERSION", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/{code}/versions/{version}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(SWITCH_PROCESS_DEFINITION_VERSION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result switchProcessDefinitionVersion(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                 @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                 @PathVariable(value = "code") long code,
                                                 @PathVariable(value = "version") int version) {
        Map<String, Object> result =
                processDefinitionService.switchProcessDefinitionVersion(loginUser, projectCode, code, version);
        return returnDataList(result);
    }

    /**
     * delete the certain process definition version by version and process definition code
     *
     * @param loginUser login user info
     * @param projectCode project code
     * @param code the process definition code
     * @param version the process definition version user want to delete
     * @return delete version result code
     */
    @Operation(summary = "deleteVersion", description = "DELETE_PROCESS_DEFINITION_VERSION_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "PROCESS_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "1")),
            @Parameter(name = "version", description = "VERSION", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @DeleteMapping(value = "/{code}/versions/{version}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_PROCESS_DEFINITION_VERSION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteProcessDefinitionVersion(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                 @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                 @PathVariable(value = "code") long code,
                                                 @PathVariable(value = "version") int version) {
        Map<String, Object> result =
                processDefinitionService.deleteProcessDefinitionVersion(loginUser, projectCode, code, version);
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
    @Operation(summary = "release", description = "RELEASE_PROCESS_DEFINITION_NOTES")
    @Parameters({
            @Parameter(name = "name", description = "PROCESS_DEFINITION_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "code", description = "PROCESS_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "123456789")),
            @Parameter(name = "releaseState", description = "PROCESS_DEFINITION_RELEASE", required = true, schema = @Schema(implementation = ReleaseState.class)),
    })
    @PostMapping(value = "/{code}/release")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(RELEASE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result releaseProcessDefinition(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                           @PathVariable(value = "code", required = true) long code,
                                           @RequestParam(value = "releaseState", required = true) ReleaseState releaseState) {
        Map<String, Object> result =
                processDefinitionService.releaseProcessDefinition(loginUser, projectCode, code, releaseState);
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
    @Operation(summary = "queryProcessDefinitionByCode", description = "QUERY_PROCESS_DEFINITION_BY_CODE_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "PROCESS_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "123456789"))
    })
    @GetMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DETAIL_OF_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProcessDefinitionByCode(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                               @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                               @PathVariable(value = "code", required = true) long code) {
        Map<String, Object> result =
                processDefinitionService.queryProcessDefinitionByCode(loginUser, projectCode, code);
        return returnDataList(result);
    }

    /**
     * query detail of process definition by name
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param name process definition name
     * @return process definition detail
     */
    @Operation(summary = "queryProcessDefinitionByName", description = "QUERY_PROCESS_DEFINITION_BY_NAME_NOTES")
    @Parameters({
            @Parameter(name = "name", description = "PROCESS_DEFINITION_NAME", required = true, schema = @Schema(implementation = String.class))
    })
    @GetMapping(value = "/query-by-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DETAIL_OF_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<ProcessDefinition> queryProcessDefinitionByName(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                  @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                  @RequestParam("name") String name) {
        Map<String, Object> result =
                processDefinitionService.queryProcessDefinitionByName(loginUser, projectCode, name);
        return returnDataList(result);
    }

    /**
     * query Process definition list
     *
     * @param loginUser login user
     * @param projectCode project code
     * @return process definition list
     */
    @Operation(summary = "queryList", description = "QUERY_PROCESS_DEFINITION_LIST_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_LIST)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProcessDefinitionList(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode) {
        Map<String, Object> result = processDefinitionService.queryProcessDefinitionList(loginUser, projectCode);
        return returnDataList(result);
    }

    /**
     * query Process definition simple list
     *
     * @param loginUser login user
     * @param projectCode project code
     * @return process definition list
     */
    @Operation(summary = "querySimpleList", description = "QUERY_PROCESS_DEFINITION_SIMPLE_LIST_NOTES")
    @GetMapping(value = "/simple-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_LIST)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProcessDefinitionSimpleList(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                   @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode) {
        Map<String, Object> result = processDefinitionService.queryProcessDefinitionSimpleList(loginUser, projectCode);
        return returnDataList(result);
    }

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
    @Operation(summary = "queryListPaging", description = "QUERY_PROCESS_DEFINITION_LIST_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "searchVal", description = "SEARCH_VAL", required = false, schema = @Schema(implementation = String.class)),
            @Parameter(name = "userId", description = "USER_ID", required = false, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "10")),
            @Parameter(name = "otherParamsJson", description = "OTHER_PARAMS_JSON", required = false, schema = @Schema(implementation = String.class))
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<PageInfo<ProcessDefinition>> queryProcessDefinitionListPaging(
                                                                                @Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                                @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                                @RequestParam(value = "searchVal", required = false) String searchVal,
                                                                                @RequestParam(value = "otherParamsJson", required = false) String otherParamsJson,
                                                                                @RequestParam(value = "userId", required = false, defaultValue = "0") Integer userId,
                                                                                @RequestParam("pageNo") Integer pageNo,
                                                                                @RequestParam("pageSize") Integer pageSize) {

        Result<PageInfo<ProcessDefinition>> result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);

        PageInfo<ProcessDefinition> pageInfo = processDefinitionService.queryProcessDefinitionListPaging(
                loginUser, projectCode, searchVal, otherParamsJson, userId, pageNo, pageSize);
        return Result.success(pageInfo);

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
    @Operation(summary = "viewTree", description = "VIEW_TREE_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "PROCESS_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "100")),
            @Parameter(name = "limit", description = "LIMIT", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/{code}/view-tree")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(ENCAPSULATION_TREEVIEW_STRUCTURE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result viewTree(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                           @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                           @PathVariable("code") long code,
                           @RequestParam("limit") Integer limit) {
        Map<String, Object> result = processDefinitionService.viewTree(loginUser, projectCode, code, limit);
        return returnDataList(result);
    }

    /**
     * get tasks list by process definition code
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param code process definition code
     * @return task list
     */
    @Operation(summary = "getTasksByDefinitionCode", description = "GET_TASK_LIST_BY_DEFINITION_CODE_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "PROCESS_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "100"))
    })
    @GetMapping(value = "/{code}/tasks")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR)
    public Result getNodeListByDefinitionCode(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                              @PathVariable("code") long code) {
        Map<String, Object> result =
                processDefinitionService.getTaskNodeListByDefinitionCode(loginUser, projectCode, code);
        return returnDataList(result);
    }

    /**
     * get tasks list map by process definition multiple code
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param codes process definition codes
     * @return node list data
     */
    @Operation(summary = "getTaskListByDefinitionCodes", description = "GET_TASK_LIST_BY_DEFINITION_CODE_NOTES")
    @Parameters({
            @Parameter(name = "codes", description = "PROCESS_DEFINITION_CODES", required = true, schema = @Schema(implementation = String.class, example = "100,200,300"))
    })
    @GetMapping(value = "/batch-query-tasks")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR)
    public Result getNodeListMapByDefinitionCodes(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                  @RequestParam("codes") String codes) {
        Map<String, Object> result =
                processDefinitionService.getNodeListMapByDefinitionCodes(loginUser, projectCode, codes);
        return returnDataList(result);
    }

    /**
     * get process definition list map by project code
     *
     * @param loginUser login user
     * @param projectCode project code
     * @return process definition list data
     */
    @Operation(summary = "getProcessListByProjectCode", description = "GET_PROCESS_LIST_BY_PROCESS_CODE_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true, schema = @Schema(implementation = long.class, example = "100"))
    })
    @GetMapping(value = "/query-process-definition-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR)
    public Result getProcessListByProjectCode(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode) {
        Map<String, Object> result = processDefinitionService.queryProcessDefinitionListByProjectCode(projectCode);
        return returnDataList(result);
    }

    /**
     * get task definition list by process definition code
     *
     * @param loginUser login user
     * @param projectCode project code
     * @return process definition list data
     */
    @Operation(summary = "getTaskListByProcessDefinitionCode", description = "GET_TASK_LIST_BY_PROCESS_CODE_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true, schema = @Schema(implementation = long.class, example = "100")),
            @Parameter(name = "processDefinitionCode", description = "PROCESS_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "100")),
    })
    @GetMapping(value = "/query-task-definition-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR)
    public Result getTaskListByProcessDefinitionCode(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                     @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                     @RequestParam(value = "processDefinitionCode") Long processDefinitionCode) {
        Map<String, Object> result = processDefinitionService
                .queryTaskDefinitionListByProcessDefinitionCode(projectCode, processDefinitionCode);
        return returnDataList(result);
    }

    @Operation(summary = "deleteByCode", description = "DELETE_PROCESS_DEFINITION_BY_ID_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "PROCESS_DEFINITION_CODE", schema = @Schema(implementation = int.class, example = "100"))
    })
    @DeleteMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_PROCESS_DEFINE_BY_CODE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteProcessDefinitionByCode(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                @PathVariable("code") long workflowDefinitionCode) {
        processDefinitionService.deleteProcessDefinitionByCode(loginUser, workflowDefinitionCode);
        return new Result(Status.SUCCESS);
    }

    /**
     * batch delete process definition by codes
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param codes process definition code list
     * @return delete result code
     */
    @Operation(summary = "batchDeleteByCodes", description = "BATCH_DELETE_PROCESS_DEFINITION_BY_IDS_NOTES")
    @Parameters({
            @Parameter(name = "codes", description = "PROCESS_DEFINITION_CODE", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping(value = "/batch-delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_DELETE_PROCESS_DEFINE_BY_CODES_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result batchDeleteProcessDefinitionByCodes(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                      @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                      @RequestParam("codes") String codes) {

        Map<String, Object> result =
                processDefinitionService.batchDeleteProcessDefinitionByCodes(loginUser, projectCode, codes);
        return returnDataList(result);
    }

    /**
     * batch export process definition by codes
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param codes process definition codes
     * @param response response
     */
    @Operation(summary = "batchExportByCodes", description = "BATCH_EXPORT_PROCESS_DEFINITION_BY_CODES_NOTES")
    @Parameters({
            @Parameter(name = "codes", description = "PROCESS_DEFINITION_CODE", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping(value = "/batch-export")
    @ResponseBody
    @AccessLogAnnotation(ignoreRequestArgs = {"loginUser", "response"})
    public void batchExportProcessDefinitionByCodes(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                    @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                    @RequestParam("codes") String codes,
                                                    HttpServletResponse response) {
        try {
            processDefinitionService.batchExportProcessDefinitionByCodes(loginUser, projectCode, codes, response);
        } catch (Exception e) {
            log.error(Status.BATCH_EXPORT_PROCESS_DEFINE_BY_IDS_ERROR.getMsg(), e);
        }
    }

    /**
     * query all process definition by project code
     *
     * @param loginUser login user
     * @param projectCode project code
     * @return process definition list
     */
    @Operation(summary = "queryAllByProjectCode", description = "QUERY_PROCESS_DEFINITION_All_BY_PROJECT_CODE_NOTES")
    @GetMapping(value = "/all")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_LIST)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAllProcessDefinitionByProjectCode(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                         @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode) {
        Map<String, Object> result =
                processDefinitionService.queryAllProcessDefinitionByProjectCode(loginUser, projectCode);
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
    @Operation(summary = "importProcessDefinition", description = "IMPORT_PROCESS_DEFINITION_NOTES")
    @Parameters({
            @Parameter(name = "file", description = "RESOURCE_FILE", required = true, schema = @Schema(implementation = MultipartFile.class))
    })
    @PostMapping(value = "/import")
    @ApiException(IMPORT_PROCESS_DEFINE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = {"loginUser", "file"})
    public Result importProcessDefinition(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                          @RequestParam("file") MultipartFile file) {
        Map<String, Object> result;
        if ("application/zip".equals(file.getContentType())) {
            result = processDefinitionService.importSqlProcessDefinition(loginUser, projectCode, file);
        } else {
            result = processDefinitionService.importProcessDefinition(loginUser, projectCode, file);
        }
        return returnDataList(result);
    }

    /**
     * create empty process definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param name process definition name
     * @param description description
     * @param globalParams globalParams
     * @param timeout timeout
     * @param scheduleJson scheduleJson
     * @return process definition code
     */
    @Operation(summary = "createEmptyProcessDefinition", description = "CREATE_EMPTY_PROCESS_NOTES")
    @Parameters({
            @Parameter(name = "name", description = "PROCESS_DEFINITION_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true, schema = @Schema(implementation = long.class, example = "123456789")),
            @Parameter(name = "description", description = "PROCESS_DEFINITION_DESC", required = false, schema = @Schema(implementation = String.class))
    })
    @PostMapping(value = "/empty")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(CREATE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createEmptyProcessDefinition(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                               @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                               @RequestParam(value = "name", required = true) String name,
                                               @RequestParam(value = "description", required = false) String description,
                                               @RequestParam(value = "globalParams", required = false, defaultValue = "[]") String globalParams,
                                               @RequestParam(value = "timeout", required = false, defaultValue = "0") int timeout,
                                               @RequestParam(value = "scheduleJson", required = false) String scheduleJson,
                                               @RequestParam(value = "executionType", defaultValue = "PARALLEL") ProcessExecutionTypeEnum executionType) {
        return returnDataList(processDefinitionService.createEmptyProcessDefinition(loginUser, projectCode, name,
                description, globalParams,
                timeout, scheduleJson, executionType));
    }

    /**
     * update process definition basic info, not including task definition, task relation and location.
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param name process definition name
     * @param code process definition code
     * @param description description
     * @param globalParams globalParams
     * @param timeout timeout
     * @param scheduleJson scheduleJson
     * @param executionType executionType
     * @param releaseState releaseState
     * @param otherParamsJson otherParamsJson handle other params
     * @return update result code
     */
    @Operation(summary = "updateBasicInfo", description = "UPDATE_PROCESS_DEFINITION_BASIC_INFO_NOTES")
    @Parameters({
            @Parameter(name = "name", description = "PROCESS_DEFINITION_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "code", description = "PROCESS_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "123456789")),
            @Parameter(name = "description", description = "PROCESS_DEFINITION_DESC", required = false, schema = @Schema(implementation = String.class)),
            @Parameter(name = "releaseState", description = "RELEASE_PROCESS_DEFINITION_NOTES", required = false, schema = @Schema(implementation = ReleaseState.class)),
            @Parameter(name = "otherParamsJson", description = "OTHER_PARAMS_JSON", required = false, schema = @Schema(implementation = String.class))
    })
    @PutMapping(value = "/{code}/basic-info")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateProcessDefinitionBasicInfo(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                   @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                   @RequestParam(value = "name", required = true) String name,
                                                   @PathVariable(value = "code", required = true) long code,
                                                   @RequestParam(value = "description", required = false) String description,
                                                   @RequestParam(value = "globalParams", required = false, defaultValue = "[]") String globalParams,
                                                   @RequestParam(value = "timeout", required = false, defaultValue = "0") int timeout,
                                                   @RequestParam(value = "scheduleJson", required = false) String scheduleJson,
                                                   @RequestParam(value = "otherParamsJson", required = false) String otherParamsJson,
                                                   @RequestParam(value = "executionType", defaultValue = "PARALLEL") ProcessExecutionTypeEnum executionType,
                                                   @RequestParam(value = "releaseState", required = false, defaultValue = "OFFLINE") ReleaseState releaseState) {
        Map<String, Object> result = processDefinitionService.updateProcessDefinitionBasicInfo(loginUser, projectCode,
                name, code, description, globalParams,
                timeout, scheduleJson, otherParamsJson, executionType);
        // If the update fails, the result will be returned directly
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataList(result);
        }

        // Judge whether to go online after editing,0 means offline, 1 means online
        if (releaseState == ReleaseState.ONLINE) {
            result = processDefinitionService.releaseWorkflowAndSchedule(loginUser, projectCode, code, releaseState);
        }
        return returnDataList(result);
    }

    /**
     * release process definition and schedule
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param code process definition code
     * @param releaseState releaseState
     * @return update result code
     */
    @Operation(summary = "releaseWorkflowAndSchedule", description = "RELEASE_WORKFLOW_SCHEDULE_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROCESS_DEFINITION_NAME", required = true, schema = @Schema(implementation = long.class)),
            @Parameter(name = "code", description = "PROCESS_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "123456789")),
            @Parameter(name = "releaseState", description = "RELEASE_PROCESS_DEFINITION_NOTES", required = true, schema = @Schema(implementation = ReleaseState.class))
    })
    @PostMapping(value = "/{code}/release-workflow")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(RELEASE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result releaseWorkflowAndSchedule(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                             @PathVariable(value = "code", required = true) long code,
                                             @RequestParam(value = "releaseState", required = true, defaultValue = "OFFLINE") ReleaseState releaseState) {
        return returnDataList(
                processDefinitionService.releaseWorkflowAndSchedule(loginUser, projectCode, code, releaseState));
    }

    /**
     * query process definition global variables and local variables
     *
     * @param loginUser login user
     * @param code process definition code
     * @return variables data
     */
    @Operation(summary = "viewVariables", description = "QUERY_PROCESS_DEFINITION_GLOBAL_VARIABLES_AND_LOCAL_VARIABLES_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "PROCESS_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "100"))
    })
    @GetMapping(value = "/{code}/view-variables")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_ALL_VARIABLES_ERROR)
    @AccessLogAnnotation
    public Result viewVariables(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                @PathVariable("code") Long code) {
        Map<String, Object> result = processDefinitionService.viewVariables(loginUser, projectCode, code);
        return returnDataList(result);
    }

}
