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

package org.apache.dolphinscheduler.dao;

import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.ProfileType;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles(ProfileType.H2)
@RunWith(SpringRunner.class)
@SpringBootApplication(scanBasePackageClasses = DaoConfiguration.class)
@SpringBootTest(classes = DaoConfiguration.class)
@Transactional
@Rollback
@EnableTransactionManagement
public class AlertDaoTest {
    @Autowired
    private AlertDao alertDao;

    @Autowired
    private AlertGroupMapper alertGroupMapper;

    @Test
    public void testAlertDao() {
        Alert alert = new Alert();
        alert.setTitle("Mysql Exception");
        alert.setContent("[\"alarm time：2018-02-05\", \"service name：MYSQL_ALTER\", \"alarm name：MYSQL_ALTER_DUMP\", "
                + "\"get the alarm exception.！，interface error，exception information：timed out\", \"request address：http://blog.csdn.net/dreamInTheWorld/article/details/78539286\"]");
        alert.setAlertGroupId(1);
        alert.setAlertStatus(AlertStatus.WAIT_EXECUTION);
        alertDao.addAlert(alert);

        List<Alert> alerts = alertDao.listPendingAlerts();
        Assert.assertNotNull(alerts);
        Assert.assertNotEquals(0, alerts.size());
    }

    @Test
    public void testAddAlertSendStatus() {
        int insertCount = alertDao.addAlertSendStatus(AlertStatus.EXECUTION_SUCCESS, "success", 1, 1);
        Assert.assertEquals(1, insertCount);
    }

    @Test
    public void testSendServerStoppedAlertDeduplicate() {
        String host = "127.0.0.998165432";
        String serverType = "Master";
        alertDao.sendServerStoppedAlert(host, serverType);
        alertDao.sendServerStoppedAlert(host, serverType);
        long count = alertDao.listPendingAlerts()
                .stream()
                .filter(alert -> alert.getContent().contains(host))
                .count();
        Assert.assertEquals(1L, count);
    }

    @Test
    public void testSendServerStoppedAlertCustomAlertGroup() {
        // backup and delete default alert group (recvFaultTolWarn=true)
        AlertGroup defaultGroup = alertGroupMapper.selectById(1);
        alertGroupMapper.deleteById(1);

        int groupCnt = 10;
        List<Integer> testAlertGroupIds = new ArrayList<>(groupCnt);
        for (int i = 0; i < groupCnt; i++) {
            int id = 1000 + i;
            testAlertGroupIds.add(id);
            AlertGroup alertGroup = new AlertGroup();
            alertGroup.setId(id);
            alertGroup.setRecvFaultTolWarn(true);
            int insert = alertGroupMapper.insert(alertGroup);
            Assert.assertEquals(1, insert);
        }

        String host = "127.0.0.998165432";
        String serverType = "Master";
        alertDao.sendServerStoppedAlert(host, serverType);
        long count = alertDao.listPendingAlerts()
                .stream()
                .filter(alert -> alert.getContent().contains(host))
                .count();
        Assert.assertEquals(10L, count);

        // recover database
        testAlertGroupIds.forEach(alertGroupMapper::deleteById);
        int recoverInsert = alertGroupMapper.insert(defaultGroup);
        Assert.assertEquals(1, recoverInsert);
    }
}
