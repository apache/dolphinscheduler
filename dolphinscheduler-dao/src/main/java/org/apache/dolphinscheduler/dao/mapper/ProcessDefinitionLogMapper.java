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
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionVersion;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * process definition log mapper interface
 */
public interface ProcessDefinitionLogMapper extends BaseMapper<ProcessDefinitionLog> {

    /**
     * query process definition log by name
     *
     * @param projectCode projectCode
     * @param name process name
     * @return process definition log list
     */
    List<ProcessDefinitionLog> queryByDefinitionName(@Param("projectCode") Long projectCode,
                                        @Param("processDefinitionName") String name);

    /**
     * query process definition log list
     *
     * @param processDefinitionCode processDefinitionCode
     * @return process definition log list
     */
    List<ProcessDefinitionLog> queryByDefinitionCode(@Param("processDefinitionCode") long processDefinitionCode);

    /**
     * query the certain process definition version info by process definition code and version number
     *
     * @param processDefinitionCode process definition code
     * @param version version number
     * @return the process definition version info
     */
    ProcessDefinitionLog queryByDefinitionCodeAndVersion(@Param("processDefinitionCode") Long processDefinitionCode, @Param("version") long version);

}
