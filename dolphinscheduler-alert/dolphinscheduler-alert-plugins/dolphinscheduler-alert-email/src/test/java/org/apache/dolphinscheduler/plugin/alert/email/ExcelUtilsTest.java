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

package org.apache.dolphinscheduler.plugin.alert.email;

import org.apache.dolphinscheduler.plugin.alert.email.exception.AlertEmailException;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ExcelUtilsTest {

    @TempDir
    public Path testFolder;

    private String xlsFilePath;

    @BeforeEach
    public void setUp() throws Exception {
        xlsFilePath = testFolder.toString();
    }

    @Test
    public void testGenExcelFile() {
        // Define correctContent
        String correctContent = "[{\"name\":\"ds name\",\"value\":\"ds value\"}]";

        // Define incorrectContent
        String incorrectContent1 = "{\"name\":\"ds name\",\"value\":\"ds value\"}";

        // Define title
        String title = "test report";

        // Invoke genExcelFile with correctContent
        ExcelUtils.genExcelFile(correctContent, title, xlsFilePath);

        // Test file exists
        File xlsFile = new File(xlsFilePath + EmailConstants.SINGLE_SLASH + title + EmailConstants.EXCEL_SUFFIX_XLSX);
        Assertions.assertTrue(xlsFile.exists());

        // Invoke genExcelFile with incorrectContent, will cause RuntimeException
        Assertions.assertThrows(AlertEmailException.class, () -> {
            ExcelUtils.genExcelFile(incorrectContent1, title, xlsFilePath);
        });

    }

    @Test
    public void testGenExcelFileByCheckDir() {
        ExcelUtils.genExcelFile("[{\"a\": \"a\"},{\"a\": \"a\"}]", "t", "/tmp/xls");
        File file = new File("/tmp/xls" + EmailConstants.SINGLE_SLASH + "t" + EmailConstants.EXCEL_SUFFIX_XLSX);
        file.delete();
    }
}
