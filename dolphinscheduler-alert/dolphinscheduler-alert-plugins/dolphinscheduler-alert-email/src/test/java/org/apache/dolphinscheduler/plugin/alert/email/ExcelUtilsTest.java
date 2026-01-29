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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
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
    void setUp() {
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
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ExcelUtils.genExcelFile(incorrectContent1, title, xlsFilePath);
        });

    }

    @Test
    public void testGenExcelFileByCheckDir() {
        String path = "/tmp/xls";
        ExcelUtils.genExcelFile("[{\"a\": \"a\"},{\"a\": \"a\"}]", "t", path);
        File file =
                new File(
                        path
                                + EmailConstants.SINGLE_SLASH
                                + "t"
                                + EmailConstants.EXCEL_SUFFIX_XLSX);
        file.delete();
        Assertions.assertFalse(file.exists());
    }

    @Test
    void testGenExcelFile_TruncateLongString() throws Exception {
        String title = "truncate_test";
        String longStrKey = "longStr";
        int maxLen = SpreadsheetVersion.EXCEL2007.getMaxTextLength();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxLen + 100; i++) {
            sb.append('X');
        }
        String longValue = sb.toString();
        String content = "[{\"" + longStrKey + "\":\"" + longValue + "\"}]";

        ExcelUtils.genExcelFile(content, title, xlsFilePath);

        try (
                FileInputStream fis = new FileInputStream(xlsFilePath + "/" + title + ".xlsx");
                Workbook wb = WorkbookFactory.create(fis)) {

            Sheet sheet = wb.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            Row dataRow = sheet.getRow(1);

            assertEquals(longStrKey, headerRow.getCell(0).getStringCellValue());
            String expected = longValue.substring(0, maxLen - 67) + "...(truncated)";
            assertEquals(expected, dataRow.getCell(0).getStringCellValue());
        }
    }
}
