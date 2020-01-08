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
package org.apache.dolphinscheduler.alert.sender.impl;

import org.apache.dolphinscheduler.alert.AlertException;
import org.apache.dolphinscheduler.alert.utils.Constants;
import org.apache.dolphinscheduler.alert.utils.MailUtils;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.ShowType;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.DaoFactory;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MailUtils.class, DaoFactory.class, AlertDao.class})
public class EmailSenderTest {

    @Test
    public void testSend() throws AlertException {
        Alert alert = new Alert();
        alert.setReceivers("");
        alert.setTitle("test");
        alert.setShowType(ShowType.TABLEATTACHMENT);
        alert.setContent("test");
        alert.setAlertType(AlertType.EMAIL);

        EmailSender sender = new EmailSender();

        // test failure
        assertFalse(sender.send(alert));

        // test success
        alert.setReceivers("test@test.com");
        PowerMockito.mockStatic(MailUtils.class);
        Map<String, Object> mockResult = Collections.singletonMap(Constants.STATUS, true);
        PowerMockito.when(MailUtils.sendMails(Collections.singletonList(alert.getReceivers()), Collections.emptyList(), alert.getTitle(),
                                              alert.getContent(), alert.getShowType())).thenReturn(mockResult);
        assertTrue(sender.send(alert));
    }

    @Test
    public void testSendGroup() throws Exception {
        Alert alert = new Alert();
        alert.setTitle("test");
        alert.setShowType(ShowType.TABLEATTACHMENT);
        alert.setContent("test");
        alert.setAlertType(AlertType.EMAIL);
        alert.setAlertGroupId(1);

        AlertDao alertDao = PowerMockito.mock(AlertDao.class);
        PowerMockito.mockStatic(DaoFactory.class);
        PowerMockito.when(DaoFactory.getDaoInstance(AlertDao.class)).thenReturn(alertDao);

        EmailSender sender = new EmailSender();

        // test failure
        PowerMockito.when(alertDao.listUserByAlertgroupId(alert.getAlertGroupId())).thenReturn(Collections.emptyList());
        assertFalse(sender.sendGroup(alert));

        // test success
        User user = new User();
        user.setEmail("test@test.com");
        PowerMockito.when(alertDao.listUserByAlertgroupId(alert.getAlertGroupId())).thenReturn(Collections.singletonList(user));
        PowerMockito.mockStatic(MailUtils.class);
        Map<String, Object> mockResult = Collections.singletonMap(Constants.STATUS, true);
        PowerMockito.when(MailUtils.sendMails(Collections.singletonList("test@test.com"), alert.getTitle(),
                                              alert.getContent(), alert.getShowType())).thenReturn(mockResult);
        assertTrue(sender.sendGroup(alert));
    }

    @Test
    public void testGetName() {
        EmailSender sender = new EmailSender();
        assertEquals(sender.getName(), "EMAIL");
    }
}
