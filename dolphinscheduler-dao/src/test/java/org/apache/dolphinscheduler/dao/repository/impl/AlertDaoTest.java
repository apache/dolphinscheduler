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
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.Alert;

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
    void testAddTaskResultAlertIdempotent() {
        String content = "[{\"taskName\":\"sql-task-1\",\"result\":\"ok\"}]";
        int workflowInstanceId = 999999;

        Alert alert = new Alert();
        alert.setTitle("SQL Task Result");
        alert.setContent(content);
        alert.setWarningType(WarningType.SUCCESS);
        alert.setAlertGroupId(1);
        alert.setAlertStatus(AlertStatus.WAIT_EXECUTION);
        alert.setWorkflowInstanceId(workflowInstanceId);
        alert.setAlertType(AlertType.TASK_RESULT);
        alert.setCreateTime(new java.util.Date());

        // First insert should succeed
        int firstCount = alertDao.addTaskResultAlert(alert);
        Assertions.assertEquals(1, firstCount);

        // Second insert with the same content + workflowInstanceId + alertType should be skipped
        Alert duplicateAlert = new Alert();
        duplicateAlert.setTitle("SQL Task Result");
        duplicateAlert.setContent(content);
        duplicateAlert.setWarningType(WarningType.SUCCESS);
        duplicateAlert.setAlertGroupId(1);
        duplicateAlert.setAlertStatus(AlertStatus.WAIT_EXECUTION);
        duplicateAlert.setWorkflowInstanceId(workflowInstanceId);
        duplicateAlert.setAlertType(AlertType.TASK_RESULT);
        duplicateAlert.setCreateTime(new java.util.Date());

        int secondCount = alertDao.addTaskResultAlert(duplicateAlert);
        Assertions.assertEquals(0, secondCount);

        // Verify only one alert row exists for this workflow instance
        long count = alertDao.listAlerts(workflowInstanceId)
                .stream()
                .filter(a -> a.getAlertType() == AlertType.TASK_RESULT)
                .count();
        Assertions.assertEquals(1L, count);
    }
}
