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

package org.apache.dolphinscheduler.api.v3.controller;

import org.apache.dolphinscheduler.api.controller.BaseController;
import org.apache.dolphinscheduler.api.v3.utils.IdObfuscator;
import org.apache.dolphinscheduler.api.v3.utils.QueryResult;
import org.apache.dolphinscheduler.api.v3.service.ProjectV3Service;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * project controller
 */
@RestController
@RequestMapping("/v3/projects")
@Slf4j
public class ProjectV3Controller extends BaseController {

    @Autowired
    private ProjectV3Service projectV3Service;

    /**
     * query project list paging
     *
     * @param searchVal  search value
     * @param nextToken  next token
     * @param maxResults max results
     * @return project list which the login user have permission to see
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public QueryResult<Project> listAvailableProjects(
                                                      Authentication authentication,
                                                      @RequestParam(value = "searchVal", required = false) String searchVal,
                                                      @RequestParam(value = "nextToken", required = false) String nextToken,
                                                      @RequestParam(value = "maxResults", required = false, defaultValue = "10") Integer maxResults) {
        User loginUser = (User) authentication.getPrincipal();
        int offset = nextToken != null ? IdObfuscator.decode(nextToken) : 0;

        QueryResult<Project> queryResult = new QueryResult<>();

        List<Project> projects = projectV3Service.listAuthorizedProject(
                loginUser,
                offset,
                maxResults,
                searchVal);

        if (!projects.isEmpty()) {
            queryResult.setNextToken(IdObfuscator.encode(projects.getLast().getId()));
        }

        queryResult.setData(projects);
        return queryResult;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Project createProject(
                                 Authentication authentication,
                                 @RequestParam("projectName") String projectName,
                                 @RequestParam(value = "description", required = false) String description) {
        return projectV3Service.createProject((User) authentication.getPrincipal(), projectName, description);
    }

    /**
     * update project
     *
     * @param code        project code
     * @param projectName project name
     * @param description description
     * @return update result code
     */
    @PutMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Project updateProject(
                                 Authentication authentication,
                                 @PathVariable("code") Long code,
                                 @RequestParam(value = "projectName", required = false) String projectName,
                                 @RequestParam(value = "description", required = false) String description) {
        User loginUser = (User) authentication.getPrincipal();
        Project project = projectV3Service.queryProjectForUpdate(loginUser, code);
        return projectV3Service.updateProject(project, projectName, description);
    }

    @DeleteMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(
                              Authentication authentication,
                              @PathVariable("code") Long code) {
        User loginUser = (User) authentication.getPrincipal();
        Project project = projectV3Service.queryProjectForUpdate(loginUser, code);
        projectV3Service.deleteProject(project);
    }
}
