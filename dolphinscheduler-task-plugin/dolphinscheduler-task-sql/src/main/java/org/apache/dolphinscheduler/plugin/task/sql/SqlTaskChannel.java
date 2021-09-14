package org.apache.dolphinscheduler.plugin.task.sql;

import org.apache.dolphinscheduler.spi.task.AbstractTask;
import org.apache.dolphinscheduler.spi.task.TaskChannel;
import org.apache.dolphinscheduler.spi.task.request.TaskRequest;

public class SqlTaskChannel implements TaskChannel {
    @Override
    public void cancelApplication(boolean status) {

    }

    @Override
    public AbstractTask createTask(TaskRequest taskRequest) {
        return null;
    }
}
