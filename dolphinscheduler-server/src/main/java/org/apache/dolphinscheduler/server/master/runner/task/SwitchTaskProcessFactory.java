package org.apache.dolphinscheduler.server.master.runner.task;

import org.apache.dolphinscheduler.common.enums.TaskType;

public class SwitchTaskProcessFactory implements ITaskProcessFactory{

    @Override
    public String type() {
        return TaskType.SWITCH.getDesc();
    }

    @Override
    public ITaskProcessor create() {
        return new SwitchTaskProcessor();
    }
}
