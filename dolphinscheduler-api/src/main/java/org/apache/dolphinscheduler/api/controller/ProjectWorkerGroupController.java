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

import static org.apache.dolphinscheduler.api.enums.Status.ASSIGN_WORKER_GROUP_TO_PROJECT_ERROR;

import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ProjectWorkerGroupRelationService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * project and worker group controller
 */
@Tag(name = "PROJECT_WORKER_GROUP_TAG")
@RestController
@RequestMapping("projects/{projectCode}/worker-group")
@Slf4j
public class ProjectWorkerGroupController extends BaseController {

    @Autowired
    private ProjectWorkerGroupRelationService projectWorkerGroupRelationService;

    /**
     * assign worker groups to the project
     *
     * @param loginUser login user
     * @param projectCode project code
     @ @RequestParam(value = "workerGroups", required = false) String workerGroups
     * @return create result code
     */
    @Operation(summary = "assignWorkerGroups", description = "ASSIGN_WORKER_GROUPS_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", schema = @Schema(implementation = long.class, example = "123456")),
            @Parameter(name = "workerGroups", description = "WORKER_GROUP_LIST", schema = @Schema(implementation = List.class))
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(ASSIGN_WORKER_GROUP_TO_PROJECT_ERROR)
    public Result assignWorkerGroups(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                     @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                     @Parameter(name = "workerGroups") String[] workerGroups) {

        List<String> workerGroupList = Arrays.stream(workerGroups).collect(Collectors.toList());
        return projectWorkerGroupRelationService.assignWorkerGroupsToProject(loginUser, projectCode, workerGroupList);
    }

    /**
     * query worker groups that assigned to the project
     *
     * @param projectCode project code
     * @return worker group list
     */
    @Operation(summary = "queryWorkerGroups", description = "QUERY_WORKER_GROUP_LIST")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", schema = @Schema(implementation = long.class, example = "123456"))
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> queryWorkerGroups(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                 @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode) {
        return projectWorkerGroupRelationService.queryWorkerGroupsByProject(loginUser, projectCode);
    }

}
