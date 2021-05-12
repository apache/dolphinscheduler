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

import org.apache.dolphinscheduler.dao.entity.DefinitionGroupByUser;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * task definition mapper interface
 */
public interface TaskDefinitionMapper extends BaseMapper<TaskDefinition> {

    /**
     * query task definition by name
     *
     * @param projectCode projectCode
     * @param name name
     * @return task definition
     */
    TaskDefinition queryByDefinitionName(@Param("projectCode") Long projectCode,
                                      @Param("taskDefinitionName") String name);

    /**
     * query task definition by id
     *
     * @param taskDefinitionId taskDefinitionId
     * @return task definition
     */
    TaskDefinition queryByDefinitionId(@Param("taskDefinitionId") int taskDefinitionId);

    /**
     * query task definition by code
     *
     * @param taskDefinitionCode taskDefinitionCode
     * @return task definition
     */
    TaskDefinition queryByDefinitionCode(@Param("taskDefinitionCode") Long taskDefinitionCode);

    /**
     * query all task definition list
     *
     * @param projectCode projectCode
     * @return task definition list
     */
    List<TaskDefinition> queryAllDefinitionList(@Param("projectCode") Long projectCode);

    /**
     * query task definition by ids
     *
     * @param ids ids
     * @return task definition list
     */
    List<TaskDefinition> queryDefinitionListByIdList(@Param("ids") Integer[] ids);

    /**
     * count task definition group by user
     *
     * @param projectCodes projectCodes
     * @return task definition list
     */
    List<DefinitionGroupByUser> countDefinitionGroupByUser(@Param("projectCodes") Long[] projectCodes);

    /**
     * list all resource ids
     *
     * @return task ids list
     */
    @MapKey("id")
    List<Map<String, Object>> listResources();

    /**
     * list all resource ids by user id
     *
     * @return resource ids list
     */
    @MapKey("id")
    List<Map<String, Object>> listResourcesByUser(@Param("userId") Integer userId);

    /**
     * delete task definition by code
     *
     * @param code code
     * @return int
     */
    int deleteByCode(@Param("code") Long code);
}
