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

import org.apache.dolphinscheduler.task.executor.ITaskExecutor;

import org.slf4j.MDC;

public class TaskExecutorMDCUtils {

    private static final String TASK_INSTANCE_ID_MDC_KEY = "taskInstanceId";
    private static final String TASK_INSTANCE_LOG_FULL_PATH_MDC_KEY = "taskInstanceLogFullPath";
    private static final String WORKFLOW_INSTANCE_ID_MDC_KEY = "workflowInstanceId";

    public static MDCAutoClosable logWithMDC(final ITaskExecutor taskExecutor) {
        return logWithMDC(taskExecutor.getId(),
                taskExecutor.getTaskExecutionContext().getLogPath(),
                taskExecutor.getTaskExecutionContext() == null ? 0
                        : taskExecutor.getTaskExecutionContext().getWorkflowInstanceId());
    }

    public static MDCAutoClosable logWithMDC(final int taskInstanceId) {
        return logWithMDC(taskInstanceId, null, 0);
    }

    public static MDCAutoClosable logWithMDC(final int taskInstanceId, final int workflowInstanceId) {
        return logWithMDC(taskInstanceId, null, workflowInstanceId);
    }

    public static MDCAutoClosable logWithMDC(final int taskInstanceId, final String logPath,
                                             final int workflowInstanceId) {

        if (logPath != null) {
            MDC.put(TASK_INSTANCE_LOG_FULL_PATH_MDC_KEY, logPath);
        }

        if (workflowInstanceId > 0) {
            MDC.put(WORKFLOW_INSTANCE_ID_MDC_KEY, String.valueOf(workflowInstanceId));
        }

        MDC.put(TASK_INSTANCE_ID_MDC_KEY, String.valueOf(taskInstanceId));

        return () -> {
            MDC.remove(TASK_INSTANCE_LOG_FULL_PATH_MDC_KEY);
            MDC.remove(TASK_INSTANCE_ID_MDC_KEY);
            MDC.remove(WORKFLOW_INSTANCE_ID_MDC_KEY);
        };
    }

    public interface MDCAutoClosable extends AutoCloseable {

        @Override
        void close();
    }

}
