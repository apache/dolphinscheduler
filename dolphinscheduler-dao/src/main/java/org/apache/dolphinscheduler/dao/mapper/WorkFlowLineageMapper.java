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

package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.entity.DependentProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessLineage;
import org.apache.dolphinscheduler.dao.entity.TaskMainInfo;
import org.apache.dolphinscheduler.dao.entity.WorkFlowLineage;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WorkFlowLineageMapper {

    /**
     * queryByName
     *
     * @param projectCode projectCode
     * @param workFlowName workFlowName
     * @return WorkFlowLineage list
     */
    List<WorkFlowLineage> queryWorkFlowLineageByName(@Param("projectCode") long projectCode,
                                                     @Param("workFlowName") String workFlowName);

    /**
     * queryWorkFlowLineageByCode
     *
     * @param projectCode projectCode
     * @param workFlowCode workFlowCode
     * @return WorkFlowLineage
     */
    WorkFlowLineage queryWorkFlowLineageByCode(@Param("projectCode") long projectCode,
                                               @Param("workFlowCode") long workFlowCode);

    /**
     * queryWorkFlowLineageByProcessDefinitionCodes
     *
     * @param workFlowCodes workFlowCodes
     * @return WorkFlowLineage
     */
    List<WorkFlowLineage> queryWorkFlowLineageByProcessDefinitionCodes(@Param("workFlowCodes") List<Long> workFlowCodes);

    /**
     * queryWorkFlowLineageByCode
     *
     * @param processLineages processLineages
     * @return WorkFlowLineage list
     */
    List<WorkFlowLineage> queryWorkFlowLineageByLineage(@Param("processLineages") List<ProcessLineage> processLineages);

    /**
     * queryProcessLineage
     *
     * @param projectCode projectCode
     * @return ProcessLineage list
     */
    List<ProcessLineage> queryProcessLineage(@Param("projectCode") long projectCode);

    /**
     * queryCodeRelation
     *
     * @param projectCode projectCode
     * @param processDefinitionCode processDefinitionCode
     * @return ProcessLineage list
     */
    List<ProcessLineage> queryProcessLineageByCode(@Param("projectCode") long projectCode,
                                                   @Param("processDefinitionCode") long processDefinitionCode);

    /**
     * query process definition by name
     *
     * @return dependent process definition
     */
    List<DependentProcessDefinition> queryDependentProcessDefinitionByProcessDefinitionCode(@Param("code") long code);

    /**
     * query downstream work flow lineage by process definition code
     *
     * @return dependent process definition
     */
    List<WorkFlowLineage> queryDownstreamLineageByProcessDefinitionCode(@Param("code") long code,
                                                                        @Param("taskType") String taskType);

    /**
     * query upstream work flow dependent task params by process definition code
     *
     * @return task_params
     */
    List<DependentProcessDefinition> queryUpstreamDependentParamsByProcessDefinitionCode(@Param("code") long code,
                                                                                         @Param("taskType") String taskType);

    /**
     * Query all tasks type sub process depend on process definition.
     *
     * Query all upstream tasks from task type sub process.
     *
     * @param projectCode Project code want to query tasks dependence
     * @param processDefinitionCode Process definition code want to query tasks dependence
     * @return List of TaskMainInfo
     */
    List<TaskMainInfo> queryTaskSubProcessDepOnProcess(@Param("projectCode") long projectCode,
                                                       @Param("processDefinitionCode") long processDefinitionCode);

    /**
     * Query all tasks type dependent depend on process definition.
     *
     * Query all downstream tasks from task type dependent, method `queryTaskDepOnTask` is a proper subset of
     * current method `queryTaskDepOnProcess`. Which mean with the same parameter processDefinitionCode, all tasks in
     * `queryTaskDepOnTask` are in the result of method `queryTaskDepOnProcess`.
     *
     * @param projectCode Project code want to query tasks dependence
     * @param processDefinitionCode Process definition code want to query tasks dependence
     * @return List of TaskMainInfo
     */
    List<TaskMainInfo> queryTaskDependentDepOnProcess(@Param("projectCode") long projectCode,
                                                      @Param("processDefinitionCode") long processDefinitionCode);

    /**
     * Query all tasks depend on task, only downstream task support currently(from dependent task type).
     *
     * In case of dependent task type, method `queryTaskDepOnTask` is a proper subset of `queryTaskDepOnProcess`. Which
     * mean with the same processDefinitionCode, all tasks in `queryTaskDepOnTask` are in method `queryTaskDepOnProcess`.
     *
     * @param projectCode Project code want to query tasks dependence
     * @param processDefinitionCode Process definition code want to query tasks dependence
     * @param taskCode Task code want to query tasks dependence
     * @return dependent process definition
     */
    List<TaskMainInfo> queryTaskDepOnTask(@Param("projectCode") long projectCode,
                                          @Param("processDefinitionCode") long processDefinitionCode,
                                          @Param("taskCode") long taskCode);
}
