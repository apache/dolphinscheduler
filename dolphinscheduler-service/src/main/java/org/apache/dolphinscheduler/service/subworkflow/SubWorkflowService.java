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

package org.apache.dolphinscheduler.service.subworkflow;

import org.apache.dolphinscheduler.dao.entity.RelationSubWorkflow;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public interface SubWorkflowService {

    List<WorkflowInstance> getAllDynamicSubWorkflow(long processInstanceId, long taskCode);

    int batchInsertRelationSubWorkflow(List<RelationSubWorkflow> relationSubWorkflowList);

    List<WorkflowInstance> filterFinishProcessInstances(List<WorkflowInstance> workflowInstanceList);

    List<WorkflowInstance> filterSuccessProcessInstances(List<WorkflowInstance> workflowInstanceList);

    List<WorkflowInstance> filterRunningProcessInstances(List<WorkflowInstance> workflowInstanceList);

    List<WorkflowInstance> filterWaitToRunProcessInstances(List<WorkflowInstance> workflowInstanceList);

    List<WorkflowInstance> filterFailedProcessInstances(List<WorkflowInstance> workflowInstanceList);

    List<Property> getWorkflowOutputParameters(WorkflowInstance workflowInstance);
}
