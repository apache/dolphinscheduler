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

import static org.apache.dolphinscheduler.api.enums.Status.BATCH_COPY_WORKFLOW_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.BATCH_DELETE_WORKFLOW_DEFINE_BY_CODES_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.BATCH_MOVE_WORKFLOW_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.CREATE_WORKFLOW_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_WORKFLOW_DEFINE_BY_CODE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_WORKFLOW_DEFINITION_VERSION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.ENCAPSULATION_TREEVIEW_STRUCTURE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GET_TASKS_LIST_BY_WORKFLOW_DEFINITION_CODE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.IMPORT_WORKFLOW_DEFINE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_DETAIL_OF_WORKFLOW_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_WORKFLOW_DEFINITION_ALL_VARIABLES_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_WORKFLOW_DEFINITION_LIST;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_WORKFLOW_DEFINITION_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_WORKFLOW_DEFINITION_VERSIONS_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.RELEASE_WORKFLOW_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.SWITCH_WORKFLOW_DEFINITION_VERSION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_WORKFLOW_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_WORKFLOW_DEFINITION_NAME_UNIQUE_ERROR;

import org.apache.dolphinscheduler.api.audit.OperatorLog;
import org.apache.dolphinscheduler.api.audit.enums.AuditType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.WorkflowDefinitionService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionTypeEnum;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
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
 * workflow definition controller
 */
@Tag(name = "WORKFLOW_DEFINITION_TAG")
@RestController
@RequestMapping("projects/{projectCode}/workflow-definition")
@Slf4j
public class WorkflowDefinitionController extends BaseController {

    @Autowired
    private WorkflowDefinitionService workflowDefinitionService;

    /**
     * create workflow definition
     *
     * @param loginUser          login user
     * @param projectCode        project code
     * @param name               workflow definition name
     * @param description        description
     * @param globalParams       globalParams
     * @param locations          locations for nodes
     * @param timeout            timeout
     * @param taskRelationJson   relation json for nodes
     * @param taskDefinitionJson taskDefinitionJson
     * @param otherParamsJson    otherParamsJson handle other params
     * @return create result code
     */
    @Operation(summary = "createWorkflowDefinition", description = "CREATE_WORKFLOW_DEFINITION_NOTES")
    @Parameters({
            @Parameter(name = "name", description = "WORKFLOW_DEFINITION_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "locations", description = "WORKFLOW_DEFINITION_LOCATIONS", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "description", description = "WORKFLOW_DEFINITION_DESC", required = false, schema = @Schema(implementation = String.class)),
            @Parameter(name = "otherParamsJson", description = "OTHER_PARAMS_JSON", required = false, schema = @Schema(implementation = String.class))
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_WORKFLOW_DEFINITION_ERROR)
    @OperatorLog(auditType = AuditType.WORKFLOW_CREATE)
    public Result createWorkflowDefinition(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                           @RequestParam(value = "name", required = true) String name,
                                           @RequestParam(value = "description", required = false) String description,
                                           @RequestParam(value = "globalParams", required = false, defaultValue = "[]") String globalParams,
                                           @RequestParam(value = "locations", required = false) String locations,
                                           @RequestParam(value = "timeout", required = false, defaultValue = "0") int timeout,
                                           @RequestParam(value = "taskRelationJson", required = true) String taskRelationJson,
                                           @RequestParam(value = "taskDefinitionJson", required = true) String taskDefinitionJson,
                                           @RequestParam(value = "otherParamsJson", required = false) String otherParamsJson,
                                           @RequestParam(value = "executionType", defaultValue = "PARALLEL") WorkflowExecutionTypeEnum executionType) {
        Map<String, Object> result = workflowDefinitionService.createWorkflowDefinition(loginUser, projectCode, name,
                description, globalParams,
                locations, timeout, taskRelationJson, taskDefinitionJson, otherParamsJson, executionType);
        return returnDataList(result);
    }

    /**
     * copy workflow definition
     *
     * @param loginUser         login user
     * @param projectCode       project code
     * @param codes             workflow definition codes
     * @param targetProjectCode target project code
     * @return copy result code
     */
    @Operation(summary = "batchCopyByCodes", description = "COPY_WORKFLOW_DEFINITION_NOTES")
    @Parameters({
            @Parameter(name = "codes", description = "WORKFLOW_DEFINITION_CODE_LIST", required = true, schema = @Schema(implementation = String.class, example = "3,4")),
            @Parameter(name = "targetProjectCode", description = "TARGET_PROJECT_CODE", required = true, schema = @Schema(implementation = long.class, example = "123"))
    })
    @PostMapping(value = "/batch-copy")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_COPY_WORKFLOW_DEFINITION_ERROR)
    @OperatorLog(auditType = AuditType.WORKFLOW_COPY)
    public Result copyWorkflowDefinition(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                         @RequestParam(value = "codes", required = true) String codes,
                                         @RequestParam(value = "targetProjectCode", required = true) long targetProjectCode) {
        return returnDataList(
                workflowDefinitionService.batchCopyWorkflowDefinition(loginUser, projectCode, codes,
                        targetProjectCode));
    }

    /**
     * move workflow definition
     *
     * @param loginUser         login user
     * @param projectCode       project code
     * @param codes             workflow definition codes
     * @param targetProjectCode target project code
     * @return move result code
     */
    @Operation(summary = "batchMoveByCodes", description = "MOVE_WORKFLOW_DEFINITION_NOTES")
    @Parameters({
            @Parameter(name = "codes", description = "WORKFLOW_DEFINITION_CODE_LIST", required = true, schema = @Schema(implementation = String.class, example = "3,4")),
            @Parameter(name = "targetProjectCode", description = "TARGET_PROJECT_CODE", required = true, schema = @Schema(implementation = long.class, example = "123"))
    })
    @PostMapping(value = "/batch-move")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_MOVE_WORKFLOW_DEFINITION_ERROR)
    public Result moveWorkflowDefinition(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                         @RequestParam(value = "codes", required = true) String codes,
                                         @RequestParam(value = "targetProjectCode", required = true) long targetProjectCode) {
        return returnDataList(
                workflowDefinitionService.batchMoveWorkflowDefinition(loginUser, projectCode, codes,
                        targetProjectCode));
    }

