package org.apache.dolphinscheduler.plugin.task.emr;

import org.apache.dolphinscheduler.spi.task.AbstractTask;
import org.apache.dolphinscheduler.spi.task.TaskChannel;
import org.apache.dolphinscheduler.spi.task.request.TaskRequest;

public class EmrTaskChannel implements TaskChannel {
    @Override
    public void cancelApplication(boolean status) {

    }

    @Override
    public AbstractTask createTask(TaskRequest taskRequest) {
        return new EmrTask(taskRequest);
    }
}
