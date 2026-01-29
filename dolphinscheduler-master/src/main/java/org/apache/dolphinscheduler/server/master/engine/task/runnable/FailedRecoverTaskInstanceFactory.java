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

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FailedRecoverTaskInstanceFactory
        extends
            AbstractTaskInstanceFactory<FailedRecoverTaskInstanceFactory.FailedRecoverTaskInstanceBuilder> {

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Override
    public FailedRecoverTaskInstanceFactory.FailedRecoverTaskInstanceBuilder builder() {
        return new FailedRecoverTaskInstanceBuilder(this);
    }

    @Transactional
    @Override
    public TaskInstance createTaskInstance(FailedRecoverTaskInstanceBuilder builder) {
        final TaskInstance needRecoverTaskInstance = builder.needRecoverTaskInstance;
        final TaskInstance taskInstance = cloneTaskInstance(needRecoverTaskInstance);
        taskInstance.setId(null);
        taskInstance.setState(TaskExecutionStatus.SUBMITTED_SUCCESS);
        taskInstance.setHost(null);
        taskInstance.setVarPool(null);
        taskInstance.setSubmitTime(new Date());
        taskInstance.setLogPath(null);
        taskInstance.setExecutePath(null);
        taskInstanceDao.insert(taskInstance);

        needRecoverTaskInstance.setFlag(Flag.NO);
        taskInstanceDao.updateById(needRecoverTaskInstance);
        return taskInstance;
    }

    public static class FailedRecoverTaskInstanceBuilder implements ITaskInstanceFactory.ITaskInstanceBuilder {

        private final FailedRecoverTaskInstanceFactory failedRecoverTaskInstanceFactory;

        private TaskInstance needRecoverTaskInstance;

        public FailedRecoverTaskInstanceBuilder(FailedRecoverTaskInstanceFactory failedRecoverTaskInstanceFactory) {
            this.failedRecoverTaskInstanceFactory = failedRecoverTaskInstanceFactory;
        }

        public FailedRecoverTaskInstanceBuilder withTaskInstance(TaskInstance needRecoverTaskInstance) {
            this.needRecoverTaskInstance = needRecoverTaskInstance;
            return this;
        }

        @Override
        public TaskInstance build() {
            return failedRecoverTaskInstanceFactory.createTaskInstance(this);
        }
    }
}
