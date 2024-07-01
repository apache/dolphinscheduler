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

import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionLog;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * process definition log mapper interface
 */
@CacheConfig(cacheNames = "processDefinition", keyGenerator = "cacheKeyGenerator")
public interface ProcessDefinitionLogMapper extends BaseMapper<ProcessDefinitionLog> {

    /**
     * query the certain process definition version info by process definition code and version number
     *
     * @param code process definition code
     * @param version version number
     * @return the process definition version info
     */
    @Cacheable(sync = true)
    ProcessDefinitionLog queryByDefinitionCodeAndVersion(@Param("code") long code, @Param("version") int version);

    /**
     * query process definition log by name
     *
     * @param projectCode projectCode
     * @param name process definition name
     * @return process definition log list
     */
    List<ProcessDefinitionLog> queryByDefinitionName(@Param("projectCode") long projectCode,
                                                     @Param("name") String name);

    /**
     * query process definition log list
     *
     * @param code process definition code
     * @return process definition log list
     */
    List<ProcessDefinitionLog> queryByDefinitionCode(@Param("code") long code);

    /**
     * query max version for definition
     */
    Integer queryMaxVersionForDefinition(@Param("code") long code);

    /**
     * query max version definition log
     */
    ProcessDefinitionLog queryMaxVersionDefinitionLog(@Param("code") long code);

    /**
     * query the paging process definition version list by pagination info
     *
     * @param page pagination info
     * @param code process definition code
     * @param projectCode project code
     * @return the paging process definition version list
     */
    IPage<ProcessDefinitionLog> queryProcessDefinitionVersionsPaging(Page<ProcessDefinitionLog> page,
                                                                     @Param("code") long code,
                                                                     @Param("projectCode") long projectCode);

    /**
     * delete the certain process definition version by process definition id and version number
     *
     * @param code process definition code
     * @param version version number
     * @return delete result
     */
    int deleteByProcessDefinitionCodeAndVersion(@Param("code") long code, @Param("version") int version);

    void deleteByProcessDefinitionCode(@Param("workflowDefinitionCode") long workflowDefinitionCode);
}
