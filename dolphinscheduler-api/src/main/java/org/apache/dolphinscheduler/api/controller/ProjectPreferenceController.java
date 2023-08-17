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

import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROJECT_PREFERENCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_PROJECT_PREFERENCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_PROJECT_PREFERENCE_STATE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ProjectPreferenceService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "PROJECT_PREFERENCE_TAG")
@RestController
@RequestMapping("projects/{projectCode}/project-preference")
@Slf4j
public class ProjectPreferenceController extends BaseController {

    @Autowired
    private ProjectPreferenceService projectPreferenceService;

    @Operation(summary = "updateProjectPreference", description = "UPDATE_PROJECT_PREFERENCE_NOTES")
    @Parameters({
            @Parameter(name = "projectPreferences", description = "PROJECT_PREFERENCES", schema = @Schema(implementation = String.class)),
    })
    @PutMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(UPDATE_PROJECT_PREFERENCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateProjectPreference(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                          @RequestParam(value = "projectPreferences", required = true) String projectPreferences) {
        return projectPreferenceService.updateProjectPreference(loginUser, projectCode, projectPreferences);
    }

    @Operation(summary = "queryProjectPreferenceByProjectCode", description = "QUERY_PROJECT_PREFERENCE_NOTES")
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROJECT_PREFERENCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProjectPreferenceByProjectCode(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                      @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode) {
        return projectPreferenceService.queryProjectPreferenceByProjectCode(loginUser, projectCode);
    }

    @Operation(summary = "enableProjectPreference", description = "UPDATE_PROJECT_PREFERENCE_STATE_NOTES")
    @Parameters({
            @Parameter(name = "state", description = "PROJECT_PREFERENCES_STATE", schema = @Schema(implementation = String.class)),
    })
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_PROJECT_PREFERENCE_STATE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result enableProjectPreference(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                          @RequestParam(value = "state", required = true) int state) {
        return projectPreferenceService.enableProjectPreference(loginUser, projectCode, state);
    }

}
