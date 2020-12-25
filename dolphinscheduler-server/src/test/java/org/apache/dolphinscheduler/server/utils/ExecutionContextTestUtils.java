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
package org.apache.dolphinscheduler.server.utils;


import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.builder.TaskExecutionContextBuilder;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;
import org.mockito.Mockito;

/**
 * for test use only
 */
public class ExecutionContextTestUtils {


    public static ExecutionContext getExecutionContext(int port){
        TaskInstance taskInstance = Mockito.mock(TaskInstance.class);
        ProcessDefinition processDefinition = Mockito.mock(ProcessDefinition.class);
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setCommandType(CommandType.COMPLEMENT_DATA);
        taskInstance.setProcessInstance(processInstance);
        TaskExecutionContext context = TaskExecutionContextBuilder.get()
                .buildTaskInstanceRelatedInfo(taskInstance)
                .buildProcessInstanceRelatedInfo(processInstance)
                .buildProcessDefinitionRelatedInfo(processDefinition)
                .create();
        ExecutionContext executionContext = new ExecutionContext(context.toCommand(), ExecutorType.WORKER);
        executionContext.setHost(Host.of(OSUtils.getHost() + ":" + port));

        return executionContext;
    }
}
