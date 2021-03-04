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
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.RuntimeContext;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

import java.lang.reflect.Method;

import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_PROJECT_ID;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_PROCESS_STATE;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_PROCESS_INSTANCE_ID;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_PROCESS_INSTANCE_NAME;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_PROCESS_INSTANCE_HOST;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_PROCESS_DEFINITION_ID;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_PROCESS_COMMAND_TYPE;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_PROCESS_WORKER_GROUP;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_PROCESS_TIMEOUT;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.MASTER_PROCESS_EXECUTION_STATUS;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.TAG_EXECUTE_METHOD;
import static org.apache.dolphinscheduler.skywalking.plugin.Utils.getProjectId;

public class MasterExecThreadMethodInterceptor implements InstanceMethodsAroundInterceptor {
    private static final String OPERATION_NAME_PREFIX = "master/process/";

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        TaskContext<ProcessInstance> taskContext = (TaskContext<ProcessInstance>) objInst.getSkyWalkingDynamicField();
        ProcessInstance processInstance = taskContext.getCache();
        ProcessDefinition processDefinition = processInstance.getProcessDefinition();
        String operationName = OPERATION_NAME_PREFIX + getProjectId(processDefinition.getProjectId()) + "/" + processDefinition.getName();

        AbstractSpan span = ContextManager.createLocalSpan(operationName);
        span.setComponent(Utils.DOLPHIN_SCHEDULER);
        TAG_PROJECT_ID.set(span, String.valueOf(processDefinition.getProjectId()));
        TAG_PROCESS_INSTANCE_ID.set(span, String.valueOf(processInstance.getId()));
        TAG_PROCESS_INSTANCE_NAME.set(span, processInstance.getName());
        TAG_PROCESS_INSTANCE_HOST.set(span, processInstance.getHost());
        TAG_PROCESS_DEFINITION_ID.set(span, String.valueOf(processInstance.getProcessDefinitionId()));
        TAG_PROCESS_COMMAND_TYPE.set(span, processInstance.getCommandType().name());
        TAG_PROCESS_WORKER_GROUP.set(span, processInstance.getWorkerGroup());
        TAG_PROCESS_TIMEOUT.set(span, String.valueOf(processInstance.getTimeout()));
        TAG_EXECUTE_METHOD.set(span, Utils.getMethodName(method));

        ContextManager.continued(taskContext.getContextSnapshot());
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        AbstractSpan span = ContextManager.activeSpan();

        RuntimeContext runtimeContext = ContextManager.getRuntimeContext();
        ExecutionStatus executionStatus = (ExecutionStatus) runtimeContext.get(MASTER_PROCESS_EXECUTION_STATUS);
        if (executionStatus == null) {
            ProcessInstance processInstance = (ProcessInstance) objInst.getSkyWalkingDynamicField();
            executionStatus = processInstance.getState();
        }

        TAG_PROCESS_STATE.set(span, executionStatus.getDescp());
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
}
