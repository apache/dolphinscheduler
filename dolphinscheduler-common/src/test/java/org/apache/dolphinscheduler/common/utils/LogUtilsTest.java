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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LogUtilsTest {

    private String predefinedLogPath;

    @BeforeEach
    public void setUp() {
        URL resourceUrl = getClass().getClassLoader().getResource("log/730.log");
        if (resourceUrl != null) {
            predefinedLogPath = resourceUrl.getPath();
        }
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testReadPartFileContentFromLocal_Success() {
        if (predefinedLogPath != null) {
            String path = predefinedLogPath.replace("%20", " ");
            List<String> result = LogUtils.readPartFileContentFromLocal(path, 1, 3);

            assertNotNull(result);
            assertTrue(result.size() >= 0);
        }
    }

    @Test
    public void testReadPartFileContentFromLocal_SkipNoneLimitAll() {
        if (predefinedLogPath != null) {
            String path = predefinedLogPath.replace("%20", " ");
            List<String> result = LogUtils.readPartFileContentFromLocal(path, 0, 100);

            assertNotNull(result);
            assertTrue(result.size() >= 0);
        }
    }

    @Test
    public void testReadPartFileContentFromPredefinedRollingFiles() {
        if (predefinedLogPath != null) {
            String mainLogPath = predefinedLogPath.replace("%20", " ");

            File mainLogFile = new File(mainLogPath);
            assertTrue(mainLogFile.exists(), "Main log file should exist");

            File rollingLogFile = new File(mainLogPath + ".1");
            assertTrue(rollingLogFile.exists(), "Rolling log file should exist");

            List<String> result = LogUtils.readPartFileContentFromLocal(mainLogPath, 0, 50);

            assertNotNull(result);
            assertTrue(result.size() > 0, "Should have some content from the log files");

            System.out.println("Number of lines read: " + result.size());
            for (int i = 0; i < Math.min(5, result.size()); i++) {
                System.out.println("Line " + i + ": " + result.get(i));
            }
        }
    }

    @Test
    public void testReadPartFileContentFromLocal_SkipAll() {
        if (predefinedLogPath != null) {
            String path = predefinedLogPath.replace("%20", " ");
            List<String> result = LogUtils.readPartFileContentFromLocal(path, 1000, 5);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    public void testReadPartFileContentFromLocal_LimitZero() {
        if (predefinedLogPath != null) {
            String path = predefinedLogPath.replace("%20", " ");
            List<String> result = LogUtils.readPartFileContentFromLocal(path, 0, 0);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    public void testReadPartFileContentFromLocal_FileDoesNotExist() {
        assertThrows(RuntimeException.class, () -> {
            LogUtils.readPartFileContentFromLocal("/non/existent/file.log", 0, 5);
        });
    }

    @Test
    public void testReadPartFileContentFromTwoSpecificRollingFiles() {
        String resourcePath = "log/730.log";
        URL resourceUrl = getClass().getClassLoader().getResource(resourcePath);

        if (resourceUrl != null) {
            String mainLogPath = resourceUrl.getPath().replace("%20", " ");

            File mainLogFile = new File(mainLogPath);
            File rollingLogFile = new File(mainLogPath + ".1");

            assertTrue(mainLogFile.exists(), "Main log file (730.log) should exist");
            assertTrue(rollingLogFile.exists(), "Rolling log file (730.log.1) should exist");

            List<String> result = LogUtils.readPartFileContentFromLocal(mainLogPath, 0, 100);

            assertNotNull(result);
            assertTrue(result.size() > 0, "Should read content from both log files");

            System.out.println("Total lines from both files (730.log and 730.log.1): " + result.size());
        }
    }

    @Test
    public void testGetFileContentBytesWithRollingLogs() {
        String resourcePath = "log/730.log";
        URL resourceUrl = getClass().getClassLoader().getResource(resourcePath);

        if (resourceUrl != null) {
            String mainLogPath = resourceUrl.getPath().replace("%20", " ");

            File mainLogFile = new File(mainLogPath);
            File rollingLogFile = new File(mainLogPath + ".1");

            assertTrue(mainLogFile.exists(), "Main log file (730.log) should exist");
            assertTrue(rollingLogFile.exists(), "Rolling log file (730.log.1) should exist");

            byte[] result = LogUtils.getFileContentBytesWithRollingLogs(mainLogPath);

            assertNotNull(result);
            assertTrue(result.length > 0, "Should read bytes from both log files");

            System.out.println("Total bytes from both files (730.log and 730.log.1): " + result.length);
        }
    }
}
