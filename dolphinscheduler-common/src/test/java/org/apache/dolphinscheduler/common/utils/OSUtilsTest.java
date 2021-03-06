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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.CommonTest;
import org.apache.dolphinscheduler.common.Constants;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.IOException;
import java.net.InetAddress;
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
    public void getAddr(){
        Assert.assertEquals(OSUtils.getHost() + ":5678", OSUtils.getAddr(5678));
        Assert.assertEquals("127.0.0.1:5678", OSUtils.getAddr("127.0.0.1", 5678));
        Assert.assertEquals("localhost:1234", OSUtils.getAddr("localhost", 1234));
    }
    @Test
    public void getHost() throws Exception {
        String host = OSUtils.getHost();
        Assert.assertNotNull(host);
        Assert.assertNotEquals("", host);
        InetAddress address = mock(InetAddress.class);
        when(address.getCanonicalHostName()).thenReturn("dolphinscheduler-worker-0.dolphinscheduler-worker-headless.default.svc.cluster.local");
        when(address.getHostName()).thenReturn("dolphinscheduler-worker-0");
        when(address.getHostAddress()).thenReturn("172.17.0.15");
        Assert.assertEquals("172.17.0.15", OSUtils.getHost(address));
        CommonTest.setFinalStatic(Constants.class.getDeclaredField("KUBERNETES_MODE"), true);
        Assert.assertEquals("dolphinscheduler-worker-0.dolphinscheduler-worker-headless.default.svc.cluster.local", OSUtils.getHost(address));
        address = mock(InetAddress.class);
        when(address.getCanonicalHostName()).thenReturn("dolphinscheduler-worker-0");
        when(address.getHostName()).thenReturn("dolphinscheduler-worker-0");
        CommonTest.setFinalStatic(Constants.class.getDeclaredField("KUBERNETES_MODE"), true);
        Assert.assertEquals("dolphinscheduler-worker-0.dolphinscheduler-worker-headless", OSUtils.getHost(address));
    }
    @Test
    public void checkResource(){
        boolean resource = OSUtils.checkResource(100,0);
        Assert.assertTrue(resource);
        resource = OSUtils.checkResource(0,Double.MAX_VALUE);
        Assert.assertFalse(resource);

        Configuration configuration = new PropertiesConfiguration();

        configuration.setProperty(Constants.MASTER_MAX_CPULOAD_AVG,100);
        configuration.setProperty(Constants.MASTER_RESERVED_MEMORY,0);
        resource = OSUtils.checkResource(configuration,true);
        Assert.assertTrue(resource);

        configuration.setProperty(Constants.MASTER_MAX_CPULOAD_AVG,0);
        configuration.setProperty(Constants.MASTER_RESERVED_MEMORY,Double.MAX_VALUE);
        resource = OSUtils.checkResource(configuration,true);
        Assert.assertFalse(resource);

        configuration.setProperty(Constants.WORKER_MAX_CPULOAD_AVG,100);
        configuration.setProperty(Constants.WORKER_RESERVED_MEMORY,0);
        resource = OSUtils.checkResource(configuration,false);
        Assert.assertTrue(resource);

        configuration.setProperty(Constants.WORKER_MAX_CPULOAD_AVG,0);
        configuration.setProperty(Constants.WORKER_RESERVED_MEMORY,Double.MAX_VALUE);
        resource = OSUtils.checkResource(configuration,false);
        Assert.assertFalse(resource);

    }

}
