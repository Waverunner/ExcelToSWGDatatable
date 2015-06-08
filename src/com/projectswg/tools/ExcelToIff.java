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

import com.projectswg.tools.libs.SWGFile;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class ExcelToIff {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Excel->IFF Datatable Converter ===");
        if (args == null || args.length == 0)
            displayHelpInfo();

        // First handle any command line arguments when starting up the program
        handleArguments(args);
        // Now create a loop that'll read any additional commands
        String line;
        while (scanner.hasNext() && ((line = scanner.nextLine()) != null)) {
            handleArguments(convertLineToArgs(line));
        }
    }

    private static void handleArguments(String[] args) {
        if (args.length <= 0)
            return;

        switch (args[0]) {
            case "help":
                displayHelpInfo();
                break;
            case "convert":
                if (args.length == 2)
                    convertWorkbook(args[1]);
                else if (args.length == 3)
                    convertSheet(args[1], args[2]);
                else System.out.println("Invalid argument: Expected path to workbook and/or sheet name/id");
                break;
            case "format":
                displayFormatInfo();
                break;
            case "formatting":
                displayFormatInfo();
                break;
            default:
                System.out.println("Invalid command: " + args[0]);
                break;
        }
    }

    private static String[] convertLineToArgs(String argumentLine) {
        return argumentLine.split(" ");
    }

    private static void displayHelpInfo() {
        System.out.println("The following are commands that you may use and how they work.\n" +
                "----------------------------------------\n" +
                "help    | Displays the information you are viewing now\n" +
                "format  | Displays information on proper spreadsheet format\n" +
                "convert | {workBookPath} [sheetName/Index] | Converts the workbook's sheets into appropriate .iff's in the same directory as the workbook." +
                " Use sheetName/index argument for creating only a specific sheet. Output files generated as workbookname_sheetname.iff or workbookname.iff if sheet specified\n");
    }

    private static void displayFormatInfo() {
        System.out.println("----------------------------------------\n" +
                "First Row: Name of each column\n" +
                "Second Row: type[defaultValue (if needed)] || Currently supports: String s[defaultValue], Integer i[defaultValue]\n" +
                "\t--- Note: defaultValue is NOT required, if none is needed then just use the character minus the brackets. Ex: s\n" +
                "Additional Rows: These should be the \"actual\" rows, their values must follow the formatting for the second row.\n" +
                "\t--- Note: Empty cells will use defaultValue if defined, however it's not required. If no defaultValue is defined, then it defaults to an empty value for that type");
    }

    private static void convertWorkbook(String path) {
        File file = new File(path);
        if (!file.exists()) {
            System.err.println(String.format("Could not convert %s as it doesn't exist!", path));
            return;
        }

        try {
            Workbook workbook = WorkbookFactory.create(file);
            System.out.println("Converting sheets from workbook " + file.getAbsolutePath());
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                path = file.getAbsolutePath().split("\\.")[0] + "_" + sheet.getSheetName() + ".iff";
                convertSheet(new File(path), sheet);
            }
            System.out.println("Conversion for workbook " + file.getAbsolutePath() + " completed.");
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        }
    }

    private static void convertSheet(String path, String sheetStr) {
        File file = new File(path);
        if (!file.exists()) {
            System.err.println(String.format("Could not convert %s as it doesn't exist!", path));
            return;
        }

        try {
            Workbook workbook = WorkbookFactory.create(file);
            Sheet sheet = workbook.getSheet(sheetStr);
            if (sheet == null)
                sheet = workbook.getSheetAt(Integer.valueOf(sheetStr));
            if (sheet == null) {
                System.err.println(String.format("Could not convert %s as there is no sheet name or id that is %s", path, sheetStr));
            }
            System.out.println("Converting sheet " + sheet.getSheetName() + " in workbook " + file.getAbsolutePath());
            convertSheet(new File(file.getAbsolutePath().split("\\.")[0] + "_" + sheet.getSheetName() + ".iff"), sheet);
            System.out.println("Conversion for sheet " + sheet.getSheetName() + " in workbook " + file.getAbsolutePath() + " completed.");
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        }
    }

    private static void convertSheet(File file, Sheet sheet) {
        System.out.println("Creating " + file.getAbsolutePath() + "...");

        SwgExcelConverter excelConverter = new SwgExcelConverter();

        SWGFile iff = excelConverter.convert(sheet);

        if (iff == null) {
            System.err.println("Failed to create " + file.getAbsolutePath());
            return;
        }

        try {
            iff.save(file);
            System.out.println("Finished creating " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
