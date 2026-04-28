package com.tutorial.crud.controllers;

import com.tutorial.crud.Main;
import com.tutorial.crud.entity.Product;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;

import java.io.IOException;

public class MenuController {

    public void openGetAllScreen() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(com.tutorial.crud.Main.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 600);
        com.tutorial.crud.Main.getCentralStage().setTitle("GetALL");
        com.tutorial.crud.Main.getCentralStage().setScene(scene);
        com.tutorial.crud.Main.getCentralStage().show();
    }

    public void openPostScreen() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(com.tutorial.crud.Main.class.getResource("post.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 600);
        com.tutorial.crud.Main.getCentralStage().setTitle("POST");
        com.tutorial.crud.Main.getCentralStage().setScene(scene);
        com.tutorial.crud.Main.getCentralStage().show();
    }

    public void openGetByIdScreen() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(com.tutorial.crud.Main.class.getResource("getById.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 600);
        com.tutorial.crud.Main.getCentralStage().setTitle("GET by ID");
        com.tutorial.crud.Main.getCentralStage().setScene(scene);
        com.tutorial.crud.Main.getCentralStage().show();
    }

    public void openGetByNameScreen() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(com.tutorial.crud.Main.class.getResource("getByName.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 600);
        com.tutorial.crud.Main.getCentralStage().setTitle("GET by Name");
        com.tutorial.crud.Main.getCentralStage().setScene(scene);
        com.tutorial.crud.Main.getCentralStage().show();
    }

    public void openUpdateScreen() throws IOException {
        HelloController helloController = HelloController.getInstance();
        Product selectedProduct = helloController.getSelectedProduct();

        if (selectedProduct == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("None selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a product before use the delete option");
            alert.showAndWait();
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(com.tutorial.crud.Main.class.getResource("update.fxml"));

        Parent root = fxmlLoader.load();

        UpdateController updateController = fxmlLoader.getController();

        if (selectedProduct != null) {
            updateController.initData(selectedProduct);
        }

        Scene scene = new Scene(root, 600, 600);
        com.tutorial.crud.Main.getCentralStage().setTitle("Update Product");
        com.tutorial.crud.Main.getCentralStage().setScene(scene);
        com.tutorial.crud.Main.getCentralStage().show();
    }

    public void delete() throws IOException {

        HelloController helloController = HelloController.getInstance();

        if (helloController == null) {
            return;
        }

        Product selectedProduct = helloController.getSelectedProduct();

        if (selectedProduct == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("None selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a product before use the delete option");
            alert.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete accept");
        confirm.setHeaderText("Delete the product: " + selectedProduct.getName() + " ?");

        try {
            Main.service.delete(selectedProduct.getId());
            helloController.onHelloButtonClick();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error in delete");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public void backupDb(){
        try{
            Main.service.backupDb();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error in backup");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public void restoreDb(){
        try{
            Main.service.restoreDb();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error in restore");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

}
