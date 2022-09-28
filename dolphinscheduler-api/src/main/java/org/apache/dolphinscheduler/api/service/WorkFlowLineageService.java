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

import org.apache.dolphinscheduler.dao.entity.TaskMainInfo;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * work flow lineage service
 */
public interface WorkFlowLineageService {

    Map<String, Object> queryWorkFlowLineageByName(long projectCode, String workFlowName);

    Map<String, Object> queryWorkFlowLineageByCode(long projectCode, long workFlowCode);

    Map<String, Object> queryWorkFlowLineage(long projectCode);

    /**
     * Query tasks depend on process definition, include upstream or downstream
     *
     * @param projectCode Project code want to query tasks dependence
     * @param processDefinitionCode Process definition code want to query tasks dependence
     * @return Set of TaskMainInfo
     */
    Set<TaskMainInfo> queryTaskDepOnProcess(long projectCode, long processDefinitionCode);

    /**
     * Query and return tasks dependence with string format, is a wrapper of queryTaskDepOnTask and task query method.
     *
     * @param projectCode Project code want to query tasks dependence
     * @param processDefinitionCode Process definition code want to query tasks dependence
     * @param taskCode Task code want to query tasks dependence
     * @return dependent process definition
     */
    Optional<String> taskDepOnTaskMsg(long projectCode, long processDefinitionCode, long taskCode);
}
