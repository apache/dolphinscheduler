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
    List<ProcessTaskRelation> queryByProcessCode(@Param("projectCode") Long projectCode,
                                                 @Param("processCode") Long processCode);

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
    List<ProcessTaskRelation> queryByTaskCode(@Param("taskCode") Long taskCode);

    /**
     * delete process task relation by processCode
     *
     * @param projectCode projectCode
     * @param processCode processCode
     * @return int
     */
    int deleteByCode(@Param("projectCode") Long projectCode,
                     @Param("processCode") Long processCode);
}
