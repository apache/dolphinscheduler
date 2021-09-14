package org.apache.dolphinscheduler.plugin.task.sql;

import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.task.TaskChannel;
import org.apache.dolphinscheduler.spi.task.TaskChannelFactory;

import java.util.List;

public class SqlTaskChannelFactory implements TaskChannelFactory {
    @Override
    public String getName() {
        return "SQL";
    }

    @Override
    public List<PluginParams> getParams() {
        return null;
    }

    @Override
    public TaskChannel create() {
        return new SqlTaskChannel();
    }
}
