module com.example.javaapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires epublib.core;
    requires java.xml;
    requires javafx.web;
    requires jdk.jsobject;
    requires php.java.bridge;
    requires ea.async;
    requires org.jsoup;
    requires jxbrowser;
    requires jxbrowser.javafx;
    requires jcef;

    opens javaapp to javafx.fxml;
    exports javaapp;
    exports javaapp.helper;
    opens javaapp.helper to javafx.fxml;
}