package javaapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import java.io.IOException;

public class eBookApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(eBookApp.class.getResource("main.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("Hello!");
        primaryStage.setScene(scene);
        primaryStage.show();
        try {
            eBookController controller = (eBookController) fxmlLoader.getController();

            //set stage
            controller.setPrimaryStage(primaryStage);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
