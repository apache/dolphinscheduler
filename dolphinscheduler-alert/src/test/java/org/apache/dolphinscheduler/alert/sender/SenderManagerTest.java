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
package org.apache.dolphinscheduler.alert.sender;

import org.apache.dolphinscheduler.alert.AlertException;
import org.apache.dolphinscheduler.alert.utils.Constants;
import org.apache.dolphinscheduler.alert.utils.MailUtils;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.ShowType;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.DaoFactory;
import org.apache.dolphinscheduler.dao.datasource.ConnectionFactory;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import java.util.Collections;
import java.util.Map;
import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MailUtils.class, DaoFactory.class, AlertDao.class, AlertGroupMapper.class, ConnectionFactory.class})
public class SenderManagerTest {

    @Test
    public void testSend() throws AlertException {
        Alert alert = new Alert();
        alert.setReceivers("test@test.com");
        alert.setTitle("test");
        alert.setShowType(ShowType.TABLEATTACHMENT);
        alert.setContent("test");

        PowerMockito.mockStatic(MailUtils.class);
        Map<String, Object> mockResult = Collections.singletonMap(Constants.STATUS, true);
        PowerMockito.when(MailUtils.sendMails(Collections.singletonList(alert.getReceivers()), Collections.emptyList(), alert.getTitle(),
                                              alert.getContent(), alert.getShowType())).thenReturn(mockResult);

        // test email sender
        alert.setAlertType(AlertType.EMAIL);
        assertTrue(SenderManager.send(alert));

        // test sms sender
        alert.setAlertType(AlertType.SMS);
        assertFalse(SenderManager.send(alert));
    }

    @Test
    public void testSendGroup() throws AlertException {
        Alert alert = new Alert();
        alert.setReceivers("test@test.com");
        alert.setTitle("test");
        alert.setShowType(ShowType.TABLEATTACHMENT);
        alert.setContent("test");
        alert.setAlertGroupId(1);

        AlertDao alertDao = PowerMockito.mock(AlertDao.class);
        PowerMockito.mockStatic(DaoFactory.class);
        PowerMockito.when(DaoFactory.getDaoInstance(AlertDao.class)).thenReturn(alertDao);

        User user = new User();
        user.setEmail("test@test.com");
        PowerMockito.when(alertDao.listUserByAlertgroupId(alert.getAlertGroupId())).thenReturn(Collections.singletonList(user));
        PowerMockito.mockStatic(MailUtils.class);
        Map<String, Object> mockResult = Collections.singletonMap(Constants.STATUS, true);
        PowerMockito.when(MailUtils.sendMails(Collections.singletonList("test@test.com"), alert.getTitle(),
                                              alert.getContent(), alert.getShowType())).thenReturn(mockResult);

        AlertGroupMapper alertGroupMapper = PowerMockito.mock(AlertGroupMapper.class);
        PowerMockito.mockStatic(ConnectionFactory.class);
        PowerMockito.when(ConnectionFactory.getMapper(AlertGroupMapper.class)).thenReturn(alertGroupMapper);

        AlertGroup alertGroup = new AlertGroup();
        alertGroup.setId(1);

        // test email sender
        alertGroup.setGroupType(AlertType.EMAIL);
        PowerMockito.when(alertGroupMapper.selectById(alert.getAlertGroupId())).thenReturn(alertGroup);
        assertTrue(SenderManager.sendGroup(alert));

        // test sms sender
        alertGroup.setGroupType(AlertType.SMS);
        PowerMockito.when(alertGroupMapper.selectById(alert.getAlertGroupId())).thenReturn(alertGroup);
        assertFalse(SenderManager.sendGroup(alert));
    }
}