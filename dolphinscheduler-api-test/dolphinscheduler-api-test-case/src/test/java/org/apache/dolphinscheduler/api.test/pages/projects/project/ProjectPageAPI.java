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

package org.apache.dolphinscheduler.api.test.pages.projects.project;

import org.apache.dolphinscheduler.api.test.core.common.Constants;
import org.apache.dolphinscheduler.api.test.core.common.RequestMethod;
import org.apache.dolphinscheduler.api.test.pages.Route;
import org.apache.dolphinscheduler.api.test.pages.projects.project.entity.ProjectRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.project.entity.ProjectResponseEntity;
import org.apache.dolphinscheduler.api.test.utils.RestResponse;
import org.apache.dolphinscheduler.api.test.utils.Result;

import com.devskiller.jfairy.Fairy;

import io.restassured.specification.RequestSpecification;

public class ProjectPageAPI implements IProjectPageAPI {
    private final Fairy fairy = Fairy.create();
    private final RequestSpecification reqSpec;
    private final String sessionId;

    public ProjectPageAPI(RequestSpecification reqSpec, String sessionId) {
        this.reqSpec = reqSpec;
        this.sessionId = sessionId;
    }

    @Override
    public RestResponse<Result> createProject(ProjectRequestEntity projectRequestEntity) {
        return toResponse(restRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
            projectRequestEntity.toMap(), Route.projects(), RequestMethod.POST));
    }

    @Override
    public ProjectResponseEntity createProject() {
        ProjectRequestEntity projectEntityInstance = getProjectEntityInstance();
        return createProjectByProjectEntity(projectEntityInstance);
    }

    @Override
    public ProjectResponseEntity createProjectByProjectEntity(ProjectRequestEntity projectRequestEntity) {
        return toResponse(restRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
            projectRequestEntity.toMap(), Route.projects(), RequestMethod.POST)).
            getResponse().jsonPath().getObject(Constants.DATA_KEY, ProjectResponseEntity.class);
    }

    @Override
    public ProjectRequestEntity getProjectEntityInstance() {
        return getProjectEntityInstance(fairy.person().getCompany().getName());
    }

    @Override
    public ProjectRequestEntity getProjectEntityInstance(String projectName) {
        ProjectRequestEntity projectRequestEntity = new ProjectRequestEntity();
        projectRequestEntity.setProjectName(projectName);
        projectRequestEntity.setDescription(fairy.person().getFullName());
        return projectRequestEntity;
    }
}
