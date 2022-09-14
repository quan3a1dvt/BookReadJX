module com.example.javaapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires epublib.core;

    opens javaapp to javafx.fxml;
    exports javaapp;
}