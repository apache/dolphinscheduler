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

import java.util.Date;

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
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_ds_alert_send_status")
public class AlertSendStatus {

    /**
     * primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * alert id
     */
    @TableField(value = "alert_id")
    private int alertId;

    /**
     * alert plugin instance id
     */
    @TableField(value = "alert_plugin_instance_id")
    private int alertPluginInstanceId;

    /**
     * alert send status
     */
    @TableField(value = "send_status")
    private AlertStatus sendStatus;

    /**
     * log
     */
    @TableField(value = "log")
    private String log;

    /**
     * create_time
     */
    @TableField("create_time")
    private Date createTime;

}
