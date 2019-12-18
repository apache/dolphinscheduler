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

import lombok.ToString;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.process.Property;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * process definition
 */
@Data
@ToString
@TableName("t_ds_process_definition")
public class ProcessDefinition {
    @TableId(value = "id", type = IdType.AUTO)
    private int id;
    private String name;
    private int version;
    private ReleaseState releaseState;
    private int projectId;
    private String processDefinitionJson;
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
    private String connects;
    private String receivers;
    private String receiversCc;
    @TableField(exist = false)
    private ReleaseState scheduleReleaseState;
    private int timeout;
    private int tenantId;

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
}
