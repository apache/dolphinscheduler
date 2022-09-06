package org.apache.dolphinscheduler.plugin.task.datasync;

import com.google.auto.service.AutoService;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannelFactory;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;

import java.util.Collections;
import java.util.List;

@AutoService(TaskChannelFactory.class)
public class DatasyncTaskChannelFactory implements TaskChannelFactory {

    @Override
    public TaskChannel create() {
        return new DatasyncTaskChannel();
    }

    @Override
    public String getName() {
        return "Datasync";
    }

    @Override
    public List<PluginParams> getParams() {
        return Collections.emptyList();
    }
}