/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.server.master.runner.operator;

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.runner.DefaultTaskExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.GlobalTaskDispatchWaitingQueue;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseTaskExecuteRunnableDispatchOperator implements TaskExecuteRunnableOperator {

    private final GlobalTaskDispatchWaitingQueue globalTaskDispatchWaitingQueue;

    private final TaskInstanceDao taskInstanceDao;

    public BaseTaskExecuteRunnableDispatchOperator(
                                                   GlobalTaskDispatchWaitingQueue globalTaskDispatchWaitingQueue,
                                                   TaskInstanceDao taskInstanceDao) {
        this.globalTaskDispatchWaitingQueue = globalTaskDispatchWaitingQueue;
        this.taskInstanceDao = taskInstanceDao;
    }

    @Override
    public void operate(DefaultTaskExecuteRunnable taskExecuteRunnable) {
        TaskInstance taskInstance = taskExecuteRunnable.getTaskInstance();
        long remainTimeMills =
                DateUtils.getRemainTime(taskInstance.getFirstSubmitTime(), taskInstance.getDelayTime() * 60L) * 1_000;
        if (remainTimeMills > 0) {
            taskInstance.setState(TaskExecutionStatus.DELAY_EXECUTION);
            taskInstanceDao.updateById(taskInstance);
            log.info("Current taskInstance: {} is choose delay execution, delay time: {}/min, remainTime: {}/ms",
                    taskInstance.getName(),
                    taskInstance.getDelayTime(),
                    remainTimeMills);
        }
        globalTaskDispatchWaitingQueue.dispatchTaskExecuteRunnableWithDelay(taskExecuteRunnable, remainTimeMills);
    }
}
