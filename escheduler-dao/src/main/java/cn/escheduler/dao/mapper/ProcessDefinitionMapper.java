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
import cn.escheduler.dao.model.DefinitionGroupByUser;
import cn.escheduler.dao.model.ProcessDefinition;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.Timestamp;
import java.util.List;

/**
 * process definition mapper
 */
public interface ProcessDefinitionMapper {

    /**
     * insert process define
     * @param processDefinition
     * @return
     */
    @InsertProvider(type = ProcessDefinitionMapperProvider.class, method = "insert")
    @Options(useGeneratedKeys = true,keyProperty = "processDefinition.id")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "processDefinition.id", before = false, resultType = int.class)
    int insert(@Param("processDefinition") ProcessDefinition processDefinition);


    /**
     * delete process define
     * @param processDefinitionId
     * @return
     */
    @DeleteProvider(type = ProcessDefinitionMapperProvider.class, method = "delete")
    int delete(@Param("processDefinitionId") int processDefinitionId);


    /**
     * update process define
     *
     * @param processDefinition
     * @return
     */
    @UpdateProvider(type = ProcessDefinitionMapperProvider.class, method = "update")
    int update(@Param("processDefinition") ProcessDefinition processDefinition);

    /**
     * update release state
     * @param processDefinitionId
     * @param releaseState
     * @return
     */
    @UpdateProvider(type = ProcessDefinitionMapperProvider.class, method = "updateProcessDefinitionReleaseState")
    int updateProcessDefinitionReleaseState(@Param("processDefinitionId") int processDefinitionId,
                                            @Param("releaseState") ReleaseState releaseState);


    /**
     * query definition by id
     * @param processDefinitionId
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "version", column = "version", javaType = Integer.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "releaseState", column = "release_state",  typeHandler = EnumOrdinalTypeHandler.class, javaType = ReleaseState.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "projectId", column = "project_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "userId", column = "user_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processDefinitionJson", column = "process_definition_json", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "globalParams", column = "global_params", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "flag", column = "flag", typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "userName", column = "user_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "locations", column = "locations", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "connects", column = "connects", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "projectName", column = "project_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "timeout", column = "timeout", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "tenantId", column = "tenant_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "receivers", column = "receivers", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "receiversCc", column = "receivers_cc", javaType = String.class, jdbcType = JdbcType.VARCHAR)

    })
    @SelectProvider(type = ProcessDefinitionMapperProvider.class, method = "queryByDefineId")
    ProcessDefinition queryByDefineId(@Param("processDefinitionId") int processDefinitionId);

    /**
     * query process definition by project id and name
     * @param projectId
     * @param name
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "version", column = "version", javaType = Integer.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "releaseState", column = "release_state",  typeHandler = EnumOrdinalTypeHandler.class, javaType = ReleaseState.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "projectId", column = "project_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "userId", column = "user_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processDefinitionJson", column = "process_definition_json", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "flag", column = "flag", typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "userName", column = "user_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "locations", column = "locations", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "connects", column = "connects", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "timeout", column = "timeout", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "tenantId", column = "tenant_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "projectName", column = "project_name", javaType = String.class, jdbcType = JdbcType.VARCHAR)
    })
    @SelectProvider(type = ProcessDefinitionMapperProvider.class, method = "queryByDefineName")
    ProcessDefinition queryByDefineName(@Param("projectId") int projectId,
                                        @Param("processDefinitionName") String name);


    /**
     * count definition number
     * @param projectId
     * @param userId
     * @param searchVal
     * @return
     */
    @SelectProvider(type = ProcessDefinitionMapperProvider.class, method = "countDefineNumber")
    Integer countDefineNumber(@Param("projectId") int projectId,
                              @Param("userId") int userId,
                              @Param("searchVal") String searchVal
    );

    /**
     * query all definition list
     * @param projectId
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
    @SelectProvider(type = ProcessDefinitionMapperProvider.class, method = "queryAllDefinitionList")
    List<ProcessDefinition> queryAllDefinitionList(@Param("projectId") int projectId);

    /**
     * query definition list paging
     * @param projectId
     * @param searchVal
     * @param userId
     * @param offset
     * @param pageSize
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
            @Result(property = "scheduleReleaseState", column = "schedule_release_state",  typeHandler = EnumOrdinalTypeHandler.class, javaType = ReleaseState.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "timeout", column = "timeout", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "tenantId", column = "tenant_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "projectName", column = "project_name", javaType = String.class, jdbcType = JdbcType.VARCHAR)
    })
    @SelectProvider(type = ProcessDefinitionMapperProvider.class, method = "queryDefineListPaging")
    List<ProcessDefinition> queryDefineListPaging(@Param("projectId") int projectId,
                                                  @Param("searchVal") String searchVal,
                                                  @Param("userId") Integer userId,
                                                  @Param("offset") int offset,
                                                  @Param("pageSize") int pageSize);

    /**
     * query definition list by define id list
     * @param ids
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "version", column = "version", javaType = Integer.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "releaseState", column = "release_state",  typeHandler = EnumOrdinalTypeHandler.class, javaType = ReleaseState.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "projectId", column = "project_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "userId", column = "user_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processDefinitionJson", column = "process_definition_json", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "flag", column = "flag", typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "locations", column = "locations", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "connects", column = "connects", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userName", column = "user_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "timeout", column = "timeout", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "tenantId", column = "tenant_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "projectName", column = "project_name", javaType = String.class, jdbcType = JdbcType.VARCHAR)
    })
    @SelectProvider(type = ProcessDefinitionMapperProvider.class, method = "queryDefinitionListByIdList")
    List<ProcessDefinition> queryDefinitionListByIdList(@Param("ids") List<String> ids);

    /**
     * count definition number group by users
     * @param userId
     * @param userType
     * @param projectId
     * @return
     */
    @Results(value = {
            @Result(property = "userId", column = "user_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "userName", column = "user_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "count", column = "count", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
    })
    @SelectProvider(type = ProcessDefinitionMapperProvider.class, method = "countDefinitionGroupByUser")
    List<DefinitionGroupByUser> countDefinitionGroupByUser(
            @Param("userId") Integer userId,
            @Param("userType") UserType userType,
            @Param("projectId") Integer projectId);

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
