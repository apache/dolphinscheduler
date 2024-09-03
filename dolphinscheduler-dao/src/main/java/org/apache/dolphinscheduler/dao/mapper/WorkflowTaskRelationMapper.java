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
import java.util.Map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * workflow task relation mapper interface
 */
public interface WorkflowTaskRelationMapper extends BaseMapper<WorkflowTaskRelation> {

    /**
     * workflow task relation by projectCode and processCode
     *
     * @param processCode processCode
     * @return ProcessTaskRelation list
     */
    List<WorkflowTaskRelation> queryByProcessCode(@Param("processCode") long processCode);

    /**
     * update
     */
    int updateById(@Param("et") WorkflowTaskRelation workflowTaskRelation);

    /**
     * delete workflow task relation by processCode
     *
     * @param projectCode projectCode
     * @param processCode processCode
     * @return int
     */
    int deleteByCode(@Param("projectCode") long projectCode, @Param("processCode") long processCode);

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
     * @param processDefinitionCode process definition code
     * @param processDefinitionVersion process definition version
     * @return ProcessTaskRelation
     */
    List<WorkflowTaskRelation> queryProcessTaskRelationsByProcessDefinitionCode(@Param("processDefinitionCode") long processDefinitionCode,
                                                                                @Param("processDefinitionVersion") Integer processDefinitionVersion);

    /**
     * count upstream by codes
     *
     * @param projectCode projectCode
     * @param taskCode taskCode
     * @param processDefinitionCodes processDefinitionCodes
     * @return upstream count list group by process definition code
     */
    List<Map<String, Long>> countUpstreamByCodeGroupByProcessDefinitionCode(@Param("projectCode") long projectCode,
                                                                            @Param("processDefinitionCodes") Long[] processDefinitionCodes,
                                                                            @Param("taskCode") long taskCode);

    /**
     * batch update workflow task relation pre task
     *
     * @param workflowTaskRelationList workflow task relation list
     * @return update num
     */
    int batchUpdateProcessTaskRelationPreTask(@Param("processTaskRelationList") List<WorkflowTaskRelation> workflowTaskRelationList);

    /**
     * query by code
     *
     * @param projectCode projectCode
     * @param processDefinitionCode processDefinitionCode
     * @param preTaskCode preTaskCode
     * @param postTaskCode postTaskCode
     * @return ProcessTaskRelation
     */
    List<WorkflowTaskRelation> queryByCode(@Param("projectCode") long projectCode,
                                           @Param("processDefinitionCode") long processDefinitionCode,
                                           @Param("preTaskCode") long preTaskCode,
                                           @Param("postTaskCode") long postTaskCode);

    /**
     * delete workflow task relation
     *
     * @param processTaskRelationLog processTaskRelationLog
     * @return int
     */
    int deleteRelation(@Param("processTaskRelationLog") WorkflowTaskRelationLog processTaskRelationLog);

    /**
     * count by code
     *
     * @param projectCode projectCode
     * @param processDefinitionCode processDefinitionCode
     * @param preTaskCode preTaskCode
     * @param postTaskCode postTaskCode
     * @return ProcessTaskRelation
     */
    int countByCode(@Param("projectCode") long projectCode,
                    @Param("processDefinitionCode") long processDefinitionCode,
                    @Param("preTaskCode") long preTaskCode,
                    @Param("postTaskCode") long postTaskCode);

    /**
     * query downstream workflow task relation by processDefinitionCode
     * @param processDefinitionCode
     * @return ProcessTaskRelation
     */
    List<WorkflowTaskRelation> queryDownstreamByProcessDefinitionCode(@Param("processDefinitionCode") long processDefinitionCode);

    /**
     * Filter workflow task relation
     *
     * @param page page
     * @param workflowTaskRelation process definition object
     * @return workflow task relation IPage
     */
    IPage<WorkflowTaskRelation> filterProcessTaskRelation(IPage<WorkflowTaskRelation> page,
                                                          @Param("relation") WorkflowTaskRelation workflowTaskRelation);

    /**
     * batch update workflow task relation version
     *
     * @param workflowTaskRelationList workflow task relation list
     * @return update num
     */
    int updateProcessTaskRelationTaskVersion(@Param("processTaskRelation") WorkflowTaskRelation workflowTaskRelationList);

    Long queryTaskCodeByTaskName(@Param("workflowCode") Long workflowCode,
                                 @Param("taskName") String taskName);

    void deleteByWorkflowDefinitionCode(@Param("workflowDefinitionCode") long workflowDefinitionCode,
                                        @Param("workflowDefinitionVersion") int workflowDefinitionVersion);

    /**
     * workflow task relation by taskCode and postTaskVersion
     *
     * @param taskCode taskCode
     * @param postTaskVersion postTaskVersion
     * @return ProcessTaskRelation
     */
    List<WorkflowTaskRelation> queryProcessTaskRelationByTaskCodeAndTaskVersion(@Param("taskCode") long taskCode,
                                                                                @Param("postTaskVersion") long postTaskVersion);
}
