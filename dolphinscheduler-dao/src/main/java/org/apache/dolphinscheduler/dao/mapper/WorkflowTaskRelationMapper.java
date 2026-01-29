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
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * workflow task relation mapper interface
 */
public interface WorkflowTaskRelationMapper extends BaseMapper<WorkflowTaskRelation> {

    /**
     * workflow task relation by projectCode and workflowDefinitionCode
     *
     * @param workflowDefinitionCode workflowDefinitionCode
     * @return ProcessTaskRelation list
     */
    List<WorkflowTaskRelation> queryByWorkflowDefinitionCode(@Param("workflowDefinitionCode") long workflowDefinitionCode);

    /**
     * update
     */
    int updateById(@Param("et") WorkflowTaskRelation workflowTaskRelation);

    /**
     * delete workflow task relation by workflowDefinitionCode
     *
     * @param projectCode projectCode
     * @param workflowDefinitionCode workflowDefinitionCode
     * @return int
     */
    int deleteByWorkflowDefinitionCode(@Param("projectCode") long projectCode,
                                       @Param("workflowDefinitionCode") long workflowDefinitionCode);

    /**
     * workflow task relation by taskCode
     *
     * @param taskCodes taskCode list
     * @return ProcessTaskRelation
     */
    List<WorkflowTaskRelation> queryByTaskCodes(@Param("taskCodes") Long[] taskCodes);

    /**
     * workflow task relation by taskCode
     *
     * @param taskCode taskCode
     * @return ProcessTaskRelation
     */
    List<WorkflowTaskRelation> queryByTaskCode(@Param("taskCode") long taskCode);

    /**
     * batch insert workflow task relation
     *
     * @param taskRelationList taskRelationList
     * @return int
     */
    int batchInsert(@Param("taskRelationList") List<WorkflowTaskRelation> taskRelationList);

    /**
     * query downstream workflow task relation by taskCode
     *
     * @param taskCode taskCode
     * @return ProcessTaskRelation
     */
    List<WorkflowTaskRelation> queryDownstreamByTaskCode(@Param("taskCode") long taskCode);

    /**
     * query upstream workflow task relation by taskCode
     *
     * @param projectCode projectCode
     * @param taskCode taskCode
     * @return ProcessTaskRelation
     */
    List<WorkflowTaskRelation> queryUpstreamByCode(@Param("projectCode") long projectCode,
                                                   @Param("taskCode") long taskCode);

    /**
     * query downstream workflow task relation by taskCode
     *
     * @param projectCode projectCode
     * @param taskCode taskCode
     * @return ProcessTaskRelation
     */
    List<WorkflowTaskRelation> queryDownstreamByCode(@Param("projectCode") long projectCode,
                                                     @Param("taskCode") long taskCode);

    /**
     * query task relation by codes
     *
     * @param projectCode projectCode
     * @param taskCode taskCode
     * @param preTaskCodes preTaskCode list
     * @return ProcessTaskRelation
     */
    List<WorkflowTaskRelation> queryUpstreamByCodes(@Param("projectCode") long projectCode,
                                                    @Param("taskCode") long taskCode,
                                                    @Param("preTaskCodes") Long[] preTaskCodes);

    /**
     * query workflow task relation by process definition code
     *
     * @param workflowDefinitionCode process definition code
     * @param workflowDefinitionVersion process definition version
     * @return ProcessTaskRelation
     */
    List<WorkflowTaskRelation> queryWorkflowTaskRelationsByWorkflowDefinitionCode(@Param("workflowDefinitionCode") long workflowDefinitionCode,
                                                                                  @Param("workflowDefinitionVersion") Integer workflowDefinitionVersion);

    /**
     * query by code
     *
     * @param projectCode projectCode
     * @param workflowDefinitionCode workflowDefinitionCode
     * @param preTaskCode preTaskCode
     * @param postTaskCode postTaskCode
     * @return ProcessTaskRelation
     */
    List<WorkflowTaskRelation> queryByCode(@Param("projectCode") long projectCode,
                                           @Param("workflowDefinitionCode") long workflowDefinitionCode,
                                           @Param("preTaskCode") long preTaskCode,
                                           @Param("postTaskCode") long postTaskCode);

    /**
     * delete workflow task relation
     *
     * @param workflowTaskRelationLog workflowTaskRelationLog
     * @return int
     */
    int deleteRelation(@Param("workflowTaskRelationLog") WorkflowTaskRelationLog workflowTaskRelationLog);

    /**
     * query downstream workflow task relation by workflowDefinitionCode
     * @param workflowDefinitionCode
     * @return ProcessTaskRelation
     */
    List<WorkflowTaskRelation> queryDownstreamByWorkflowDefinitionCode(@Param("workflowDefinitionCode") long workflowDefinitionCode);

    /**
     * Filter workflow task relation
     *
     * @param page page
     * @param workflowTaskRelation process definition object
     * @return workflow task relation IPage
     */
    IPage<WorkflowTaskRelation> filterWorkflowTaskRelation(IPage<WorkflowTaskRelation> page,
                                                           @Param("relation") WorkflowTaskRelation workflowTaskRelation);

    /**
     * batch update workflow task relation version
     *
     * @param workflowTaskRelation workflow task relation list
     * @return update num
     */
    int updateWorkflowTaskRelationTaskVersion(@Param("workflowTaskRelation") WorkflowTaskRelation workflowTaskRelation);

    Long queryTaskCodeByTaskName(@Param("workflowCode") Long workflowCode,
                                 @Param("taskName") String taskName);

    void deleteByWorkflowDefinitionCodeAndVersion(@Param("workflowDefinitionCode") long workflowDefinitionCode,
                                                  @Param("workflowDefinitionVersion") int workflowDefinitionVersion);

    /**
     * workflow task relation by taskCode and postTaskVersion
     *
     * @param taskCode taskCode
     * @param postTaskVersion postTaskVersion
     * @return ProcessTaskRelation
     */
    List<WorkflowTaskRelation> queryWorkflowTaskRelationByTaskCodeAndTaskVersion(@Param("taskCode") long taskCode,
                                                                                 @Param("postTaskVersion") long postTaskVersion);
}
