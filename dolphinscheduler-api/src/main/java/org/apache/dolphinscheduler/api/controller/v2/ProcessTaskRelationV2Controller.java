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

package org.apache.dolphinscheduler.api.controller.v2;

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_PROCESS_TASK_RELATION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_TASK_PROCESS_RELATION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_UPSTREAM_TASK_PROCESS_RELATION_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.controller.BaseController;
import org.apache.dolphinscheduler.api.dto.taskRelation.TaskRelationCreateRequest;
import org.apache.dolphinscheduler.api.dto.taskRelation.TaskRelationDeleteRequest;
import org.apache.dolphinscheduler.api.dto.taskRelation.TaskRelationUpdateUpstreamRequest;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ProcessTaskRelationService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * process task relation controller
 */
@Tag(name = "PROCESS_TASK_RELATION_TAG")
@RestController
@RequestMapping("v2/relations")
public class ProcessTaskRelationV2Controller extends BaseController {

    @Autowired
    private ProcessTaskRelationService processTaskRelationService;

    /**
     * create resource process task relation
     *
     * @param loginUser login user
     * @param TaskRelationCreateRequest process task definition json contains the object you want to create
     * @return Result object created
     */
    @Operation(summary = "create", description = "CREATE_PROCESS_TASK_RELATION_NOTES")
    @PostMapping(consumes = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_PROCESS_TASK_RELATION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<ProcessTaskRelation> createTaskRelation(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                          @RequestBody TaskRelationCreateRequest TaskRelationCreateRequest) {
        ProcessTaskRelation processTaskRelation =
                processTaskRelationService.createProcessTaskRelationV2(loginUser, TaskRelationCreateRequest);
        return Result.success(processTaskRelation);
    }

    /**
     * delete resource process task relation
     *
     * @param loginUser login user
     * @param codePair code pair you want to delete the task relation, use `upstream,downstream` as example, will delete exists relation upstream -> downstream, throw error if not exists
     * @return delete result code
     */
    @Operation(summary = "delete", description = "DELETE_PROCESS_TASK_RELATION_NOTES")
    @Parameters({
            @Parameter(name = "code-pair", description = "TASK_DEFINITION_CODE", schema = @Schema(implementation = long.class, example = "123456,78901", required = true))
    })
    @DeleteMapping(value = "/{code-pair}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_TASK_PROCESS_RELATION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteTaskRelation(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                     @PathVariable("code-pair") String codePair) {
        TaskRelationDeleteRequest taskRelationDeleteRequest = new TaskRelationDeleteRequest(codePair);
        processTaskRelationService.deleteTaskProcessRelationV2(loginUser, taskRelationDeleteRequest.getUpstreamCode(),
                taskRelationDeleteRequest.getDownstreamCode());
        return Result.success();
    }

    /**
     * Update resource task relation by code, only update this code's upstreams
     *
     * @param loginUser         login user
     * @param code              resource task code want to update its upstreams
     * @param taskRelationUpdateUpstreamRequest workflowUpdateRequest
     * @return ResourceResponse object updated
     */
    @Operation(summary = "update", description = "UPDATE_PROCESS_TASK_RELATION_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "DOWMSTREAM_TASK_DEFINITION_CODE", schema = @Schema(implementation = long.class, example = "123456", required = true))
    })
    @PutMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_UPSTREAM_TASK_PROCESS_RELATION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<List<ProcessTaskRelation>> updateUpstreamTaskDefinition(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                          @PathVariable("code") Long code,
                                                                          @RequestBody TaskRelationUpdateUpstreamRequest taskRelationUpdateUpstreamRequest) {
        List<ProcessTaskRelation> processTaskRelations = processTaskRelationService
                .updateUpstreamTaskDefinitionWithSyncDag(loginUser, code, Boolean.TRUE,
                        taskRelationUpdateUpstreamRequest);
        return Result.success(processTaskRelations);
    }
}
