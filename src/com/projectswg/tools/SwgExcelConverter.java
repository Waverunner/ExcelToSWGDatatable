/*******************************************************************************
 * Excel to SWG Iff Datatable
 * Copyright (C) 2015  Waverunner
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 ******************************************************************************/

package com.projectswg.tools;

import com.projectswg.tools.libs.IffNode;
import com.projectswg.tools.libs.SWGFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Waverunner on 6/7/2015
 */
public class SwgExcelConverter {

    public SwgExcelConverter() {
    }

    public SWGFile convert(Sheet sheet) {
        Row header = sheet.getRow(sheet.getFirstRowNum());
        if (header == null)
            return null;

        int headerNum = header.getRowNum();

        // Create base datatable iff
        SWGFile swgFile = new SWGFile("DTII");
        swgFile.addForm("0001");
        // Create individual iff info
        int columns = createTableColumnData(swgFile, header);

        String[] types = createTableTypeData(swgFile, sheet.getRow(headerNum + 1), columns);
        if (types == null)
            return null;

        int rows = sheet.getPhysicalNumberOfRows();
        List<DatatableRow> rowList = new ArrayList<>();
        for (int i = headerNum + 2; i < rows; i++) {
            rowList.add(getDataTableRow(sheet.getRow(i), columns, types));
        }

        createTableRowData(swgFile, rowList);

        return swgFile;
    }

    private void createTableRowData(SWGFile swgFile, List<DatatableRow> rowList) {
        // Setup size for the buffer
        int size = 4;

        for (DatatableRow datatableRow : rowList) {
            size += datatableRow.getSize();
        }

        // Throw in the data
        IffNode rows = swgFile.addChunk("ROWS");
        ByteBuffer data = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        data.putInt(rowList.size());

        rowList.forEach(datatableRow -> datatableRow.encode(data));

        rows.setChunkData(data);
    }

    private int createTableColumnData(SWGFile swgFile, Row row) {
        // Perform spreadsheet checks
        int count = row.getPhysicalNumberOfCells();
        if (count <= 0)
            System.err.println("Excel sheet has no columns!");

        // Setup size for the buffer
        int size = 4;

        String[] columns = new String[count];
        for (int i = 0; i < count; i++) {
            String column = row.getCell(i).getStringCellValue();
            columns[i] = column;
            size += column.length() + 1;
        }

        // Throw in the data
        IffNode cols = swgFile.addChunk("COLS");
        ByteBuffer data = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        data.putInt(count);

        for (String column : columns) {
            data.put(column.getBytes(Charset.forName("US-ASCII")));
            data.put((byte) 0);
        }

        cols.setChunkData(data);
        return count;
    }

    private String[] createTableTypeData(SWGFile swgFile, Row row, int expectedColumns) {
        // Perform spreadsheet checks
        if (row == null || expectedColumns == 0)
            return null;

        int count = row.getPhysicalNumberOfCells();
        if (count != expectedColumns) {
            System.err.println("2nd row only had " + count + " rows, expected " + expectedColumns);
            return null;
        }

        // Setup size for the buffer
        int size = 0;

        String[] types = new String[count];
        for (int i = 0; i < count; i++) {
            String type = row.getCell(i).getStringCellValue();
            types[i] = type.substring(0, 1).toLowerCase() + type.substring(1);
            size += type.length() + 1;
        }

        // Throw in the data
        IffNode type = swgFile.addChunk("TYPE");
        ByteBuffer data = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);

        for (String sType : types) {
            data.put(sType.getBytes(Charset.forName("US-ASCII")));
            data.put((byte) 0);
        }

        type.setChunkData(data);
        return types;
    }

    private DatatableRow getDataTableRow(Row row, int expectedColumns, String[] types) {
        if (row == null || expectedColumns == 0)
            return null;

        int count = row.getPhysicalNumberOfCells();
        // Use > because empty cells are not considered a "physical cell"
        if (count > expectedColumns) {
            System.err.println("Row " + row.getRowNum() + " has " + count + " cells, expected " + expectedColumns);
            return null;
        }

        DatatableRow dataRow = new DatatableRow(expectedColumns);

        for (int i = 0; i < expectedColumns; i++) {
            Cell cell = row.getCell(i);
            if (cell == null) { // empty cell
                parseDataEmptyCell(types, dataRow, i);
                continue;
            }

            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_STRING:
                    parseDataCell(types, dataRow, i, cell.getStringCellValue());
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    parseDataCell(types, dataRow, i, cell.getNumericCellValue());
                    break;
                default:
                    System.out.println("UNK CELL TYPE: " + cell.getCellType());
            }
        }
        return dataRow;
    }

    private void parseDataCell(String[] types, DatatableRow row, int cellNum, Object value) {
        if (value instanceof String) {
            String s = (String) value;

            if (s.isEmpty()) {
                String defVal = getDefaultValue(types[cellNum]);
                if (defVal != null) {
                    s = defVal;
                }
            }

            row.values[cellNum] = s;
        } else if (value instanceof Double) {
            int i = ((Double) value).intValue();
            row.values[cellNum] = i;
        } else {
            System.err.println("Can't parse cell value " + value.getClass().getName());
        }
    }

    private void parseDataEmptyCell(String[] types, DatatableRow row, int cellNum) {
        String type = types[cellNum];
        String defValue = null;

        if (type.length() > 1) {
            defValue = getDefaultValue(type);
            type = type.split("\\[")[0];
        }

        switch (type) {
            case "s":
                if (defValue != null)
                    row.values[cellNum] = defValue;
                else
                    row.values[cellNum] = "";
                break;
            case "i":
                if (defValue != null)
                    row.values[cellNum] = Integer.valueOf(defValue);
                else
                    row.values[cellNum] = 0;
                break;
            case "f":
                if (defValue != null)
                    row.values[cellNum] = Float.valueOf(defValue);
                else
                    row.values[cellNum] = (float) 0;
                break;
            case "l":
                if (defValue != null)
                    row.values[cellNum] = Long.valueOf(defValue);
                else
                    row.values[cellNum] = (long) 0;
                break;
            default:
                System.err.println("\tDon't know how to parse type " + type + " for an empty cell!");
        }
    }

    private String getDefaultValue(String type) {
        if (!type.contains("["))
            return null;
        return type.substring(2, type.length() - 1);
    }

    private class DatatableRow {
        private Object[] values;

        private DatatableRow(int columnCount) {
            values = new Object[columnCount];
        }

        public int getSize() {
            int size = 0;
            for (Object value : values) {
                if (value instanceof String) {
                    size += ((String) value).length() + 1;
                } else if (value instanceof Double || value instanceof Integer || value instanceof Float) {
                    size += 4;
                } else if (value instanceof Long) {
                    size += 8;
                } else {
                    System.err.println("Don't know size for value w/class " + value.getClass().getName());
                }
            }
            return size;
        }

        public void encode(ByteBuffer buffer) {
            for (Object value : values) {
                if (value instanceof String) {
                    buffer.put(((String) value).getBytes(Charset.forName("US-ASCII")));
                    buffer.put((byte) 0);
                } else if (value instanceof Double) {
                    buffer.putDouble((double) value);
                } else if (value instanceof Integer) {
                    buffer.putInt((int) value);
                } else if (value instanceof Float) {
                    buffer.putFloat((float) value);
                } else if (value instanceof Long) {
                    buffer.putLong((long) value);
                } else {
                    System.err.println("Don't know how to encode for value w/class " + value.getClass().getName());
                }
            }
        }
    }
}
