package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.example.Views.ContainerApp;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {

        ContainerApp container =
                new ContainerApp();

        Scene scene = new Scene(
                container,
                1000,
                600
        );

        stage.setTitle("Editor Vetorial");

        stage.setScene(scene);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}