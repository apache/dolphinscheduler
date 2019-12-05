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
import cn.escheduler.common.utils.EnumFieldUtil;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

public class AlertMapperProvider {

    private static final String TABLE_NAME = "t_escheduler_alert";

    /**
     * 插入告警信息
     *
     * @param parameter
     * @return
     */
    public String insert(Map<String, Object> parameter) {
        return new SQL() {
            {
                INSERT_INTO(TABLE_NAME);
                VALUES("`title`", "#{alert.title}");
                VALUES("`show_type`", EnumFieldUtil.genFieldStr("alert.showType", ShowType.class));
                VALUES("`content`", "#{alert.content}");
                VALUES("`alert_type`", EnumFieldUtil.genFieldStr("alert.alertType", AlertType.class));
                VALUES("`alertgroup_id`", "#{alert.alertGroupId}");
                VALUES("`receivers`", "#{alert.receivers}");
                VALUES("`receivers_cc`", "#{alert.receiversCc}");
                VALUES("`create_time`", "#{alert.createTime}");
                VALUES("`update_time`", "#{alert.updateTime}");
            }
        }.toString();
    }

    /**
     * 根据告警状态查询
     * @param parameter
     * @return
     */
    public String queryAlertByStatus(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*");

                FROM(TABLE_NAME);

                WHERE("`alert_status` = "+EnumFieldUtil.genFieldStr("alertStatus", AlertStatus.class));
            }
        }.toString();
    }

    /**
     * delete by id
     * @param parameter
     * @return
     */
    public String deleteById(Map<String, Object> parameter){

        return new SQL() {
            {
                DELETE_FROM(TABLE_NAME);
                WHERE("`id`=#{alertId}");

            }}.toString();
    }

        /**
     * 更新消息信息
     *
     * @param parameter
     * @return
     */
    public String update(Map<String, Object> parameter) {
        return new SQL() {
            {
                UPDATE(TABLE_NAME);
                 SET("`alert_status`="+EnumFieldUtil.genFieldStr("alertStatus", AlertType.class));
                 SET("`log`=#{log}");
                 SET("`update_time`=#{updateTime}");

                  WHERE("`id` = #{id}");
            }
        }.toString();
    }

    /**
     * list alert information by field alertStatus
     * @param parameter
     * @return
     */
    public String listAlertByStatus(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*");

                FROM(TABLE_NAME);

                WHERE("`alert_status` = "+EnumFieldUtil.genFieldStr("alertStatus", AlertStatus.class));
            }
        }.toString();
    }
}
