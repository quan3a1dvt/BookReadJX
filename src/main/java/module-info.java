module com.example.javaapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires epublib.core;
    requires java.xml;
    requires javafx.web;

    opens javaapp to javafx.fxml;
    exports javaapp;
    exports javaapp.helper;
    opens javaapp.helper to javafx.fxml;
}