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

import org.apache.hadoop.conf.Configuration;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
//todo there is no hadoop environment
public class HadoopUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(HadoopUtilsTest.class);
    private HadoopUtils hadoopUtils = HadoopUtils.getInstance();

    @Test
    public void getActiveRMTest() {
        try {
            hadoopUtils.getAppAddress("http://ark1:8088/ws/v1/cluster/apps/%s", "192.168.xx.xx,192.168.xx.xx");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Test
    public void getConfiguration() {
        Configuration conf = hadoopUtils.getConfiguration();

    }

    @Test
    public void isYarnEnabled() {
        boolean result = hadoopUtils.isYarnEnabled();
        Assert.assertEquals(true, result);
    }

    @Test
    public void getApplicationStatus() {
        try {
            logger.info(hadoopUtils.getApplicationStatus("application_1542010131334_0029").toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Test(expected = Exception.class)
    public void getApplicationUrl() throws Exception {
        String applicationUrl = hadoopUtils.getApplicationUrl("application_1529051418016_0167");
        logger.info(applicationUrl);
    }

    @Test
    public void getJobHistoryUrl() {
        String applicationUrl = hadoopUtils.getJobHistoryUrl("application_1529051418016_0167");
        logger.info(applicationUrl);
    }
}
