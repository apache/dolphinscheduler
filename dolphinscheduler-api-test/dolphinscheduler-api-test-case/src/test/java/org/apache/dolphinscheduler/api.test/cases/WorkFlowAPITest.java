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

package org.apache.dolphinscheduler.api.test.cases;

import org.apache.dolphinscheduler.api.test.base.AbstractAPITest;
import org.apache.dolphinscheduler.api.test.core.extensions.DolphinScheduler;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.WorkFlowPageAPI;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.WorkFlowDefinitionResponseEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.WorkFlowRunRequestEntity;
import org.apache.dolphinscheduler.api.test.utils.RestResponse;
import org.apache.dolphinscheduler.api.test.utils.Result;
import org.apache.dolphinscheduler.api.test.utils.enums.ReleaseState;

import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.devskiller.jfairy.Fairy;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
@DisplayName("WorkFlow Page API test")
public class WorkFlowAPITest extends AbstractAPITest {
    private final Fairy fairy = Fairy.create();
    private WorkFlowPageAPI workFlowPageAPI;

    @BeforeAll
    public void initWorkFlowAPIFactory() {
        workFlowPageAPI = pageAPIFactory.createWorkFlowPageAPI();
    }

    @Test
    @DisplayName("Test shell workflowBasic workflow")
    public void testRunWorkFlow() {
        WorkFlowDefinitionResponseEntity workFlowDefinitionResponseEntity = workFlowPageAPI.createWorkFlowDefinitionByTenant(
            "shell", "echo 123", "SHELL").getResponseDataObject(WorkFlowDefinitionResponseEntity.class);
        workFlowPageAPI.releaseWorkFlowDefinition(workFlowDefinitionResponseEntity.getName(), ReleaseState.ONLINE,
            workFlowDefinitionResponseEntity.getProjectCode(), workFlowDefinitionResponseEntity.getCode()).isResponseSuccessful();
        WorkFlowRunRequestEntity workFlowRunRequestEntity = workFlowPageAPI.getWorkFlowRunRequestEntityInstance(workFlowDefinitionResponseEntity.getCode(), new Date(), new Date());
        RestResponse<Result> resultRestResponse = workFlowPageAPI.runWorkFlowDefinition(workFlowRunRequestEntity, workFlowDefinitionResponseEntity.getProjectCode());
        resultRestResponse.isResponseSuccessful();
    }


}
