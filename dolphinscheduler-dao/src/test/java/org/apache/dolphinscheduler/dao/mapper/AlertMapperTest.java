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
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;



@RunWith(SpringRunner.class)
@SpringBootTest
public class AlertMapperTest {

    @Autowired
    AlertMapper alertMapper;

    private Alert insertOne(){
        //insertOne
        Alert alert = new Alert();
        alert.setLog("success");
        alert.setReceivers("xx@aa.com");
        alert.setAlertGroupId(1);
        alert.setAlertStatus(AlertStatus.EXECUTION_SUCCESS);
        alert.setCreateTime(new Date());
        alert.setUpdateTime(new Date());
        alertMapper.insert(alert);
        return alert;
    }

    @Test
    public void testUpdate(){
        //insertOne
        Alert alert = insertOne();
        //update
        alert.setTitle("hello");
        int update = alertMapper.updateById(alert);
        Assert.assertEquals(update, 1);
        alertMapper.deleteById(alert.getId());
    }

    @Test
    public void testDelete(){

        Alert alert = insertOne();
        int delete = alertMapper.deleteById(alert.getId());
        Assert.assertEquals(delete, 1);
    }

    @Test
    public void testQuery() {
        Alert alert = insertOne();
        //query
        List<Alert> alerts = alertMapper.selectList(null);
        Assert.assertNotEquals(alerts.size(), 0);
        alertMapper.deleteById(alert.getId());
    }

    @Test
    public void testListAlertByStatus() {
        Alert alert = insertOne();
        //query
        List<Alert> alerts = alertMapper.listAlertByStatus(AlertStatus.EXECUTION_SUCCESS);
        Assert.assertNotEquals(alerts.size(), 0);
        alertMapper.deleteById(alert.getId());
    }
}