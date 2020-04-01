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
package org.apache.dolphinscheduler.common.os;


import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.hardware.GlobalMemory;

import java.math.RoundingMode;
import java.text.DecimalFormat;


/**
 * OSUtilsTest
 */
public class OSUtilsTest {

    private static Logger logger = LoggerFactory.getLogger(OSUtilsTest.class);


    @Test
    public void getHost(){
        logger.info(OSUtils.getHost());
    }


    @Test
    public void memoryUsage() {
        logger.info("memoryUsage : {}", OSUtils.memoryUsage());// 0.3361799418926239
    }

    @Test
    public void availablePhysicalMemorySize() {
        logger.info("availablePhysicalMemorySize : {}", OSUtils.availablePhysicalMemorySize());
        logger.info("availablePhysicalMemorySize : {}", OSUtils.totalMemorySize() / 10);
    }


    @Test
    public void loadAverage() {
        logger.info("memoryUsage : {}", OSUtils.loadAverage());
    }


    private void printMemory(GlobalMemory memory) {
        logger.info("memoryUsage : {} %" , (memory.getTotal() - memory.getAvailable()) * 100 / memory.getTotal() );
    }


    @Test
    public void cpuUsage() throws Exception {
        logger.info("cpuUsage : {}", OSUtils.cpuUsage());
        Thread.sleep(1000L);
        logger.info("cpuUsage : {}", OSUtils.cpuUsage());

        double cpuUsage = OSUtils.cpuUsage();

        DecimalFormat df = new DecimalFormat("0.00");

        df.setRoundingMode(RoundingMode.HALF_UP);

        logger.info("cpuUsage1 : {}", df.format(cpuUsage));
    }
}
