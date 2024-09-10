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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.dao.entity.DependentLineageTask;
import org.apache.dolphinscheduler.dao.entity.DependentWorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkFlowLineage;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelationDetail;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskLineage;

import java.util.List;
import java.util.Optional;

public interface WorkflowLineageService {

    List<WorkFlowRelationDetail> queryWorkFlowLineageByName(long projectCode, String workflowDefinitionName);

    WorkFlowLineage queryWorkFlowLineageByCode(long projectCode, long workflowDefinitionCode);

    WorkFlowLineage queryWorkFlowLineage(long projectCode);

    /**
     * Query downstream tasks depend on a workflow definition or a task
     *
     * @param workflowDefinitionCode workflow definition code want to query tasks dependence
     * @return downstream dependent workflow definition list
     */
    List<DependentWorkflowDefinition> queryDownstreamDependentWorkflowDefinitions(Long workflowDefinitionCode);

    /**
     * Query and return tasks dependence with string format, is a wrapper of queryTaskDepOnTask and task query method.
     *
     * @param projectCode           Project code want to query tasks dependence
     * @param workflowDefinitionCode workflow definition code want to query tasks dependence
     * @param taskCode              Task code want to query tasks dependence
     * @return dependent workflow definition
     */
    Optional<String> taskDependentMsg(long projectCode, long workflowDefinitionCode, long taskCode);

    List<DependentLineageTask> queryDependentWorkflowDefinitions(long projectCode, long workflowDefinitionCode,
                                                                 Long taskCode);

    int createWorkflowLineage(List<WorkflowTaskLineage> workflowTaskLineages);

    int updateWorkflowLineage(List<WorkflowTaskLineage> workflowTaskLineages);

    int deleteWorkflowLineage(List<Long> workflowDefinitionCodes);
}
