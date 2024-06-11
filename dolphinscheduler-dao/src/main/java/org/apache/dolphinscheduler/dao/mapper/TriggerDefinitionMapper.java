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

import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.dao.entity.*;
import org.apache.dolphinscheduler.dao.model.WorkflowDefinitionCountDto;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * trigger definition mapper interface
 */

public interface TriggerDefinitionMapper extends BaseMapper<TriggerDefinition> {

    /**
     * query trigger definition by name
     *
     * @param projectCode projectCode
     * @param processCode processCode
     * @param name name
     * @return trigger definition
     */
    TriggerDefinition queryByName(@Param("projectCode") long projectCode,
                               @Param("processCode") long processCode,
                               @Param("name") String name);

    /**
     * query trigger definition by code
     *
     * @param code taskDefinitionCode
     * @return trigger definition
     */
    TriggerDefinition queryByCode(@Param("code") long code);

    /**
     * query all trigger definition list
     *
     * @param projectCode projectCode
     * @return trigger definition list
     */
    List<TriggerDefinition> queryAllDefinitionList(@Param("projectCode") long projectCode);

    /**
     * task main info page
     *
     * @param page page
     * @param projectCode projectCode
     * @param searchTriggerName searchTriggerName
     * @param triggerType triggerType
     * @return task main info IPage
     */
    IPage<TriggerMainInfo> queryDefinitionListPaging(IPage<TriggerMainInfo> page,
                                                 @Param("projectCode") long projectCode,
                                                 @Param("searchTaskName") String searchTriggerName,
                                                 @Param("taskType") String triggerType);

    /**
     * delete trigger definition by code
     *
     * @param code code
     * @return int
     */
    int deleteByCode(@Param("code") long code);
}

