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

import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;

import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * task definition log mapper interface
 */
@CacheConfig(cacheNames = "taskDefinition", keyGenerator = "cacheKeyGenerator")
public interface TaskDefinitionLogMapper extends BaseMapper<TaskDefinitionLog> {

    /**
     * query task definition log
     *
     * @param code taskDefinitionCode
     * @param version version
     * @return task definition log
     */
    @Cacheable(sync = true)
    TaskDefinitionLog queryByDefinitionCodeAndVersion(@Param("code") long code, @Param("version") int version);

    /**
     * update
     */
    @CacheEvict(key = "#p0.code + '_' + #p0.version")
    int updateById(@Param("et") TaskDefinitionLog taskDefinitionLog);

    /**
     * delete the certain task definition version by task definition code and version
     *
     * @param code task definition code
     * @param version task definition version
     * @return delete result
     */
    @CacheEvict
    int deleteByCodeAndVersion(@Param("code") long code, @Param("version") int version);

    /**
     * query max version for definition
     *
     * @param code taskDefinitionCode
     */
    Integer queryMaxVersionForDefinition(@Param("code") long code);

    /**
     * todo: rename to query by code and version
     * @param taskDefinitions taskDefinition list
     * @return list
     */
    List<TaskDefinitionLog> queryByTaskDefinitions(@Param("taskDefinitions") Collection<TaskDefinition> taskDefinitions);

    /**
     * batch insert task definition logs
     *
     * @param taskDefinitionLogs taskDefinitionLogs
     * @return int
     */
    int batchInsert(@Param("taskDefinitionLogs") List<TaskDefinitionLog> taskDefinitionLogs);

    /**
     * query the paging task definition version list by pagination info
     *
     * @param page pagination info
     * @param projectCode project code
     * @param code process definition code
     * @return the paging task definition version list
     */
    IPage<TaskDefinitionLog> queryTaskDefinitionVersionsPaging(Page<TaskDefinitionLog> page, @Param("code") long code,
                                                               @Param("projectCode") long projectCode);

    void deleteByTaskDefinitionCodes(@Param("taskDefinitionCodes") Set<Long> taskDefinitionCodes);
}