    /**
     * verify workflow definition name unique
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param name        name
     * @return true if workflow definition name not exists, otherwise false
     */
    @Operation(summary = "verify-name", description = "VERIFY_WORKFLOW_DEFINITION_NAME_NOTES")
    @Parameters({
            @Parameter(name = "name", description = "WORKFLOW_DEFINITION_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "code", description = "WORKFLOW_DEFINITION_CODE", required = false, schema = @Schema(implementation = Long.class)),
    })
    @GetMapping(value = "/verify-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_WORKFLOW_DEFINITION_NAME_UNIQUE_ERROR)
    public Result verifyWorkflowDefinitionName(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                               @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                               @RequestParam(value = "name", required = true) String name,
                                               @RequestParam(value = "workflowDefinitionCode", required = false, defaultValue = "0") long workflowDefinitionCode) {
        Map<String, Object> result =
                workflowDefinitionService.verifyWorkflowDefinitionName(loginUser, projectCode, name,
                        workflowDefinitionCode);
        return returnDataList(result);
    }

    /**
     * update workflow definition, with whole workflow definition object including task definition, task relation and location.
     *
     * @param loginUser          login user
     * @param projectCode        project code
     * @param name               workflow definition name
     * @param code               workflow definition code
     * @param description        description
     * @param globalParams       globalParams
     * @param locations          locations for nodes
     * @param timeout            timeout
     * @param taskRelationJson   relation json for nodes
     * @param taskDefinitionJson taskDefinitionJson
     * @return update result code
     */
    @Operation(summary = "update", description = "UPDATE_WORKFLOW_DEFINITION_NOTES")
    @Parameters({
            @Parameter(name = "name", description = "WORKFLOW_DEFINITION_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "code", description = "WORKFLOW_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "123456789")),
            @Parameter(name = "locations", description = "WORKFLOW_DEFINITION_LOCATIONS", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "description", description = "WORKFLOW_DEFINITION_DESC", required = false, schema = @Schema(implementation = String.class)),
            @Parameter(name = "releaseState", description = "RELEASE_WORKFLOW_DEFINITION_NOTES", required = false, schema = @Schema(implementation = ReleaseState.class))
    })
    @PutMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_WORKFLOW_DEFINITION_ERROR)
    @OperatorLog(auditType = AuditType.WORKFLOW_UPDATE)
    public Result updateWorkflowDefinition(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                           @RequestParam(value = "name", required = true) String name,
                                           @PathVariable(value = "code", required = true) long code,
                                           @RequestParam(value = "description", required = false) String description,
                                           @RequestParam(value = "globalParams", required = false, defaultValue = "[]") String globalParams,
                                           @RequestParam(value = "locations", required = false) String locations,
                                           @RequestParam(value = "timeout", required = false, defaultValue = "0") int timeout,
                                           @RequestParam(value = "taskRelationJson", required = true) String taskRelationJson,
                                           @RequestParam(value = "taskDefinitionJson", required = true) String taskDefinitionJson,
                                           @RequestParam(value = "executionType", defaultValue = "PARALLEL") WorkflowExecutionTypeEnum executionType,
                                           @RequestParam(value = "releaseState", required = false, defaultValue = "OFFLINE") ReleaseState releaseState) {

        Map<String, Object> result = workflowDefinitionService.updateWorkflowDefinition(loginUser, projectCode, name,
                code, description, globalParams,
                locations, timeout, taskRelationJson, taskDefinitionJson, executionType);
        // If the update fails, the result will be returned directly
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataList(result);
        }

