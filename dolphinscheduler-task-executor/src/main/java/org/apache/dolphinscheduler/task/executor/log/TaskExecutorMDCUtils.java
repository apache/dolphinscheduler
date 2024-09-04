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

package org.apache.dolphinscheduler.task.executor.log;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.task.executor.ITaskExecutor;

import org.slf4j.MDC;

public class TaskExecutorMDCUtils {

    private static final String TASK_INSTANCE_ID_MDC_KEY = "taskInstanceId";
    private static final String TASK_INSTANCE_LOG_FULL_PATH_MDC_KEY = "taskInstanceLogFullPath";

    public static MDCAutoClosable logWithMDC(final ITaskExecutor taskExecutor) {
        return logWithMDC(taskExecutor.getId(), taskExecutor.getTaskExecutionContext().getLogPath());
    }

    public static MDCAutoClosable logWithMDC(final int taskExecutorId) {
        return logWithMDC(taskExecutorId, null);
    }

    public static MDCAutoClosable logWithMDC(final int taskExecutorId, final String logPath) {

        if (logPath != null) {
            MDC.put(TASK_INSTANCE_LOG_FULL_PATH_MDC_KEY, logPath);
        }

        MDC.put(Constants.TASK_INSTANCE_ID_MDC_KEY, String.valueOf(taskExecutorId));

        return () -> {
            MDC.remove(TASK_INSTANCE_LOG_FULL_PATH_MDC_KEY);
            MDC.remove(TASK_INSTANCE_ID_MDC_KEY);
        };
    }

    public interface MDCAutoClosable extends AutoCloseable {

        @Override
        void close();
    }

}
