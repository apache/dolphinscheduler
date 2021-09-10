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

import org.apache.dolphinscheduler.common.enums.ConditionType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.util.Date;
import java.util.Objects;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * process task relation
 */
@TableName("t_ds_process_task_relation")
public class ProcessTaskRelation {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    /**
     * name
     */
    private String name;

    /**
     * process version
     */
    private int processDefinitionVersion;

    /**
     * project code
     */
    private long projectCode;

    /**
     * process code
     */
    private long processDefinitionCode;

    /**
     * pre task code
     */
    private long preTaskCode;

    /**
     * pre node version
     */
    private int preTaskVersion;

    /**
     * post task code
     */
    private long postTaskCode;

    /**
     * post node version
     */
    private int postTaskVersion;

    /**
     * condition type
     */
    private ConditionType conditionType;

    /**
     * condition parameters
     */
    @JsonDeserialize(using = JSONUtils.JsonDataDeserializer.class)
    @JsonSerialize(using = JSONUtils.JsonDataSerializer.class)
    private String conditionParams;

    /**
     * create time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * update time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    public ProcessTaskRelation() {
    }

    public ProcessTaskRelation(String name,
                               int processDefinitionVersion,
                               long projectCode,
                               long processDefinitionCode,
                               long preTaskCode,
                               int preTaskVersion,
                               long postTaskCode,
                               int postTaskVersion,
                               ConditionType conditionType,
                               String conditionParams,
                               Date createTime,
                               Date updateTime) {
        this.name = name;
        this.processDefinitionVersion = processDefinitionVersion;
        this.projectCode = projectCode;
        this.processDefinitionCode = processDefinitionCode;
        this.preTaskCode = preTaskCode;
        this.preTaskVersion = preTaskVersion;
        this.postTaskCode = postTaskCode;
        this.postTaskVersion = postTaskVersion;
        this.conditionType = conditionType;
        this.conditionParams = conditionParams;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getConditionParams() {
        return conditionParams;
    }

    public void setConditionParams(String conditionParams) {
        this.conditionParams = conditionParams;
    }

    public int getProcessDefinitionVersion() {
        return processDefinitionVersion;
    }

    public void setProcessDefinitionVersion(int processDefinitionVersion) {
        this.processDefinitionVersion = processDefinitionVersion;
    }

    public long getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(long projectCode) {
        this.projectCode = projectCode;
    }

    public long getProcessDefinitionCode() {
        return processDefinitionCode;
    }

    public void setProcessDefinitionCode(long processDefinitionCode) {
        this.processDefinitionCode = processDefinitionCode;
    }

    public long getPreTaskCode() {
        return preTaskCode;
    }

    public void setPreTaskCode(long preTaskCode) {
        this.preTaskCode = preTaskCode;
    }

    public long getPostTaskCode() {
        return postTaskCode;
    }

    public void setPostTaskCode(long postTaskCode) {
        this.postTaskCode = postTaskCode;
    }

    public ConditionType getConditionType() {
        return conditionType;
    }

    public void setConditionType(ConditionType conditionType) {
        this.conditionType = conditionType;
    }

    public int getPreTaskVersion() {
        return preTaskVersion;
    }

    public void setPreTaskVersion(int preTaskVersion) {
        this.preTaskVersion = preTaskVersion;
    }

    public int getPostTaskVersion() {
        return postTaskVersion;
    }

    public void setPostTaskVersion(int postTaskVersion) {
        this.postTaskVersion = postTaskVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProcessTaskRelation that = (ProcessTaskRelation) o;
        return processDefinitionVersion == that.processDefinitionVersion
            && projectCode == that.projectCode
            && processDefinitionCode == that.processDefinitionCode
            && preTaskCode == that.preTaskCode
            && preTaskVersion == that.preTaskVersion
            && postTaskCode == that.postTaskCode
            && postTaskVersion == that.postTaskVersion
            && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, processDefinitionVersion, projectCode, processDefinitionCode, preTaskCode, preTaskVersion, postTaskCode, postTaskVersion);
    }

    @Override
    public String toString() {
        return "ProcessTaskRelation{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", processDefinitionVersion=" + processDefinitionVersion
            + ", projectCode=" + projectCode
            + ", processDefinitionCode=" + processDefinitionCode
            + ", preTaskCode=" + preTaskCode
            + ", preTaskVersion=" + preTaskVersion
            + ", postTaskCode=" + postTaskCode
            + ", postTaskVersion=" + postTaskVersion
            + ", conditionType=" + conditionType
            + ", conditionParams='" + conditionParams + '\''
            + ", createTime=" + createTime
            + ", updateTime=" + updateTime
            + '}';
    }
}
