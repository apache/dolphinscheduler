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
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
        int taskInstanceId = 5001;

        Alert alert = new Alert();
        alert.setTitle("SQL Task Result");
        alert.setContent(content);
        alert.setWarningType(WarningType.SUCCESS);
        alert.setAlertGroupId(1);
        alert.setAlertStatus(AlertStatus.WAIT_EXECUTION);
        alert.setWorkflowInstanceId(workflowInstanceId);
        alert.setTaskInstanceId(taskInstanceId);
        alert.setAlertType(AlertType.TASK_RESULT);
        alert.setCreateTime(new java.util.Date());

        // First insert should succeed
        int firstCount = alertDao.addTaskResultAlert(alert);
        Assertions.assertEquals(1, firstCount);

        // Second insert with the same dedup key should be skipped
        Alert duplicateAlert = new Alert();
        duplicateAlert.setTitle("SQL Task Result");
        duplicateAlert.setContent(content);
        duplicateAlert.setWarningType(WarningType.SUCCESS);
        duplicateAlert.setAlertGroupId(1);
        duplicateAlert.setAlertStatus(AlertStatus.WAIT_EXECUTION);
        duplicateAlert.setWorkflowInstanceId(workflowInstanceId);
        duplicateAlert.setTaskInstanceId(taskInstanceId);
        duplicateAlert.setAlertType(AlertType.TASK_RESULT);
        duplicateAlert.setCreateTime(new java.util.Date());

        int secondCount = alertDao.addTaskResultAlert(duplicateAlert);
        Assertions.assertEquals(0, secondCount);

        long count = alertDao.listAlerts(workflowInstanceId)
                .stream()
                .filter(a -> a.getAlertType() == AlertType.TASK_RESULT)
                .count();
        Assertions.assertEquals(1L, count);
    }

    /**
     * Two different tasks in the same workflow instance returning identical results
     * must NOT be treated as duplicates.
     */
    @Test
    void testAddTaskResultAlertDifferentTaskSameContentNotDuplicate() {
        String content = "[{\"result\":\"ok\"}]";
        int workflowInstanceId = 999998;

        // First task alert
        Alert alert1 = new Alert();
        alert1.setTitle("SQL Task A Result");
        alert1.setContent(content);
        alert1.setWarningType(WarningType.SUCCESS);
        alert1.setAlertGroupId(1);
        alert1.setAlertStatus(AlertStatus.WAIT_EXECUTION);
        alert1.setWorkflowInstanceId(workflowInstanceId);
        alert1.setTaskInstanceId(6001);
        alert1.setAlertType(AlertType.TASK_RESULT);
        alert1.setCreateTime(new java.util.Date());
        int firstCount = alertDao.addTaskResultAlert(alert1);
        Assertions.assertEquals(1, firstCount);

        // Second task alert — same content, different task instance
        Alert alert2 = new Alert();
        alert2.setTitle("SQL Task B Result");
        alert2.setContent(content);
        alert2.setWarningType(WarningType.SUCCESS);
        alert2.setAlertGroupId(2);
        alert2.setAlertStatus(AlertStatus.WAIT_EXECUTION);
        alert2.setWorkflowInstanceId(workflowInstanceId);
        alert2.setTaskInstanceId(6002);
        alert2.setAlertType(AlertType.TASK_RESULT);
        alert2.setCreateTime(new java.util.Date());
        int secondCount = alertDao.addTaskResultAlert(alert2);
        Assertions.assertEquals(1, secondCount);

        long count = alertDao.listAlerts(workflowInstanceId)
                .stream()
                .filter(a -> a.getAlertType() == AlertType.TASK_RESULT)
                .count();
        Assertions.assertEquals(2L, count);
    }

    /**
     * Concurrent calls to addTaskResultAlert with the same dedup key must insert exactly one row.
     * The uk_alert_dedup unique constraint rejects duplicates; the DAO layer catches
     * DuplicateKeyException and returns 0.
     */
    @Test
    void testConcurrentAddTaskResultAlertIdempotent() throws Exception {
        String content = "[{\"taskName\":\"sql-concurrent\",\"result\":\"ok\"}]";
        int workflowInstanceId = 999997;
        int taskInstanceId = 7001;
        int threadCount = 8;

        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        List<Future<Integer>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                startLatch.await();

                Alert alert = new Alert();
                alert.setTitle("SQL Task Result");
                alert.setContent(content);
                alert.setWarningType(WarningType.SUCCESS);
                alert.setAlertGroupId(1);
                alert.setAlertStatus(AlertStatus.WAIT_EXECUTION);
                alert.setWorkflowInstanceId(workflowInstanceId);
                alert.setTaskInstanceId(taskInstanceId);
                alert.setAlertType(AlertType.TASK_RESULT);
                alert.setCreateTime(new java.util.Date());

                return alertDao.addTaskResultAlert(alert);
            }));
        }

        startLatch.countDown();
        executor.shutdown();

        int successCount = 0;
        for (Future<Integer> f : futures) {
            int inserted = f.get();
            if (inserted > 0) {
                successCount++;
            }
        }

        Assertions.assertEquals(1, successCount);

        long dbCount = alertDao.listAlerts(workflowInstanceId)
                .stream()
                .filter(a -> a.getAlertType() == AlertType.TASK_RESULT)
                .count();
        Assertions.assertEquals(1L, dbCount);
    }

    /**
     * Duplicate calls to addAlert with the same dedup key must be skipped instead of
     * throwing, so existing workflow alert paths stay safe once the uk_alert_dedup
     * unique constraint applies to all inserts.
     */
    @Test
    void testAddAlertIdempotent() {
        String content = "[{\"workflowInstanceId\":888881,\"event\":\"FAILURE\"}]";
        int workflowInstanceId = 888881;

        Alert alert = new Alert();
        alert.setTitle("Workflow Failure");
        alert.setContent(content);
        alert.setWarningType(WarningType.FAILURE);
        alert.setAlertGroupId(1);
        alert.setAlertStatus(AlertStatus.WAIT_EXECUTION);
        alert.setWorkflowInstanceId(workflowInstanceId);
        alert.setAlertType(AlertType.WORKFLOW_INSTANCE_FAILURE);
        alert.setCreateTime(new java.util.Date());

        int firstCount = alertDao.addAlert(alert);
        Assertions.assertEquals(1, firstCount);

        // Second insert with the same dedup key must be skipped, not throw
        Alert duplicateAlert = new Alert();
        duplicateAlert.setTitle("Workflow Failure");
        duplicateAlert.setContent(content);
        duplicateAlert.setWarningType(WarningType.FAILURE);
        duplicateAlert.setAlertGroupId(1);
        duplicateAlert.setAlertStatus(AlertStatus.WAIT_EXECUTION);
        duplicateAlert.setWorkflowInstanceId(workflowInstanceId);
        duplicateAlert.setAlertType(AlertType.WORKFLOW_INSTANCE_FAILURE);
        duplicateAlert.setCreateTime(new java.util.Date());

        int secondCount = alertDao.addAlert(duplicateAlert);
        Assertions.assertEquals(0, secondCount);

        long count = alertDao.listAlerts(workflowInstanceId)
                .stream()
                .filter(a -> a.getAlertType() == AlertType.WORKFLOW_INSTANCE_FAILURE)
                .count();
        Assertions.assertEquals(1L, count);
    }

    /**
     * Repeated timeout alerts for the same task share the same dedup key; the unique
     * constraint must skip the duplicate instead of throwing in the timeout alert path.
     */
    @Test
    void testSendTaskTimeoutAlertIdempotent() {
        int workflowInstanceId = 888882;

        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(workflowInstanceId);
        workflowInstance.setWarningGroupId(1);
        workflowInstance.setProjectCode(1L);
        workflowInstance.setWorkflowDefinitionCode(1L);
        workflowInstance.setName("timeout-workflow");
        workflowInstance.setCommandType(CommandType.START_PROCESS);
        workflowInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
        workflowInstance.setRunTimes(1);
        workflowInstance.setStartTime(new java.util.Date());
        workflowInstance.setHost("127.0.0.1:5678");

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setName("timeout-task");
        taskInstance.setTaskType("SQL");
        taskInstance.setStartTime(new java.util.Date());
        taskInstance.setHost("127.0.0.1:1234");

        ProjectUser projectUser = new ProjectUser();
        projectUser.setProjectCode(1L);
        projectUser.setProjectName("timeout-project");
        projectUser.setUserName("admin");

        // First insert should succeed, the duplicate must be skipped without throwing
        alertDao.sendTaskTimeoutAlert(workflowInstance, taskInstance, projectUser);
        alertDao.sendTaskTimeoutAlert(workflowInstance, taskInstance, projectUser);

        long count = alertDao.listAlerts(workflowInstanceId)
                .stream()
                .filter(a -> a.getAlertType() == AlertType.TASK_TIMEOUT)
                .count();
        Assertions.assertEquals(1L, count);
    }
}
