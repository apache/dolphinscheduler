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

package org.apache.dolphinscheduler.service.alert;

import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.UserDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionLogDao;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskAlertInfo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkflowAlertManagerTest {

    @Mock
    private AlertDao alertDao;

    @Mock
    private WorkflowDefinitionLogDao workflowDefinitionLogDao;

    @Mock
    private UserDao userDao;

    @Mock
    private ProjectDao projectDao;

    @InjectMocks
    private WorkflowAlertManager workflowAlertManager;

    /**
     * Verify that the production path {@link WorkflowAlertManager#sendTaskResultAlert}
     * sets {@link AlertStatus#WAIT_EXECUTION} on the Alert before persisting it.
     * <p>If this status is not set, the alert server will never pick up the alert
     * because it only polls records whose status is WAIT_EXECUTION (0).
     */
    @Test
    void sendTaskResultAlert_setsWaitExecutionStatus() {
        // --- arrange ---
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(1001);
        workflowInstance.setName("test-workflow");
        workflowInstance.setProjectCode(2001L);
        workflowInstance.setWorkflowDefinitionCode(3001L);

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setName("sql-query-task");

        TaskAlertInfo taskAlertInfo = new TaskAlertInfo();
        taskAlertInfo.setTitle("SQL Task Result");
        taskAlertInfo.setContent("[{\"taskName\":\"sql-query-task\",\"result\":\"ok\"}]");
        taskAlertInfo.setAlertGroupId(1);
        taskAlertInfo.setAlertType(AlertType.TASK_RESULT);

        Mockito.when(alertDao.addTaskResultAlert(Mockito.any(Alert.class))).thenReturn(1);

        // --- act ---
        workflowAlertManager.sendTaskResultAlert(workflowInstance, taskInstance, taskAlertInfo);

        // --- assert ---
        ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
        Mockito.verify(alertDao).addTaskResultAlert(alertCaptor.capture());

        Alert captured = alertCaptor.getValue();
        Assertions.assertEquals(AlertStatus.WAIT_EXECUTION, captured.getAlertStatus(),
                "Task-result alert must be initialized with WAIT_EXECUTION so the alert server can poll it");
        Assertions.assertEquals("SQL Task Result", captured.getTitle());
        Assertions.assertEquals(WarningType.SUCCESS, captured.getWarningType());
        Assertions.assertEquals(AlertType.TASK_RESULT, captured.getAlertType());
        Assertions.assertEquals(1001, captured.getWorkflowInstanceId());
        Assertions.assertEquals(2001L, captured.getProjectCode());
        Assertions.assertEquals(3001L, captured.getWorkflowDefinitionCode());
        Assertions.assertEquals(1, captured.getAlertGroupId());
        Assertions.assertNotNull(captured.getCreateTime());
    }

    /**
     * When taskAlertInfo is null, no alert should be persisted.
     */
    @Test
    void sendTaskResultAlert_nullTaskAlertInfo_doesNothing() {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(1001);
        TaskInstance taskInstance = new TaskInstance();

        workflowAlertManager.sendTaskResultAlert(workflowInstance, taskInstance, null);

        Mockito.verifyNoInteractions(alertDao);
    }

    /**
     * When alertGroupId is null, no alert should be persisted.
     */
    @Test
    void sendTaskResultAlert_nullAlertGroupId_doesNothing() {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(1001);
        TaskInstance taskInstance = new TaskInstance();

        TaskAlertInfo taskAlertInfo = new TaskAlertInfo();
        taskAlertInfo.setTitle("SQL Task Result");
        taskAlertInfo.setContent("[]");
        taskAlertInfo.setAlertGroupId(null);

        workflowAlertManager.sendTaskResultAlert(workflowInstance, taskInstance, taskAlertInfo);

        Mockito.verifyNoInteractions(alertDao);
    }
}
