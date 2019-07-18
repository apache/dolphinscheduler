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
package cn.escheduler.common.os;


import cn.escheduler.common.utils.OSUtils;
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
//    static SystemInfo si = new SystemInfo();
//    static HardwareAbstractionLayer hal = si.getHardware();


    @Test
    public void getHost(){
        logger.info(OSUtils.getHost());
    }


    @Test
    public void memoryUsage() {
        logger.info("memoryUsage : {}", OSUtils.memoryUsage());// 0.3361799418926239
//        printMemory(hal.getMemory());// 35 %
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
        Thread.sleep(1000l);
        logger.info("cpuUsage : {}", OSUtils.cpuUsage());

        double cpuUsage = OSUtils.cpuUsage();

        DecimalFormat df = new DecimalFormat("0.00");

        df.setRoundingMode(RoundingMode.HALF_UP);

        logger.info("cpuUsage1 : {}", df.format(cpuUsage));
    }


//
//    @Test
//    public void getUserList() {
//        logger.info("getUserList : {}", OSUtils.getUserList());
//    }
//
//
//    @Test
//    public void getGroup() throws Exception {
//        logger.info("getGroup : {}", OSUtils.getGroup());
//        logger.info("getGroup : {}", OSUtils.exeShell("groups"));
//
//
//    }
//
//
//    @Test
//    public void getProcessID() {
//        logger.info("getProcessID : {}", OSUtils.getProcessID());
//    }
//
//
//    @Test
//    public void getHost() {
//        logger.info("getHost : {}", OSUtils.getHost());
//    }
//
//
//
//    @Test
//    public void anotherGetOsInfoTest() throws InterruptedException {
//        OperatingSystemMXBean os = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
//        final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
//
//        MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage();
//        double usage = (double)memoryUsage.getUsed() / (double)memoryUsage.getCommitted();
//        logger.info("memory usage : {}",usage);
//
//        if (os instanceof UnixOperatingSystemMXBean) {
//            UnixOperatingSystemMXBean unixOs = (UnixOperatingSystemMXBean) os;
//            logger.info("getMaxFileDescriptorCount : {}" ,unixOs.getMaxFileDescriptorCount()); //10240
//            logger.info("getOpenFileDescriptorCount : {}",unixOs.getOpenFileDescriptorCount()); //241
//            logger.info("getAvailableProcessors : {}",unixOs.getAvailableProcessors()); //8
//
//            logger.info("getSystemLoadAverage : {}",unixOs.getSystemLoadAverage()); //1.36083984375
//
//            logger.info("getFreePhysicalMemorySize : {}",unixOs.getFreePhysicalMemorySize()); //209768448
//
//            logger.info("getTotalPhysicalMemorySize : {}",unixOs.getTotalPhysicalMemorySize()); //17179869184  16G
//
//            for(int i = 0; i < 3; i++) {
//                logger.info("getSystemCpuLoad : {}", unixOs.getSystemCpuLoad()); //0.0
//
//                logger.info("getProcessCpuLoad : {}", unixOs.getProcessCpuLoad() * 10); //0.0
//                Thread.sleep(1000l);
//            }
//        }
//    }
//

}
