package org.apache.dolphinscheduler.plugin.task.api;

import org.apache.dolphinscheduler.plugin.task.util.LoggerUtils;
import org.apache.dolphinscheduler.spi.task.AbstractTask;
import org.apache.dolphinscheduler.spi.task.TaskRequest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public abstract class AbstractTaskExecutor extends AbstractTask {

    public static final Marker FINALIZE_SESSION_MARKER = MarkerFactory.getMarker("FINALIZE_SESSION");

    protected  Logger logger ;

    /**
     * constructor
     *
     * @param taskRequest taskRequest
     */
    protected AbstractTaskExecutor(TaskRequest taskRequest) {
        super(taskRequest);
        logger = LoggerFactory.getLogger(LoggerUtils.buildTaskId(LoggerUtils.TASK_LOGGER_INFO_PREFIX,
                taskRequest.getProcessDefineId(),
                taskRequest.getProcessInstanceId(),
                taskRequest.getTaskInstanceId()));
    }

    /**
     * log handle
     *
     * @param logs log list
     */
    public void logHandle(List<String> logs) {
        // note that the "new line" is added here to facilitate log parsing
        if (logs.contains(FINALIZE_SESSION_MARKER.toString())) {
            logger.info(FINALIZE_SESSION_MARKER, FINALIZE_SESSION_MARKER.toString());
        } else {
            logger.info(" -> {}", String.join("\n\t", logs));
        }
    }
}
