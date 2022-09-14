module com.example.javaapp {
    requires javafx.controls;
    requires javafx.fxml;
            
                    requires org.kordamp.ikonli.javafx;
                
    opens com.example.javaapp to javafx.fxml;
    exports com.example.javaapp;
}