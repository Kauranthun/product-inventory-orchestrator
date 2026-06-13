package com.tutorial.crud;

import com.tutorial.crud.services.CommunicationHTTP;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application{
    public static CommunicationHTTP service = new CommunicationHTTP();
    private static Stage centralStage;

    @Override
    public void start(Stage stage) throws IOException {

        centralStage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Auth.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 600);
        stage.setTitle("Authentication!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static Stage getCentralStage() {
        return centralStage;
    }
}