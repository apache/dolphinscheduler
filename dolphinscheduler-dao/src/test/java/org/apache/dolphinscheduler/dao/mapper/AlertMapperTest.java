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
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.ShowType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 *  alert mapper test
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
public class AlertMapperTest {

    @Autowired
    private AlertMapper alertMapper;

    /**
     * test insert
     * @return
     */
    @Test
    public void testInsert(){
        Alert expectedAlert = createAlert();
        assertNotNull(expectedAlert.getId());
        assertThat(expectedAlert.getId(), greaterThan(0));
    }


    /**
     * test select by id
     * @return
     */
    @Test
    public void testSelectById(){
        Alert expectedAlert = createAlert();
        Alert actualAlert = alertMapper.selectById(expectedAlert.getId());
        assertEquals(expectedAlert, actualAlert);
    }

    /**
     * test update
     */
    @Test
    public void testUpdate(){

        Alert expectedAlert = createAlert();

        expectedAlert.setAlertStatus(AlertStatus.EXECUTION_FAILURE);
        expectedAlert.setLog("error");
        expectedAlert.setUpdateTime(DateUtils.getCurrentDate());

        alertMapper.updateById(expectedAlert);

        Alert actualAlert = alertMapper.selectById(expectedAlert.getId());

        assertEquals(expectedAlert, actualAlert);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){
        Alert expectedAlert = createAlert();

        alertMapper.deleteById(expectedAlert.getId());

        Alert actualAlert = alertMapper.selectById(expectedAlert.getId());

        assertNull(actualAlert);
    }


    /**
     * test list alert by status
     */
    @Test
    public void testListAlertByStatus() {
        Integer count = 10;
        AlertStatus waitExecution = AlertStatus.WAIT_EXECUTION;

        Map<Integer,Alert> expectedAlertMap = createAlertMap(count, waitExecution);

        List<Alert> actualAlerts = alertMapper.listAlertByStatus(waitExecution);

        for (Alert actualAlert : actualAlerts){
            Alert expectedAlert = expectedAlertMap.get(actualAlert.getId());
            if (expectedAlert != null){
                assertEquals(expectedAlert,actualAlert);
            }
        }
    }

    /**
     *  create alert map
     * @param count alert count
     * @param alertStatus alert status
     * @return alert map
     */
    private Map<Integer,Alert> createAlertMap(Integer count,AlertStatus alertStatus){
        Map<Integer,Alert> alertMap = new HashMap<>();

        for (int i = 0 ; i < count ;i++){
            Alert alert = createAlert(alertStatus);
            alertMap.put(alert.getId(),alert);
        }

        return alertMap;

    }


    /**
     * create alert
     * @return alert
     * @throws Exception
     */
    private Alert createAlert(){
        return createAlert(AlertStatus.WAIT_EXECUTION);
    }

    /**
     * create alert
     * @param alertStatus alert status
     * @return alert
     */
    private Alert createAlert(AlertStatus alertStatus){
        Alert alert = new Alert();
        alert.setShowType(ShowType.TABLE);
        alert.setTitle("test alert");
        alert.setContent("[{'type':'WORKER','host':'192.168.xx.xx','event':'server down','warning level':'serious'}]");
        alert.setAlertType(AlertType.EMAIL);
        alert.setAlertStatus(alertStatus);
        alert.setLog("success");
        alert.setReceivers("aa@aa.com");
        alert.setReceiversCc("bb@aa.com");
        alert.setCreateTime(DateUtils.getCurrentDate());
        alert.setUpdateTime(DateUtils.getCurrentDate());

        alertMapper.insert(alert);
        return alert;
    }
}