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
package cn.escheduler.common.utils;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * configuration test
 */
public class CommonUtilsTest {
    private static final Logger logger = LoggerFactory.getLogger(CommonUtilsTest.class);
    @Test
    public void getHdfsDataBasePath() {
        logger.info(HadoopUtils.getHdfsDataBasePath());
    }

    @Test
    public void getDownloadFilename() {
        logger.info(FileUtils.getDownloadFilename("a.txt"));
    }

    @Test
    public void getUploadFilename() {
        logger.info(FileUtils.getUploadFilename("1234", "a.txt"));
    }

    @Test
    public void getHdfsDir() {
        logger.info(HadoopUtils.getHdfsResDir("1234"));
    }

    @Test
    public void test(){
        InetAddress IP = null;
        try {
            IP = InetAddress.getLocalHost();
            logger.info(IP.getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}