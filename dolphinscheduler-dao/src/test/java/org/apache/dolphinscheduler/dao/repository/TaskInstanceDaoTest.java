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

package org.apache.dolphinscheduler.dao.repository;

import org.apache.dolphinscheduler.common.enums.ProfileType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(ProfileType.H2)
class TaskInstanceDaoTest extends BaseDaoTest {

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private TaskInstanceMapper taskInstanceMapper;

    @Test
    public void giveFinishedProcess_whenSubmitTaskInstanceToDB_expectNull() {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setState(WorkflowExecutionStatus.FAILURE);

        TaskInstance nullResult = taskInstanceDao.submitTaskInstanceToDB(createTaskInstance(), processInstance);
        Assertions.assertNull(nullResult);
    }

    @Test
    public void giveNotFinishedProcess_whenSubmitTaskInstanceToDB_expectSuccess() {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setState(WorkflowExecutionStatus.READY_PAUSE);
        processInstance.setExecutorId(1);

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setName("taskInstance");
        taskInstance.setTaskCode(1L);
        taskInstance.setTaskType("SHELL");

        TaskInstance result = taskInstanceDao.submitTaskInstanceToDB(taskInstance, processInstance);
        Assertions.assertEquals(1, result.getExecutorId());
    }

    private TaskInstance createTaskInstance() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setName("taskInstance");
        taskInstance.setTaskCode(1L);
        taskInstance.setTaskType("SHELL");
        return taskInstance;
    }

    private void insertOneTaskInstance() {
        TaskInstance taskInstance = createTaskInstance();
        taskInstanceDao.insertTaskInstance(taskInstance);
    }

}
