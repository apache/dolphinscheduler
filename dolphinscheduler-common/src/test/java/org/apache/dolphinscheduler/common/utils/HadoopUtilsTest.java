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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ResourceType;
import org.apache.hadoop.conf.Configuration;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Ignore
//todo there is no hadoop environment
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
    public void getConfiguration(){
        Configuration conf = HadoopUtils.getInstance().getConfiguration();

    }

    @Test
    public void mkdir() throws IOException {
       boolean result = HadoopUtils.getInstance().mkdir("/dolphinscheduler/hdfs");
       Assert.assertEquals(true, result);
    }

    @Test
    public void delete() throws IOException {
        boolean result = HadoopUtils.getInstance().delete("/dolphinscheduler/hdfs",true);
        Assert.assertEquals(true, result);
    }

    @Test
    public void exists() throws IOException {
        boolean result = HadoopUtils.getInstance().exists("/dolphinscheduler/hdfs");
        Assert.assertEquals(true, result);
    }

    @Test
    public void getHdfsDataBasePath() throws IOException {
        String result = HadoopUtils.getInstance().getHdfsDataBasePath();
        Assert.assertEquals("/dolphinscheduler", result);
    }

    @Test
    public void getHdfsResDir() throws IOException {
        String result = HadoopUtils.getInstance().getHdfsResDir("11000");
        Assert.assertEquals("/dolphinscheduler/resources/11000", result);
    }

    @Test
    public void getHdfsUserDir() throws IOException {
        String result = HadoopUtils.getInstance().getHdfsUserDir("11000",1000);
        Assert.assertEquals("/dolphinscheduler/11000/home/1000", result);
    }

    @Test
    public void getHdfsUdfDir() throws IOException {
        String result = HadoopUtils.getInstance().getHdfsUdfDir("11000");
        Assert.assertEquals("/dolphinscheduler/11000/udfs", result);
    }

    @Test
    public void getHdfsFileName() throws IOException {
        String result = HadoopUtils.getInstance().getHdfsFileName(ResourceType.FILE,"11000","aa.txt");
        Assert.assertEquals("/dolphinscheduler/resources/11000/aa.txt", result);
    }

    @Test
    public void isYarnEnabled() throws IOException {
        boolean result = HadoopUtils.getInstance().isYarnEnabled();
        Assert.assertEquals(false, result);
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
            logger.error(e.getMessage(),e);
        }
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
    public void catFileWithLimitTest()throws Exception{
        List<String> stringList = HadoopUtils.getInstance().catFile("/dolphinscheduler/hdfs/resources/WCSparkPython.py", 0, 1000);
        logger.info(String.join(",",stringList));
    }

    @Test
    public void catFileTest()throws Exception{
        byte[] content = HadoopUtils.getInstance().catFile("/dolphinscheduler/hdfs/resources/WCSparkPython.py");
        logger.info(Arrays.toString(content));
    }
}