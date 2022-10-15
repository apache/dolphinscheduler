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

package org.apache.dolphinscheduler.remote.command.alert;

import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AlertSendRequestCommandTest {

    @Test
    public void testConvert2Command() {
        int groupId = 1;
        String title = "test-title";
        String content = "test-content";
        AlertSendRequestCommand requestCommand = new AlertSendRequestCommand(groupId,title,content,WarningType.FAILURE.getCode());
        Command command = requestCommand.convert2Command();
        Assertions.assertEquals(CommandType.ALERT_SEND_REQUEST, command.getType());
        AlertSendRequestCommand verifyCommand = new AlertSendRequestCommand();
        verifyCommand.setGroupId(groupId);
        verifyCommand.setContent(content);
        verifyCommand.setTitle(title);

    }
}
