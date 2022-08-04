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
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROCESS_DEFINITION_LIST;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROCESS_DEFINITION_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROCESS_DEFINITION_VERSIONS_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.RELEASE_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.SWITCH_PROCESS_DEFINITION_VERSION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.dto.workflow.CreateEmptyWorkflowDefinitionRequest;
import org.apache.dolphinscheduler.api.dto.workflow.CreateEmptyWorkflowDefinitionResponse;
import org.apache.dolphinscheduler.api.dto.workflow.CreateWorkflowDefinitionRequest;
import org.apache.dolphinscheduler.api.dto.workflow.CreateWorkflowDefinitionResponse;
import org.apache.dolphinscheduler.api.dto.workflow.GetDependentSimplifyDefinitionListResponse;
import org.apache.dolphinscheduler.api.dto.workflow.GetTaskListMapResponse;
import org.apache.dolphinscheduler.api.dto.workflow.GetTaskListResponse;
import org.apache.dolphinscheduler.api.dto.workflow.ProjectWorkflowRequest;
import org.apache.dolphinscheduler.api.dto.workflow.QuerySimpleWorkflowListResponse;
import org.apache.dolphinscheduler.api.dto.workflow.QueryWorkflowListResponse;
import org.apache.dolphinscheduler.api.dto.workflow.QueryWorkflowRequest;
import org.apache.dolphinscheduler.api.dto.workflow.QueryWorkflowResponse;
import org.apache.dolphinscheduler.api.dto.workflow.QueryWorkflowVersionsResponse;
import org.apache.dolphinscheduler.api.dto.workflow.TreeViewResponse;
import org.apache.dolphinscheduler.api.dto.workflow.UpdateWorkflowBasicInfoRequest;
import org.apache.dolphinscheduler.api.dto.workflow.UpdateWorkflowDefinitionRequest;
import org.apache.dolphinscheduler.api.dto.workflow.UpdateWorkflowDefinitionResponse;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowDetailResponse;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

/**
 * workflow definition controller
 */
