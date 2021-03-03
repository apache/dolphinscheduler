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

import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.utils.EnumUtils;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

import java.lang.reflect.Method;

import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_TASK_INSTANCE_ID;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_TASK_INSTANCE_HOST;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_TASK_EXECUTE_PATH;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_TASK_LOG_PATH;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_PROJECT_ID;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_PROCESS_DEFINITION_ID;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_PROCESS_INSTANCE_ID;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.getProjectId;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.getProcessDefinitionId;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_EXECUTE_METHOD;

public class TaskExecuteThreadMethodInterceptor implements InstanceMethodsAroundInterceptor {
    private static final String OPERATION_NAME_PREFIX = "worker/execute/";

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        TaskContext<TaskExecutionContext> taskContext = (TaskContext) objInst.getSkyWalkingDynamicField();
        TaskExecutionContext executionContext = taskContext.getCache();
        TaskType type = EnumUtils.getEnum(TaskType.class, executionContext.getTaskType());
        String operationName = OPERATION_NAME_PREFIX
                    + type.getDescp()
                    + "/" + getProjectId(executionContext.getProjectId())
                    + "/" + getProcessDefinitionId(executionContext.getProcessDefineId())
                    + "/" + executionContext.getTaskName();

        AbstractSpan span = ContextManager.createLocalSpan(operationName);
        span.setComponent(Utils.DOLPHIN_SCHEDULER);

        TAG_PROJECT_ID.set(span, String.valueOf(executionContext.getProjectId()));
        TAG_PROCESS_DEFINITION_ID.set(span, String.valueOf(executionContext.getProcessDefineId()));
        TAG_PROCESS_INSTANCE_ID.set(span, String.valueOf(executionContext.getProcessInstanceId()));
        TAG_TASK_INSTANCE_HOST.set(span, executionContext.getHost());
        TAG_TASK_INSTANCE_ID.set(span, String.valueOf(executionContext.getTaskInstanceId()));
        TAG_TASK_EXECUTE_PATH.set(span, executionContext.getExecutePath());
        TAG_TASK_LOG_PATH.set(span, executionContext.getLogPath());
        TAG_EXECUTE_METHOD.set(span, Utils.getMethodName(method));

        ContextManager.continued(taskContext.getContextSnapshot());
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        ContextManager.stopSpan();
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        ContextManager.activeSpan().log(t);
    }
}

