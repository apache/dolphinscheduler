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

import org.apache.dolphinscheduler.dao.entity.EnvironmentWorkerGroupRelation;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * environment worker group relation mapper interface
 */
public interface EnvironmentWorkerGroupRelationMapper extends BaseMapper<EnvironmentWorkerGroupRelation> {

    /**
     * environment worker group relation by environmentCode
     *
     * @param environmentCode environmentCode
     * @return EnvironmentWorkerGroupRelation list
     */
    List<EnvironmentWorkerGroupRelation> queryByEnvironmentCode(@Param("environmentCode") Long environmentCode);

    /**
     * environment worker group relation by workerGroupName
     *
     * @param workerGroupName workerGroupName
     * @return EnvironmentWorkerGroupRelation list
     */
    List<EnvironmentWorkerGroupRelation> queryByWorkerGroupName(@Param("workerGroupName") String workerGroupName);

    /**
     * delete environment worker group relation by processCode
     *
     * @param environmentCode environmentCode
     * @param workerGroupName workerGroupName
     * @return int
     */
    int deleteByCode(@Param("environmentCode") Long environmentCode, @Param("workerGroupName") String workerGroupName);
}
