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

import lombok.Data;
import lombok.NoArgsConstructor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Data
@NoArgsConstructor
@TableName("t_ds_process_task_relation")
public class ProcessTaskRelation {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

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
    private Date createTime;

    /**
     * update time
     */
    private Date updateTime;

    public ProcessTaskRelation(String name,
                               int processDefinitionVersion,
                               long projectCode,
                               long processDefinitionCode,
                               long preTaskCode,
                               int preTaskVersion,
                               long postTaskCode,
                               int postTaskVersion,
                               ConditionType conditionType,
                               String conditionParams) {
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

        Date now = new Date();
        this.createTime = now;
        this.updateTime = now;
    }

    public ProcessTaskRelation(ProcessTaskRelationLog processTaskRelationLog) {
        this.name = processTaskRelationLog.getName();
        this.processDefinitionVersion = processTaskRelationLog.getProcessDefinitionVersion();
        this.projectCode = processTaskRelationLog.getProjectCode();
        this.processDefinitionCode = processTaskRelationLog.getProcessDefinitionCode();
        this.preTaskCode = processTaskRelationLog.getPreTaskCode();
        this.preTaskVersion = processTaskRelationLog.getPreTaskVersion();
        this.postTaskCode = processTaskRelationLog.getPostTaskCode();
        this.postTaskVersion = processTaskRelationLog.getPostTaskVersion();
        this.conditionType = processTaskRelationLog.getConditionType();
        this.conditionParams = processTaskRelationLog.getConditionParams();

        this.createTime = processTaskRelationLog.getCreateTime();
        this.updateTime = new Date();
    }

}
