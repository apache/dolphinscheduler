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

package org.apache.dolphinscheduler.server.master.runner.execute;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.runner.DefaultTaskExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.TaskExecuteRunnableFactory;
import org.apache.dolphinscheduler.server.master.runner.TaskExecutionContextFactory;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.operator.TaskExecuteRunnableOperatorManager;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DefaultTaskExecuteRunnableFactory implements TaskExecuteRunnableFactory<DefaultTaskExecuteRunnable> {

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    @Autowired
    private TaskExecutionContextFactory taskExecutionContextFactory;

    @Autowired
    private TaskExecuteRunnableOperatorManager taskExecuteRunnableOperatorManager;

    @Override
    public DefaultTaskExecuteRunnable createTaskExecuteRunnable(TaskInstance taskInstance) {
        WorkflowExecuteRunnable workflowExecuteRunnable =
                processInstanceExecCacheManager.getByProcessInstanceId(taskInstance.getProcessInstanceId());
        return new DefaultTaskExecuteRunnable(
                workflowExecuteRunnable.getWorkflowExecuteContext().getWorkflowInstance(),
                taskInstance,
                taskExecutionContextFactory.createTaskExecutionContext(taskInstance),
                taskExecuteRunnableOperatorManager);
    }
}
