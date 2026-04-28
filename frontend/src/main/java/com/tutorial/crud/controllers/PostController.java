package com.tutorial.crud.controllers;

import com.tutorial.crud.Main;
import com.tutorial.crud.entity.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class PostController {
    @FXML
    private TextField productName;
    @FXML
    private TextField productPrice;

    public void post(){
        try{
            Product product = new Product(productName.getText(),Float.parseFloat(productPrice.getText()));

            Main.service.create(product);

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Impossible to create the product");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
