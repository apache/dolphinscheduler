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

import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * process task relation mapper interface
 */
@CacheConfig(cacheNames = "processTaskRelation", keyGenerator = "cacheKeyGenerator")
public interface ProcessTaskRelationMapper extends BaseMapper<ProcessTaskRelation> {

    /**
     * process task relation by projectCode and processCode
     *
     * @param projectCode projectCode
     * @param processCode processCode
     * @return ProcessTaskRelation list
     */
    @Cacheable(unless = "#result == null || #result.size() == 0")
    List<ProcessTaskRelation> queryByProcessCode(@Param("projectCode") long projectCode,
                                                 @Param("processCode") long processCode);

    /**
     * update
     */
    @CacheEvict(key = "#p0.projectCode + '_' + #p0.processDefinitionCode")
    int updateById(@Param("et") ProcessTaskRelation processTaskRelation);

    /**
     * delete process task relation by processCode
     *
     * @param projectCode projectCode
     * @param processCode processCode
     * @return int
     */
    @CacheEvict
    int deleteByCode(@Param("projectCode") long projectCode, @Param("processCode") long processCode);

    /**
     * process task relation by taskCode
     *
     * @param taskCodes taskCode list
     * @return ProcessTaskRelation
     */
    List<ProcessTaskRelation> queryByTaskCodes(@Param("taskCodes") Long[] taskCodes);

    /**
     * process task relation by taskCode
     *
     * @param taskCode taskCode
     * @return ProcessTaskRelation
     */
    List<ProcessTaskRelation> queryByTaskCode(@Param("taskCode") long taskCode);

    /**
     * batch insert process task relation
     *
     * @param taskRelationList taskRelationList
     * @return int
     */
    int batchInsert(@Param("taskRelationList") List<ProcessTaskRelation> taskRelationList);

    /**
     * query downstream process task relation by taskCode
     *
     * @param taskCode taskCode
     * @return ProcessTaskRelation
     */
    List<ProcessTaskRelation> queryDownstreamByTaskCode(@Param("taskCode") long taskCode);

    /**
     * query upstream process task relation by taskCode
     *
     * @param projectCode projectCode
     * @param taskCode taskCode
     * @return ProcessTaskRelation
     */
    List<ProcessTaskRelation> queryUpstreamByCode(@Param("projectCode") long projectCode,
                                                  @Param("taskCode") long taskCode);

    /**
     * query downstream process task relation by taskCode
     *
     * @param projectCode projectCode
     * @param taskCode taskCode
     * @return ProcessTaskRelation
     */
    List<ProcessTaskRelation> queryDownstreamByCode(@Param("projectCode") long projectCode,
                                                    @Param("taskCode") long taskCode);

    /**
     * query task relation by codes
     *
     * @param projectCode projectCode
     * @param taskCode taskCode
     * @param preTaskCodes preTaskCode list
     * @return ProcessTaskRelation
     */
    List<ProcessTaskRelation> queryUpstreamByCodes(@Param("projectCode") long projectCode,
                                                   @Param("taskCode") long taskCode,
                                                   @Param("preTaskCodes") Long[] preTaskCodes);

    /**
     * query process task relation by process definition code
     *
     * @param processDefinitionCode process definition code
     * @param processDefinitionVersion process definition version
     * @return ProcessTaskRelation
     */
    List<ProcessTaskRelation> queryProcessTaskRelationsByProcessDefinitionCode(@Param("processDefinitionCode") long processDefinitionCode,
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
     * batch update process task relation pre task
     *
     * @param processTaskRelationList process task relation list
     * @return update num
     */
    int batchUpdateProcessTaskRelationPreTask(@Param("processTaskRelationList") List<ProcessTaskRelation> processTaskRelationList);

    /**
     * query by code
     *
     * @param projectCode projectCode
     * @param processDefinitionCode processDefinitionCode
     * @param preTaskCode preTaskCode
     * @param postTaskCode postTaskCode
     * @return ProcessTaskRelation
     */
    List<ProcessTaskRelation> queryByCode(@Param("projectCode") long projectCode,
                                          @Param("processDefinitionCode") long processDefinitionCode,
                                          @Param("preTaskCode") long preTaskCode,
                                          @Param("postTaskCode") long postTaskCode);

    /**
     * delete process task relation
     *
     * @param processTaskRelationLog processTaskRelationLog
     * @return int
     */
    int deleteRelation(@Param("processTaskRelationLog") ProcessTaskRelationLog processTaskRelationLog);

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
     * query downstream process task relation by processDefinitionCode
     * @param processDefinitionCode
     * @return ProcessTaskRelation
     */
    List<ProcessTaskRelation> queryDownstreamByProcessDefinitionCode(@Param("processDefinitionCode") long processDefinitionCode);

    /**
     * Filter process task relation
     *
     * @param page page
     * @param processTaskRelation process definition object
     * @return process task relation IPage
     */
    IPage<ProcessTaskRelation> filterProcessTaskRelation(IPage<ProcessTaskRelation> page,
                                                         @Param("relation") ProcessTaskRelation processTaskRelation);

    /**
     * batch update process task relation version
     *
     * @param processTaskRelationList process task relation list
     * @return update num
     */
    int updateProcessTaskRelationTaskVersion(@Param("processTaskRelation") ProcessTaskRelation processTaskRelationList);

    Long queryTaskCodeByTaskName(@Param("workflowCode") Long workflowCode,
                                 @Param("taskName") String taskName);

    void deleteByWorkflowDefinitionCode(@Param("workflowDefinitionCode") long workflowDefinitionCode,
                                        @Param("workflowDefinitionVersion") int workflowDefinitionVersion);
}
