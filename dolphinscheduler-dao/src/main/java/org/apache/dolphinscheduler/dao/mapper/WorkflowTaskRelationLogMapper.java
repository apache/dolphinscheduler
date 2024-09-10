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

import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelationLog;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface WorkflowTaskRelationLogMapper extends BaseMapper<WorkflowTaskRelationLog> {

    /**
     * query workflow task relation log
     *
     * @param workflowDefinitionCode workflow definition code
     * @param workflowDefinitionVersion workflow version
     * @return workflow task relation log
     */
    List<WorkflowTaskRelationLog> queryByWorkflowCodeAndVersion(@Param("workflowDefinitionCode") long workflowDefinitionCode,
                                                                @Param("workflowDefinitionVersion") int workflowDefinitionVersion);

    /**
     * batch insert workflow task relation
     *
     * @param taskRelationList taskRelationList
     * @return int
     */
    int batchInsert(@Param("taskRelationList") List<WorkflowTaskRelationLog> taskRelationList);

    /**
     * delete workflow task relation log by workflowDefinitionCode and version
     *
     * @param workflowDefinitionCode workflow definition code
     * @param workflowDefinitionVersion workflow version
     * @return int
     */
    int deleteByCode(@Param("workflowDefinitionCode") long workflowDefinitionCode,
                     @Param("workflowDefinitionVersion") int workflowDefinitionVersion);

    /**
     * delete workflow task relation
     *
     * @param workflowTaskRelationLog  workflowTaskRelationLog
     * @return int
     */
    int deleteRelation(@Param("workflowTaskRelationLog") WorkflowTaskRelationLog workflowTaskRelationLog);

    /**
     * query workflow task relation log
     *
     * @param workflowTaskRelation workflowTaskRelation
     * @return workflow task relation log
     */
    WorkflowTaskRelationLog queryRelationLogByRelation(@Param("workflowTaskRelation") WorkflowTaskRelation workflowTaskRelation);

    List<WorkflowTaskRelationLog> queryByWorkflowDefinitionCode(@Param("workflowDefinitionCode") long workflowDefinitionCode);

    void deleteByWorkflowDefinitionCode(@Param("workflowDefinitionCode") long workflowDefinitionCode);
}
