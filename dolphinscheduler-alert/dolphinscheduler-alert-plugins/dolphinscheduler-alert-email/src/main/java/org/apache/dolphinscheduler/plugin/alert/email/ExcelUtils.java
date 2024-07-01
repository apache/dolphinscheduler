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

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.alert.email.exception.AlertEmailException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ExcelUtils {

    private static final int XLSX_WINDOW_ROW = 10000;
    private ExcelUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * generate excel file
     *
     * @param content the content
     * @param title the title
     * @param xlsFilePath the xls path
     */
    public static void genExcelFile(String content, String title, String xlsFilePath) {
        File file = new File(xlsFilePath);
        if (!file.exists() && !file.mkdirs()) {
            log.error("Create xlsx directory error, path:{}", xlsFilePath);
            throw new AlertEmailException("Create xlsx directory error");
        }

        List<LinkedHashMap> itemsList = JSONUtils.toList(content, LinkedHashMap.class);

        if (CollectionUtils.isEmpty(itemsList)) {
            log.error("itemsList is null");
            throw new AlertEmailException("itemsList is null");
        }

        LinkedHashMap<String, Object> headerMap = itemsList.get(0);

        List<String> headerList = new ArrayList<>();

        for (Map.Entry<String, Object> en : headerMap.entrySet()) {
            headerList.add(en.getKey());
        }
        try (
                SXSSFWorkbook wb = new SXSSFWorkbook(XLSX_WINDOW_ROW);
                FileOutputStream fos = new FileOutputStream(String.format("%s/%s.xlsx", xlsFilePath, title))) {
            // declare a workbook
            // generate a table
            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);
            // set the height of the first line
            row.setHeight((short) 500);

            // set Horizontal right
            CellStyle cellStyle = wb.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.RIGHT);

            // setting excel headers
            for (int i = 0; i < headerList.size(); i++) {
                Cell cell = row.createCell(i);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(headerList.get(i));
            }

            // setting excel body
            int rowIndex = 1;
            for (LinkedHashMap<String, Object> itemsMap : itemsList) {
                Object[] values = itemsMap.values().toArray();
                row = sheet.createRow(rowIndex);
                // setting excel body height
                row.setHeight((short) 500);
                rowIndex++;
                for (int j = 0; j < values.length; j++) {
                    Cell cell1 = row.createCell(j);
                    cell1.setCellStyle(cellStyle);
                    if (values[j] instanceof Number) {
                        cell1.setCellValue(Double.parseDouble(String.valueOf(values[j])));
                    } else {
                        cell1.setCellValue(String.valueOf(values[j]));
                    }
                }
            }

            for (int i = 0; i < headerList.size(); i++) {
                sheet.setColumnWidth(i, headerList.get(i).length() * 800);
            }

            // setting file output
            wb.write(fos);
            wb.dispose();
        } catch (Exception e) {
            throw new AlertEmailException("generate excel error", e);
        }
    }

}
