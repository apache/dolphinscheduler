package org.apache.dolphinscheduler.plugin.task.spark;

import org.apache.dolphinscheduler.spi.task.AbstractTask;
import org.apache.dolphinscheduler.spi.task.TaskChannel;
import org.apache.dolphinscheduler.spi.task.TaskRequest;

import org.slf4j.Logger;

public class SparkTaskChannel implements TaskChannel {
    @Override
    public void cancelApplication(boolean status) {

    }

    @Override
    public AbstractTask createTask(TaskRequest taskRequest) {
        return new SparkTask(taskRequest);
    }
}
