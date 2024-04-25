package org.apache.dolphinscheduler.plugin.task.aliyunserverlessspark;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ParametersNode;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;

public class AliyunServerlessSparkTaskChannel implements TaskChannel {
    @Override
    public void cancelApplication(boolean status) {

    }

    @Override
    public AbstractTask createTask(TaskExecutionContext taskRequest) {
        return new AliyunServerlessSparkTask(taskRequest);
    }

    @Override
    public AbstractParameters parseParameters(ParametersNode parametersNode) {
        return JSONUtils.parseObject(parametersNode.getTaskParams(), AliyunServerlessSparkParameters.class);
    }

    @Override
    public ResourceParametersHelper getResources(String parameters) {
        return JSONUtils.parseObject(parameters, AliyunServerlessSparkParameters.class).getResources();
    }
}
