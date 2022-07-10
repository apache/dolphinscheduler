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

package org.apache.dolphinscheduler.api.test.pages.projects.workflow;

import org.apache.dolphinscheduler.api.test.base.IPageAPI;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.WorkFlowDefinitionRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.WorkFlowReleaseRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.WorkFlowRunRequestEntity;
import org.apache.dolphinscheduler.api.test.utils.RestResponse;
import org.apache.dolphinscheduler.api.test.utils.Result;

public interface IWorkFlowPageAPI extends IPageAPI {
    RestResponse<Result> createWorkFlowDefinition(WorkFlowDefinitionRequestEntity workFlowDefinitionRequestEntity, String projectCode);

    RestResponse<Result> releaseWorkFlowDefinition(WorkFlowReleaseRequestEntity workFlowReleaseRequestEntity, String projectCode, String workFlowDefinitionCode);

    RestResponse<Result> runWorkFlowDefinition(WorkFlowRunRequestEntity workFlowRunRequestEntity, String projectCode);
}
