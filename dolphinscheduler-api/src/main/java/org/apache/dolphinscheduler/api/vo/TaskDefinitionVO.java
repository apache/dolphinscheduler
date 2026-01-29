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

import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.springframework.beans.BeanUtils;

@EqualsAndHashCode(callSuper = true)
@Data
public class TaskDefinitionVO extends TaskDefinition {

    private List<WorkflowTaskRelation> workflowTaskRelationList;

    public TaskDefinitionVO() {
    }

    public TaskDefinitionVO(List<WorkflowTaskRelation> workflowTaskRelationList) {
        this.workflowTaskRelationList = workflowTaskRelationList;
    }

    public static TaskDefinitionVO fromTaskDefinition(TaskDefinition taskDefinition) {
        TaskDefinitionVO taskDefinitionVo = new TaskDefinitionVO();
        BeanUtils.copyProperties(taskDefinition, taskDefinitionVo);
        if (TimeoutFlag.CLOSE == taskDefinition.getTimeoutFlag()) {
            taskDefinitionVo.setTimeoutNotifyStrategy(null);
        }
        return taskDefinitionVo;
    }

}
