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

package org.apache.dolphinscheduler.server.log;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.service.log.LogClientService;
import org.junit.Test;

public class LoggerServerTest {


    @Test
    public void testRollViewLog(){
        LoggerServer loggerServer = new LoggerServer();
        loggerServer.start();

        LogClientService logClientService = new LogClientService();
        logClientService.rollViewLog("localhost", Constants.RPC_PORT,"/opt/demo.txt",0,1000);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {

        }

        loggerServer.stop();
        logClientService.close();
    }

    @Test
    public void testRemoveTaskLog(){
        LoggerServer loggerServer = new LoggerServer();
        loggerServer.start();

        LogClientService logClientService = new LogClientService();
        logClientService.removeTaskLog("localhost", Constants.RPC_PORT,"/opt/zhangsan");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {

        }

        loggerServer.stop();
        logClientService.close();
    }
}
