# ExcelToSWGDatatable
Python program for converting properly formatted excel workbooks and spreadsheets to SWG IFF Datatables

## How to Use
To use, either start the jar via a command line:
(for windows): java -jar ExcelSwgDatatable.jar

Or launch start.bat by double clicking on it (Windows only)

The program will accept virtually any type of an excel spreadsheet, all the way up to the latest XLSX format as it uses Apache POI libraries

To generate:
convert path_to_my_workbook.xlsx MySheet
or
convert path_to_my_workbook.xlsx 
to convert all the sheets to individual .iff's
(If running in same folder, full directory not needed for workbook paths)

Formatting is specific:
First row must be the name of the columns
Second must be the data type for each column (As of this release, only string and integer's are support (s, i)). Use brackets to add in a default value if needed.

Commands:
convert -- Convert workbook and/or sheets within workbook
help -- display info
format -- formatting information for sheets
