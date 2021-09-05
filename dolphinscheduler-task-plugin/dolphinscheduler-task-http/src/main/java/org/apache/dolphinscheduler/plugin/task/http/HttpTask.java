package org.apache.dolphinscheduler.plugin.task.http;

import org.apache.dolphinscheduler.plugin.task.api.AbstractTaskExecutor;
import org.apache.dolphinscheduler.spi.task.AbstractParameters;
import org.apache.dolphinscheduler.spi.task.AbstractTask;
import org.apache.dolphinscheduler.spi.task.TaskRequest;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

public class HttpTask extends AbstractTaskExecutor {

    /**
     * taskExecutionContext
     */
    private TaskRequest taskExecutionContext;

    private HttpParameters httpParameters;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    public HttpTask(TaskRequest taskExecutionContext) {
        super(taskExecutionContext);
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
