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

package org.apache.dolphinscheduler.server.master.engine.executor.plugin.fake;

import org.apache.dolphinscheduler.plugin.task.api.task.LogicFakeTaskChannelFactory;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.ILogicTaskPluginFactory;
import org.apache.dolphinscheduler.task.executor.ITaskExecutor;

import org.springframework.stereotype.Component;

import com.google.common.annotations.VisibleForTesting;

@Component
@VisibleForTesting
public class LogicFakeTaskPluginFactory implements ILogicTaskPluginFactory<LogicFakeTask> {

    private final IWorkflowRepository workflowRepository;

    public LogicFakeTaskPluginFactory(final IWorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    @Override
    public LogicFakeTask createLogicTask(final ITaskExecutor taskExecutor) {
        return new LogicFakeTask(taskExecutor.getTaskExecutionContext());
    }

    @Override
    public String getTaskType() {
        return LogicFakeTaskChannelFactory.NAME;
    }
}
