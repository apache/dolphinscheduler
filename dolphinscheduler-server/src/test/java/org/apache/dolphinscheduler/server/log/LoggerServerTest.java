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
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.service.log.LogClientService;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LoggerServerTest {

    private LoggerServer loggerServer;

    private LogClientService logClientService;

    @Before
    public void startServerAndClient() {
        this.loggerServer = new LoggerServer();
        this.loggerServer.start();
        this.logClientService = new LogClientService();
    }

    @Test
    public void testRollViewLog() throws IOException {
        String expectedTmpDemoString = "testRolloViewLog";
        org.apache.commons.io.FileUtils.writeStringToFile(new File("/tmp/demo.txt"), expectedTmpDemoString, Charset.defaultCharset());

        String resultTmpDemoString = this.logClientService.rollViewLog(
                "localhost", Constants.RPC_PORT,"/tmp/demo.txt", 0, 1000);

        Assert.assertEquals(expectedTmpDemoString, resultTmpDemoString.replaceAll("[\r|\n|\t]", StringUtils.EMPTY));

        FileUtils.deleteFile("/tmp/demo.txt");
    }

    @Test
    public void testRemoveTaskLog() throws IOException {
        String expectedTmpRemoveString = "testRemoveTaskLog";
        org.apache.commons.io.FileUtils.writeStringToFile(new File("/tmp/remove.txt"), expectedTmpRemoveString, Charset.defaultCharset());

        Boolean b = this.logClientService.removeTaskLog("localhost", Constants.RPC_PORT,"/tmp/remove.txt");

        Assert.assertTrue(b);

        String result = this.logClientService.viewLog("localhost", Constants.RPC_PORT,"/tmp/demo.txt");

        Assert.assertEquals(StringUtils.EMPTY, result);
    }

    @After
    public void stopServerAndClient() {
        this.loggerServer.stop();
        this.logClientService.close();
    }
}
