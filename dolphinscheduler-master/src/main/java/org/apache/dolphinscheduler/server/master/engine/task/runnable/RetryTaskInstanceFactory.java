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
import org.apache.dolphinscheduler.server.master.engine.task.runnable.RetryTaskInstanceFactory.RetryTaskInstanceBuilder;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RetryTaskInstanceFactory extends AbstractTaskInstanceFactory<RetryTaskInstanceBuilder> {

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Override
    public RetryTaskInstanceBuilder builder() {
        return new RetryTaskInstanceBuilder(this);
    }

    @Transactional
    @Override
    public TaskInstance createTaskInstance(RetryTaskInstanceBuilder builder) {
        final TaskInstance needRetryTaskInstance = builder.taskInstance;
        final TaskInstance taskInstance = cloneTaskInstance(needRetryTaskInstance);
        taskInstance.setId(null);
        taskInstance.setState(TaskExecutionStatus.SUBMITTED_SUCCESS);
        taskInstance.setPid(0);
        taskInstance.setHost(null);
        taskInstance.setExecutePath(null);
        taskInstance.setLogPath(null);
        taskInstance.setCacheKey(null);
        taskInstance.setStartTime(null);
        taskInstance.setEndTime(null);
        taskInstance.setSubmitTime(new Date());
        taskInstance.setRetryTimes(taskInstance.getRetryTimes() + 1);
        taskInstanceDao.insert(taskInstance);

        needRetryTaskInstance.setFlag(Flag.NO);
        taskInstanceDao.updateById(needRetryTaskInstance);
        return taskInstance;
    }

    public static class RetryTaskInstanceBuilder implements ITaskInstanceFactory.ITaskInstanceBuilder {

        private final RetryTaskInstanceFactory retryTaskInstanceFactory;

        private TaskInstance taskInstance;

        public RetryTaskInstanceBuilder(RetryTaskInstanceFactory retryTaskInstanceFactory) {
            this.retryTaskInstanceFactory = retryTaskInstanceFactory;
        }

        public RetryTaskInstanceBuilder withTaskInstance(TaskInstance taskInstance) {
            this.taskInstance = taskInstance;
            return this;
        }

        @Override
        public TaskInstance build() {
            return retryTaskInstanceFactory.createTaskInstance(this);
        }

    }
}
