package org.apache.dolphinscheduler.plugin.alert.wechatrobot;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExportExcel {

    public static void exportExcel(String sheetName, List<String> column, List<Map<String, Object>> data, File file) {
        // 创建工作薄
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        // 创建sheet
        HSSFSheet sheet = hssfWorkbook.createSheet(sheetName);
        // 设置样式
        CellStyle titleStyle = hssfWorkbook.createCellStyle();
        // 设置背景色
        titleStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
        // 必须设置 否则背景色不生效
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 表头
        HSSFRow headRow = sheet.createRow(0);
        for (int i = 0; i < column.size(); i++) {
            HSSFCell cell = headRow.createCell(i);
            cell.setCellStyle(titleStyle);
            cell.setCellValue(column.get(i));
        }
        headRow.setHeight((short) 500);
        Map<Integer, Integer> columnWidthMap = new HashMap<>();
        for (int i = 0; i < data.size(); i++) {
            HSSFRow dataRow = sheet.createRow(sheet.getLastRowNum() + 1);
            for (int x = 0; x < column.size(); x++) {
                Object s = data.get(i).get(column.get(x));
                if (s == null) {
                    s = "";
                }
                dataRow.createCell(x).setCellValue(s.toString());
                int max = Math.max(columnWidthMap.getOrDefault(x, 0), s.toString().getBytes().length);
                columnWidthMap.put(x, max);
                sheet.setColumnWidth(x, (max + 2) * 256);
            }
        }

        try {
            hssfWorkbook.write(file);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                hssfWorkbook.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
