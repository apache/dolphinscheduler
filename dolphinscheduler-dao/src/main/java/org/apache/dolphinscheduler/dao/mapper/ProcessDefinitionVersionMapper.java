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

import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionVersion;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * process definition mapper interface
 */
public interface ProcessDefinitionVersionMapper extends BaseMapper<ProcessDefinitionVersion> {

    /**
     * query max version by process definition id
     *
     * @param processDefinitionId process definition id
     * @return the max version of this process definition id
     */
    Long queryMaxVersionByProcessDefinitionId(@Param("processDefinitionId") int processDefinitionId);

    /**
     * query the paging process definition version list by pagination info
     *
     * @param page pagination info
     * @param processDefinitionId process definition id
     * @return the paging process definition version list
     */
    IPage<ProcessDefinitionVersion> queryProcessDefinitionVersionsPaging(Page<ProcessDefinitionVersion> page,
                                                                         @Param("processDefinitionId") int processDefinitionId);

    /**
     * query the certain process definition version info by process definition id and version number
     *
     * @param processDefinitionId process definition id
     * @param version version number
     * @return the process definition version info
     */
    ProcessDefinitionVersion queryByProcessDefinitionIdAndVersion(@Param("processDefinitionId") int processDefinitionId, @Param("version") long version);

    /**
     * delete the certain process definition version by process definition id and version number
     *
     * @param processDefinitionId process definition id
     * @param version version number
     * @return delete result
     */
    int deleteByProcessDefinitionIdAndVersion(@Param("processDefinitionId") int processDefinitionId, @Param("version") long version);

}
