package org.apache.dolphinscheduler.plugin.task.flink;

import org.apache.dolphinscheduler.spi.DolphinSchedulerPlugin;
import org.apache.dolphinscheduler.spi.task.TaskChannelFactory;

import com.google.common.collect.ImmutableList;

public class FlinkTaskPlugin implements DolphinSchedulerPlugin {
    @Override
    public Iterable<TaskChannelFactory> getTaskChannelFactorys() {
        return ImmutableList.of(new FlinkTaskChannelFactory());
    }
}
