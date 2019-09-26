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
package cn.escheduler.dao.mapper;

import cn.escheduler.common.enums.Flag;
import cn.escheduler.common.enums.ReleaseState;
import cn.escheduler.common.enums.UserType;
import cn.escheduler.dao.entity.DefinitionGroupByUser;
import cn.escheduler.dao.entity.ProcessDefinition;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProcessDefinitionMapper extends BaseMapper<ProcessDefinition> {


    ProcessDefinition queryByDefineName(@Param("projectId") int projectId,
                                        @Param("processDefinitionName") String name);

    IPage<ProcessDefinition> queryDefineListPaging(IPage<ProcessDefinition> page,
                                                   @Param("searchVal") String searchVal,
                                                   @Param("userId") int userId,
                                                   @Param("projectId") int projectId);

    List<ProcessDefinition> queryAllDefinitionList(@Param("projectId") int projectId);

    List<ProcessDefinition> queryDefinitionListByIdList(@Param("ids") Integer[] ids);

    List<DefinitionGroupByUser> countDefinitionGroupByUser(
            @Param("userId") Integer userId,
            @Param("projectIds") Integer[] projectIds);

    /**
     * update receivers and cc by definition id
     * @param receivers
     * @param receiversCc
     * @param processDefinitionId
     * @return
     */
    @UpdateProvider(type = ProcessDefinitionMapperProvider.class, method = "updateReceiversAndCcById")
    int updateReceiversAndCcById(@Param("receivers") String receivers,
                                 @Param("receiversCc") String receiversCc,
                                 @Param("processDefinitionId") int processDefinitionId);

    /**
     * query all
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "version", column = "version", javaType = Integer.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "releaseState", column = "release_state",  typeHandler = EnumOrdinalTypeHandler.class, javaType = ReleaseState.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "projectId", column = "project_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "userId", column = "user_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "flag", column = "flag", typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "userName", column = "user_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "timeout", column = "timeout", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "tenantId", column = "tenant_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "projectName", column = "project_name", javaType = String.class, jdbcType = JdbcType.VARCHAR)
    })
    @SelectProvider(type = ProcessDefinitionMapperProvider.class, method = "queryAll")
    List<ProcessDefinition> queryAll();
}
