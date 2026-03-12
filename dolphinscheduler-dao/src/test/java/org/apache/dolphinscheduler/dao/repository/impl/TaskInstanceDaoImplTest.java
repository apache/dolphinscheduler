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

package org.apache.dolphinscheduler.dao.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TaskInstanceDaoImplTest extends BaseDaoTest {

    private static final int WORKFLOW_INSTANCE_ID = 1;
    private static final long EXTRACT_TASK = 8001L;
    private static final long TRANSFORM_TASK = 8002L;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Test
    void queryLastTaskInstanceListIntervalInWorkflowInstance() {
        Date earlier = new Date(System.currentTimeMillis() - 3600_000);
        Date later = new Date();

        insertTaskInstance(EXTRACT_TASK, TaskExecutionStatus.SUCCESS, earlier);
        insertTaskInstance(EXTRACT_TASK, TaskExecutionStatus.SUCCESS, later);
        insertTaskInstance(TRANSFORM_TASK, TaskExecutionStatus.SUCCESS, later);

        Set<Long> taskCodes = new HashSet<>(Arrays.asList(EXTRACT_TASK, TRANSFORM_TASK));
        List<TaskInstance> result = taskInstanceDao.queryLastTaskInstanceListIntervalInWorkflowInstance(
                WORKFLOW_INSTANCE_ID, taskCodes);

        assertEquals(2, result.size());
        TaskInstance extractResult = result.stream()
                .filter(ti -> ti.getTaskCode() == EXTRACT_TASK)
                .findFirst().orElse(null);
        assertNotNull(extractResult);
        assertEquals(later.getTime() / 1000, extractResult.getEndTime().getTime() / 1000);
    }

    @Test
    void queryLastTaskInstanceIntervalInWorkflowInstance() {
        Date earlier = new Date(System.currentTimeMillis() - 3600_000);
        Date later = new Date();

        insertTaskInstance(EXTRACT_TASK, TaskExecutionStatus.SUCCESS, earlier);
        insertTaskInstance(EXTRACT_TASK, TaskExecutionStatus.SUCCESS, later);

        TaskInstance result = taskInstanceDao.queryLastTaskInstanceIntervalInWorkflowInstance(
                WORKFLOW_INSTANCE_ID, EXTRACT_TASK);
        assertNotNull(result);
        assertEquals(later.getTime() / 1000, result.getEndTime().getTime() / 1000);
    }

    private void insertTaskInstance(long taskCode, TaskExecutionStatus state, Date endTime) {
        TaskInstance ti = TaskInstance.builder()
                .name("shell-task-" + taskCode)
                .taskType("SHELL")
                .workflowInstanceId(WORKFLOW_INSTANCE_ID)
                .workflowInstanceName("daily-etl-pipeline")
                .taskCode(taskCode)
                .taskDefinitionVersion(1)
                .state(state)
                .flag(Flag.YES)
                .submitTime(new Date())
                .firstSubmitTime(new Date())
                .startTime(new Date())
                .endTime(endTime)
                .host("192.168.1.50:5678")
                .executePath("/tmp/dolphinscheduler/exec/" + WORKFLOW_INSTANCE_ID + "/" + taskCode)
                .logPath("/tmp/dolphinscheduler/logs/" + WORKFLOW_INSTANCE_ID + "/" + taskCode + ".log")
                .build();
        taskInstanceDao.upsertTaskInstance(ti);
    }
}
