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
package org.apache.dolphinscheduler.alert;

import org.apache.dolphinscheduler.alert.sender.SenderManager;
import org.apache.dolphinscheduler.alert.utils.MailUtils;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.ShowType;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.DaoFactory;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SenderManager.class, AlertDao.class, DaoFactory.class})
public class AlertDispatchExecutorTest {

    @Test
    public void testSend() throws AlertException {
        PowerMockito.mockStatic(SenderManager.class);
        AlertDao alertDao = PowerMockito.mock(AlertDao.class);
        PowerMockito.mockStatic(DaoFactory.class);
        PowerMockito.when(DaoFactory.getDaoInstance(AlertDao.class)).thenReturn(alertDao);

        Alert alert = new Alert();
        alert.setTitle("test");
        alert.setShowType(ShowType.TABLEATTACHMENT);
        alert.setContent("test");
        alert.setAlertType(AlertType.EMAIL);
        alert.setAlertGroupId(1);

        // test senderManager.send return false and senderManager.sendGroup return  false
        PowerMockito.when(SenderManager.send(alert)).thenReturn(false);
        PowerMockito.when(SenderManager.sendGroup(alert)).thenReturn(false);
        AlertDispatchExecutor.send(alert);

        // test senderManager.send return true and senderManager.sendGroup return  false
        PowerMockito.when(SenderManager.send(alert)).thenReturn(true);
        PowerMockito.when(SenderManager.sendGroup(alert)).thenReturn(false);
        AlertDispatchExecutor.send(alert);

        // test senderManager.send return false and senderManager.sendGroup return  true
        PowerMockito.when(SenderManager.send(alert)).thenReturn(false);
        PowerMockito.when(SenderManager.sendGroup(alert)).thenReturn(true);
        AlertDispatchExecutor.send(alert);

        // test senderManager.send return true and senderManager.sendGroup return  true
        PowerMockito.when(SenderManager.send(alert)).thenReturn(true);
        PowerMockito.when(SenderManager.sendGroup(alert)).thenReturn(true);
        AlertDispatchExecutor.send(alert);

        // test alert exception
        PowerMockito.when(SenderManager.send(alert)).thenThrow(new AlertException("test exception", alert));
        PowerMockito.when(SenderManager.sendGroup(alert)).thenReturn(true);
        AlertDispatchExecutor.send(alert);
    }
}