package org.apache.dolphinscheduler.plugin.task.python;

import org.apache.dolphinscheduler.spi.DolphinSchedulerPlugin;
import org.apache.dolphinscheduler.spi.task.TaskChannelFactory;

import com.google.common.collect.ImmutableList;

public class PythonTaskPlugin implements DolphinSchedulerPlugin {

    @Override
    public Iterable<TaskChannelFactory> getTaskChannelFactorys() {
        return ImmutableList.of(new PythonTaskChannelFactory());
    }
}
