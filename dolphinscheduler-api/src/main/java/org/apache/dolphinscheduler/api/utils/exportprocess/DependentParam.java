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
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * task node add dependent param strategy
 */
@Service
public class DependentParam implements exportProcessAddTaskParam, InitializingBean {


    @Autowired
    ProcessDefinitionMapper processDefineMapper;

    /**
     * add dependent param
     * @param taskNode task node json object
     * @return task node json object
     */
    @Override
    public JSONObject addSpecialParam(JSONObject taskNode) {
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
     * put dependent strategy
     */
    @Override
    public void afterPropertiesSet() {
        TaskNodeParamFactory.register(TaskType.DEPENDENT.name(), this);
    }
}
