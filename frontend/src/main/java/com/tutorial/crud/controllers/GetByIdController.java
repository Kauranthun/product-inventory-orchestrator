package com.tutorial.crud.controllers;

import com.tutorial.crud.Main;
import com.tutorial.crud.entity.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class GetByIdController {

    @FXML
    private TextField productIdTextField;

    @FXML
    private TextField productNameTextField;

    @FXML
    private TextField productPriceTextField;

    public void getById(){

        String Id = productIdTextField.getText();

        if (Id == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("None selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a product before use the get option");
            alert.showAndWait();
            return;
        }

        try {
            Product product = Main.service.getById(Id);

            productNameTextField.setText(product.getName());
            productPriceTextField.setText(String.valueOf(product.getPrice()));

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Impossible to get the product");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
