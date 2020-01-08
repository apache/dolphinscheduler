package org.apache.dolphinscheduler.api.utils.exportprocess;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @ClassName DependentParam
 */
public class DependentParam implements exportProcessAddTaskParam, InitializingBean {


    @Autowired
    ProcessDefinitionMapper processDefineMapper;

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

    @Override
    public void afterPropertiesSet() {
        TaskNodeParamFactory.register(TaskType.DEPENDENT.name(), this);
    }
}
