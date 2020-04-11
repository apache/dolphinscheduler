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

import org.apache.dolphinscheduler.common.enums.ResourceType;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@Ignore
public class HadoopUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(HadoopUtilsTest.class);

    @Test
    public void getActiveRMTest() {
        logger.info(HadoopUtils.getAppAddress("http://ark1:8088/ws/v1/cluster/apps/%s","192.168.xx.xx,192.168.xx.xx"));
    }

    @Test
    public void getApplicationStatusAddressTest(){
        logger.info(HadoopUtils.getInstance().getApplicationUrl("application_1548381297012_0030"));
    }

    @Test
    public void test() throws IOException {
        HadoopUtils.getInstance().copyLocalToHdfs("/root/teamviewer_13.1.8286.x86_64.rpm", "/journey", true, true);
    }

    @Test
    public void readFileTest(){
        try {
            byte[] bytes = HadoopUtils.getInstance().catFile("/dolphinscheduler/hdfs/resources/35435.sh");
            logger.info(new String(bytes));
        } catch (Exception e) {

        }
    }
    @Test
    public void testCapacity(){

    }
    @Test
    public void testMove(){
        HadoopUtils instance = HadoopUtils.getInstance();
        try {
            instance.copy("/opt/apptest/test.dat","/opt/apptest/test.dat.back",true,true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }


    }

    @Test
    public void getApplicationStatus() {
         logger.info(HadoopUtils.getInstance().getApplicationStatus("application_1542010131334_0029").toString());
    }

    @Test
    public void getApplicationUrl(){
        String application_1516778421218_0042 = HadoopUtils.getInstance().getApplicationUrl("application_1529051418016_0167");
        logger.info(application_1516778421218_0042);
    }

    @Test
    public void catFileTest()throws Exception{
        List<String> stringList = HadoopUtils.getInstance().catFile("/dolphinscheduler/hdfs/resources/WCSparkPython.py", 0, 1000);
        logger.info(String.join(",",stringList));
    }

    @Test
    public void getHdfsFileNameTest(){
        logger.info(HadoopUtils.getHdfsFileName(ResourceType.FILE,"test","/test"));
    }

    @Test
    public void getHdfsResourceFileNameTest(){
        logger.info(HadoopUtils.getHdfsResourceFileName("test","/test"));
    }

    @Test
    public void getHdfsUdfFileNameTest(){
        logger.info(HadoopUtils.getHdfsUdfFileName("test","/test.jar"));
    }
}