package org.apache.dolphinscheduler.server.worker.task.kettle;

import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.kettle.KettleParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.AbstractTask;
import org.apache.dolphinscheduler.server.worker.task.KettleExecutor;
import org.slf4j.Logger;

/**
 * kettle task
 */
public class KettleTask extends AbstractTask {

    /**
     * kettleParameters
     */
    private KettleParameters kettleParameters;

    /**
     * shell command executor
     */
    private KettleExecutor kettleExecutor;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     * @param logger               logger
     */
    public KettleTask(TaskExecutionContext taskExecutionContext, Logger logger) {
        super(taskExecutionContext, logger);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {
        logger.info("kettle task params {}", taskExecutionContext.getTaskParams());

        kettleParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), KettleParameters.class);

        if (!kettleParameters.checkParameters()) {
            throw new RuntimeException("kettle task params is not valid");
        }
    }

    @Override
    public void handle() throws Exception {

    }

    @Override
    public void cancelApplication(boolean cancelApplication) throws Exception {
        // cancel process
        kettleExecutor.cancelApplication();
    }

    @Override
    public AbstractParameters getParameters() {
        return kettleParameters;
    }
}
