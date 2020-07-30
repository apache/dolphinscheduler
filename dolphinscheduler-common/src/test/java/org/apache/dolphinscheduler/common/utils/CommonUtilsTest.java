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

import org.junit.Assert;
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
    public void getSystemEnvPath() {
        logger.info(CommonUtils.getSystemEnvPath());
        Assert.assertTrue(true);
    }
    @Test
    public void isDevelopMode() {
        logger.info("develop mode: {}",CommonUtils.isDevelopMode());
        Assert.assertTrue(true);
    }
    @Test
    public void getKerberosStartupState(){
        logger.info("kerberos startup state: {}",CommonUtils.getKerberosStartupState());
        Assert.assertTrue(true);
    }
    @Test
    public void loadKerberosConf(){
        try {
            CommonUtils.loadKerberosConf();
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.fail("load Kerberos Conf failed");
        }
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
    public void test(){
        InetAddress IP = null;
        try {
            IP = InetAddress.getLocalHost();
            logger.info(IP.getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(true);
    }

    @Test
    public void encodePasswordOn() {
        Assert.assertEquals("",CommonUtils.encodePassword(""));
        Assert.assertNotEquals("IUAjJCVeJioxMjM0NTY=",CommonUtils.encodePassword("123456"));
        Assert.assertNotEquals("IUAjJCVeJiohUUFaWFNXQA==",CommonUtils.encodePassword("!QAZXSW@"));
        Assert.assertNotEquals("IUAjJCVeJio1ZGZnZXIoQA==",CommonUtils.encodePassword("5dfger(@"));
    }

    @Test
    public void decodePasswordOn() {
        Assert.assertEquals("",CommonUtils.decodePassword(""));
        Assert.assertNotEquals("123456",CommonUtils.decodePassword("IUAjJCVeJioxMjM0NTY="));
        Assert.assertNotEquals("!QAZXSW@",CommonUtils.decodePassword("IUAjJCVeJiohUUFaWFNXQA=="));
        Assert.assertNotEquals("5dfger(@",CommonUtils.decodePassword("IUAjJCVeJio1ZGZnZXIoQA=="));
    }


    @Test
    public void encodePassword() {
        Assert.assertEquals("",CommonUtils.encodePassword(""));
        Assert.assertEquals("123456",CommonUtils.encodePassword("123456"));
        Assert.assertEquals("!QAZXSW@",CommonUtils.encodePassword("!QAZXSW@"));
        Assert.assertEquals("5dfger(@",CommonUtils.encodePassword("5dfger(@"));
    }

    @Test
    public void decodePassword() {
        Assert.assertEquals("",CommonUtils.decodePassword(""));
        Assert.assertEquals("123456",CommonUtils.decodePassword("123456"));
        Assert.assertEquals("!QAZXSW@",CommonUtils.decodePassword("!QAZXSW@"));
        Assert.assertEquals("5dfger(@",CommonUtils.decodePassword("5dfger(@"));
    }

    @Test
    public void encodeAnddecodePassword() {
        Assert.assertEquals(CommonUtils.encodePassword(""),CommonUtils.encodePassword(CommonUtils.decodePassword(CommonUtils.encodePassword("")))) ;
        Assert.assertEquals(CommonUtils.encodePassword("dolphinscheduler"),CommonUtils.encodePassword(CommonUtils.decodePassword(CommonUtils.encodePassword("dolphinscheduler")))) ;
        Assert.assertEquals(CommonUtils.encodePassword("ITVkSDFZmdlcihA"),CommonUtils.encodePassword(CommonUtils.decodePassword(CommonUtils.encodePassword("ITVkSDFZmdlcihA")))) ;
        Assert.assertEquals(CommonUtils.encodePassword("ITVkSDFmdlcihA"),CommonUtils.encodePassword(CommonUtils.decodePassword(CommonUtils.encodePassword("ITVkSDFmdlcihA"))));
        Assert.assertEquals(CommonUtils.encodePassword("ITVkSDFZmdlc3ihA"),CommonUtils.encodePassword(CommonUtils.decodePassword(CommonUtils.encodePassword("ITVkSDFZmdlc3ihA"))));
        Assert.assertEquals(CommonUtils.encodePassword("ITVkS^8DFZmdlcihA"),CommonUtils.encodePassword(CommonUtils.decodePassword(CommonUtils.encodePassword("ITVkS^8DFZmdlcihA"))));
        Assert.assertEquals(CommonUtils.encodePassword("ITVkSDFZC你好dlcihA"),CommonUtils.encodePassword(CommonUtils.decodePassword(CommonUtils.encodePassword("ITVkSDFZC你好dlcihA"))));
        Assert.assertEquals(CommonUtils.encodePassword("ITVkSDFZm#$%^&*(dlcihA"),CommonUtils.encodePassword(CommonUtils.decodePassword(CommonUtils.encodePassword("ITVkSDFZm#$%^&*(dlcihA"))));
        Assert.assertEquals(CommonUtils.encodePassword("ITVkSDF~!@#$%^&*()ZmdlcihA"),CommonUtils.encodePassword(CommonUtils.decodePassword(CommonUtils.encodePassword("ITVkSDF~!@#$%^&*()ZmdlcihA"))));
    }
    
}