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

package org.apache.dolphinscheduler.dao.entity.event;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ListenerEventType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionTypeEnum;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessDefinitionCreatedListenerEvent implements AbstractListenerEvent {

    /**
     * id
     */
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
    private List<Property> globalParamList;

    /**
     * user define parameter map
     */
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
     * create user name
     */
    private String userName;

    /**
     * project name
     */
    private String projectName;

    /**
     * locations array for web
     */
    private String locations;

    /**
     * schedule release state : online/offline
     */
    private ReleaseState scheduleReleaseState;

    /**
     * process warning time out. unit: minute
     */
    private int timeout;

    /**
     * modify user name
     */
    private String modifyBy;

    /**
     * warningGroupId
     */
    private Integer warningGroupId;

    /**
     * execution type
     */
    private WorkflowExecutionTypeEnum executionType;

    /**
     * task definitions
     */
    List<TaskDefinitionLog> taskDefinitionLogs;

    /**
     *
     */
    List<WorkflowTaskRelationLog> taskRelationList;

    public ProcessDefinitionCreatedListenerEvent(WorkflowDefinition workflowDefinition) {
        this.setId(workflowDefinition.getId());
        this.setCode(workflowDefinition.getCode());
        this.setName(workflowDefinition.getName());
        this.setVersion(workflowDefinition.getVersion());
        this.setReleaseState(workflowDefinition.getReleaseState());
        this.setProjectCode(workflowDefinition.getProjectCode());
        this.setDescription(workflowDefinition.getDescription());
        this.setGlobalParams(workflowDefinition.getGlobalParams());
        this.setGlobalParamList(workflowDefinition.getGlobalParamList());
        this.setGlobalParamMap(workflowDefinition.getGlobalParamMap());
        this.setCreateTime(workflowDefinition.getCreateTime());
        this.setUpdateTime(workflowDefinition.getUpdateTime());
        this.setFlag(workflowDefinition.getFlag());
        this.setUserId(workflowDefinition.getUserId());
        this.setUserName(workflowDefinition.getUserName());
        this.setProjectName(workflowDefinition.getProjectName());
        this.setLocations(workflowDefinition.getLocations());
        this.setScheduleReleaseState(workflowDefinition.getScheduleReleaseState());
        this.setTimeout(workflowDefinition.getTimeout());
        this.setModifyBy(workflowDefinition.getModifyBy());
        this.setWarningGroupId(workflowDefinition.getWarningGroupId());
        this.setExecutionType(workflowDefinition.getExecutionType());
    }
    @Override
    public ListenerEventType getEventType() {
        return ListenerEventType.PROCESS_DEFINITION_CREATED;
    }

    @Override
    public String getTitle() {
        return String.format("process definition created:%s", this.name);
    }
}
