package org.apache.dolphinscheduler.api.utils.relation_sort;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SubProcessParameters;

public class SubRelationDefinitionUtils {

    public static Long getProcessDefinitionCode(String taskParams) {
        SubProcessParameters subProcessParameters = JSONUtils.parseObject(taskParams, SubProcessParameters.class);
        return subProcessParameters.getProcessDefinitionCode();
    }


    public static String updateProcessDefinitionCode(String taskParams, long newProcessDefinitionCode) {
        SubProcessParameters subProcessParameters = JSONUtils.parseObject(taskParams, SubProcessParameters.class);
        subProcessParameters.setProcessDefinitionCode(newProcessDefinitionCode);
        taskParams = JSONUtils.toJsonString(subProcessParameters);
        return taskParams;
    }

}
