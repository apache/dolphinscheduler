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
package org.apache.dolphinscheduler.common.utils.process;

import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(OSUtils.class)
public class ProcessBuilderForWin32Test {

    private static final Logger logger = LoggerFactory.getLogger(ProcessBuilderForWin32Test.class);

    @Before
    public void before() {
        PowerMockito.mockStatic(OSUtils.class);
        PowerMockito.when(OSUtils.isWindows()).thenReturn(true);
    }

    @Test
    public void testCreateProcessBuilderForWin32() {
        try {
            ProcessBuilderForWin32 builder = new ProcessBuilderForWin32();
            Assert.assertNotNull(builder);

            builder = new ProcessBuilderForWin32("net");
            Assert.assertNotNull(builder);

            builder = new ProcessBuilderForWin32(Collections.singletonList("net"));
            Assert.assertNotNull(builder);

            try {
                builder = new ProcessBuilderForWin32((List<String>) null);
                Assert.assertNotNull(builder);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void testBuildUser() {
        try {
            ProcessBuilderForWin32 builder = new ProcessBuilderForWin32();
            builder.user("test", StringUtils.EMPTY);
            Assert.assertNotNull(builder);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void testBuildCommand() {
        try {
            ProcessBuilderForWin32 builder = new ProcessBuilderForWin32();
            builder.command(Collections.singletonList("net"));
            Assert.assertNotEquals(0, builder.command().size());

            builder = new ProcessBuilderForWin32();
            builder.command("net");
            Assert.assertNotEquals(0, builder.command().size());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void testEnvironment() {
        try {
            ProcessBuilderForWin32 builder = new ProcessBuilderForWin32();
            Assert.assertNotNull(builder.environment());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        try {
            Process process = Runtime.getRuntime().exec("net", new String[]{ "a=123" });
            Assert.assertNotNull(process);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void testDirectory() {
        try {
            ProcessBuilderForWin32 builder = new ProcessBuilderForWin32();
            builder.directory(new File("/tmp"));
            Assert.assertNotNull(builder.directory());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void testRedirect() {
        try {
            ProcessBuilderForWin32 builder = new ProcessBuilderForWin32();
            builder.redirectInput(new File("/tmp"));
            Assert.assertNotNull(builder.redirectInput());
            builder.redirectOutput(new File("/tmp"));
            Assert.assertNotNull(builder.redirectOutput());
            builder.redirectError(new File("/tmp"));
            Assert.assertNotNull(builder.redirectError());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void runCmdViaUser() {
        try {
            ProcessBuilderForWin32 builder = new ProcessBuilderForWin32();
            builder.user("test123", StringUtils.EMPTY);

            List<String> commands = new ArrayList<>();
            commands.add("cmd.exe");
            commands.add("/c");
            commands.add("net user");

            builder.command(commands);

            Process process = builder.start();
            BufferedReader inReader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("GBK")));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = inReader.readLine()) != null) {
                sb.append(line);
            }
            logger.info("net user: {}", sb.toString());
            Assert.assertNotEquals(StringUtils.EMPTY, sb.toString());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

}
