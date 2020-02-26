package org.apache.dolphinscheduler.server.master.cache;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.command.ExecuteTaskAckCommand;
import org.apache.dolphinscheduler.remote.command.ExecuteTaskResponseCommand;
import org.apache.dolphinscheduler.remote.entity.TaskExecutionContext;

/**
 *  task instance state manager
 */
public interface TaskInstanceCacheManager {

    /**
     * get taskInstance by taskInstance id
     *
     * @param taskInstanceId taskInstanceId
     * @return taskInstance
     */
    TaskInstance getByTaskInstanceId(Integer taskInstanceId);

    /**
     * cache taskInstance
     *
     * @param taskExecutionContext taskExecutionContext
     */
    void cacheTaskInstance(TaskExecutionContext taskExecutionContext);

    /**
     * cache taskInstance
     *
     * @param taskAckCommand taskAckCommand
     */
    void cacheTaskInstance(ExecuteTaskAckCommand taskAckCommand);

    /**
     * cache taskInstance
     *
     * @param executeTaskResponseCommand executeTaskResponseCommand
     */
    void cacheTaskInstance(ExecuteTaskResponseCommand executeTaskResponseCommand);
}
