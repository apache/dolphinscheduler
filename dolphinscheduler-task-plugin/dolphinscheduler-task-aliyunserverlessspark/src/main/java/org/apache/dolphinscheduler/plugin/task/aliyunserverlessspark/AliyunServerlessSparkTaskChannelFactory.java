package org.apache.dolphinscheduler.plugin.task.aliyunserverlessspark;

import org.apache.dolphinscheduler.plugin.task.api.TaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannelFactory;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;

import java.util.List;

import com.google.auto.service.AutoService;

@AutoService(TaskChannelFactory.class)
public class AliyunServerlessSparkTaskChannelFactory implements TaskChannelFactory {
    @Override
    public String getName() {
        return "ALIYUN_SERVERLESS_SPARK";
    }

    @Override
    public List<PluginParams> getParams() {
        return null;
    }

    @Override
    public TaskChannel create() {
        return new AliyunServerlessSparkTaskChannel();
    }
}
