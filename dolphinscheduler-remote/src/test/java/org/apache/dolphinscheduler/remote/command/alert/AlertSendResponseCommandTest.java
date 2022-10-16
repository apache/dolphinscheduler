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

import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class AlertSendResponseCommandTest {

    @Test
    public void testConvert2Command() {
        AlertSendResponseCommand alertSendResponseCommand = new AlertSendResponseCommand();
        alertSendResponseCommand.setSuccess(false);
        List<AlertSendResponseResult> responseResults = new ArrayList<>();
        AlertSendResponseResult responseResult1 = new AlertSendResponseResult();
        responseResult1.setSuccess(false);
        responseResult1.setMessage("fail");
        responseResults.add(responseResult1);

        AlertSendResponseResult responseResult2 = new AlertSendResponseResult(true, "success");
        responseResults.add(responseResult2);
        alertSendResponseCommand.setResResults(responseResults);

        Command command = alertSendResponseCommand.convert2Command(1);
        Assertions.assertEquals(CommandType.ALERT_SEND_RESPONSE, command.getType());
    }
}
