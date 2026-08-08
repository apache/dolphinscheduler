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

package org.apache.dolphinscheduler.server.master.engine.task.execution;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FailedRecoverTaskInstanceFactoryTest {

    private static final int MAX_RETRY_TIMES = 3;

    @InjectMocks
    private FailedRecoverTaskInstanceFactory failedRecoverTaskInstanceFactory;

    @Mock
    private TaskInstanceDao taskInstanceDao;

    @Test
    @DisplayName("Test the recreated task instance gets the whole retry budget back")
    void testCreateTaskInstance_resetRetryTimes() {
        final TaskInstance needRecoverTaskInstance = createExhaustedFailedTaskInstance();

        final TaskInstance taskInstance = failedRecoverTaskInstanceFactory.builder()
                .withTaskInstance(needRecoverTaskInstance)
                .build();

        assertThat(taskInstance.getRetryTimes()).isEqualTo(0);
        assertThat(taskInstance.getMaxRetryTimes()).isEqualTo(MAX_RETRY_TIMES);
    }

    @Test
    @DisplayName("Test the recreated task instance doesn't inherit the runtime state of the failed attempt")
    void testCreateTaskInstance_clearPreviousRuntimeState() {
        final TaskInstance needRecoverTaskInstance = createExhaustedFailedTaskInstance();

        final TaskInstance taskInstance = failedRecoverTaskInstanceFactory.builder()
                .withTaskInstance(needRecoverTaskInstance)
                .build();

        assertThat(taskInstance.getId()).isNull();
        assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUBMITTED_SUCCESS);
        assertThat(taskInstance.getStartTime()).isNull();
        assertThat(taskInstance.getEndTime()).isNull();
        assertThat(taskInstance.getHost()).isNull();
        assertThat(taskInstance.getLogPath()).isNull();
        assertThat(taskInstance.getExecutePath()).isNull();
        assertThat(taskInstance.getVarPool()).isNull();
        assertThat(taskInstance.getPid()).isEqualTo(0);
        assertThat(taskInstance.getAlertFlag()).isEqualTo(Flag.NO);
        assertThat(taskInstance.getSubmitTime()).isNotNull();
    }

    @Test
    @DisplayName("Test the recovered task instance is inserted and the origin one is marked as invalid")
    void testCreateTaskInstance_markOriginTaskInstanceInvalid() {
        final TaskInstance needRecoverTaskInstance = createExhaustedFailedTaskInstance();

        final TaskInstance taskInstance = failedRecoverTaskInstanceFactory.builder()
                .withTaskInstance(needRecoverTaskInstance)
                .build();

        assertThat(taskInstance.getFlag()).isEqualTo(Flag.YES);
        assertThat(needRecoverTaskInstance.getFlag()).isEqualTo(Flag.NO);
        verify(taskInstanceDao).insert(taskInstance);
        verify(taskInstanceDao).updateById(needRecoverTaskInstance);
    }

    /**
     * Create a task instance which is failed and has already used up all its retry times.
     */
    private TaskInstance createExhaustedFailedTaskInstance() {
        final TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setName("A");
        taskInstance.setState(TaskExecutionStatus.FAILURE);
        taskInstance.setFlag(Flag.YES);
        taskInstance.setRetryTimes(MAX_RETRY_TIMES);
        taskInstance.setMaxRetryTimes(MAX_RETRY_TIMES);
        taskInstance.setRetryInterval(1);
        taskInstance.setStartTime(new Date());
        taskInstance.setEndTime(new Date());
        taskInstance.setSubmitTime(new Date());
        taskInstance.setHost("127.0.0.1:1234");
        taskInstance.setLogPath("/tmp/log/A.log");
        taskInstance.setExecutePath("/tmp/exec/A");
        taskInstance.setVarPool("[]");
        taskInstance.setPid(1234);
        taskInstance.setAlertFlag(Flag.YES);
        return taskInstance;
    }
}
