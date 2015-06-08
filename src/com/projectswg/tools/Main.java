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

import com.projectswg.tools.controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by Waverunner on 6/8/2015
 */
public class Main extends Application {
    private static Main instance;
    private MainController controller;
    private Stage stage;

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            throwable.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Exception");
            alert.setHeaderText("An exception has occurred");
            alert.setContentText(throwable.getLocalizedMessage());

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            throwable.printStackTrace(pw);

            String exceptionText = sw.toString();

            Label label = new Label("The exception stacktrace was:");

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            alert.getDialogPane().setExpandableContent(expContent);

            alert.showAndWait();
        });
        launch(args);
    }

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void start(Stage primaryStage) {
        instance = this;
        stage = primaryStage;

        FXMLLoader fxmlLoader = new FXMLLoader();
        try {
            fxmlLoader.load(getClass().getResourceAsStream("ui/main.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        controller = fxmlLoader.getController();
        Parent parent = fxmlLoader.getRoot();
        Scene scene = new Scene(parent);
        primaryStage.setTitle("Excel 2 SWG Iff Datatable Converter");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public Stage getStage() {
        return stage;
    }
}