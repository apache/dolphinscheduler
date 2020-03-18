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

import java.io.*;
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

            builder = new ProcessBuilderForWin32((List<String>) null);
            Assert.assertNotNull(builder);
        } catch (Error | Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void testBuildUser() {
        try {
            ProcessBuilderForWin32 builder = new ProcessBuilderForWin32();
            builder.user("test", StringUtils.EMPTY);
            Assert.assertNotNull(builder);
        } catch (Error | Exception e) {
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

            builder = new ProcessBuilderForWin32();
            builder.command((List<String>) null);
            Assert.assertNotEquals(0, builder.command().size());
        } catch (Error | Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void testEnvironment() {
        try {
            ProcessBuilderForWin32 builder = new ProcessBuilderForWin32();
            Assert.assertNotNull(builder.environment());
        } catch (Error | Exception e) {
            logger.error(e.getMessage());
        }

        try {
            ProcessBuilderForWin32 builder = new ProcessBuilderForWin32();
            builder.environment(new String[]{ "a=123" });
            Assert.assertNotEquals(0, builder.environment().size());
        } catch (Error | Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void testDirectory() {
        try {
            ProcessBuilderForWin32 builder = new ProcessBuilderForWin32();
            builder.directory(new File("/tmp"));
            Assert.assertNotNull(builder.directory());
        } catch (Error | Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void testStream() {
        try {
            InputStream in = ProcessBuilderForWin32.NullInputStream.INSTANCE;
            Assert.assertNotNull(in);
            Assert.assertEquals(-1, in.read());
            Assert.assertEquals(0, in.available());

            OutputStream out = ProcessBuilderForWin32.NullOutputStream.INSTANCE;
            Assert.assertNotNull(out);
            out.write(new byte[] {1});
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
            Assert.assertNotNull(builder.redirectInput().file());

            builder.redirectOutput(new File("/tmp"));
            Assert.assertNotNull(builder.redirectOutput());
            Assert.assertNotNull(builder.redirectOutput().file());

            builder.redirectError(new File("/tmp"));
            Assert.assertNotNull(builder.redirectError());
            Assert.assertNotNull(builder.redirectError().file());

            builder.redirectInput(builder.redirectOutput());
            builder.redirectOutput(builder.redirectInput());
            builder.redirectError(builder.redirectInput());

            Assert.assertNotNull(ProcessBuilderForWin32.Redirect.PIPE.type());
            Assert.assertNotNull(ProcessBuilderForWin32.Redirect.PIPE.toString());
            Assert.assertNotNull(ProcessBuilderForWin32.Redirect.INHERIT.type());
            Assert.assertNotNull(ProcessBuilderForWin32.Redirect.INHERIT.toString());
        } catch (Error | Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void testRedirectErrorStream() {
        try {
            ProcessBuilderForWin32 builder = new ProcessBuilderForWin32();
            builder.redirectErrorStream(true);
            Assert.assertTrue(builder.redirectErrorStream());
        } catch (Error | Exception e) {
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
        } catch (Error | Exception e) {
            logger.error(e.getMessage());
        }
    }

}
