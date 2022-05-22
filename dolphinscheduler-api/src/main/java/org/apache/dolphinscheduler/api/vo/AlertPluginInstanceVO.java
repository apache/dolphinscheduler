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

package org.apache.dolphinscheduler.api.vo;

import java.util.Date;

/**
 * AlertPluginInstanceVO
 */
public class AlertPluginInstanceVO {

    /**
     * id
     */
    private int id;

    /**
     * plugin_define_id
     */
    private int pluginDefineId;

    /**
     * alert plugin instance name
     */
    private String instanceName;

    /**
     * plugin_instance_params
     */
    private String pluginInstanceParams;

    /**
     * create_time
     */
    private Date createTime;

    /**
     * update_time
     */
    private Date updateTime;

    /**
     * alert plugin name
     */
    private String alertPluginName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPluginDefineId() {
        return pluginDefineId;
    }

    public void setPluginDefineId(int pluginDefineId) {
        this.pluginDefineId = pluginDefineId;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getPluginInstanceParams() {
        return pluginInstanceParams;
    }

    public void setPluginInstanceParams(String pluginInstanceParams) {
        this.pluginInstanceParams = pluginInstanceParams;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getAlertPluginName() {
        return alertPluginName;
    }

    public void setAlertPluginName(String alertPluginName) {
        this.alertPluginName = alertPluginName;
    }
}
