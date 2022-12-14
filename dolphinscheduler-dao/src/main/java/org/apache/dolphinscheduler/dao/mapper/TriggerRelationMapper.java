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

import org.apache.dolphinscheduler.dao.entity.TriggerRelation;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * triggerRelation mapper interface
 */
public interface TriggerRelationMapper extends BaseMapper<TriggerRelation> {

    /**
     * query by code and id
     * @param triggerType
     * @param jobId
     * @return
     */
    TriggerRelation queryByTypeAndJobId(@Param("triggerType") Integer triggerType, @Param("jobId") int jobId);

    /**
     * query triggerRelation by code
     *
     * @param triggerCode triggerCode
     * @return triggerRelation
     */
    List<TriggerRelation> queryByTriggerRelationCode(@Param("triggerCode") Long triggerCode);

    /**
     * query triggerRelation by code
     *
     * @param triggerCode triggerCode
     * @return triggerRelation
     */
    List<TriggerRelation> queryByTriggerRelationCodeAndType(@Param("triggerCode") Long triggerCode,
                                                            @Param("triggerType") Integer triggerType);

    /**
     * delete triggerRelation by code
     *
     * @param triggerCode triggerCode
     * @return int
     */
    int deleteByCode(@Param("triggerCode") Long triggerCode);

    /**
     * if exist update else  insert
     *
     * @param triggerRelation
     */
    void upsert(@Param("triggerRelation") TriggerRelation triggerRelation);
}
