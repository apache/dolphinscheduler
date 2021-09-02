package org.apache.dolphinscheduler.plugin.task.http;

import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.task.TaskChannel;
import org.apache.dolphinscheduler.spi.task.TaskChannelFactory;

import java.util.List;

public class HttpTaskChannelFactory implements TaskChannelFactory {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public List<PluginParams> getParams() {
        return null;
    }

    @Override
    public TaskChannel create() {
        return null;
    }
}
