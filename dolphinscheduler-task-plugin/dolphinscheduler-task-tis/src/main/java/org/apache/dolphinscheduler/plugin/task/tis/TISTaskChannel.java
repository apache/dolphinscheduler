package org.apache.dolphinscheduler.plugin.task.tis;

import org.apache.dolphinscheduler.spi.task.AbstractTask;
import org.apache.dolphinscheduler.spi.task.TaskChannel;
import org.apache.dolphinscheduler.spi.task.TaskRequest;

/**
 **/
public class TISTaskChannel implements TaskChannel {
    @Override
    public void cancelApplication(boolean status) {

    }

    @Override
    public AbstractTask createTask(TaskRequest taskRequest, org.slf4j.Logger logger) {
        return null;
    }
}
