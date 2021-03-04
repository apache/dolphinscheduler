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
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.runner.MasterBaseTaskExecThread;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

import java.lang.reflect.Method;

import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_TASK_STATE;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_TASK_TYPE;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_TASK_INSTANCE_ID;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_PROCESS_DEFINITION_ID;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_PROCESS_INSTANCE_ID;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_TASK_INSTANCE_NAME;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_TASK_WORKER_GROUP;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_EXECUTE_METHOD;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.getProcessDefinitionId;

public class MasterBaseTaskExecThreadMethodInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        TaskContext<TaskInstance> taskContext = (TaskContext) objInst.getSkyWalkingDynamicField();
        TaskInstance taskInstance = taskContext.getCache();
        String operationName = getOperationNamePrefix(taskInstance) + taskInstance.getName();

        AbstractSpan span = ContextManager.createLocalSpan(operationName);
        span.setComponent(Utils.DOLPHIN_SCHEDULER);
        TAG_PROCESS_DEFINITION_ID.set(span, String.valueOf(taskInstance.getProcessDefinitionId()));
        TAG_PROCESS_INSTANCE_ID.set(span, String.valueOf(taskInstance.getProcessInstanceId()));
        TAG_TASK_TYPE.set(span, taskInstance.getTaskType());
        TAG_TASK_INSTANCE_ID.set(span, String.valueOf(taskInstance.getId()));
        TAG_TASK_INSTANCE_NAME.set(span, taskInstance.getName());
        TAG_TASK_WORKER_GROUP.set(span, taskInstance.getWorkerGroup());
        TAG_EXECUTE_METHOD.set(span, Utils.getMethodName(method));

        ContextManager.continued(taskContext.getContextSnapshot());
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        AbstractSpan span = ContextManager.activeSpan();

        MasterBaseTaskExecThread original = (MasterBaseTaskExecThread) objInst;
        TaskInstance taskInstance = original.getTaskInstance();
        ExecutionStatus executionStatus = taskInstance.getState();

        TAG_TASK_STATE.set(span, taskInstance.getState().getDescp());
        if (!ExecutionStatus.SUCCESS.equals(executionStatus)) {
            span.errorOccurred();
        }

        ContextManager.stopSpan();
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        ContextManager.activeSpan().log(t);
    }

    private static String getOperationNamePrefix(TaskInstance taskInstance) {
        String prefix = "";
        if (taskInstance.isSubProcess()) {
            prefix = "master/subprocess_task/";
        } else if (taskInstance.isDependTask()) {
            prefix = "master/depend_task/";
        } else if (taskInstance.isConditionsTask()) {
            prefix = "master/conditions_task/";
        } else {
            prefix = "master/task/";
        }

        return prefix + getProcessDefinitionId(taskInstance.getProcessDefinitionId()) + "/";
    }
}

