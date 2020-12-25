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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
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
    public JSONObject addExportSpecialParam(JSONObject taskNode) {
        // add dependent param
        JSONObject dependentParameters = JSONUtils.parseObject(taskNode.getString("dependence"));

        if (null != dependentParameters) {
            JSONArray dependTaskList = (JSONArray) dependentParameters.get("dependTaskList");
            for (int j = 0; j < dependTaskList.size(); j++) {
                JSONObject dependentTaskModel = dependTaskList.getJSONObject(j);
                JSONArray dependItemList = (JSONArray) dependentTaskModel.get("dependItemList");
                for (int k = 0; k < dependItemList.size(); k++) {
                    JSONObject dependentItem = dependItemList.getJSONObject(k);
                    int definitionId = dependentItem.getInteger("definitionId");
                    ProcessDefinition definition = processDefineMapper.queryByDefineId(definitionId);
                    if (null != definition) {
                        dependentItem.put("projectName", definition.getProjectName());
                        dependentItem.put("definitionName", definition.getName());
                    }
                }
            }
            taskNode.put("dependence", dependentParameters);
        }

        return taskNode;
    }

    /**
     * import process add dependent param
     * @param taskNode task node json object
     * @return
     */
    @Override
    public JSONObject addImportSpecialParam(JSONObject taskNode) {
        JSONObject dependentParameters =  JSONUtils.parseObject(taskNode.getString("dependence"));
        if(dependentParameters != null){
            JSONArray dependTaskList = (JSONArray) dependentParameters.get("dependTaskList");
            for (int h = 0; h < dependTaskList.size(); h++) {
                JSONObject dependentTaskModel = dependTaskList.getJSONObject(h);
                JSONArray dependItemList = (JSONArray) dependentTaskModel.get("dependItemList");
                for (int k = 0; k < dependItemList.size(); k++) {
                    JSONObject dependentItem = dependItemList.getJSONObject(k);
                    Project dependentItemProject = projectMapper.queryByName(dependentItem.getString("projectName"));
                    if(dependentItemProject != null){
                        ProcessDefinition definition = processDefineMapper.queryByDefineName(dependentItemProject.getId(),dependentItem.getString("definitionName"));
                        if(definition != null){
                            dependentItem.put("projectId",dependentItemProject.getId());
                            dependentItem.put("definitionId",definition.getId());
                        }
                    }
                }
            }
            taskNode.put("dependence", dependentParameters);
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
