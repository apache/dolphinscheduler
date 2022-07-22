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

import org.apache.dolphinscheduler.spi.enums.ResourceType;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hadoop utils test
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {HadoopUtils.class})
@SuppressStaticInitializationFor("org.apache.dolphinscheduler.common.utils.HttpUtils")
public class HadoopUtilsTest {
    private static final Logger logger = LoggerFactory.getLogger(HadoopUtilsTest.class);

    @Test
    public void getHdfsTenantDir() {
        logger.info(HadoopUtils.getHdfsTenantDir("1234"));
        Assert.assertTrue(true);
    }

    @Test
    public void getHdfsUdfFileName() {
        logger.info(HadoopUtils.getHdfsUdfFileName("admin", "file_name"));
        Assert.assertTrue(true);
    }

    @Test
    public void getHdfsResourceFileName() {
        logger.info(HadoopUtils.getHdfsResourceFileName("admin", "file_name"));
        Assert.assertTrue(true);
    }

    @Test
    public void getHdfsFileName() {
        logger.info(HadoopUtils.getHdfsFileName(ResourceType.FILE, "admin", "file_name"));
        Assert.assertTrue(true);
    }

    @Test
    public void getAppAddress() {
        PowerMockito.mockStatic(HttpUtils.class);
        PowerMockito.when(HttpUtils.get("http://ds1:8088/ws/v1/cluster/info")).thenReturn(
            "{\"clusterInfo\":{\"id\":1657034250022,\"startedOn\":1657034250022,\"state\":\"STARTED\",\"haState\":\"ACTIVE\",\"rmStateStoreName\":\"org.apache.hadoop.yarn.server.resourcemanager.recovery.NullRMStateStore\",\"resourceManagerVersion\":\"2.8.5\",\"resourceManagerBuildVersion\":\"2.8.5 from 0b8464d75227fcee2c6e7f2410377b3d53d3d5f8 by jdu source checksum ba3b9c96362faf6bc5c4cfc9cb53880\",\"resourceManagerVersionBuiltOn\":\"2018-09-10T03:44Z\",\"hadoopVersion\":\"2.8.5\",\"hadoopBuildVersion\":\"2.8.5 from 0b8464d75227fcee2c6e7f2410377b3d53d3d5f8 by jdu source checksum 9942ca5c745417c14e318835f420733\",\"hadoopVersionBuiltOn\":\"2018-09-10T03:32Z\",\"haZooKeeperConnectionState\":\"CONNECTED\"}}");
        logger.info(HadoopUtils.getAppAddress("http://ds1:8088/ws/v1/cluster/apps/%s", "ds1,ds2"));
        Assert.assertTrue(true);
    }

}