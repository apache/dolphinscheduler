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

package org.apache.dolphinscheduler.plugin.task.api.k8s;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;

import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.utils.K8sUtils;

import org.slf4j.Logger;

public abstract class AbstractK8sTaskExecutor {

    protected Logger logger;
    protected TaskExecutionContext taskRequest;
    protected K8sUtils k8sUtils;
    protected StringBuilder logStringBuffer;

    protected AbstractK8sTaskExecutor(Logger logger, TaskExecutionContext taskRequest) {
        this.logger = logger;
        this.taskRequest = taskRequest;
        this.k8sUtils = new K8sUtils();
        this.logStringBuffer = new StringBuilder();
    }

    public abstract TaskResponse run(String k8sParameterStr) throws Exception;

    public abstract void cancelApplication(String k8sParameterStr);

    public void waitTimeout(Boolean timeout) throws TaskException {
        if (Boolean.TRUE.equals(timeout)) {
            throw new TaskException("K8sTask is timeout");
        }
    }

    public void flushLog(TaskResponse taskResponse) {
        if (logStringBuffer.length() != 0 && taskResponse.getExitStatusCode() == EXIT_CODE_FAILURE) {
            logger.error(logStringBuffer.toString());
        } else if (logStringBuffer.length() != 0) {
            logger.info(logStringBuffer.toString());
        }
    }

    public abstract void submitJob2k8s(String k8sParameterStr);

    public abstract void stopJobOnK8s(String k8sParameterStr);
}
