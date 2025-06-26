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
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.ss.usermodel.*;


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
    public void testSetCellValueWithSplit_NoSplit() {
         Row row;
         CellStyle cellStyle;
         try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
             Sheet sheet = wb.createSheet();
             row = sheet.createRow(0);
             cellStyle = wb.createCellStyle();
         } catch (IOException e) {
             throw new RuntimeException(e);
         }

         String value = "short string";
        int nextCol = ExcelUtils.setCellValueWithSplit(row, 0, cellStyle, value);

        assertEquals(1, nextCol);
        assertEquals(value, row.getCell(0).getStringCellValue());
    }

    @Test
    public void testSetCellValueWithSplit_Split() {
        Row row;
        CellStyle cellStyle;
        try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            row = sheet.createRow(0);
            cellStyle = wb.createCellStyle();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Generate a string longer than 32767
        int maxLen = 32767;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxLen + 10; i++) {
            sb.append('a');
        }
        String longValue = sb.toString();
        int nextCol = ExcelUtils.setCellValueWithSplit(row, 0, cellStyle, longValue);

        assertEquals(2, nextCol); // Should occupy 2 cells
        assertEquals(longValue.substring(0, maxLen), row.getCell(0).getStringCellValue());
        assertEquals(longValue.substring(maxLen), row.getCell(1).getStringCellValue());
    }

    @Test
    public void testSetCellValueWithSplit_Number() {
        Row row;
        CellStyle cellStyle;
        try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            row = sheet.createRow(0);
            cellStyle = wb.createCellStyle();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Double value = 123.45;
        int nextCol = ExcelUtils.setCellValueWithSplit(row, 0, cellStyle, value);

        assertEquals(1, nextCol);
        assertEquals("123.45", row.getCell(0).getStringCellValue());
    }
}
