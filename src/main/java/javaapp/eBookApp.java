package javaapp;

import com.ea.async.Async;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
public class eBookApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }
//    static {
//        Async.init();
//    }
    @Override
    public void start(Stage primaryStage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(eBookApp.class.getResource("main.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("Hello!");
        primaryStage.setScene(scene);
        primaryStage.show();
        try {
            MainController controller = (MainController) fxmlLoader.getController();

            //set stage
            controller.setPrimaryStage(primaryStage);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
