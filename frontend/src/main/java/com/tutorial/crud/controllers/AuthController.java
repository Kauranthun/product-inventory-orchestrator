package com.tutorial.crud.controllers;

import com.tutorial.crud.Main;
import com.tutorial.crud.entity.Credentials;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AuthController {

    @FXML private TextField nameLogin;
    @FXML private PasswordField passwordLogin;

    @FXML private TextField nameSignup;
    @FXML private PasswordField passwordSignup;

    @FXML
    public void handleLogin() {
        Credentials creds = new Credentials();
        creds.setUsername(nameLogin.getText());
        creds.setPassword(passwordLogin.getText());

        try {
            Main.service.login(creds);

            System.out.println("Login successful!");

            FXMLLoader fxmlLoader = new FXMLLoader(com.tutorial.crud.Main.class.getResource("hello-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 600);
            com.tutorial.crud.Main.getCentralStage().setTitle("GetALL");
            com.tutorial.crud.Main.getCentralStage().setScene(scene);
            com.tutorial.crud.Main.getCentralStage().show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Login Failed", "Verify your credentials or the server state.\n" + e.getMessage());
        }
    }

    @FXML
    public void handleSignup() {
        Credentials newUser = new Credentials();
        newUser.setUsername(nameSignup.getText());
        newUser.setPassword(passwordSignup.getText());

        try {
            Main.service.Signup(newUser);

            System.out.println("Signup successful!");

            FXMLLoader fxmlLoader = new FXMLLoader(com.tutorial.crud.Main.class.getResource("hello-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 600);
            com.tutorial.crud.Main.getCentralStage().setTitle("GetALL");
            com.tutorial.crud.Main.getCentralStage().setScene(scene);
            com.tutorial.crud.Main.getCentralStage().show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Signup Failed", "Impossible to create the account.\n" + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}