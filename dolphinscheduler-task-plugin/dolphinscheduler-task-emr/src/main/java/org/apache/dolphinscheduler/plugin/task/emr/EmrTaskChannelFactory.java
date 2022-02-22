package org.apache.dolphinscheduler.plugin.task.emr;

import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.task.TaskChannel;
import org.apache.dolphinscheduler.spi.task.TaskChannelFactory;

import java.util.List;

import com.google.auto.service.AutoService;

@AutoService(TaskChannelFactory.class)
public class EmrTaskChannelFactory implements TaskChannelFactory {
    @Override
    public String getName() {
        return "EMR";
    }

    @Override
    public List<PluginParams> getParams() {
        return null;
    }

    @Override
    public TaskChannel create() {
        return new EmrTaskChannel();
    }
}
