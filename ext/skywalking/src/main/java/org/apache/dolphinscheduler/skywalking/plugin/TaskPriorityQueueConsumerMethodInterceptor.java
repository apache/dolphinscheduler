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

import org.apache.dolphinscheduler.service.queue.TaskPriority;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.ContextSnapshot;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

import java.lang.reflect.Method;
import java.util.Map;

import static org.apache.dolphinscheduler.skywalking.plugin.Utils.*;

public class TaskPriorityQueueConsumerMethodInterceptor implements InstanceMethodsAroundInterceptor {
    private static final String OPERATION_NAME = "master/queue/take";

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        TaskPriority taskPriority = (TaskPriority) allArguments[0];
        Map<String, Object> taskContext = taskPriority.getContext();

        AbstractSpan span = ContextManager.createLocalSpan(OPERATION_NAME);
        span.setComponent(Utils.DOLPHIN_SCHEDULER);
        TAG_TASK_ID.set(span, String.valueOf(taskPriority.getTaskId()));
        TAG_PROCESS_INSTANCE_ID.set(span, String.valueOf(taskPriority.getProcessInstanceId()));
        Tags.LOGIC_ENDPOINT.set(span, Tags.VAL_LOCAL_SPAN_AS_LOGIC_ENDPOINT);
        TAG_EXECUTE_METHOD.set(span, Utils.getMethodName(method));

        ContextSnapshot contextSnapshot = (ContextSnapshot) taskContext.get(SKYWALKING_TRACING_CONTEXT);
        ContextManager.continued(contextSnapshot);
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        AbstractSpan span = ContextManager.activeSpan();

        boolean handleResult = (boolean) ret;
        if (!handleResult) {
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
