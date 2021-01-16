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
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.dolphinscheduler.common.enums.ConditionType;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * process task relation
 */
@TableName("t_ds_process_task_relation")
public class ProcessTaskRelationLog {

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
     * version
     */
    private int version;

    /**
     * project code
     */
    private long projectCode;

    /**
     * process code
     */
    private long processDefinitionCode;

    /**
     * pre project code
     */
    private long preProjectCode;

    /**
     * pre task code
     */
    private long preTaskCode;

    /**
     * post project code
     */
    private long postProjectCode;

    /**
     * post task code
     */
    private long postTaskCode;

    /**
     * condition type
     */
    private ConditionType conditionType;

    /**
     * condition parameters
     */
    private String conditionParams;

    /**
     * condition parameter list
     */
    @TableField(exist = false)
    private List<Property> conditionParamList;

    /**
     * condition parameter map
     */
    @TableField(exist = false)
    private Map<String, String> conditionParamMap;

    /**
     * operator user id
     */
    private int operator;

    /**
     * operate time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date operateTime;

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
        if (conditionParams == null) {
            this.conditionParamList = new ArrayList<>();
        } else {
            this.conditionParamList = JSONUtils.toList(conditionParams, Property.class);
        }
        this.conditionParams = conditionParams;
    }

    public List<Property> getConditionParamList() {
        return conditionParamList;
    }

    public void setConditionParamList(List<Property> conditionParamList) {
        this.conditionParams = JSONUtils.toJsonString(conditionParamList);
        this.conditionParamList = conditionParamList;
    }

    public Map<String, String> getConditionParamMap() {
        if (conditionParamMap == null && StringUtils.isNotEmpty(conditionParams)) {
            List<Property> propList = JSONUtils.toList(conditionParams, Property.class);
            conditionParamMap = propList.stream().collect(Collectors.toMap(Property::getProp, Property::getValue));
        }

        return conditionParamMap;
    }

    public void setConditionParamMap(Map<String, String> conditionParamMap) {
        this.conditionParamMap = conditionParamMap;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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

    public long getPreProjectCode() {
        return preProjectCode;
    }

    public void setPreProjectCode(long preProjectCode) {
        this.preProjectCode = preProjectCode;
    }

    public long getPreTaskCode() {
        return preTaskCode;
    }

    public void setPreTaskCode(long preTaskCode) {
        this.preTaskCode = preTaskCode;
    }

    public long getPostProjectCode() {
        return postProjectCode;
    }

    public void setPostProjectCode(long postProjectCode) {
        this.postProjectCode = postProjectCode;
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

    public int getOperator() {
        return operator;
    }

    public void setOperator(int operator) {
        this.operator = operator;
    }

    public Date getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }

    @Override
    public String toString() {
        return "ProcessTaskRelationLog{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", version=" + version +
                ", projectCode=" + projectCode +
                ", processDefinitionCode=" + processDefinitionCode +
                ", preProjectCode=" + preProjectCode +
                ", preTaskCode=" + preTaskCode +
                ", postProjectCode=" + postProjectCode +
                ", postTaskCode=" + postTaskCode +
                ", conditionType=" + conditionType +
                ", conditionParams='" + conditionParams + '\'' +
                ", conditionParamList=" + conditionParamList +
                ", conditionParamMap=" + conditionParamMap +
                ", operator=" + operator +
                ", operateTime=" + operateTime +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
