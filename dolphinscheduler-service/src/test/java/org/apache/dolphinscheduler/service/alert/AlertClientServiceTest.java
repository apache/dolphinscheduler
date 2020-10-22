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

package org.apache.dolphinscheduler.service.alert;

import org.apache.dolphinscheduler.remote.command.alert.AlertSendResponseCommand;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * alert client service test
 */
public class AlertClientServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(AlertClientServiceTest.class);


    @Test
    public void testSendAlert(){
        String host;
        int port = 50501;
        int groupId = 1;
        String title = "test-title";
        String content = "test-content";
        AlertClientService alertClient = new AlertClientService();

        // alter server does not exist
        host = "128.0.10.1";
        AlertSendResponseCommand alertSendResponseCommand = alertClient.sendAlert(host, port, groupId, title, content);
        Assert.assertNull(alertSendResponseCommand);

        host = "127.0.0.1";
        AlertSendResponseCommand alertSendResponseCommand_1 = alertClient.sendAlert(host, port, groupId, title, content);

        if (Objects.nonNull(alertClient) && alertClient.isRunning()) {
            alertClient.close();
        }

    }
}
