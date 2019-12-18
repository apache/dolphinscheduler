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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.ShowType;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * alert
 */
@Data
@Builder
@TableName("t_ds_alert")
@ToString
@NoArgsConstructor
public class Alert {
    @TableId(value = "id", type = IdType.AUTO)
    private int id;
    private String title;
    private ShowType showType;
    private String content;
    private AlertType alertType;
    private AlertStatus alertStatus;
    private String log;
    @TableField("alertgroup_id")
    private int alertGroupId;
    private String receivers;
    private String receiversCc;
    private Date createTime;
    private Date updateTime;
    @TableField(exist = false)
    private Map<String, Object> info = new HashMap<>();

    public Alert(int id, String title) {
        this.id = id;
        this.title = title;
    }
}
