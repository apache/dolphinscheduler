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

package org.apache.dolphinscheduler.plugin.task.api.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

public class LogUtilsTest {

    private static final String APP_ID_FILE = LogUtilsTest.class.getResource("/appId.txt")
            .getFile();
    private static final String APP_INFO_FILE = LogUtilsTest.class.getResource("/appInfo.log")
            .getFile();

    @Test
    public void getAppIdsFromLogFile() {
        List<String> appIds = LogUtils.getAppIds(APP_ID_FILE, APP_INFO_FILE, "log");
        Assertions.assertEquals(Lists.newArrayList("application_1548381669007_1234"), appIds);
    }

    @Test
    public void getAppIdsFromAppInfoFile() {
        List<String> appIds = LogUtils.getAppIds(APP_ID_FILE, APP_INFO_FILE, "aop");
        appIds = appIds.stream().filter(a -> a.contains("application")).collect(Collectors.toList());
        Assertions.assertEquals(Lists.newArrayList("application_1548381669007_1234"), appIds);
    }
}
