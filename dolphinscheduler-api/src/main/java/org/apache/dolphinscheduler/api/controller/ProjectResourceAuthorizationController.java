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

import org.apache.dolphinscheduler.api.service.ProjectResourceAuthorizationService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ProjectResourceType;
import org.apache.dolphinscheduler.dao.entity.ProjectResourceAuthorization;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("projects/{projectCode}/resource-authorizations/{resourceType}")
@RequiredArgsConstructor
public class ProjectResourceAuthorizationController {

    private final ProjectResourceAuthorizationService authorizationService;

    @GetMapping
    public Result<List<ProjectResourceAuthorization>> queryAuthorizations(
                                                                          @RequestAttribute(Constants.SESSION_USER) User loginUser,
                                                                          @PathVariable("projectCode") long projectCode,
                                                                          @PathVariable("resourceType") ProjectResourceType resourceType) {
        return Result.success(authorizationService.queryAuthorizations(loginUser, projectCode, resourceType));
    }

    @PutMapping
    public Result<Void> grantResource(@RequestAttribute(Constants.SESSION_USER) User loginUser,
                                      @PathVariable("projectCode") long projectCode,
                                      @PathVariable("resourceType") ProjectResourceType resourceType,
                                      @RequestParam("resourceKey") String resourceKey) {
        authorizationService.grantResource(loginUser, projectCode, resourceType, resourceKey);
        return Result.success();
    }

    @DeleteMapping
    public Result<Void> revokeResource(@RequestAttribute(Constants.SESSION_USER) User loginUser,
                                       @PathVariable("projectCode") long projectCode,
                                       @PathVariable("resourceType") ProjectResourceType resourceType,
                                       @RequestParam("resourceKey") String resourceKey) {
        authorizationService.revokeResource(loginUser, projectCode, resourceType, resourceKey);
        return Result.success();
    }
}
