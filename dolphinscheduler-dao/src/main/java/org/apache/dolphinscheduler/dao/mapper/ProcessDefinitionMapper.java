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
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProcessDefinitionMapper extends BaseMapper<ProcessDefinition> {


    ProcessDefinition queryByDefineName(@Param("projectId") int projectId,
                                        @Param("processDefinitionName") String name);

    ProcessDefinition queryByDefineId(@Param("processDefineId") int processDefineId);

    IPage<ProcessDefinition> queryDefineListPaging(IPage<ProcessDefinition> page,
                                                   @Param("searchVal") String searchVal,
                                                   @Param("userId") int userId,
                                                   @Param("projectId") int projectId);

    List<ProcessDefinition> queryAllDefinitionList(@Param("projectId") int projectId);

    List<ProcessDefinition> queryDefinitionListByIdList(@Param("ids") Integer[] ids);

    List<ProcessDefinition> queryDefinitionListByTenant(@Param("tenantId") int tenantId);

    List<DefinitionGroupByUser> countDefinitionGroupByUser(
            @Param("userId") Integer userId,
            @Param("projectIds") Integer[] projectIds);
}
