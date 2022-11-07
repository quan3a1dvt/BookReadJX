module com.example.javaapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires epublib.core;
    requires java.xml;
    requires javafx.web;
    requires jdk.jsobject;
    requires php.java.bridge;
    requires ea.async;
    requires org.jsoup;
    requires org.jfxtras.styles.jmetro;
    requires java.desktop;
    opens javaapp to javafx.fxml;
    exports javaapp;
    exports javaapp.helper;
    opens javaapp.helper to javafx.fxml;
}