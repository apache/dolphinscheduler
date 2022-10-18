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

package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.Alert;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * alert mapper test
 */
public class AlertMapperTest extends BaseDaoTest {

    @Autowired
    private AlertMapper alertMapper;

    /**
     * test insert
     *
     * @return
     */
    @Test
    public void testInsert() {
        Alert expectedAlert = createAlert();
        Assertions.assertTrue(expectedAlert.getId() > 0);
    }

    /**
     * test select by id
     *
     * @return
     */
    @Test
    public void testSelectById() {
        Alert expectedAlert = createAlert();
        Alert actualAlert = alertMapper.selectById(expectedAlert.getId());
        Assertions.assertEquals(expectedAlert, actualAlert);
    }

    /**
     * test update
     */
    @Test
    public void testUpdate() {

        Alert expectedAlert = createAlert();

        expectedAlert.setAlertStatus(AlertStatus.EXECUTION_FAILURE);
        expectedAlert.setLog("error");
        expectedAlert.setUpdateTime(DateUtils.getCurrentDate());

        alertMapper.updateById(expectedAlert);

        Alert actualAlert = alertMapper.selectById(expectedAlert.getId());

        Assertions.assertEquals(expectedAlert, actualAlert);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete() {
        Alert expectedAlert = createAlert();

        alertMapper.deleteById(expectedAlert.getId());

        Alert actualAlert = alertMapper.selectById(expectedAlert.getId());

        Assertions.assertNull(actualAlert);
    }

    /**
     * create alert map
     *
     * @param count       alert count
     * @param alertStatus alert status
     * @return alert map
     */
    private Map<Integer, Alert> createAlertMap(Integer count, AlertStatus alertStatus) {
        Map<Integer, Alert> alertMap = new HashMap<>();

        for (int i = 0; i < count; i++) {
            Alert alert = createAlert(alertStatus);
            alertMap.put(alert.getId(), alert);
        }
        return alertMap;
    }

    /**
     * create alert
     *
     * @return alert
     * @throws Exception
     */
    private Alert createAlert() {
        return createAlert(AlertStatus.WAIT_EXECUTION);
    }

    /**
     * create alert
     *
     * @param alertStatus alert status
     * @return alert
     */
    private Alert createAlert(AlertStatus alertStatus) {
        String content = "[{'type':'WORKER','host':'192.168.xx.xx','event':'server down','warning level':'serious'}]";
        Alert alert = new Alert();
        alert.setTitle("test alert");
        alert.setContent(content);
        alert.setSign(DigestUtils.sha1Hex(content));
        alert.setAlertStatus(alertStatus);
        alert.setWarningType(WarningType.FAILURE);
        alert.setLog("success");
        alert.setCreateTime(DateUtils.getCurrentDate());
        alert.setUpdateTime(DateUtils.getCurrentDate());

        alertMapper.insert(alert);
        return alert;
    }
}