@Api(tags = "WORKFLOW_DEFINITION_TAG")
@RestController
@RequestMapping("v2/projects/{projectCode}/workflow-definition")
public class WorkflowDefinitionV2Controller extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowDefinitionV2Controller.class);

    @Autowired
    private ProcessDefinitionService workflowDefinitionService;

    /**
     * create workflow definition
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param request     workflow definition request
     * @return create workflow response
     */
    @ApiOperation(value = "createWorkflowDefinition", notes = "CREATE_WORKFLOW_DEFINITION_NOTES")
    @PostMapping(consumes = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public CreateWorkflowDefinitionResponse createWorkflowDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                     @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                     @RequestBody CreateWorkflowDefinitionRequest request) {
        Result result = workflowDefinitionService.createProcessDefinition(loginUser, projectCode, request.getName(),
            request.getDescription(), request.getGlobalParams(), request.getLocations(), request.getTimeout(), request.getTenantCode(),
            request.getTaskRelationJson(), request.getTaskDefinitionJson(), request.getOtherParamsJson(), request.getExecutionType());
        return new CreateWorkflowDefinitionResponse(result);
    }

    /**
     * copy workflow definition
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param request     project workflow request
     * @return copy result
     */
    @ApiOperation(value = "batchCopyByCodes", notes = "COPY_WORKFLOW_DEFINITION_NOTES")
    @PostMapping(consumes = {"application/json"}, value = "/batch-copy")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_COPY_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public CreateWorkflowDefinitionResponse copyWorkflowDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                   @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                   @RequestBody ProjectWorkflowRequest request) {
        Result result = workflowDefinitionService.batchCopyProcessDefinition(loginUser, projectCode, request.getCodes(), request.getTargetProjectCode());
        return new CreateWorkflowDefinitionResponse(result);
    }

    /**
     * move workflow definition
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param request     project workflow request
     * @return move result
     */
    @ApiOperation(value = "batchMoveByCodes", notes = "MOVE_WORKFLOW_DEFINITION_NOTES")
    @PostMapping(consumes = {"application/json"}, value = "/batch-move")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_MOVE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public CreateWorkflowDefinitionResponse moveWorkflowDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                   @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                   @RequestBody ProjectWorkflowRequest request) {
        Result result = workflowDefinitionService.batchMoveProcessDefinition(loginUser, projectCode, request.getCodes(), request.getTargetProjectCode());
        return new CreateWorkflowDefinitionResponse(result);
    }

    /**
     * verify workflow definition name unique
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param name        name
     * @return true if workflow definition name not exists, otherwise false
     */
    @ApiOperation(value = "verify-name", notes = "VERIFY_WORKFLOW_DEFINITION_NAME_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "name", value = "WORKFLOW_DEFINITION_NAME", required = true, type = "String")
    })
    @GetMapping(consumes = {"application/json"}, value = "/verify-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result verifyWorkflowDefinitionName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                               @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                               @RequestParam(value = "name", required = true) String name) {
        return workflowDefinitionService.verifyProcessDefinitionName(loginUser, projectCode, name);
    }

    /**
     * update workflow definition, with whole workflow definition object including task definition, task relation and location.
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param code        workflow definition code
     * @param request     update workflow request
     * @return
     */
    @ApiOperation(value = "update", notes = "UPDATE_WORKFLOW_DEFINITION_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "code", value = "WORKFLOW_DEFINITION_CODE", required = true, dataType = "Long", example = "123456789")
    })
    @PutMapping(consumes = {"application/json"}, value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public UpdateWorkflowDefinitionResponse updateWorkflowDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                     @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                     @PathVariable(value = "code", required = true) long code,
                                                                     @RequestBody UpdateWorkflowDefinitionRequest request) {

        Result result = workflowDefinitionService.updateProcessDefinition(loginUser, projectCode, request.getName(), code, request.getDescription(), request.getGlobalParams(),
            request.getLocations(), request.getTimeout(), request.getTenantCode(), request.getTaskRelationJson(), request.getTaskDefinitionJson(), request.getOtherParamsJson(),
            request.getExecutionType());
        //  If the update fails, the result will be returned directly
        if (Status.SUCCESS.getCode() != result.getCode()) {
            return new UpdateWorkflowDefinitionResponse(result);
        }

        //  Judge whether to go online after editing,0 means offline, 1 means online
        if (request.getReleaseState() == ReleaseState.ONLINE) {
            result = workflowDefinitionService.releaseProcessDefinition(loginUser, projectCode, code, request.getReleaseState());
        }
        return new UpdateWorkflowDefinitionResponse(result);
    }

    /**
     * create empty workflow definition
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param request     workflow definition request
     * @return create empty workflow response
     */
    @ApiOperation(value = "createEmptyWorkflowDefinition", notes = "CREATE_EMPTY_WORKFLOW_NOTES")
    @PostMapping(consumes = {"application/json"}, value = "/empty")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(CREATE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public CreateEmptyWorkflowDefinitionResponse createEmptyWorkflowDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                               @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                               @RequestBody CreateEmptyWorkflowDefinitionRequest request) {
        Result result = workflowDefinitionService.createEmptyProcessDefinition(loginUser, projectCode, request.getName(),
            request.getDescription(), request.getGlobalParams(), request.getTimeout(), request.getTenantCode(),
            request.getScheduleJson(), request.getExecutionType());
        return new CreateEmptyWorkflowDefinitionResponse(result);
    }

    /**
     * query workflow definition version paging list info
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param request     query request
     * @param code        workflow definition code
     * @return the workflow definition version list
     */
    @ApiOperation(value = "queryVersions", notes = "QUERY_WORKFLOW_DEFINITION_VERSIONS_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "code", value = "WORKFLOW_DEFINITION_CODE", required = true, dataType = "Long", example = "1")
    })
    @GetMapping(consumes = {"application/json"}, value = "/{code}/versions")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_VERSIONS_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public QueryWorkflowVersionsResponse queryWorkflowDefinitionVersions(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                         @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                         @PathVariable(value = "code") long code,
                                                                         @RequestBody QueryWorkflowRequest request) {
        Result result = checkPageParams(request.getPageNo(), request.getPageSize());
        if (!result.checkResult()) {
            return new QueryWorkflowVersionsResponse(result);
        }
        result = workflowDefinitionService.queryProcessDefinitionVersions(loginUser, projectCode, request.getPageNo(), request.getPageSize(), code);
        return new QueryWorkflowVersionsResponse(result);
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
    @ApiOperation(value = "switchVersion", notes = "SWITCH_WORKFLOW_DEFINITION_VERSION_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "code", value = "WORKFLOW_DEFINITION_CODE", required = true, dataType = "Long", example = "1"),
        @ApiImplicitParam(name = "version", value = "VERSION", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(consumes = {"application/json"}, value = "/{code}/versions/{version}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(SWITCH_PROCESS_DEFINITION_VERSION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result switchWorkflowDefinitionVersion(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                  @PathVariable(value = "code") long code,
                                                  @PathVariable(value = "version") int version) {
        return workflowDefinitionService.switchProcessDefinitionVersion(loginUser, projectCode, code, version);
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
    @ApiOperation(value = "deleteVersion", notes = "DELETE_WORKFLOW_DEFINITION_VERSION_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "code", value = "WORKFLOW_DEFINITION_CODE", required = true, dataType = "Long", example = "1"),
        @ApiImplicitParam(name = "version", value = "VERSION", required = true, dataType = "Int", example = "100")
    })
    @DeleteMapping(consumes = {"application/json"}, value = "/{code}/versions/{version}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_PROCESS_DEFINITION_VERSION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteWorkflowDefinitionVersion(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                  @PathVariable(value = "code") long code,
                                                  @PathVariable(value = "version") int version) {
        return workflowDefinitionService.deleteProcessDefinitionVersion(loginUser, projectCode, code, version);
    }

    /**
     * release workflow definition
     *
     * @param loginUser    login user
     * @param projectCode  project code
     * @param code         workflow definition code
     * @param releaseState release state
     * @return release result code
     */
    @ApiOperation(value = "release", notes = "RELEASE_WORKFLOW_DEFINITION_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "name", value = "WORKFLOW_DEFINITION_NAME", required = true, type = "String"),
        @ApiImplicitParam(name = "code", value = "WORKFLOW_DEFINITION_CODE", required = true, dataType = "Long", example = "123456789"),
        @ApiImplicitParam(name = "releaseState", value = "WORKFLOW_DEFINITION_RELEASE", required = true, dataType = "ReleaseState"),
    })
    @PostMapping(consumes = {"application/json"}, value = "/{code}/release")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(RELEASE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result releaseWorkflowDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                            @PathVariable(value = "code", required = true) long code,
                                            @RequestParam(value = "releaseState", required = true) ReleaseState releaseState) {
        return workflowDefinitionService.releaseProcessDefinition(loginUser, projectCode, code, releaseState);
    }

    /**
     * query detail of workflow definition by code
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param code        workflow definition code
     * @return workflow definition detail
     */
    @ApiOperation(value = "queryWorkflowDefinitionByCode", notes = "QUERY_WORKFLOW_DEFINITION_BY_CODE_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "code", value = "WORKFLOW_DEFINITION_CODE", required = true, dataType = "Long", example = "123456789")
    })
    @GetMapping(consumes = {"application/json"}, value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DETAIL_OF_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public WorkflowDetailResponse queryWorkflowDefinitionByCode(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                @PathVariable(value = "code", required = true) long code) {
        Result result = workflowDefinitionService.queryProcessDefinitionByCode(loginUser, projectCode, code);
        return new WorkflowDetailResponse(result);
    }

    /**
     * query detail of workflow definition by name
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param name        workflow definition name
     * @return workflow definition detail
     */
    @ApiOperation(value = "queryWorkflowDefinitionByName", notes = "QUERY_WORKFLOW_DEFINITION_BY_NAME_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "name", value = "WORKFLOW_DEFINITION_NAME", required = true, dataType = "String")
    })
    @GetMapping(consumes = {"application/json"}, value = "/query-by-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DETAIL_OF_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public WorkflowDetailResponse queryWorkflowDefinitionByName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                @RequestParam("name") String name) {
        Result result = workflowDefinitionService.queryProcessDefinitionByName(loginUser, projectCode, name);
        return new WorkflowDetailResponse(result);
    }

    /**
     * query workflow definition list
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return workflow definition list
     */
    @ApiOperation(value = "queryList", notes = "QUERY_WORKFLOW_DEFINITION_LIST_NOTES")
    @GetMapping(consumes = {"application/json"}, value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_LIST)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public QueryWorkflowListResponse queryWorkflowDefinitionList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                 @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode) {
        Result result = workflowDefinitionService.queryProcessDefinitionList(loginUser, projectCode);
        return new QueryWorkflowListResponse(result);
    }

    /**
     * query workflow definition simple list
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return workflow definition list
     */
    @ApiOperation(value = "querySimpleList", notes = "QUERY_WORKFLOW_DEFINITION_SIMPLE_LIST_NOTES")
    @GetMapping(consumes = {"application/json"}, value = "/simple-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_LIST)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public QuerySimpleWorkflowListResponse queryWorkflowDefinitionSimpleList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                             @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode) {
        Result result = workflowDefinitionService.queryProcessDefinitionSimpleList(loginUser, projectCode);
        return new QuerySimpleWorkflowListResponse(result);
    }

    /**
     * query workflow definition list paging
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param request     query request
     * @return workflow definition page
     */
    @ApiOperation(value = "queryListPaging", notes = "QUERY_WORKFLOW_DEFINITION_LIST_PAGING_NOTES")
    @GetMapping(consumes = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public QueryWorkflowResponse queryWorkflowDefinitionListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                   @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                   @RequestBody QueryWorkflowRequest request) {
        Result result = checkPageParams(request.getPageNo(), request.getPageSize());
        if (!result.checkResult()) {
            return new QueryWorkflowResponse(result);
        }
        request.setSearchVal(ParameterUtils.handleEscapes(request.getSearchVal()));

        result = workflowDefinitionService.queryProcessDefinitionListPaging(loginUser, projectCode, request.getSearchVal(), request.getOtherParamsJson(), request.getUserId(), request.getPageNo(),
            request.getPageSize());

        return new QueryWorkflowResponse(result);
    }

    /**
     * get tasks list by workflow definition code
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param code        workflow definition code
     * @return task list
     */
    @ApiOperation(value = "getTasksByDefinitionCode", notes = "GET_TASK_LIST_BY_DEFINITION_CODE_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "code", value = "WORKFLOW_DEFINITION_CODE", required = true, dataType = "Long", example = "100")
    })
    @GetMapping(consumes = {"application/json"}, value = "/{code}/tasks")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR)
    public GetTaskListResponse getNodeListByDefinitionCode(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                           @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                           @PathVariable("code") long code) {
        Result result = workflowDefinitionService.getTaskNodeListByDefinitionCode(loginUser, projectCode, code);
        return new GetTaskListResponse(result);
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
    @ApiOperation(value = "viewTree", notes = "VIEW_TREE_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "code", value = "WORKFLOW_DEFINITION_CODE", required = true, dataType = "Long", example = "100"),
        @ApiImplicitParam(name = "limit", value = "LIMIT", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(consumes = {"application/json"}, value = "/{code}/view-tree")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(ENCAPSULATION_TREEVIEW_STRUCTURE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public TreeViewResponse viewTree(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                     @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                     @PathVariable("code") long code,
                                     @RequestParam("limit") Integer limit) {
        Result result = workflowDefinitionService.viewTree(loginUser, projectCode, code, limit);
        return new TreeViewResponse(result);
    }

    /**
     * get tasks list map by workflow definition multiple code
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param codes       workflow definition codes
     * @return node list data
     */
    @ApiOperation(value = "getTaskListByDefinitionCodes", notes = "GET_TASK_LIST_BY_DEFINITION_CODE_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "codes", value = "WORKFLOW_DEFINITION_CODES", required = true, type = "String", example = "100,200,300")
    })
    @GetMapping(consumes = {"application/json"}, value = "/batch-query-tasks")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR)
    public GetTaskListMapResponse getNodeListMapByDefinitionCodes(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                  @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                  @RequestParam("codes") String codes) {
        Result result = workflowDefinitionService.getNodeListMapByDefinitionCodes(loginUser, projectCode, codes);
        return new GetTaskListMapResponse(result);
    }

    /**
     * get workflow definition list map by project code
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return workflow definition list data
     */
    @ApiOperation(value = "getWorkflowListByProjectCode", notes = "GET_WORKFLOW_LIST_BY_WORKFLOW_CODE_NOTES")
    @GetMapping(consumes = {"application/json"}, value = "/query-workflow-definition-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR)
    public GetDependentSimplifyDefinitionListResponse getWorkflowListByProjectCodes(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                                    @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode
    ) {
        Result result = workflowDefinitionService.queryProcessDefinitionListByProjectCode(projectCode);
        return new GetDependentSimplifyDefinitionListResponse(result);
    }

    /**
     * get task definition list by workflow definition code
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return workflow definition list data
     */
    @ApiOperation(value = "getTaskListByWorkflowDefinitionCode", notes = "GET_TASK_LIST_BY_WORKFLOW_CODE_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", required = true, type = "Long", example = "100"),
        @ApiImplicitParam(name = "workflowDefinitionCode", value = "WORKFLOW_DEFINITION_CODE", required = true, dataType = "Long", example = "100"),
    })
    @GetMapping(consumes = {"application/json"}, value = "/query-task-definition-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR)
    public GetDependentSimplifyDefinitionListResponse getTaskListByWorkflowDefinitionCode(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                                          @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                                          @RequestParam(value = "workflowDefinitionCode") Long workflowDefinitionCode) {
        Result result = workflowDefinitionService.queryTaskDefinitionListByProcessDefinitionCode(projectCode, workflowDefinitionCode);
        return new GetDependentSimplifyDefinitionListResponse(result);
    }

    /**
     * delete workflow definition by code
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param code        workflow definition code
     * @return delete result code
     */
    @ApiOperation(value = "deleteByCode", notes = "DELETE_WORKFLOW_DEFINITION_BY_ID_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "code", value = "WORKFLOW_DEFINITION_CODE", dataType = "Int", example = "100")
    })
    @DeleteMapping(consumes = {"application/json"}, value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_PROCESS_DEFINE_BY_CODE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteWorkflowDefinitionByCode(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                 @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                 @PathVariable("code") long code) {
        return workflowDefinitionService.deleteProcessDefinitionByCode(loginUser, projectCode, code);
    }

    /**
     * batch delete workflow definition by codes
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param codes       workflow definition code list
     * @return delete result code
     */
    @ApiOperation(value = "batchDeleteByCodes", notes = "BATCH_DELETE_WORKFLOW_DEFINITION_BY_IDS_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "codes", value = "WORKFLOW_DEFINITION_CODE", required = true, dataType = "String")
    })
    @PostMapping(consumes = {"application/json"}, value = "/batch-delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_DELETE_PROCESS_DEFINE_BY_CODES_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result batchDeleteWorkflowDefinitionByCodes(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                       @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                       @RequestParam("codes") String codes) {
        Result result = new Result();
        Set<String> deleteFailedCodeSet = new HashSet<>();
        if (!StringUtils.isEmpty(codes)) {
            String[] workflowDefinitionCodeArray = codes.split(",");
            for (String strWorkflowDefinitionCode : workflowDefinitionCodeArray) {
                long code = Long.parseLong(strWorkflowDefinitionCode);
                try {
                    Result deleteResult = workflowDefinitionService.deleteProcessDefinitionByCode(loginUser, projectCode, code);
                    if (Status.SUCCESS.getCode() != deleteResult.getCode()) {
                        deleteFailedCodeSet.add(deleteResult.getMsg());
                        logger.error(deleteResult.getMsg());
                    }
                } catch (Exception e) {
                    deleteFailedCodeSet.add(MessageFormat.format(Status.DELETE_PROCESS_DEFINE_BY_CODES_ERROR.getMsg(), strWorkflowDefinitionCode));
                }
            }
        }

        if (!deleteFailedCodeSet.isEmpty()) {
            putMsg(result, BATCH_DELETE_PROCESS_DEFINE_BY_CODES_ERROR, String.join("\n", deleteFailedCodeSet));
        } else {
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * batch export workflow definition by codes
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param codes       workflow definition codes
     * @param response    response
     */
    @ApiOperation(value = "batchExportByCodes", notes = "BATCH_EXPORT_WORKFLOW_DEFINITION_BY_CODES_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "codes", value = "WORKFLOW_DEFINITION_CODE", required = true, dataType = "String")
    })
    @PostMapping(value = "/batch-export")
    @ResponseBody
    @AccessLogAnnotation(ignoreRequestArgs = {"loginUser", "response"})
    public void batchExportWorkflowDefinitionByCodes(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                     @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                     @RequestParam("codes") String codes,
                                                     HttpServletResponse response) {
        try {
            workflowDefinitionService.batchExportProcessDefinitionByCodes(loginUser, projectCode, codes, response);
        } catch (Exception e) {
            logger.error(Status.BATCH_EXPORT_PROCESS_DEFINE_BY_IDS_ERROR.getMsg(), e);
        }
    }

    /**
     * query all workflow definition by project code
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return workflow definition list
     */
    @ApiOperation(value = "queryAllByProjectCode", notes = "QUERY_WORKFLOW_DEFINITION_All_BY_PROJECT_CODE_NOTES")
    @GetMapping(consumes = {"application/json"}, value = "/all")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_LIST)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public QueryWorkflowListResponse queryAllWorkflowDefinitionByProjectCode(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                             @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode) {
        Result result = workflowDefinitionService.queryAllProcessDefinitionByProjectCode(loginUser, projectCode);
        return new QueryWorkflowListResponse(result);
    }

    /**
     * import workflow definition
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param file        resource file
     * @return import result code
     */
    @ApiOperation(value = "importWorkflowDefinition", notes = "IMPORT_WORKFLOW_DEFINITION_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "file", value = "RESOURCE_FILE", required = true, dataType = "MultipartFile")
    })
    @PostMapping(value = "/import")
    @ApiException(IMPORT_PROCESS_DEFINE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = {"loginUser", "file"})
    public Result importWorkflowDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                           @RequestParam("file") MultipartFile file) {
        Result result;
        if ("application/zip".equals(file.getContentType())) {
            result = workflowDefinitionService.importSqlProcessDefinition(loginUser, projectCode, file);
        } else {
            result = workflowDefinitionService.importProcessDefinition(loginUser, projectCode, file);
        }
        return result;
    }

    /**
     * update workflow definition basic info, not including task definition, task relation and location.
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param code        workflow definition code
     * @param request     update workflow request
     * @return
     */
    @ApiOperation(value = "updateBasicInfo", notes = "UPDATE_WORKFLOW_DEFINITION_BASIC_INFO_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "code", value = "WORKFLOW_DEFINITION_CODE", required = true, dataType = "Long", example = "123456789"),
        @ApiImplicitParam(name = "releaseState", value = "RELEASE_WORKFLOW_DEFINITION_NOTES", required = false, dataType = "ReleaseState"),
        @ApiImplicitParam(name = "otherParamsJson", value = "OTHER_PARAMS_JSON", required = false, type = "String")
    })
    @PutMapping(consumes = {"application/json"}, value = "/{code}/basic-info")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public UpdateWorkflowDefinitionResponse updateWorkflowDefinitionBasicInfo(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                              @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                              @PathVariable(value = "code", required = true) long code,
                                                                              @RequestBody UpdateWorkflowBasicInfoRequest request) {
        Result result = workflowDefinitionService.updateProcessDefinitionBasicInfo(loginUser, projectCode, request.getName(), code, request.getDescription(), request.getGlobalParams(),
            request.getTimeout(), request.getTenantCode(), request.getScheduleJson(), request.getOtherParamsJson(), request.getExecutionType());
        //  If the update fails, the result will be returned directly
        if (Status.SUCCESS.getCode() != result.getCode()) {
            return new UpdateWorkflowDefinitionResponse(result);
        }

        //  Judge whether to go online after editing,0 means offline, 1 means online
        if (request.getReleaseState() == ReleaseState.ONLINE) {
            result = workflowDefinitionService.releaseWorkflowAndSchedule(loginUser, projectCode, code, request.getReleaseState());
        }
        return new UpdateWorkflowDefinitionResponse(result);
    }

    /**
     * release workflow definition and schedule
     *
     * @param loginUser    login user
     * @param projectCode  project code
     * @param code         workflow definition code
     * @param releaseState releaseState
     * @return update result code
     */
    @ApiOperation(value = "releaseWorkflowAndSchedule", notes = "RELEASE_WORKFLOW_SCHEDULE_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "projectCode", value = "WORKFLOW_DEFINITION_NAME", required = true, type = "Long"),
        @ApiImplicitParam(name = "code", value = "WORKFLOW_DEFINITION_CODE", required = true, dataType = "Long", example = "123456789"),
        @ApiImplicitParam(name = "releaseState", value = "RELEASE_WORKFLOW_DEFINITION_NOTES", required = true, dataType = "ReleaseState")
    })
    @PostMapping(consumes = {"application/json"}, value = "/{code}/release-workflow")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(RELEASE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result releaseWorkflowAndSchedule(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                             @PathVariable(value = "code", required = true) long code,
                                             @RequestParam(value = "releaseState", required = true, defaultValue = "OFFLINE") ReleaseState releaseState) {
        return workflowDefinitionService.releaseWorkflowAndSchedule(loginUser, projectCode, code, releaseState);
    }

}
