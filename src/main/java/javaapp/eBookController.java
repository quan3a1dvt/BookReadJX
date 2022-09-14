package javaapp;
import java.io.File;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.util.ArrayList;
import javaapp.eBook;
import javafx.stage.Stage;
public class eBookController {


    @FXML
    private ImageView add_book;

    @FXML
    private TableColumn<?, ?> author;

    @FXML
    private TableView<?> book_table;

    @FXML
    private TableColumn<?, ?> size;

    @FXML
    private TableColumn<?, ?> title;

    ArrayList<eBook> ebookList = new ArrayList<>();

    @FXML
    public void addBookClick() throws IOException{
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF", "*.pdf")
        );
        File file = fileChooser.showOpenDialog(this.primaryStage);
        if (file != null) {
            eBook book = new eBook(file.getPath());
            ebookList.add(book);
            System.out.println(file.getPath());
        }
    }

    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage){
        this.primaryStage = primaryStage;
    }

}
