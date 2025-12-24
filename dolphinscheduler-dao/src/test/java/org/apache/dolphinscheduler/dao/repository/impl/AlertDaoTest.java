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

import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AlertDaoTest extends BaseDaoTest {

    @Autowired
    private AlertDao alertDao;

    @Test
    void testAlertDao() {
        Alert alert = new Alert();
        alert.setTitle("Mysql Exception");
        alert.setContent("[\"alarm time：2018-02-05\", \"service name：MYSQL_ALTER\", \"alarm name：MYSQL_ALTER_DUMP\", "
                + "\"get the alarm exception.！，interface error，exception information：timed out\", \"request address：http://blog.csdn.net/dreamInTheWorld/article/details/78539286\"]");
        alert.setAlertGroupId(1);
        alert.setAlertStatus(AlertStatus.WAIT_EXECUTION);
        alertDao.addAlert(alert);

        List<Alert> alerts = alertDao.listPendingAlerts(-1);
        Assertions.assertNotNull(alerts);
        Assertions.assertNotEquals(0, alerts.size());
    }

    @Test
    void testAddAlertSendStatus() {
        int insertCount = alertDao.addAlertSendStatus(AlertStatus.EXECUTION_SUCCESS, "success", 1, 1);
        Assertions.assertEquals(1, insertCount);
    }

    @Test
    void testSendServerStoppedAlert() {
        String host = "127.0.0.998165432";
        String serverType = "Master";
        alertDao.sendServerStoppedAlert(host, serverType);
        alertDao.sendServerStoppedAlert(host, serverType);
        long count = alertDao.listPendingAlerts(-1)
                .stream()
                .filter(alert -> alert.getContent().contains(host))
                .count();
        Assertions.assertEquals(1L, count);
    }

    @Test
    void testSendWorkflowTimeoutAlert() {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(1);
        workflowInstance.setName("test-workflow-timeout");
        workflowInstance.setWorkflowDefinitionCode(100L);
        workflowInstance.setCommandType(CommandType.START_PROCESS);
        workflowInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
        workflowInstance.setStartTime(new Date());
        workflowInstance.setHost("localhost");
        workflowInstance.setWarningGroupId(1);

        ProjectUser projectUser = new ProjectUser();
        projectUser.setProjectCode(1L);
        projectUser.setProjectName("test-project");
        projectUser.setUserName("admin");

        alertDao.sendWorkflowTimeoutAlert(workflowInstance, projectUser, "admin");

        List<Alert> alerts = alertDao.listPendingAlerts(-1);
        Assertions.assertNotNull(alerts);

        long timeoutAlertCount = alerts.stream()
                .filter(alert -> AlertType.WORKFLOW_INSTANCE_TIMEOUT.equals(alert.getAlertType()))
                .filter(alert -> alert.getWorkflowInstanceId() != null
                        && alert.getWorkflowInstanceId().equals(workflowInstance.getId()))
                .count();
        Assertions.assertEquals(1L, timeoutAlertCount);

        Alert timeoutAlert = alerts.stream()
                .filter(alert -> AlertType.WORKFLOW_INSTANCE_TIMEOUT.equals(alert.getAlertType()))
                .filter(alert -> alert.getWorkflowInstanceId() != null
                        && alert.getWorkflowInstanceId().equals(workflowInstance.getId()))
                .findFirst()
                .orElse(null);

        Assertions.assertNotNull(timeoutAlert);
        Assertions.assertEquals("Workflow Timeout Warn", timeoutAlert.getTitle());
        Assertions.assertEquals(projectUser.getProjectCode(), timeoutAlert.getProjectCode());
        Assertions.assertEquals(workflowInstance.getWorkflowDefinitionCode(),
                timeoutAlert.getWorkflowDefinitionCode());
        Assertions.assertEquals(workflowInstance.getId(), timeoutAlert.getWorkflowInstanceId());
        Assertions.assertTrue(timeoutAlert.getContent().contains("test-workflow-timeout"));
    }
}
