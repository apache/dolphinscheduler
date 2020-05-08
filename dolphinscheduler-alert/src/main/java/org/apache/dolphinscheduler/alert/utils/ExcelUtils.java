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
package org.apache.dolphinscheduler.alert.utils;

import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * excel utils
 */
public class ExcelUtils {

    private static final Logger logger = LoggerFactory.getLogger(ExcelUtils.class);
    /**
     * generate excel file
     * @param content the content
     * @param title the title
     * @param xlsFilePath the xls path
     */
    public static void genExcelFile(String content,String title,String xlsFilePath){
        List<LinkedHashMap> itemsList;

        //The JSONUtils.toList has been try catch ex
        itemsList = JSONUtils.toList(content, LinkedHashMap.class);

        if (CollectionUtils.isEmpty(itemsList)){
            logger.error("itemsList is null");
            throw new RuntimeException("itemsList is null");
        }

        LinkedHashMap<String, Object> headerMap = itemsList.get(0);

        List<String> headerList = new ArrayList<>();

        Iterator<Map.Entry<String, Object>> iter = headerMap.entrySet().iterator();
        while (iter.hasNext()){
            Map.Entry<String, Object> en = iter.next();
            headerList.add(en.getKey());
        }

        HSSFWorkbook wb = null;
        FileOutputStream fos = null;
           try {
               // declare a workbook
               wb = new HSSFWorkbook();
               // generate a table
               HSSFSheet sheet = wb.createSheet();
               HSSFRow row = sheet.createRow(0);
               //set the height of the first line
               row.setHeight((short)500);

               //set Horizontal right
               CellStyle cellStyle = wb.createCellStyle();
               cellStyle.setAlignment(HorizontalAlignment.RIGHT);

               //setting excel headers
               for (int i = 0; i < headerList.size(); i++) {
                   HSSFCell cell = row.createCell(i);
                   cell.setCellStyle(cellStyle);
                   cell.setCellValue(headerList.get(i));
               }

               //setting excel body
               int rowIndex = 1;
               for (LinkedHashMap<String, Object> itemsMap : itemsList){
                   Object[] values = itemsMap.values().toArray();
                   row = sheet.createRow(rowIndex);
                   //setting excel body height
                   row.setHeight((short)500);
                   rowIndex++;
                   for (int j = 0 ; j < values.length ; j++){
                       HSSFCell cell1 = row.createCell(j);
                       cell1.setCellStyle(cellStyle);
                       cell1.setCellValue(String.valueOf(values[j]));
                   }
               }

               for (int i = 0; i < headerList.size(); i++) {
                   sheet.setColumnWidth(i, headerList.get(i).length() * 800);
               }

               File file = new File(xlsFilePath);
               if (!file.exists()) {
                   file.mkdirs();
               }

               //setting file output
               fos = new FileOutputStream(xlsFilePath + Constants.SINGLE_SLASH + title + Constants.EXCEL_SUFFIX_XLS);

               wb.write(fos);

           }catch (Exception e){
               logger.error("generate excel error",e);
               throw new RuntimeException("generate excel error",e);
           }finally {
               if (wb != null){
                   try {
                       wb.close();
                   } catch (IOException e) {
                       logger.error(e.getMessage(),e);
                   }
               }
               if (fos != null){
                   try {
                       fos.close();
                   } catch (IOException e) {
                       logger.error(e.getMessage(),e);
                   }
               }
           }
    }

}
