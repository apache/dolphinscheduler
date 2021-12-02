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

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * process task relation mapper interface
 */
public interface ProcessTaskRelationMapper extends BaseMapper<ProcessTaskRelation> {

    /**
     * process task relation by projectCode and processCode
     *
     * @param projectCode projectCode
     * @param processCode processCode
     * @return ProcessTaskRelation list
     */
    List<ProcessTaskRelation> queryByProcessCode(@Param("projectCode") long projectCode,
                                                 @Param("processCode") long processCode);

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
     * delete process task relation by processCode
     *
     * @param projectCode projectCode
     * @param processCode processCode
     * @return int
     */
    int deleteByCode(@Param("projectCode") long projectCode,
                     @Param("processCode") long processCode);

    /**
     * batch insert process task relation
     *
     * @param taskRelationList taskRelationList
     * @return int
     */
    int batchInsert(@Param("taskRelationList") List<ProcessTaskRelationLog> taskRelationList);

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
     * @param taskCode    taskCode
     * @return ProcessTaskRelation
     */
    List<ProcessTaskRelation> queryUpstreamByCode(@Param("projectCode") long projectCode, @Param("taskCode") long taskCode);

    /**
     * query downstream process task relation by taskCode
     *
     * @param projectCode projectCode
     * @param taskCode    taskCode
     * @return ProcessTaskRelation
     */
    List<ProcessTaskRelation> queryDownstreamByCode(@Param("projectCode") long projectCode, @Param("taskCode") long taskCode);

    /**
     * query by code
     *
     * @param projectCode           projectCode
     * @param processDefinitionCode processDefinitionCode
     * @param preTaskCode           preTaskCode
     * @param postTaskCode          postTaskCode
     * @return ProcessTaskRelation
     */
    List<ProcessTaskRelation> queryByCode(@Param("projectCode") long projectCode,
                                          @Param("processDefinitionCode") long processDefinitionCode,
                                          @Param("preTaskCode") long preTaskCode,
                                          @Param("postTaskCode") long postTaskCode);

    /**
     * delete process task relation
     *
     * @param processTaskRelationLog  processTaskRelationLog
     * @return int
     */
    int deleteRelation(@Param("processTaskRelationLog") ProcessTaskRelationLog processTaskRelationLog);
}
