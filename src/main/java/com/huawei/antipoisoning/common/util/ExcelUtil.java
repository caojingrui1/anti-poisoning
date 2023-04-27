package com.huawei.antipoisoning.common.util;

import com.huawei.antipoisoning.common.util.annocation.ExcelAttribute;
import com.huawei.antipoisoning.common.util.annocation.ExcelColumnType;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * excel工具类
 *
 * @since 2021-4-2
 */
@Component
public class ExcelUtil {
    private static final Logger logger = LoggerFactory.getLogger(ExcelUtil.class);

    /**
     * 导出
     *
     * @param sheetName excel的sheet名
     * @param title     标题
     * @param workbook  工作簿
     * @param tList     list
     * @param <T>       t
     * @return T 返回实体
     */
    public static <T> Workbook export(Workbook workbook, List<T> tList,
                                      String sheetName, HashMap<Class, String[]> title) {
        Sheet sheet = workbook.createSheet(sheetName);
        // 设置宽度
        sheet.setDefaultColumnWidth(40);
        // 设置表头
        T meter = tList.get(0);
        Class<?> clazz = meter.getClass();
        String[] titles = title.get(clazz);
        if (Objects.isNull(titles)) {
            return workbook;
        }
        Row rowTitle = sheet.createRow(0);
        for (int i = 0; i < titles.length; i++) {
            Cell cell = rowTitle.createCell(i);
            cell.setCellValue(titles[i]);
            cell.setCellStyle(setStyleTitle(workbook));
        }
        // 异常时方便定位
        int errorRow = 0;
        int errorCol = 0;
        CellStyle cellStyle = setStyleCell(workbook);
        try {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (int i = 0; i < tList.size(); i++) {
                // 创建行 除去表头 从下标为1开始
                Row row = sheet.createRow(i + 1);
                errorRow = i;
                for (Field field : declaredFields) {
                    ExcelAttribute annotation = field.getAnnotation(ExcelAttribute.class);
                    if (annotation == null) {
                        continue;
                    }
                    int columnIndex = annotation.columnIndex();
                    errorCol = columnIndex;
                    // 创建列
                    Cell cell = row.createCell(columnIndex);
                    cell.setCellStyle(cellStyle);
                    field.setAccessible(true);
                    Object value = field.get(tList.get(i));
                    checkType(value, annotation, cell);
                }
            }
        } catch (IllegalAccessException e) {
            logger.error("get Excel error in row:{}  col:{}", errorRow, errorCol);
        }
        return workbook;
    }

    private static void checkType(Object value, ExcelAttribute annotation, Cell cell) {
        int maxSize = 32767;
        if (Objects.nonNull(value)) {
            if (value.toString().length() > maxSize) {
                cell.setCellValue(value.toString().substring(0, maxSize));
            } else {
                cell.setCellValue(value.toString());
            }
            if (annotation.columnType().equals(ExcelColumnType.INT)) {
                cell.setCellValue(value.toString());
            }
        }
    }

    /**
     * 设置表头的样式
     *
     * @param workbook 参数
     * @return CellStyle
     */
    private static CellStyle setStyleTitle(Workbook workbook) {
        // 字体
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);
        // 设置表头样式
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setFillForegroundColor((short) 13);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(font);
        return style;
    }

    /**
     * 设置单元格的样式
     *
     * @param workbook 参数体
     * @return CellStyle
     */
    private static CellStyle setStyleCell(Workbook workbook) {
        // 单元格
        CellStyle cellstyle = workbook.createCellStyle();
        Font cellfont = workbook.createFont();
        cellfont.setFontName("宋体");
        cellfont.setFontHeightInPoints((short) 11);
        cellstyle.setFont(cellfont);
        return cellstyle;
    }

    /**
     * webhook公共处理
     *
     * @param workbook workbook
     * @param type     type
     * @return workbookToBytes
     */
    public static byte[] workbookToBytes(Workbook workbook, String type) {
        if (Objects.isNull(workbook)) {
            return new byte[0];
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            workbook.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            logger.error(" export {}s To File  error ", type);
            return new byte[0];
        }
    }
}
