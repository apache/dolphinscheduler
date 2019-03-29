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

import cn.escheduler.common.enums.AlertStatus;
import cn.escheduler.common.enums.AlertType;
import cn.escheduler.common.enums.ShowType;
import cn.escheduler.dao.model.Alert;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public interface AlertMapper {

    /**
     * insert alert information
     * @param alert
     * @return
     */
    @InsertProvider(type = AlertMapperProvider.class, method = "insert")
    @Options(useGeneratedKeys = true,keyProperty = "alert.id")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "alert.id", before = false, resultType = int.class)
    int insert(@Param("alert") Alert alert);


    /**
     * query alert list by status
     * @param alertStatus
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
        @Result(property = "title", column = "title", javaType = String.class, jdbcType = JdbcType.VARCHAR),
        @Result(property = "showType", column = "show_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = ShowType.class, jdbcType = JdbcType.TINYINT),
        @Result(property = "content", column = "content", javaType = String.class, jdbcType = JdbcType.VARCHAR),
        @Result(property = "alertType", column = "alert_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = AlertType.class, jdbcType = JdbcType.TINYINT),
        @Result(property = "alertStatus", column = "alert_status", typeHandler = EnumOrdinalTypeHandler.class, javaType = AlertStatus.class, jdbcType = JdbcType.TINYINT),
        @Result(property = "log", column = "log", javaType = String.class, jdbcType = JdbcType.VARCHAR),
        @Result(property = "alertGroupId", column = "alertgroup_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
        @Result(property = "receivers", column = "receivers", javaType = String.class, jdbcType = JdbcType.VARCHAR),
        @Result(property = "receiversCc", column = "receivers_cc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
        @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
        @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = AlertMapperProvider.class, method = "queryAlertByStatus")
    List<Alert> queryAlertByStatus(@Param("alertStatus") AlertStatus alertStatus);


    /**
     * update alert information
     * @param alertStatus
     * @param log
     * @param updateTime
     * @param id
     * @return
     */
    @UpdateProvider(type = AlertMapperProvider.class, method = "update")
    int update(@Param("alertStatus") AlertStatus alertStatus,@Param("log") String log,
               @Param("updateTime") Date updateTime,@Param("id") int id);

    /**
     * delete by alert id
     * @param alertId
     * @return
     */
    @UpdateProvider(type = AlertMapperProvider.class, method = "deleteById")
    int delete(@Param("alertId") int alertId);

    /**
     * list alert information by field alertStatus
     * @param alertStatus
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "title", column = "title", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "showType", column = "show_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = ShowType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "content", column = "content", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "alertType", column = "alert_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = AlertType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "alertStatus", column = "alert_status", typeHandler = EnumOrdinalTypeHandler.class, javaType = AlertStatus.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "log", column = "log", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "alertGroupId", column = "alertgroup_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "receivers", column = "receivers", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "receiversCc", column = "receivers_cc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = AlertMapperProvider.class, method = "listAlertByStatus")
    List<Alert> listAlertByStatus(@Param("alertStatus") AlertStatus alertStatus);

}
