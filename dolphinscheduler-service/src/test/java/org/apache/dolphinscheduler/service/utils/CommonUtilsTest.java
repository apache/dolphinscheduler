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

package org.apache.dolphinscheduler.service.utils;

import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.service.storage.impl.HadoopUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * configuration test
 */
@ExtendWith(MockitoExtension.class)
public class CommonUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtilsTest.class);

    @Test
    public void getSystemEnvPath() {
        String envPath;
        envPath = CommonUtils.getSystemEnvPath();
        Assertions.assertEquals("/etc/profile", envPath);
    }

    @Test
    public void isDevelopMode() {
        logger.info("develop mode: {}", CommonUtils.isDevelopMode());
        Assertions.assertTrue(true);
    }

    @Test
    public void getHdfsDataBasePath() {
        logger.info(HadoopUtils.getHdfsDataBasePath());
        Assertions.assertTrue(true);
    }

    @Test
    public void getDownloadFilename() {
        logger.info(FileUtils.getDownloadFilename("a.txt"));
        Assertions.assertTrue(true);
    }

    @Test
    public void getUploadFilename() {
        logger.info(FileUtils.getUploadFilename("1234", "a.txt"));
        Assertions.assertTrue(true);
    }

    @Test
    public void getHdfsDir() {
        logger.info(HadoopUtils.getHdfsResDir("1234"));
        Assertions.assertTrue(true);
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
        Assertions.assertTrue(true);
    }

}
