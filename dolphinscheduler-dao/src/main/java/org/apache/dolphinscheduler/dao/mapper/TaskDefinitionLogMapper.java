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

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * task definition log mapper interface
 */
public interface TaskDefinitionLogMapper extends BaseMapper<TaskDefinitionLog> {

    /**
     * query task definition log by name
     *
     * @param projectCode projectCode
     * @param name name
     * @return task definition log list
     */
    List<TaskDefinitionLog> queryByDefinitionName(@Param("projectCode") Long projectCode,
                                                  @Param("taskDefinitionName") String name);

    /**
     * query max version for definition
     *
     * @param taskDefinitionCode taskDefinitionCode
     */
    Integer queryMaxVersionForDefinition(@Param("taskDefinitionCode") long taskDefinitionCode);

    /**
     * query task definition log
     *
     * @param taskDefinitionCode taskDefinitionCode
     * @param version version
     * @return task definition log
     */
    TaskDefinitionLog queryByDefinitionCodeAndVersion(@Param("taskDefinitionCode") long taskDefinitionCode,
                                                      @Param("version") int version);


    /**
     *
     * @param taskDefinitions
     * @return
     */
    List<TaskDefinitionLog> queryByTaskDefinitions(@Param("taskDefinitions") Collection<TaskDefinition> taskDefinitions);

}
