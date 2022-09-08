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

import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("t_ds_alert_plugin_instance")
public class AlertPluginInstance {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * plugin_define_id
     */
    @TableField(value = "plugin_define_id", updateStrategy = FieldStrategy.NEVER)
    private int pluginDefineId;

    /**
     * alert plugin instance name
     */
    @TableField("instance_name")
    private String instanceName;

    /**
     * plugin_instance_params
     */
    @TableField("plugin_instance_params")
    private String pluginInstanceParams;

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

    public AlertPluginInstance() {
        this.createTime = new Date();
        this.updateTime = new Date();
    }

    public AlertPluginInstance(int pluginDefineId, String pluginInstanceParams, String instanceName) {
        this.pluginDefineId = pluginDefineId;
        this.pluginInstanceParams = pluginInstanceParams;
        this.createTime = new Date();
        this.updateTime = new Date();
        this.instanceName = instanceName;
    }

    public AlertPluginInstance(int id, String pluginInstanceParams, String instanceName, Date updateDate) {
        this.id = id;
        this.pluginInstanceParams = pluginInstanceParams;
        this.updateTime = updateDate;
        this.instanceName = instanceName;
    }
}
