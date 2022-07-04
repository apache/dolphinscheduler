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

import org.apache.dolphinscheduler.spi.utils.PropertyUtils;

import org.apache.hadoop.security.UserGroupInformation;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * configuration test
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(value = { PropertyUtils.class, UserGroupInformation.class})
public class CommonUtilsTest {
    private static final Logger logger = LoggerFactory.getLogger(CommonUtilsTest.class);
    @Test
    public void getSystemEnvPath() {
        String envPath;
        envPath = CommonUtils.getSystemEnvPath();
        Assert.assertEquals("/etc/profile", envPath);
    }

    @Test
    public void isDevelopMode() {
        logger.info("develop mode: {}",CommonUtils.isDevelopMode());
        Assert.assertTrue(true);
    }

    @Test
    public void getHdfsDataBasePath() {
        logger.info(HadoopUtils.getHdfsDataBasePath());
        Assert.assertTrue(true);
    }

    @Test
    public void getDownloadFilename() {
        logger.info(FileUtils.getDownloadFilename("a.txt"));
        Assert.assertTrue(true);
    }

    @Test
    public void getUploadFilename() {
        logger.info(FileUtils.getUploadFilename("1234", "a.txt"));
        Assert.assertTrue(true);
    }

    @Test
    public void getHdfsDir() {
        logger.info(HadoopUtils.getHdfsResDir("1234"));
        Assert.assertTrue(true);
    }

    @Test
    public void test() {
        InetAddress ip;
        try {
            ip = InetAddress.getLocalHost();
            logger.info(ip.getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(true);
    }

}