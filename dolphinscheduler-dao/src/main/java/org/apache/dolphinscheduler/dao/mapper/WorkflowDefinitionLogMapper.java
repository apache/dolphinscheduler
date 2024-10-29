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

import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * workflow definition log mapper interface
 */

public interface WorkflowDefinitionLogMapper extends BaseMapper<WorkflowDefinitionLog> {

    /**
     * query the certain workflow definition version info by workflow definition code and version number
     *
     * @param code workflow definition code
     * @param version version number
     * @return the workflow definition version info
     */

    WorkflowDefinitionLog queryByDefinitionCodeAndVersion(@Param("code") long code, @Param("version") int version);

    /**
     * query workflow definition log by name
     *
     * @param projectCode projectCode
     * @param name workflow definition name
     * @return workflow definition log list
     */
    List<WorkflowDefinitionLog> queryByDefinitionName(@Param("projectCode") long projectCode,
                                                      @Param("name") String name);

    /**
     * query workflow definition log list
     *
     * @param code workflow definition code
     * @return workflow definition log list
     */
    List<WorkflowDefinitionLog> queryByDefinitionCode(@Param("code") long code);

    /**
     * query max version for definition
     */
    Integer queryMaxVersionForDefinition(@Param("code") long code);

    /**
     * query max version definition log
     */
    WorkflowDefinitionLog queryMaxVersionDefinitionLog(@Param("code") long code);

    /**
     * query the paging workflow definition version list by pagination info
     *
     * @param page pagination info
     * @param code workflow definition code
     * @param projectCode project code
     * @return the paging workflow definition version list
     */
    IPage<WorkflowDefinitionLog> queryWorkflowDefinitionVersionsPaging(Page<WorkflowDefinitionLog> page,
                                                                       @Param("code") long code,
                                                                       @Param("projectCode") long projectCode);

    /**
     * delete the certain workflow definition version by workflow definition id and version number
     *
     * @param code workflow definition code
     * @param version version number
     * @return delete result
     */
    int deleteByWorkflowDefinitionCodeAndVersion(@Param("code") long code, @Param("version") int version);

    void deleteByWorkflowDefinitionCode(@Param("workflowDefinitionCode") long workflowDefinitionCode);
}
