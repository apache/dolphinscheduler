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

import org.apache.dolphinscheduler.api.permission.ProjectAuthorizationService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.UserSimpleInfoVO;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ProjectPermission;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;
import java.util.Set;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("projects/{projectCode}/members")
public class ProjectAuthorizationController {

    private final ProjectAuthorizationService authorizationService;

    public ProjectAuthorizationController(ProjectAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @GetMapping("/candidates")
    public Result<List<UserSimpleInfoVO>> queryAssignableMembers(
                                                                 @RequestAttribute(Constants.SESSION_USER) User loginUser,
                                                                 @PathVariable("projectCode") long projectCode) {
        return Result.success(authorizationService.queryAssignableMembers(loginUser, projectCode));
    }

    @GetMapping("/{userId}/permissions")
    public Result<Set<ProjectPermission>> queryMemberPermissions(
                                                                 @RequestAttribute(Constants.SESSION_USER) User loginUser,
                                                                 @PathVariable("projectCode") long projectCode,
                                                                 @PathVariable("userId") int userId) {
        return Result.success(authorizationService.queryMemberPermissions(loginUser, projectCode, userId));
    }

    @PutMapping("/{userId}/permissions")
    public Result<Void> replaceMemberPermissions(@RequestAttribute(Constants.SESSION_USER) User loginUser,
                                                 @PathVariable("projectCode") long projectCode,
                                                 @PathVariable("userId") int userId,
                                                 @RequestBody Set<ProjectPermission> permissions) {
        authorizationService.replaceMemberPermissions(loginUser, projectCode, userId, permissions);
        return Result.success();
    }
}