        // Judge whether to go online after editing,0 means offline, 1 means online
        if (releaseState == ReleaseState.ONLINE) {
            workflowDefinitionService.onlineWorkflowDefinition(loginUser, projectCode, code);
        }
        return returnDataList(result);
    }

    /**
     * query workflow definition version paging list info
     *
     * @param loginUser   login user info
     * @param projectCode project code
     * @param pageNo      the workflow definition version list current page number
     * @param pageSize    the workflow definition version list page size
     * @param code        the workflow definition code
     * @return the workflow definition version list
     */
    @Operation(summary = "queryVersions", description = "QUERY_WORKFLOW_DEFINITION_VERSIONS_NOTES")
    @Parameters({
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "10")),
            @Parameter(name = "code", description = "WORKFLOW_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "1"))
    })
    @GetMapping(value = "/{code}/versions")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_WORKFLOW_DEFINITION_VERSIONS_ERROR)
    public Result queryWorkflowDefinitionVersions(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                  @RequestParam(value = "pageNo") int pageNo,
                                                  @RequestParam(value = "pageSize") int pageSize,
                                                  @PathVariable(value = "code") long code) {

        checkPageParams(pageNo, pageSize);
        return workflowDefinitionService.queryWorkflowDefinitionVersions(loginUser, projectCode, pageNo, pageSize,
                code);
    }

    /**
     * switch certain workflow definition version
     *
     * @param loginUser   login user info
     * @param projectCode project code
     * @param code        the workflow definition code
     * @param version     the version user want to switch
     * @return switch version result code
     */
    @Operation(summary = "switchVersion", description = "SWITCH_WORKFLOW_DEFINITION_VERSION_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "WORKFLOW_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "1")),
            @Parameter(name = "version", description = "VERSION", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/{code}/versions/{version}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(SWITCH_WORKFLOW_DEFINITION_VERSION_ERROR)
    @OperatorLog(auditType = AuditType.WORKFLOW_SWITCH_VERSION)
    public Result switchWorkflowDefinitionVersion(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                  @PathVariable(value = "code") long code,
                                                  @PathVariable(value = "version") int version) {
        Map<String, Object> result =
                workflowDefinitionService.switchWorkflowDefinitionVersion(loginUser, projectCode, code, version);
        return returnDataList(result);
    }

    /**
     * delete the certain workflow definition version by version and workflow definition code
     *
     * @param loginUser   login user info
     * @param projectCode project code
     * @param code        the workflow definition code
     * @param version     the workflow definition version user want to delete
     * @return delete version result code
     */
    @Operation(summary = "deleteVersion", description = "DELETE_WORKFLOW_DEFINITION_VERSION_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "WORKFLOW_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "1")),
            @Parameter(name = "version", description = "VERSION", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @DeleteMapping(value = "/{code}/versions/{version}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_WORKFLOW_DEFINITION_VERSION_ERROR)
    @OperatorLog(auditType = AuditType.WORKFLOW_DELETE_VERSION)
    public Result<Void> deleteWorkflowDefinitionVersion(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                        @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                        @PathVariable(value = "code") long code,
                                                        @PathVariable(value = "version") int version) {
        workflowDefinitionService.deleteWorkflowDefinitionVersion(loginUser, projectCode, code, version);
        return Result.success();
    }

    @Operation(summary = "release", description = "RELEASE_WORKFLOW_DEFINITION_NOTES")
    @Parameters({
            @Parameter(name = "name", description = "WORKFLOW_DEFINITION_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "code", description = "WORKFLOW_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "123456789")),
            @Parameter(name = "releaseState", description = "WORKFLOW_DEFINITION_RELEASE", required = true, schema = @Schema(implementation = ReleaseState.class)),
    })
    @PostMapping(value = "/{code}/release")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(RELEASE_WORKFLOW_DEFINITION_ERROR)
    @OperatorLog(auditType = AuditType.WORKFLOW_RELEASE)
    public Result<Boolean> releaseWorkflowDefinition(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                     @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                     @PathVariable(value = "code", required = true) long workflowDefinitionCode,
                                                     @RequestParam(value = "releaseState", required = true) ReleaseState releaseState) {
        switch (releaseState) {
            case ONLINE:
                workflowDefinitionService.onlineWorkflowDefinition(loginUser, projectCode, workflowDefinitionCode);
                break;
            case OFFLINE:
                workflowDefinitionService.offlineWorkflowDefinition(loginUser, projectCode, workflowDefinitionCode);
                break;
            default:
                throw new IllegalArgumentException(
                        "The releaseState " + releaseState + " is illegal, please check it.");
        }
        return Result.success(true);
    }

    /**
     * query detail of workflow definition by code
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param code        workflow definition code
     * @return workflow definition detail
     */
    @Operation(summary = "queryWorkflowDefinitionByCode", description = "QUERY_WORKFLOW_DEFINITION_BY_CODE_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "WORKFLOW_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "123456789"))
    })
    @GetMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DETAIL_OF_WORKFLOW_DEFINITION_ERROR)
    public Result queryWorkflowDefinitionByCode(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                @PathVariable(value = "code", required = true) long code) {
        Map<String, Object> result =
                workflowDefinitionService.queryWorkflowDefinitionByCode(loginUser, projectCode, code);
        return returnDataList(result);
    }

    /**
     * query detail of workflow definition by name
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param name        workflow definition name
     * @return workflow definition detail
     */
    @Operation(summary = "queryWorkflowDefinitionByName", description = "QUERY_WORKFLOW_DEFINITION_BY_NAME_NOTES")
    @Parameters({
            @Parameter(name = "name", description = "WORKFLOW_DEFINITION_NAME", required = true, schema = @Schema(implementation = String.class))
    })
    @GetMapping(value = "/query-by-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DETAIL_OF_WORKFLOW_DEFINITION_ERROR)
    public Result<WorkflowDefinition> queryWorkflowDefinitionByName(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                    @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                    @RequestParam("name") String name) {
        Map<String, Object> result =
                workflowDefinitionService.queryWorkflowDefinitionByName(loginUser, projectCode, name);
        return returnDataList(result);
    }

    /**
     * query workflow definition list
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return workflow definition list
     */
    @Operation(summary = "queryList", description = "QUERY_WORKFLOW_DEFINITION_LIST_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_WORKFLOW_DEFINITION_LIST)
    public Result queryWorkflowDefinitionList(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode) {
        Map<String, Object> result = workflowDefinitionService.queryWorkflowDefinitionList(loginUser, projectCode);
        return returnDataList(result);
    }

    /**
     * query workflow definition simple list
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return workflow definition list
     */
    @Operation(summary = "querySimpleList", description = "QUERY_WORKFLOW_DEFINITION_SIMPLE_LIST_NOTES")
    @GetMapping(value = "/simple-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_WORKFLOW_DEFINITION_LIST)
    public Result queryWorkflowDefinitionSimpleList(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                    @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode) {
        Map<String, Object> result =
                workflowDefinitionService.queryWorkflowDefinitionSimpleList(loginUser, projectCode);
        return returnDataList(result);
    }

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
    @Operation(summary = "queryListPaging", description = "QUERY_WORKFLOW_DEFINITION_LIST_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "searchVal", description = "SEARCH_VAL", required = false, schema = @Schema(implementation = String.class)),
            @Parameter(name = "userId", description = "USER_ID", required = false, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "10")),
            @Parameter(name = "otherParamsJson", description = "OTHER_PARAMS_JSON", required = false, schema = @Schema(implementation = String.class))
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_WORKFLOW_DEFINITION_LIST_PAGING_ERROR)
    public Result<PageInfo<WorkflowDefinition>> queryWorkflowDefinitionListPaging(
                                                                                  @Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                                  @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                                  @RequestParam(value = "searchVal", required = false) String searchVal,
                                                                                  @RequestParam(value = "otherParamsJson", required = false) String otherParamsJson,
                                                                                  @RequestParam(value = "userId", required = false, defaultValue = "0") Integer userId,
                                                                                  @RequestParam("pageNo") Integer pageNo,
                                                                                  @RequestParam("pageSize") Integer pageSize) {

        checkPageParams(pageNo, pageSize);
        searchVal = ParameterUtils.handleEscapes(searchVal);

        PageInfo<WorkflowDefinition> pageInfo = workflowDefinitionService.queryWorkflowDefinitionListPaging(
                loginUser, projectCode, searchVal, otherParamsJson, userId, pageNo, pageSize);
        return Result.success(pageInfo);

    }

    /**
     * encapsulation tree view structure
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param code        workflow definition code
     * @param limit       limit
     * @return tree view json data
     */
    @Operation(summary = "viewTree", description = "VIEW_TREE_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "WORKFLOW_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "100")),
            @Parameter(name = "limit", description = "LIMIT", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/{code}/view-tree")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(ENCAPSULATION_TREEVIEW_STRUCTURE_ERROR)
    public Result viewTree(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                           @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                           @PathVariable("code") long code,
                           @RequestParam("limit") Integer limit) {
        Map<String, Object> result = workflowDefinitionService.viewTree(loginUser, projectCode, code, limit);
        return returnDataList(result);
    }

    /**
     * get tasks list by workflow definition code
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param code        workflow definition code
     * @return task list
     */
    @Operation(summary = "getTasksByDefinitionCode", description = "GET_TASK_LIST_BY_DEFINITION_CODE_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "WORKFLOW_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "100"))
    })
    @GetMapping(value = "/{code}/tasks")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_TASKS_LIST_BY_WORKFLOW_DEFINITION_CODE_ERROR)
    public Result getNodeListByDefinitionCode(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                              @PathVariable("code") long code) {
        Map<String, Object> result =
                workflowDefinitionService.getTaskNodeListByDefinitionCode(loginUser, projectCode, code);
        return returnDataList(result);
    }

    /**
     * get tasks list map by workflow definition multiple code
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param codes       workflow definition codes
     * @return node list data
     */
    @Operation(summary = "getTaskListByDefinitionCodes", description = "GET_TASK_LIST_BY_DEFINITION_CODE_NOTES")
    @Parameters({
            @Parameter(name = "codes", description = "WORKFLOW_DEFINITION_CODE_LIST", required = true, schema = @Schema(implementation = String.class, example = "100,200,300"))
    })
    @GetMapping(value = "/batch-query-tasks")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_TASKS_LIST_BY_WORKFLOW_DEFINITION_CODE_ERROR)
    public Result getNodeListMapByDefinitionCodes(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                  @RequestParam("codes") String codes) {
        Map<String, Object> result =
                workflowDefinitionService.getNodeListMapByDefinitionCodes(loginUser, projectCode, codes);
        return returnDataList(result);
    }

    /**
     * get workflow definition list map by project code
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return workflow definition list data
     */
    @Operation(summary = "getWorkflowListByProjectCode", description = "GET_WORKFLOW_LIST_BY_WORKFLOW_CODE_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true, schema = @Schema(implementation = long.class, example = "100"))
    })
    @GetMapping(value = "/query-workflow-definition-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_TASKS_LIST_BY_WORKFLOW_DEFINITION_CODE_ERROR)
    public Result getWorkflowListByProjectCode(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                               @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode) {
        Map<String, Object> result = workflowDefinitionService.queryWorkflowDefinitionListByProjectCode(projectCode);
        return returnDataList(result);
    }

    /**
     * get task definition list by workflow definition code
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return workflow definition list data
     */
    @Operation(summary = "getTaskListByWorkflowDefinitionCode", description = "GET_TASK_LIST_BY_WORKFLOW_CODE_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true, schema = @Schema(implementation = long.class, example = "100")),
            @Parameter(name = "workflowDefinitionCode", description = "WORKFLOW_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "100")),
    })
    @GetMapping(value = "/query-task-definition-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_TASKS_LIST_BY_WORKFLOW_DEFINITION_CODE_ERROR)
    public Result getTaskListByWorkflowDefinitionCode(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                      @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                      @RequestParam(value = "workflowDefinitionCode") Long workflowDefinitionCode) {
        Map<String, Object> result = workflowDefinitionService
                .queryTaskDefinitionListByWorkflowDefinitionCode(projectCode, workflowDefinitionCode);
        return returnDataList(result);
    }

    @Operation(summary = "deleteByWorkflowDefinitionCode", description = "DELETE_WORKFLOW_DEFINITION_BY_ID_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "WORKFLOW_DEFINITION_CODE", schema = @Schema(implementation = int.class, example = "100"))
    })
    @DeleteMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_WORKFLOW_DEFINE_BY_CODE_ERROR)
    @OperatorLog(auditType = AuditType.WORKFLOW_DELETE)
    public Result deleteWorkflowDefinitionByCode(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                 @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                 @PathVariable("code") long code) {
        workflowDefinitionService.deleteWorkflowDefinitionByCode(loginUser, code);
        return new Result(Status.SUCCESS);
    }

    /**
     * batch delete workflow definition by codes
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param codes       workflow definition code list
     * @return delete result code
     */
    @Operation(summary = "batchDeleteByCodes", description = "BATCH_DELETE_WORKFLOW_DEFINITION_BY_IDS_NOTES")
    @Parameters({
            @Parameter(name = "codes", description = "WORKFLOW_DEFINITION_CODE", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping(value = "/batch-delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_DELETE_WORKFLOW_DEFINE_BY_CODES_ERROR)
    @OperatorLog(auditType = AuditType.WORKFLOW_BATCH_DELETE)
    public Result batchDeleteWorkflowDefinitionByCodes(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                       @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                       @RequestParam("codes") String codes) {

        Map<String, Object> result =
                workflowDefinitionService.batchDeleteWorkflowDefinitionByCodes(loginUser, projectCode, codes);
        return returnDataList(result);
    }

    /**
     * batch export workflow definition by codes
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param codes       workflow definition codes
     * @param response    response
     */
    @Operation(summary = "batchExportByCodes", description = "BATCH_EXPORT_WORKFLOW_DEFINITION_BY_CODES_NOTES")
    @Parameters({
            @Parameter(name = "codes", description = "WORKFLOW_DEFINITION_CODE", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping(value = "/batch-export")
    @ResponseBody
    @OperatorLog(auditType = AuditType.WORKFLOW_EXPORT)
    public void batchExportWorkflowDefinitionByCodes(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                     @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                     @RequestParam("codes") String codes,
                                                     HttpServletResponse response) {
        try {
            workflowDefinitionService.batchExportWorkflowDefinitionByCodes(loginUser, projectCode, codes, response);
        } catch (Exception e) {
            log.error(Status.BATCH_EXPORT_WORKFLOW_DEFINE_BY_IDS_ERROR.getMsg(), e);
        }
    }

    /**
     * query all workflow definition by project code
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return workflow definition list
     */
    @Operation(summary = "queryAllByProjectCode", description = "QUERY_WORKFLOW_DEFINITION_All_BY_PROJECT_CODE_NOTES")
    @GetMapping(value = "/all")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_WORKFLOW_DEFINITION_LIST)
    public Result queryAllWorkflowDefinitionByProjectCode(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                          @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode) {
        Map<String, Object> result =
                workflowDefinitionService.queryAllWorkflowDefinitionByProjectCode(loginUser, projectCode);
        return returnDataList(result);
    }

    /**
     * import workflow definition
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param file        resource file
     * @return import result code
     */
    @Operation(summary = "importWorkflowDefinition", description = "IMPORT_WORKFLOW_DEFINITION_NOTES")
    @Parameters({
            @Parameter(name = "file", description = "RESOURCE_FILE", required = true, schema = @Schema(implementation = MultipartFile.class))
    })
    @PostMapping(value = "/import")
    @ApiException(IMPORT_WORKFLOW_DEFINE_ERROR)
    @OperatorLog(auditType = AuditType.WORKFLOW_IMPORT)
    public Result importWorkflowDefinition(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                           @RequestParam("file") MultipartFile file) {
        Map<String, Object> result;
        if ("application/zip".equals(file.getContentType())) {
            result = workflowDefinitionService.importSqlWorkflowDefinition(loginUser, projectCode, file);
        } else {
            result = workflowDefinitionService.importWorkflowDefinition(loginUser, projectCode, file);
        }
        return returnDataList(result);
    }

    /**
     * query workflow definition global variables and local variables
     *
     * @param loginUser login user
     * @param code      workflow definition code
     * @return variables data
     */
    @Operation(summary = "viewVariables", description = "QUERY_WORKFLOW_DEFINITION_GLOBAL_VARIABLES_AND_LOCAL_VARIABLES_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "WORKFLOW_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "100"))
    })
    @GetMapping(value = "/{code}/view-variables")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_WORKFLOW_DEFINITION_ALL_VARIABLES_ERROR)
    public Result viewVariables(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                @PathVariable("code") Long code) {
        Map<String, Object> result = workflowDefinitionService.viewVariables(loginUser, projectCode, code);
        return returnDataList(result);
    }

}
