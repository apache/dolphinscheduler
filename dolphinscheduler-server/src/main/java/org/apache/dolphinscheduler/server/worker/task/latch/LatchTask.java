package org.apache.dolphinscheduler.server.worker.task.latch;

import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.latch.LatchParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.AbstractTask;
import org.slf4j.Logger;

public class LatchTask extends AbstractTask {


    private LatchParameters latchParameters ;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    /**
     * init Connectors config
     */
    @Override
    public void init() {
        logger.info("latch task params {}", taskExecutionContext.getTaskParams());
        latchParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), LatchParameters.class);
        if (!latchParameters.checkParameters()) {
            throw new RuntimeException("latch task params is not valid");
        }
    }


    /**
     * cancel Connectors process
     *
     * @param cancelApplication cancelApplication
     * @throws Exception if error throws Exception
     */
    @Override
    public void cancelApplication(boolean cancelApplication)
            throws Exception {
        super.cancelApplication(cancelApplication);
    }

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
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void handle() throws Exception {
        // 获取当前毫秒数
        long currentTimeMillis = System.currentTimeMillis();

        // 计算需要停止休眠的时间
        long targetTimeMillis = currentTimeMillis + latchParameters.getMilliseconds();

        // 比较大小
        // 只有 [   done为false  ]
        // 并且
        // [ 目标时间 小于 当前时间的条件下 ] , 才会继续休眠.
        while (!cancel && (System.currentTimeMillis() < targetTimeMillis) ){

            Thread.sleep(500);
        }

    }

    @Override
    public AbstractParameters getParameters() {
        return latchParameters;
    }


}
