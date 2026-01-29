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
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionTypeEnum;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.google.common.base.Strings;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_ds_workflow_definition")
public class WorkflowDefinition {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private long code;

    private String name;

    private int version;

    private ReleaseState releaseState;

    private long projectCode;

    private String description;

    private String globalParams;

    @TableField(exist = false)
    private List<Property> globalParamList;

    @TableField(exist = false)
    private Map<String, String> globalParamMap;

    private Date createTime;

    private Date updateTime;

    private Flag flag;

    private int userId;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String projectName;

    private String locations;

    @TableField(exist = false)
    private ReleaseState scheduleReleaseState;

    @TableField(exist = false)
    private Schedule schedule;

    private int timeout;

    @TableField(exist = false)
    private String modifyBy;

    @TableField(exist = false)
    private Integer warningGroupId;

    private WorkflowExecutionTypeEnum executionType;

    public WorkflowDefinition(long projectCode,
                              String name,
                              long code,
                              String description,
                              String globalParams,
                              String locations,
                              int timeout,
                              int userId) {
        set(projectCode, name, description, globalParams, locations, timeout);
        this.code = code;
        this.userId = userId;
        Date date = new Date();
        this.createTime = date;
        this.updateTime = date;
    }

    public void set(long projectCode,
                    String name,
                    String description,
                    String globalParams,
                    String locations,
                    int timeout) {
        this.projectCode = projectCode;
        this.name = name;
        this.description = description;
        this.globalParams = globalParams;
        this.locations = locations;
        this.timeout = timeout;
        this.flag = Flag.YES;
    }

    public void setGlobalParams(String globalParams) {
        this.globalParamList = JSONUtils.toList(globalParams, Property.class);
        if (this.globalParamList == null) {
            this.globalParamList = new ArrayList<>();
        }
        this.globalParams = globalParams;
    }

    public Map<String, String> getGlobalParamMap() {
        if (globalParamMap == null && !Strings.isNullOrEmpty(globalParams)) {
            List<Property> propList = JSONUtils.toList(globalParams, Property.class);
            globalParamMap = propList.stream().collect(Collectors.toMap(Property::getProp, Property::getValue));
        }

        return globalParamMap;
    }
}
