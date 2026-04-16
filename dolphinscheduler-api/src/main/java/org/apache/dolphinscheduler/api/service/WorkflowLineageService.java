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
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
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
     * Resolve downstream workflow definitions that depend on the given root workflow, using stored lineage.
     * <ul>
     *     <li>{@code allLevelDependent == false}: only direct dependents of the root.</li>
     *     <li>{@code allLevelDependent == true}: transitive dependents (BFS over direct dependents), skipping the root
     *     when it reappears as an edge target (cycle back to root).</li>
     * </ul>
     *
     * @param rootWorkflowDefinitionCode workflow to start from (not included in the result)
     * @param allLevelDependent          {@code true} for transitive closure, {@code false} for one hop only
     * @return ordered distinct downstream workflow definitions (stable order: BFS / insertion order)
     */
    default List<WorkflowDefinition> resolveDownstreamWorkflowDefinitionCodes(long rootWorkflowDefinitionCode,
                                                                              boolean allLevelDependent) {
        return resolveDownstreamWorkflowDefinitionCodes(rootWorkflowDefinitionCode, allLevelDependent, false);
    }

    /**
     * Resolve downstream workflow definitions and optionally filter offline workflows.
     * When {@code filterOfflineWorkflow} is true, offline workflow definitions are excluded from the result and are
     * not expanded further during transitive traversal.
     *
     * @param rootWorkflowDefinitionCode workflow to start from (not included in the result)
     * @param allLevelDependent          {@code true} for transitive closure, {@code false} for one hop only
     * @param filterOfflineWorkflow      whether offline workflows should be filtered out during traversal
     * @return ordered distinct downstream workflow definitions (stable order: BFS / insertion order)
     */
    List<WorkflowDefinition> resolveDownstreamWorkflowDefinitionCodes(long rootWorkflowDefinitionCode,
                                                                      boolean allLevelDependent,
                                                                      boolean filterOfflineWorkflow);

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

    /**
     * Replace the lineage of given workflow definition by new lineage list.
     * When the list is empty, existing lineage data will be deleted.
     *
     * @param workflowDefinitionCode workflow definition to update
     * @param workflowTaskLineages   new lineage list, can be empty
     * @return affected rows
     */
    int updateWorkflowLineage(long workflowDefinitionCode, List<WorkflowTaskLineage> workflowTaskLineages);

    int deleteWorkflowLineage(List<Long> workflowDefinitionCodes);
}
