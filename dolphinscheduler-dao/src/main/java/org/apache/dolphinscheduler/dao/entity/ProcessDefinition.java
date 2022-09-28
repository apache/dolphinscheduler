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
import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
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
@TableName("t_ds_process_definition")
public class ProcessDefinition {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * code
     */
    private long code;

    /**
     * name
     */
    private String name;

    /**
     * version
     */
    private int version;

    /**
     * release state : online/offline
     */
    private ReleaseState releaseState;

    /**
     * project code
     */
    private long projectCode;

    /**
     * description
     */
    private String description;

    /**
     * user defined parameters
     */
    private String globalParams;

    /**
     * user defined parameter list
     */
    @TableField(exist = false)
    private List<Property> globalParamList;

    /**
     * user define parameter map
     */
    @TableField(exist = false)
    private Map<String, String> globalParamMap;

    /**
     * create time
     */
    private Date createTime;

    /**
     * update time
     */
    private Date updateTime;

    /**
     * process is valid: yes/no
     */
    private Flag flag;

    /**
     * process user id
     */
    private int userId;

    /**
     * user name
     */
    @TableField(exist = false)
    private String userName;

    /**
     * project name
     */
    @TableField(exist = false)
    private String projectName;

    /**
     * locations array for web
     */
    private String locations;

    /**
     * schedule release state : online/offline
     */
    @TableField(exist = false)
    private ReleaseState scheduleReleaseState;

    /**
     * process warning time out. unit: minute
     */
    private int timeout;

    /**
     * tenant id
     */
    private int tenantId;

    /**
     * tenant code
     */
    @TableField(exist = false)
    private String tenantCode;

    /**
     * modify user name
     */
    @TableField(exist = false)
    private String modifyBy;

    /**
     * warningGroupId
     */
    @TableField(exist = false)
    private Integer warningGroupId;

    /**
     * execution type
     */
    private ProcessExecutionTypeEnum executionType;

    public ProcessDefinition(long projectCode,
                             String name,
                             long code,
                             String description,
                             String globalParams,
                             String locations,
                             int timeout,
                             int userId,
                             int tenantId) {
        set(projectCode, name, description, globalParams, locations, timeout, tenantId);
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
                    int timeout,
                    int tenantId) {
        this.projectCode = projectCode;
        this.name = name;
        this.description = description;
        this.globalParams = globalParams;
        this.locations = locations;
        this.timeout = timeout;
        this.tenantId = tenantId;
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
