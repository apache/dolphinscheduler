/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.dolphinscheduler.skywalking.plugin;

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.AbstractTask;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

import java.lang.reflect.Method;

import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_TASK_PARAMS;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_TASK_STATE;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_EXECUTE_METHOD;

public class TaskExecuteMethodInterceptor implements InstanceMethodsAroundInterceptor {
    private static final int DEFAULT_TASK_STATUS_CODE = -1;
    private static final int TASK_PARAMS_MAX_LENGTH = 2048;
    private static final String OPERATION_NAME_PREFIX = "worker/";

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        TaskContext<TaskExecutionContext> taskContext = (TaskContext) objInst.getSkyWalkingDynamicField();
        String operationName = OPERATION_NAME_PREFIX + taskContext.getCache().getTaskType() + "/" + method.getName();

        AbstractSpan span = ContextManager.createLocalSpan(operationName);
        span.setComponent(Utils.DOLPHIN_SCHEDULER);
        TAG_EXECUTE_METHOD.set(span, Utils.getMethodName(method));
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        AbstractSpan span = ContextManager.activeSpan();

        AbstractTask task = (AbstractTask) objInst;
        int statusCode = task.getExitStatusCode();
        ExecutionStatus status = task.getExitStatus();
        TAG_TASK_STATE.set(span, status.getDescp());
        if (statusCode != DEFAULT_TASK_STATUS_CODE && ExecutionStatus.FAILURE.equals(status)) {
            logTaskParams(objInst);
            span.errorOccurred();
        }

        ContextManager.stopSpan();
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        logTaskParams(objInst);

        ContextManager.activeSpan().log(t);
    }

    private void logTaskParams(EnhancedInstance objInst) {
        TaskContext<TaskExecutionContext> taskContext = (TaskContext) objInst.getSkyWalkingDynamicField();
        TaskExecutionContext executionContext = taskContext.getCache();
        String taskParams = executionContext.getTaskParams();

        String limitTaskParams = taskParams;
        if (taskParams.length() > TASK_PARAMS_MAX_LENGTH) {
            limitTaskParams = taskParams.substring(TASK_PARAMS_MAX_LENGTH);
        }

        AbstractSpan span = ContextManager.activeSpan();
        TAG_TASK_PARAMS.set(span, limitTaskParams);
    }
}
