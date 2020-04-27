package org.apache.dolphinscheduler.server.worker.task.latch;

import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.AbstractTask;
import org.slf4j.Logger;

public class LatchTask extends AbstractTask {




    /**
     * constructor
     *
     * @param taskExecutionContext
     *         taskExecutionContext
     * @param logger
     *         logger
     */
    protected LatchTask(TaskExecutionContext taskExecutionContext, Logger logger) {
        super(taskExecutionContext, logger);
    }

    @Override
    public void handle() throws Exception {

    }

    @Override
    public AbstractParameters getParameters() {
        return null;
    }


}
