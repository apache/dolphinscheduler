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

import org.apache.dolphinscheduler.dao.entity.ProcessLineage;
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
    List<WorkFlowLineage> queryWorkFlowLineageByName(@Param("projectCode") long projectCode, @Param("workFlowName") String workFlowName);

    /**
     * queryWorkFlowLineageByCode
     *
     * @param projectCode projectCode
     * @param workFlowCode workFlowCode
     * @return WorkFlowLineage
     */
    WorkFlowLineage queryWorkFlowLineageByCode(@Param("projectCode") long projectCode, @Param("workFlowCode") long workFlowCode);

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
     * @param taskCode taskCode
     * @param taskVersion taskVersion
     * @param processDefinitionCode processDefinitionCode
     * @return ProcessLineage list
     */
    List<ProcessLineage> queryCodeRelation(@Param("projectCode") long projectCode,
                                           @Param("processDefinitionCode") long processDefinitionCode,
                                           @Param("taskCode") long taskCode,
                                           @Param("taskVersion") int taskVersion);
}
