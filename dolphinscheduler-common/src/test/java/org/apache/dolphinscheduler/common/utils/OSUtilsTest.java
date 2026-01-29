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

import org.apache.commons.lang3.SystemUtils;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class OSUtilsTest {

    @Test
    public void existTenantCodeInLinux() {
        if (SystemUtils.IS_OS_LINUX) {
            boolean test = OSUtils.existTenantCodeInLinux("root");
            Assertions.assertTrue(test);
            boolean test1 = OSUtils.existTenantCodeInLinux("xxxtt");
            Assertions.assertFalse(test1);
        } else {
            Assertions.assertFalse(false, "system must be linux");
        }

    }

    @Test
    public void existOSTenandCode() {
        if (SystemUtils.IS_OS_LINUX) {
            List<String> userList = OSUtils.getUserList();
            Assertions.assertTrue(userList.contains("root"));
            Assertions.assertFalse(userList.contains("xxxtt"));
        } else {
            Assertions.assertFalse(false, "system must be linux");
        }
    }

    @Test
    void getTotalSystemMemory() throws InterruptedException {
        double totalSystemMemory = OSUtils.getTotalSystemMemory();
        Assertions.assertTrue(totalSystemMemory > 0);
        // Assert that the memory is not changed
        Thread.sleep(1000L);
        Assertions.assertEquals(totalSystemMemory, OSUtils.getTotalSystemMemory());
    }

    @Test
    void getSystemMemoryAvailable() {
        long systemAvailableMemoryUsed = OSUtils.getSystemAvailableMemoryUsed();
        Assertions.assertTrue(systemAvailableMemoryUsed > 0);
    }

    @Test
    void getSystemMemoryUsedPercentage() {
        long totalSystemMemory = OSUtils.getTotalSystemMemory();
        long systemMemoryAvailable = OSUtils.getSystemAvailableMemoryUsed();
        double systemAvailableMemoryUsedPercentage =
                (double) (totalSystemMemory - systemMemoryAvailable) / totalSystemMemory;

        Assertions.assertTrue(systemAvailableMemoryUsedPercentage > 0);
    }
}
