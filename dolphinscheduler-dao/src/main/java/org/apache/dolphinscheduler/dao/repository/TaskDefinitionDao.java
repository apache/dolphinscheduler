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

package org.apache.dolphinscheduler.dao.repository;

import org.apache.dolphinscheduler.dao.entity.TaskDefinition;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Task Instance DAO
 */
public interface TaskDefinitionDao extends IDao<TaskDefinition> {

    /**
     * Get list of task definition by process definition code
     *
     * @param processDefinitionCode process definition code
     * @return list of task definition
     */
    List<TaskDefinition> getTaskDefinitionListByDefinition(long processDefinitionCode);

    /**
     * Query task definition by code and version
     * @param taskCode task code
     * @param taskDefinitionVersion task definition version
     * @return task definition
     */
    TaskDefinition findTaskDefinition(long taskCode, int taskDefinitionVersion);

    void deleteByWorkflowDefinitionCodeAndVersion(long workflowDefinitionCode, int workflowDefinitionVersion);

    void deleteByTaskDefinitionCodes(Set<Long> needToDeleteTaskDefinitionCodes);

    List<TaskDefinition> queryByCodes(Collection<Long> taskDefinitionCodes);
}
