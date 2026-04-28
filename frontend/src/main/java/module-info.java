module com.tutorial.crud {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    opens com.tutorial.crud to javafx.fxml;
    opens com.tutorial.crud.controllers to javafx.fxml;

    opens com.tutorial.crud.entity to com.fasterxml.jackson.databind;
    exports com.tutorial.crud.entity;

    exports com.tutorial.crud;
    exports com.tutorial.crud.controllers;
}