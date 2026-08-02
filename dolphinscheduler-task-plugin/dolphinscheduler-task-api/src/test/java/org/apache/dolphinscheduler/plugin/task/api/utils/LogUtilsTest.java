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

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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

    @Test
    public void getAppIdsFromLogFile_nonExistentFile() {
        List<String> appIds = LogUtils.getAppIdsFromLogFile("/nonexistent/file.log");
        Assertions.assertTrue(appIds.isEmpty(), "Should return empty list for non-existent file");
    }

    @Test
    public void getAppIdsFromAppInfoFile_moreThan10000Lines() throws IOException {
        Path tempFile = Files.createTempFile("ds-appinfo-test", ".log");
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 15000; i++) {
                sb.append("application_").append(i).append("\n");
            }
            Files.write(tempFile, sb.toString().getBytes(StandardCharsets.UTF_8));

            List<String> appIds = LogUtils.getAppIdsFromAppInfoFile(tempFile.toString());
            Assertions.assertEquals(10000, appIds.size(),
                    "Should only return first 10000 lines");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * Regression: a log file larger than the head-scan limit (64MB) must NOT be skipped — the appId
     * emitted at the head (when the app is submitted) must still be extracted, so Spark/Flink app
     * tracking and kill keep working for large-log tasks.
     */
    @Test
    public void getAppIdsFromLogFile_extractsAppIdFromHeadOfOversizedFile() throws IOException {
        Path bigLog = Files.createTempFile("ds-appid-big", ".log");
        try {
            String appId = "application_1700000000000_0001";
            Files.write(bigLog, ("Submitting YARN app " + appId + "\n").getBytes(StandardCharsets.UTF_8));
            // Pad with 1MB newline-terminated lines until the file exceeds 64MB.
            byte[] chunk = new byte[1024 * 1024];
            Arrays.fill(chunk, (byte) 'x');
            chunk[chunk.length - 1] = '\n';
            try (FileOutputStream fos = new FileOutputStream(bigLog.toFile(), true)) {
                for (int i = 0; i < 65; i++) {
                    fos.write(chunk);
                }
            }
            Assertions.assertTrue(Files.size(bigLog) > 64L * 1024 * 1024, "precondition: file > 64MB");

            List<String> appIds = LogUtils.getAppIdsFromLogFile(bigLog.toString());
            Assertions.assertTrue(appIds.contains(appId),
                    "appId at head of >64MB log must be extracted, got: " + appIds);
        } finally {
            Files.deleteIfExists(bigLog);
        }
    }
}
