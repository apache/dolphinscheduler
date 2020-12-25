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
package org.apache.dolphinscheduler.server.utils;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProcessUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(ProcessUtilsTest.class);

    @Test
    public void getPidsStr() throws Exception {
        String pidList = ProcessUtils.getPidsStr(1);
        Assert.assertNotEquals("The child process of process 1 should not be empty", pidList, "");
        logger.info("Sub process list : {}", pidList);
    }

    @Test
    public void testBuildCommandStr() {
        List<String> commands = new ArrayList<>();
        commands.add("sudo");
        try {
            Assert.assertEquals(ProcessUtils.buildCommandStr(commands), "sudo");
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

}
