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
package org.apache.dolphinscheduler.common.utils;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSUtilsTest {
    private static final Logger logger = LoggerFactory.getLogger(OSUtilsTest.class);

    @Test
    public void getUserList() {
        List<String> userList = OSUtils.getUserList();
        Assert.assertNotEquals("System user list should not be empty", userList.size(), 0);
        logger.info("OS user list : {}", userList.toString());
    }

    @Test
    public void testOSMetric(){
        double availablePhysicalMemorySize = OSUtils.availablePhysicalMemorySize();
        Assert.assertTrue(availablePhysicalMemorySize > 0.0f);
        double totalMemorySize = OSUtils.totalMemorySize();
        Assert.assertTrue(totalMemorySize > 0.0f);
        double loadAverage = OSUtils.loadAverage();
        logger.info("loadAverage {}", loadAverage);
        double memoryUsage = OSUtils.memoryUsage();
        Assert.assertTrue(memoryUsage > 0.0f);
        double cpuUsage = OSUtils.cpuUsage();
        Assert.assertTrue(cpuUsage >= 0.0f);
    }

    @Test
    public void getGroup() {
        try {
            String group = OSUtils.getGroup();
            Assert.assertNotNull(group);
        } catch (IOException e) {
            Assert.fail("get group failed " + e.getMessage());
        }
    }

    @Test
    public void createUser() {
        boolean result = OSUtils.createUser("test123");
        if (result) {
            Assert.assertTrue("create user test123 success", true);
        } else {
            Assert.assertTrue("create user test123 fail", true);
        }
    }

    @Test
    public void exeCmd() {
        if(OSUtils.isMacOS() || !OSUtils.isWindows()){
            try {
                String result = OSUtils.exeCmd("echo helloWorld");
                Assert.assertEquals("helloWorld\n",result);
            } catch (IOException e) {
                Assert.fail("exeCmd " + e.getMessage());
            }
        }
    }
    @Test
    public void getProcessID(){
        int processId = OSUtils.getProcessID();
        Assert.assertNotEquals(0, processId);
    }
    @Test
    public void checkResource(){
        boolean resource = OSUtils.checkResource(100,0);
        Assert.assertTrue(resource);
        resource = OSUtils.checkResource(0,Double.MAX_VALUE);
        Assert.assertFalse(resource);
    }

}
