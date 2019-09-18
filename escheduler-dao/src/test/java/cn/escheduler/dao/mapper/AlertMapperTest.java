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
package cn.escheduler.dao.mapper;

import cn.escheduler.common.enums.AlertStatus;
import cn.escheduler.common.enums.AlertType;
import cn.escheduler.common.enums.ShowType;
import cn.escheduler.dao.datasource.ConnectionFactory;
import cn.escheduler.dao.model.Alert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * alert mapper test
 */
public class AlertMapperTest {


    AlertMapper alertMapper;

    @Before
    public void before(){
        alertMapper = ConnectionFactory.getSqlSession().getMapper(AlertMapper.class);
    }

    @Test
    public void testMapper(){
        Alert alert = new Alert();
        alert.setAlertType(AlertType.EMAIL);
        alert.setContent("content test ");
        alert.setShowType(ShowType.TABLE);
        alert.setTitle("alert test");
        alert.setAlertGroupId(1);
        alert.setCreateTime(new Date());
        alert.setUpdateTime(new Date());
        alert.setAlertStatus(AlertStatus.WAIT_EXECUTION);
        alertMapper.insert(alert);
        Assert.assertNotEquals(alert.getId(), 0);

        alert.setTitle("alert title");
        int update = alertMapper.update(AlertStatus.EXECUTION_SUCCESS, "execute successfully",
                new Date(), alert.getId());

        Assert.assertEquals(update, 1);
        int delete = alertMapper.delete(alert.getId());
        Assert.assertEquals(delete, 1);
    }
}
