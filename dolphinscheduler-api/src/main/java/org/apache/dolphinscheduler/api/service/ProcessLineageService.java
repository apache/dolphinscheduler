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

import org.apache.dolphinscheduler.dao.entity.DependentProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessLineage;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelationDetail;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * work flow lineage service
 */
public interface ProcessLineageService {

    List<WorkFlowRelationDetail> queryWorkFlowLineageByName(long projectCode, String processDefinitionName);

    Map<String, Object> queryWorkFlowLineageByCode(long projectCode, long processDefinitionCode);

    Map<String, Object> queryWorkFlowLineage(long projectCode);

    /**
     * Query downstream tasks depend on a process definition or a task
     *
     * @param processDefinitionCode Process definition code want to query tasks dependence
     * @return downstream dependent process definition list
     */
    List<DependentProcessDefinition> queryDownstreamDependentProcessDefinitions(Long processDefinitionCode);

    /**
     * Query and return tasks dependence with string format, is a wrapper of queryTaskDepOnTask and task query method.
     *
     * @param projectCode           Project code want to query tasks dependence
     * @param processDefinitionCode Process definition code want to query tasks dependence
     * @param taskCode              Task code want to query tasks dependence
     * @return dependent process definition
     */
    Optional<String> taskDependentMsg(long projectCode, long processDefinitionCode, long taskCode);

    Map<String, Object> queryDependentProcessDefinitions(long projectCode, long processDefinitionCode, Long taskCode);

    int createProcessLineage(List<ProcessLineage> processLineages);

    int updateProcessLineage(List<ProcessLineage> processLineages);

    int deleteProcessLineage(List<Long> processDefinitionCodes);
}
