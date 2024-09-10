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
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.FailoverTaskInstanceFactory.FailoverTaskInstanceBuilder;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FailoverTaskInstanceFactory extends AbstractTaskInstanceFactory<FailoverTaskInstanceBuilder> {

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Override
    public FailoverTaskInstanceFactory.FailoverTaskInstanceBuilder builder() {
        return new FailoverTaskInstanceBuilder(this);
    }

    @Transactional
    @Override
    public TaskInstance createTaskInstance(FailoverTaskInstanceBuilder builder) {
        final TaskInstance needFailoverTaskInstance = builder.needFailoverTaskInstance;
        final TaskInstance taskInstance = cloneTaskInstance(needFailoverTaskInstance);
        taskInstance.setId(null);
        taskInstance.setState(TaskExecutionStatus.SUBMITTED_SUCCESS);
        taskInstance.setHost(null);
        taskInstance.setVarPool(null);
        taskInstance.setSubmitTime(new Date());
        taskInstance.setLogPath(null);
        taskInstance.setExecutePath(null);
        taskInstanceDao.insert(taskInstance);

        needFailoverTaskInstance.setFlag(Flag.NO);
        needFailoverTaskInstance.setState(TaskExecutionStatus.NEED_FAULT_TOLERANCE);
        taskInstanceDao.updateById(needFailoverTaskInstance);
        return taskInstance;
    }

    public static class FailoverTaskInstanceBuilder implements ITaskInstanceFactory.ITaskInstanceBuilder {

        private final FailoverTaskInstanceFactory failoverTaskInstanceFactory;

        private TaskInstance needFailoverTaskInstance;

        public FailoverTaskInstanceBuilder(FailoverTaskInstanceFactory failoverTaskInstanceFactory) {
            this.failoverTaskInstanceFactory = failoverTaskInstanceFactory;
        }

        public FailoverTaskInstanceBuilder withTaskInstance(TaskInstance needFailoverTaskInstance) {
            this.needFailoverTaskInstance = needFailoverTaskInstance;
            return this;
        }

        @Override
        public TaskInstance build() {
            return failoverTaskInstanceFactory.createTaskInstance(this);
        }
    }
}
