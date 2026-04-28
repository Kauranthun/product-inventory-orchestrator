package com.tutorial.crud.controllers;

import com.tutorial.crud.Main;
import com.tutorial.crud.entity.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;



public class UpdateController {
    @FXML
    private TextField productName;

    @FXML
    private TextField productPrice;

    private static int idProduct;

    public void initData(Product product) {
        productName.setText(product.getName());
        productPrice.setText(Float.toString(product.getPrice()));
        idProduct=product.getId();
    }
    public void update(){
        Product product = new Product(productName.getText(),Float.parseFloat(productPrice.getText()));
        product.setId(idProduct);

        try{
            Main.service.update(product);
        }catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Error during the update");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }

    }
}
