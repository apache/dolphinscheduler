package org.apache.dolphinscheduler.plugin.task.http;

import org.apache.dolphinscheduler.spi.task.AbstractParameters;
import org.apache.dolphinscheduler.spi.task.AbstractTask;
import org.apache.dolphinscheduler.spi.task.TaskRequest;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.slf4j.Logger;

public class HttpTask extends AbstractTask {

    /**
     * taskExecutionContext
     */
    private TaskRequest taskExecutionContext;

    private HttpParameters httpParameters;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     * @param logger               logger
     */
    public HttpTask(TaskRequest taskExecutionContext, Logger logger) {
        super(taskExecutionContext, logger);
    }

    @Override
    public void init() {
        logger.info("http task params {}", taskExecutionContext.getTaskParams());
        this.httpParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), HttpParameters.class);

        if (!httpParameters.checkParameters()) {
            throw new RuntimeException("http task params is not valid");
        }
    }

    @Override
    public void handle() throws Exception {

    }

    @Override
    public AbstractParameters getParameters() {
        return null;
    }
}
