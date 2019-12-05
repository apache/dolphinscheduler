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

import cn.escheduler.common.enums.AlertType;
import cn.escheduler.dao.model.AlertGroup;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.Timestamp;
import java.util.List;

/**
 * alert group mapper
 */
public interface AlertGroupMapper {
    /**
     * insert warning group
     * @param alertGroup
     * @return
     */
    @InsertProvider(type = AlertGroupMapperProvider.class, method = "insert")
    @Options(useGeneratedKeys = true,keyProperty = "alertGroup.id")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "alertGroup.id", before = false, resultType = int.class)
    int insert(@Param("alertGroup") AlertGroup alertGroup);


    /**
     * delete alert group by id
     * @param id
     * @return
     */
    @DeleteProvider(type = AlertGroupMapperProvider.class, method = "delete")
    int  delete(@Param("id") int id);


    /**
     * update alert group information
     * @param alertGroup
     * @return
     */
    @UpdateProvider(type = AlertGroupMapperProvider.class, method = "update")
    int update(@Param("alertGroup") AlertGroup alertGroup);


    /**
     * query alert group by id
     * @param alertGroupId
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "groupName", column = "group_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "groupType", column = "group_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = AlertType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = AlertGroupMapperProvider.class, method = "queryById")
    AlertGroup queryById(@Param("alertGroupId") int alertGroupId);


    /**
     * query all alert group list
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "groupName", column = "group_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "groupType", column = "group_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = AlertType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = AlertGroupMapperProvider.class, method = "queryAllGroupList")
    List<AlertGroup> queryAllGroupList();

    /**
     * count alert group
     * @param searchVal
     * @return
     */
    @SelectProvider(type = AlertGroupMapperProvider.class, method = "countAlertGroupPaging")
    Integer countAlertGroupPaging(@Param("searchVal") String searchVal);

    /**
     * query alert groups paging
     * @param searchVal
     * @param offset
     * @param pageSize
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "groupName", column = "group_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "groupType", column = "group_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = AlertType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = AlertGroupMapperProvider.class, method = "queryAlertGroupPaging")
    List<AlertGroup> queryAlertGroupPaging(@Param("searchVal") String searchVal,
                                           @Param("offset") Integer offset,
                                           @Param("pageSize") Integer pageSize);

    /**
     * query alert group by user id
     * @param userId
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "groupName", column = "group_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "groupType", column = "group_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = AlertType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = AlertGroupMapperProvider.class, method = "queryByUserId")
    List<AlertGroup> queryByUserId(@Param("userId") int userId);



    /**
     * query alert group by name
     * @param groupName
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "groupName", column = "group_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "groupType", column = "group_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = AlertType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = AlertGroupMapperProvider.class, method = "queryByGroupName")
    AlertGroup queryByGroupName(@Param("groupName") String groupName);
}
