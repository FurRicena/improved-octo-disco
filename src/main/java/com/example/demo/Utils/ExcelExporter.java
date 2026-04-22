package com.example.demo.Utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.util.List;

public class ExcelExporter {

    /**
     * 导出数据到 Excel
     * @param data 数据列表
     * @param sheetName 工作表名称
     * @param columnHeaders 列头（中文）
     * @param fieldNames 字段名（与 data 中对象的字段对应，支持嵌套如 "user.username"）
     * @param response HttpServletResponse
     * @param <T> 数据类型
     */
    public static <T> void exportToExcel(List<T> data, String sheetName, String[] columnHeaders, String[] fieldNames, HttpServletResponse response) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            for (int i = 0; i < columnHeaders.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowNum = 1;
            for (T item : data) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < fieldNames.length; i++) {
                    Object value = getNestedFieldValue(item, fieldNames[i]);
                    row.createCell(i).setCellValue(value != null ? value.toString() : "");
                }
            }

            ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
            // 自动调整列宽
            for (int i = 0; i < columnHeaders.length; i++) {
                sheet.autoSizeColumn(i);
                // 设置最小宽度
                if (sheet.getColumnWidth(i) < 3000) sheet.setColumnWidth(i, 3000);
            }


            // 写入响应
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + sheetName + "_" + System.currentTimeMillis() + ".xlsx");
            workbook.write(response.getOutputStream());
            workbook.dispose();
        } catch (Exception e) {
            throw new RuntimeException("导出 Excel 失败", e);
        }
    }

    // 支持嵌套字段，如 "user.username"
    private static Object getNestedFieldValue(Object obj, String fieldPath) {
        String[] fields = fieldPath.split("\\.");
        Object current = obj;
        for (String field : fields) {
            if (current == null) return null;
            current = getFieldValue(current, field);
        }
        return current;
    }

    private static Object getFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // 尝试父类
            try {
                Field field = obj.getClass().getSuperclass().getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
