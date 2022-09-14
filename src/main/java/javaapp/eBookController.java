package javaapp;
import java.io.File;
import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javaapp.eBook;
import javafx.stage.Stage;
public class eBookController implements Initializable {


    @FXML
    private ImageView add_book;

    @FXML
    private TableView<eBook> book_table;

    @FXML
    private TableColumn<eBook, String> author;

    @FXML
    private TableColumn<eBook, Double> size;

    @FXML
    private TableColumn<eBook, String> title;

    ArrayList<eBook> ebookList = new ArrayList<>();

    ObservableList<eBook> eBookObservableList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        author.setCellValueFactory(new PropertyValueFactory<eBook, String>("Author"));
        size.setCellValueFactory(new PropertyValueFactory<eBook, Double>("Size"));
        title.setCellValueFactory(new PropertyValueFactory<eBook, String>("Title"));
    }

//    String[] Extension={"epub"};
    @FXML
    public void addBookClick() throws IOException{
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Book File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("EPUB", "*.epub")
        );
        File file = fileChooser.showOpenDialog(this.primaryStage);
        if (file != null) {
            eBook book = new eBook(file.getPath());
            eBookObservableList.add(book);
            book_table.setItems(eBookObservableList);
        }
    }

    @FXML
    public void addBookSingleFolderClick() throws IOException{
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Book Folder");
        File folder = directoryChooser.showDialog(this.primaryStage);
        File filesList[] = folder.listFiles();
        for(File file : filesList) {
            String fileName = file.toString();
            if (fileName.endsWith("epub")){

                eBook book = new eBook(file.getPath());
                eBookObservableList.add(book);
                book_table.setItems(eBookObservableList);
            }
        }
    }
    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage){
        this.primaryStage = primaryStage;
    }


}
