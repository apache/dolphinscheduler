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

import org.apache.dolphinscheduler.common.shell.ShellExecutorTest;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class ProcessBuilderForWin32Test {

    private static final Logger logger = LoggerFactory.getLogger(ProcessBuilderForWin32Test.class);

    @Test
    public void runCmdViaUser() {
        if (OSUtils.isWindows()) {
            Assert.assertTrue(OSUtils.createUser("test123"));
            ProcessBuilderForWin32 builder = new ProcessBuilderForWin32();
            builder.user("test123", StringUtils.EMPTY);

            List<String> commands = new ArrayList<>();
            commands.add("cmd.exe");
            commands.add("/c");
            commands.add("net user");

            builder.command(commands);
            try {
                Process process = builder.start();
                BufferedReader inReader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("GBK")));
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = inReader.readLine()) != null) {
                    sb.append(line);
                }
                logger.info("net user: {}", sb.toString());
                Assert.assertNotEquals(StringUtils.EMPTY, sb.toString());
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }
    }

}
