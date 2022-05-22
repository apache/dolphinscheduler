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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_PROCESS_TASK_RELATION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DATA_IS_NOT_VALID;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_EDGE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_TASK_PROCESS_RELATION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_TASK_PROCESS_RELATION_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ProcessTaskRelationService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

/**
 * process task relation controller
 */
@Api(tags = "PROCESS_TASK_RELATION_TAG")
@RestController
@RequestMapping("projects/{projectCode}/process-task-relation")
public class ProcessTaskRelationController extends BaseController {

    @Autowired
    private ProcessTaskRelationService processTaskRelationService;

    /**
     * create process task relation
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionCode processDefinitionCode
     * @param preTaskCode preTaskCode
     * @param postTaskCode postTaskCode
     * @return create result code
     */
    @ApiOperation(value = "save", notes = "CREATE_PROCESS_TASK_RELATION_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", required = true, type = "Long"),
        @ApiImplicitParam(name = "processDefinitionCode", value = "PROCESS_DEFINITION_CODE", required = true, type = "Long"),
        @ApiImplicitParam(name = "preTaskCode", value = "PRE_TASK_CODE", required = true, type = "Long"),
        @ApiImplicitParam(name = "postTaskCode", value = "POST_TASK_CODE", required = true, type = "Long")
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_PROCESS_TASK_RELATION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createProcessTaskRelation(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                            @RequestParam(name = "processDefinitionCode", required = true) long processDefinitionCode,
                                            @RequestParam(name = "preTaskCode", required = true) long preTaskCode,
                                            @RequestParam(name = "postTaskCode", required = true) long postTaskCode) {
        Map<String, Object> result = new HashMap<>();
        if (postTaskCode == 0L) {
            putMsg(result, DATA_IS_NOT_VALID, "postTaskCode");
        } else if (processDefinitionCode == 0L) {
            putMsg(result, DATA_IS_NOT_VALID, "processDefinitionCode");
        } else {
            result = processTaskRelationService.createProcessTaskRelation(loginUser, projectCode, processDefinitionCode, preTaskCode, postTaskCode);
        }
        return returnDataList(result);
    }

    /**
     * delete process task relation (delete task from workflow)
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionCode process definition code
     * @param taskCode the post task code
     * @return delete result code
     */
    @ApiOperation(value = "deleteRelation", notes = "DELETE_PROCESS_TASK_RELATION_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", required = true, type = "Long"),
        @ApiImplicitParam(name = "processDefinitionCode", value = "PROCESS_DEFINITION_CODE", required = true, type = "Long"),
        @ApiImplicitParam(name = "taskCode", value = "TASK_CODE", required = true, type = "Long")
    })
    @DeleteMapping(value = "/{taskCode}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_TASK_PROCESS_RELATION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteTaskProcessRelation(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                            @RequestParam(name = "processDefinitionCode", required = true) long processDefinitionCode,
                                            @PathVariable("taskCode") long taskCode) {
        return returnDataList(processTaskRelationService.deleteTaskProcessRelation(loginUser, projectCode, processDefinitionCode, taskCode));
    }

    /**
     * delete task upstream relation
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param preTaskCodes the pre task codes, sep ','
     * @param taskCode the post task code
     * @return delete result code
     */
    @ApiOperation(value = "deleteUpstreamRelation", notes = "DELETE_UPSTREAM_RELATION_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", required = true, type = "Long"),
        @ApiImplicitParam(name = "preTaskCodes", value = "PRE_TASK_CODES", required = true, type = "String", example = "1,2"),
        @ApiImplicitParam(name = "taskCode", value = "TASK_CODE", required = true, type = "Long")
    })
    @DeleteMapping(value = "/{taskCode}/upstream")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_TASK_PROCESS_RELATION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteUpstreamRelation(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                         @RequestParam(name = "preTaskCodes", required = true) String preTaskCodes,
                                         @PathVariable("taskCode") long taskCode) {
        return returnDataList(processTaskRelationService.deleteUpstreamRelation(loginUser, projectCode, preTaskCodes, taskCode));
    }

    /**
     * delete task downstream relation
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param postTaskCodes the post task codes, sep ','
     * @param taskCode the pre task code
     * @return delete result code
     */
    @ApiOperation(value = "deleteDownstreamRelation", notes = "DELETE_DOWNSTREAM_RELATION_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", required = true, type = "Long"),
        @ApiImplicitParam(name = "postTaskCodes", value = "POST_TASK_CODES", required = true, type = "String", example = "1,2"),
        @ApiImplicitParam(name = "taskCode", value = "TASK_CODE", required = true, type = "Long")
    })
    @DeleteMapping(value = "/{taskCode}/downstream")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_TASK_PROCESS_RELATION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteDownstreamRelation(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                           @RequestParam(name = "postTaskCodes", required = true) String postTaskCodes,
                                           @PathVariable("taskCode") long taskCode) {
        return returnDataList(processTaskRelationService.deleteDownstreamRelation(loginUser, projectCode, postTaskCodes, taskCode));
    }

    /**
     * query task upstream relation
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskCode current task code (post task code)
     * @return process task relation list
     */
    @ApiOperation(value = "queryUpstreamRelation", notes = "QUERY_UPSTREAM_RELATION_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", required = true, type = "Long"),
        @ApiImplicitParam(name = "taskCode", value = "TASK_CODE", required = true, type = "Long")
    })
    @GetMapping(value = "/{taskCode}/upstream")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_PROCESS_RELATION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryUpstreamRelation(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                        @PathVariable("taskCode") long taskCode) {
        return returnDataList(processTaskRelationService.queryUpstreamRelation(loginUser, projectCode, taskCode));
    }

    /**
     * query task downstream relation
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskCode pre task code
     * @return process task relation list
     */
    @ApiOperation(value = "queryDownstreamRelation", notes = "QUERY_DOWNSTREAM_RELATION_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", required = true, type = "Long"),
        @ApiImplicitParam(name = "taskCode", value = "TASK_CODE", required = true, type = "Long")
    })
    @GetMapping(value = "/{taskCode}/downstream")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_PROCESS_RELATION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryDownstreamRelation(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                          @PathVariable("taskCode") long taskCode) {
        return returnDataList(processTaskRelationService.queryDownstreamRelation(loginUser, projectCode, taskCode));
    }

    /**
     * delete edge
     *
     * @param loginUser             login user
     * @param projectCode           project code
     * @param processDefinitionCode process definition code
     * @param preTaskCode pre task code
     * @param postTaskCode post task code
     * @return delete result code
     */
    @ApiOperation(value = "deleteEdge", notes = "DELETE_EDGE_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", required = true, type = "Long"),
        @ApiImplicitParam(name = "processDefinitionCode", value = "PROCESS_DEFINITION_CODE", required = true, type = "Long"),
        @ApiImplicitParam(name = "preTaskCode", value = "PRE_TASK_CODE", required = true, type = "Long"),
        @ApiImplicitParam(name = "postTaskCode", value = "POST_TASK_CODE", required = true, type = "Long")
    })
    @DeleteMapping(value = "/{processDefinitionCode}/{preTaskCode}/{postTaskCode}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_EDGE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteEdge(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                             @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true)
                             @PathVariable long projectCode,
                             @PathVariable long processDefinitionCode,
                             @PathVariable long preTaskCode,
                             @PathVariable long postTaskCode) {
        return returnDataList(processTaskRelationService.deleteEdge(loginUser, projectCode, processDefinitionCode, preTaskCode, postTaskCode));
    }

}
