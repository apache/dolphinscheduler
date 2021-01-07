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
package org.apache.dolphinscheduler.api.utils.exportprocess;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.utils.*;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * task node add dependent param strategy
 */
@Service
public class DependentParam implements ProcessAddTaskParam, InitializingBean {

    private static final String DEPENDENCE = "dependence";

    @Autowired
    ProcessDefinitionMapper processDefineMapper;

    @Autowired
    ProjectMapper projectMapper;

    /**
     * add dependent param
     * @param taskNode task node json object
     * @return task node json object
     */
    @Override
    public JsonNode addExportSpecialParam(JsonNode taskNode) {
        // add dependent param
        ObjectNode dependentParameters = JSONUtils.parseObject(taskNode.path(DEPENDENCE).asText());

        if (null != dependentParameters) {
            ArrayNode dependTaskList = (ArrayNode) dependentParameters.get("dependTaskList");
            for (int j = 0; j < dependTaskList.size(); j++) {
                JsonNode dependentTaskModel = dependTaskList.path(j);
                ArrayNode dependItemList = (ArrayNode) dependentTaskModel.get("dependItemList");
                for (int k = 0; k < dependItemList.size(); k++) {
                    ObjectNode dependentItem = (ObjectNode) dependItemList.path(k);
                    int definitionId = dependentItem.path("definitionId").asInt();
                    ProcessDefinition definition = processDefineMapper.queryByDefineId(definitionId);
                    if (null != definition) {
                        dependentItem.put("projectName", definition.getProjectName());
                        dependentItem.put("definitionName", definition.getName());
                    }
                }
            }
            ((ObjectNode)taskNode).set(DEPENDENCE, dependentParameters);
        }

        return taskNode;
    }

    /**
     * import process add dependent param
     * @param taskNode task node json object
     * @return
     */
    @Override
    public JsonNode addImportSpecialParam(JsonNode taskNode) {
        ObjectNode dependentParameters =  JSONUtils.parseObject(taskNode.path(DEPENDENCE).asText());
        if(dependentParameters != null){
            ArrayNode dependTaskList = (ArrayNode) dependentParameters.path("dependTaskList");
            for (int h = 0; h < dependTaskList.size(); h++) {
                ObjectNode dependentTaskModel = (ObjectNode) dependTaskList.path(h);
                ArrayNode dependItemList = (ArrayNode) dependentTaskModel.get("dependItemList");
                for (int k = 0; k < dependItemList.size(); k++) {
                    ObjectNode dependentItem = (ObjectNode) dependItemList.path(k);
                    Project dependentItemProject = projectMapper.queryByName(dependentItem.path("projectName").asText());
                    if(dependentItemProject != null){
                        ProcessDefinition definition = processDefineMapper.queryByDefineName(dependentItemProject.getId(),dependentItem.path("definitionName").asText());
                        if(definition != null){
                            dependentItem.put("projectId",dependentItemProject.getId());
                            dependentItem.put("definitionId",definition.getId());
                        }
                    }
                }
            }
            ((ObjectNode)taskNode).set(DEPENDENCE, dependentParameters);
        }
        return taskNode;
    }

    /**
     * put dependent strategy
     */
    @Override
    public void afterPropertiesSet() {
        TaskNodeParamFactory.register(TaskType.DEPENDENT.name(), this);
    }
}
