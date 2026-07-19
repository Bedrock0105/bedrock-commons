package org.bedrock.common.excel.model;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * 隐藏选项表 的公共方法
 */
@RequiredArgsConstructor
public class OptionsSheet {

    private final Sheet sheet;

    private int rowIndex = 0;

    private int columnIndex = 0;

    public Sheet sheet() {
        return sheet;
    }

    public int rowIndex() {
        return rowIndex;
    }

    public int columnIndex() {
        return columnIndex;
    }

    public OptionsSheet addRowIndex() {
        rowIndex++;
        return this;
    }

    public OptionsSheet setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
        return this;
    }

    public OptionsSheet addColumnIndex() {
        columnIndex++;
        /**
         * 大于256列，兼容老版 xls做换行处理
         */
        if (columnIndex >= 256) {
            columnIndex = 0;
            rowIndex = sheet.getLastRowNum() + 2;
        }
        return this;
    }

    public OptionsSheet setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
        return this;
    }

    public String getColumnIndex() {
        return coverColumnIndex(this.columnIndex + 1);
    }

    public String formula(int size) {
        return String.format("%s!$%s$%d:$%s$%d", sheet.getSheetName(), getColumnIndex(), rowIndex + 1, getColumnIndex(), rowIndex + size);
    }

    public OptionsSheet setCellValue(int rowIndex, String value) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            cell = row.createCell(columnIndex);
        }
        cell.setCellValue(value);
        return this;
    }

    /**
     * 列索引转字符串
     */
    public static String coverColumnIndex(int columnIndex) {
        if (columnIndex <= 26) {
            return (char) ((int) 'A' + columnIndex - 1) + "";
        }
        StringBuilder sb = new StringBuilder();
        while (columnIndex > 0) {
            columnIndex--;
            char letter = (char) (columnIndex % 26 + 'A');
            sb.append(letter);
            columnIndex = columnIndex / 26;
        }
        return sb.reverse().toString();
    }

}
