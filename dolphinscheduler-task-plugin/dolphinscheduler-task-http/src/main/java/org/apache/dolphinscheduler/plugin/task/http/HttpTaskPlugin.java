package org.apache.dolphinscheduler.plugin.task.http;

import com.google.common.collect.ImmutableList;
import org.apache.dolphinscheduler.spi.DolphinSchedulerPlugin;
import org.apache.dolphinscheduler.spi.task.TaskChannelFactory;

public class HttpTaskPlugin implements DolphinSchedulerPlugin {

    @Override
    public Iterable<TaskChannelFactory> getTaskChannelFactorys() {
        return ImmutableList.of(new HttpTaskChannelFactory());
    }
}
