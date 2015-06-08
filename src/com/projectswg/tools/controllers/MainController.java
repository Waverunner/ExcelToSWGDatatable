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

package com.projectswg.tools.controllers;

import com.projectswg.tools.Main;
import com.projectswg.tools.SwgExcelConverter;
import com.projectswg.tools.libs.SWGFile;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.FileChooser;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by Waverunner on 6/8/2015
 */
public class MainController implements Initializable {

    @FXML
    ListView<FileItem> listView;
    @FXML
    TextField directoryField;

    ObservableList<FileItem> outputSelectionList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        outputSelectionList = FXCollections.observableList(new ArrayList<>());
        listView.setItems(outputSelectionList);
        listView.setCellFactory(CheckBoxListCell.forListView(param -> param.getSelectedProperty()));
    }

    @FXML
    private void handleButtonAction(ActionEvent event) {
        if (!(event.getSource() instanceof Button))
            return;

        Button button = (Button) event.getSource();

        switch (button.getText()) {
            case "Browse":
                handleBrowseDirectory();
                break;
            case "Convert":
                handleConvertSelections();
                break;
        }
    }

    private void handleBrowseDirectory() {
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Select Worksheet to Convert");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook", "*.xlsx", "*.xls"));
        File workbook = fileChooser.showOpenDialog(Main.getInstance().getStage());
        if (workbook == null)
            return;
        directoryField.setText(workbook.getAbsolutePath());
        try {
            handlePopulateList(workbook);
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        }
    }

    private void handleConvertSelections() {
        SwgExcelConverter converter = new SwgExcelConverter();

        ArrayList<FileItem> fileItems = new ArrayList<>(outputSelectionList.filtered(FileItem::isSelected));

        String text = "Finished creating:\n";

        for (FileItem fileItem : fileItems) {
            convertFileItem(fileItem, converter);
            text += "\t" + fileItem.getFile().getAbsolutePath() + "\n";
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(Main.getInstance().getStage().getTitle());
        alert.setHeaderText("Excel Conversion");
        alert.setContentText(text);
        alert.show();

        directoryField.clear();
        outputSelectionList.clear();
    }

    private void convertFileItem(FileItem item, SwgExcelConverter converter) {
        SWGFile iff = converter.convert(item.getSheet());
        if (iff != null) {
            try {
                iff.save(item.getFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handlePopulateList(File source) throws IOException, InvalidFormatException {
        Workbook workbook = WorkbookFactory.create(source);
        outputSelectionList.clear();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            outputSelectionList.add(getFileItem(source.getAbsolutePath(), workbook.getSheetAt(i)));
        }
        workbook.close();
    }

    private FileItem getFileItem(String parent, Sheet sheet) {
        String path = parent.substring(0, parent.lastIndexOf("\\") + 1) + sheet.getSheetName() + ".iff";
        return new FileItem(new File(path), sheet);
    }

    private class FileItem {
        private File file;
        private Sheet sheet;
        private BooleanProperty selectedProperty = new SimpleBooleanProperty(false);

        public FileItem(File file, Sheet sheet) {
            this.file = file;
            this.sheet = sheet;
        }

        public final boolean isSelected() {
            return selectedProperty.getValue();
        }

        public final File getFile() {
            return file;
        }

        public Sheet getSheet() {
            return sheet;
        }

        public final BooleanProperty getSelectedProperty() {
            return selectedProperty;
        }

        @Override
        public String toString() {
            return sheet.getSheetName() + "\t--> " + file.getAbsolutePath();
        }
    }
}
