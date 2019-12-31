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

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.process.Property;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@TableName("t_ds_process_definition")
public class ProcessDefinition {
    @TableId(value = "id", type = IdType.AUTO)
    private int id;
    @TableField(value = "name")
    private String name;
    @TableField(value = "version")
    private int version;
    @TableField(value = "release_state")
    private ReleaseState releaseState;
    @TableField(value = "project_id")
    private int projectId;
    @TableField(value = "process_definition_json")
    private String processDefinitionJson;
    @TableField(value = "description")
    private String description;
    @TableField(value = "global_params")
    private String globalParams;
    @TableField(exist = false)
    private List<Property> globalParamList;
    @TableField(exist = false)
    private Map<String, String> globalParamMap;
    @TableField(value = "create_time")
    private Date createTime;
    @TableField(value = "update_time")
    private Date updateTime;
    @TableField(value = "flag")
    private Flag flag;
    @TableField(value = "user_id")
    private int userId;
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String projectName;
    @TableField(value = "locations")
    private String locations;
    @TableField(value = "connects")
    private String connects;
    @TableField(value = "receivers")
    private String receivers;
    @TableField(value = "receivers_cc")
    private String receiversCc;
    @TableField(exist = false)
    private ReleaseState scheduleReleaseState;
    @TableField(value = "timeout")
    private int timeout;
    @TableField(value = "tenant_id")
    private int tenantId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ReleaseState getReleaseState() {
        return releaseState;
    }

    public void setReleaseState(ReleaseState releaseState) {
        this.releaseState = releaseState;
    }

    public String getProcessDefinitionJson() {
        return processDefinitionJson;
    }

    public void setProcessDefinitionJson(String processDefinitionJson) {
        this.processDefinitionJson = processDefinitionJson;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Flag getFlag() {
        return flag;
    }

    public void setFlag(Flag flag) {
        this.flag = flag;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }


    public String getGlobalParams() {
        return globalParams;
    }

    public void setGlobalParams(String globalParams) {
        this.globalParamList = JSONObject.parseArray(globalParams, Property.class);
        this.globalParams = globalParams;
    }

    public List<Property> getGlobalParamList() {
        return globalParamList;
    }

    public void setGlobalParamList(List<Property> globalParamList) {
        this.globalParams = JSONObject.toJSONString(globalParamList);
        this.globalParamList = globalParamList;
    }

    public Map<String, String> getGlobalParamMap() {
        List<Property> propList;

        if (globalParamMap == null && StringUtils.isNotEmpty(globalParams)) {
            propList = JSONObject.parseArray(globalParams, Property.class);
            globalParamMap = propList.stream().collect(Collectors.toMap(Property::getProp, Property::getValue));
        }

        return globalParamMap;
    }

    public void setGlobalParamMap(Map<String, String> globalParamMap) {
        this.globalParamMap = globalParamMap;
    }

    public String getLocations() {
        return locations;
    }

    public void setLocations(String locations) {
        this.locations = locations;
    }

    public String getConnects() {
        return connects;
    }

    public void setConnects(String connects) {
        this.connects = connects;
    }

    public String getReceivers() {
        return receivers;
    }

    public void setReceivers(String receivers) {
        this.receivers = receivers;
    }

    public String getReceiversCc() {
        return receiversCc;
    }

    public void setReceiversCc(String receiversCc) {
        this.receiversCc = receiversCc;
    }

    public ReleaseState getScheduleReleaseState() {
        return scheduleReleaseState;
    }

    public void setScheduleReleaseState(ReleaseState scheduleReleaseState) {
        this.scheduleReleaseState = scheduleReleaseState;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ProcessDefinition{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", version=" + version +
                ", releaseState=" + releaseState +
                ", projectId=" + projectId +
                ", processDefinitionJson='" + processDefinitionJson + '\'' +
                ", globalParams='" + globalParams + '\'' +
                ", globalParamList=" + globalParamList +
                ", globalParamMap=" + globalParamMap +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", flag=" + flag +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", projectName='" + projectName + '\'' +
                ", locations='" + locations + '\'' +
                ", connects='" + connects + '\'' +
                ", receivers='" + receivers + '\'' +
                ", receiversCc='" + receiversCc + '\'' +
                ", scheduleReleaseState=" + scheduleReleaseState +
                ", timeout=" + timeout +
                ", tenantId=" + tenantId +
                '}';
    }
}
