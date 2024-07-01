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

package org.apache.dolphinscheduler.dao.entity;

import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.WarningType;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_ds_alert")
public class Alert {

    /**
     * primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * sign
     */
    @TableField(value = "sign")
    private String sign;
    /**
     * title
     */
    @TableField(value = "title")
    private String title;

    /**
     * content
     */
    @TableField(value = "content")
    private String content;

    /**
     * alert_status
     */
    @TableField(value = "alert_status")
    private AlertStatus alertStatus;

    /**
     * warning_type
     */
    @TableField(value = "warning_type")
    private WarningType warningType;

    /**
     * log
     */
    @TableField(value = "log")
    private String log;

    /**
     * alertgroup_id
     */
    @TableField("alertgroup_id")
    private Integer alertGroupId;

    /**
     * create_time
     */
    @TableField("create_time")
    private Date createTime;
    /**
     * update_time
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * project_code
     */
    @TableField("project_code")
    private Long projectCode;

    /**
     * process_definition_code
     */
    @TableField("process_definition_code")
    private Long processDefinitionCode;

    /**
     * process_instance_id
     */
    @TableField("process_instance_id")
    private Integer processInstanceId;

    /**
     * alert_type
     */
    @TableField("alert_type")
    private AlertType alertType;

    @TableField(exist = false)
    private Map<String, Object> info = new HashMap<>();

}
