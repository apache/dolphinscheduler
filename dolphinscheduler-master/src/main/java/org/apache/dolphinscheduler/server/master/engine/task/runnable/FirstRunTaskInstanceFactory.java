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

package org.apache.dolphinscheduler.server.master.engine.task.runnable;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.FirstRunTaskInstanceFactory.FirstRunTaskInstanceBuilder;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

@Component
public class FirstRunTaskInstanceFactory extends AbstractTaskInstanceFactory<FirstRunTaskInstanceBuilder> {

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Override
    public FirstRunTaskInstanceBuilder builder() {
        return new FirstRunTaskInstanceBuilder(this);
    }

    @Override
    public TaskInstance createTaskInstance(FirstRunTaskInstanceBuilder builder) {
        final TaskDefinition taskDefinition = Preconditions.checkNotNull(builder.taskDefinition);
        final WorkflowInstance workflowInstance = Preconditions.checkNotNull(builder.workflowInstance);

        TaskInstance taskInstance = new TaskInstance();
        injectMetadataFromTaskDefinition(taskInstance, taskDefinition);
        injectMetadataFromWorkflowInstance(taskInstance, workflowInstance);
        injectEnvironmentConfigFromDB(taskInstance);

        taskInstance.setState(TaskExecutionStatus.SUBMITTED_SUCCESS);
        taskInstance.setFirstSubmitTime(new Date());
        taskInstance.setSubmitTime(new Date());
        taskInstance.setStartTime(null);
        taskInstance.setEndTime(null);
        taskInstance.setHost(null);
        taskInstance.setExecutePath(null);
        taskInstance.setLogPath(null);
        taskInstance.setRetryTimes(0);
        taskInstance.setAlertFlag(Flag.NO);
        taskInstance.setFlag(Flag.YES);
        taskInstanceDao.insert(taskInstance);
        return taskInstance;
    }

    public static class FirstRunTaskInstanceBuilder implements ITaskInstanceFactory.ITaskInstanceBuilder {

        private final FirstRunTaskInstanceFactory firstRunTaskInstanceFactory;

        private WorkflowInstance workflowInstance;

        private TaskDefinition taskDefinition;

        public FirstRunTaskInstanceBuilder(FirstRunTaskInstanceFactory firstRunTaskInstanceFactory) {
            this.firstRunTaskInstanceFactory = firstRunTaskInstanceFactory;
        }

        public FirstRunTaskInstanceBuilder withWorkflowInstance(WorkflowInstance workflowInstance) {
            this.workflowInstance = workflowInstance;
            return this;
        }

        public FirstRunTaskInstanceBuilder withTaskDefinition(TaskDefinition taskDefinition) {
            this.taskDefinition = taskDefinition;
            return this;
        }

        @Override
        public TaskInstance build() {
            return firstRunTaskInstanceFactory.createTaskInstance(this);
        }
    }
}
