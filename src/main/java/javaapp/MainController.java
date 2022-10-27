package javaapp;
import java.io.File;

import com.ea.async.Async;
import javaapp.book.Book;
import javaapp.helper.MenuHelper;
import javaapp.helper.TableHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import java.net.URL;
import java.util.ResourceBundle;

import javaapp.book.epub.Epub;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetroStyleClass;

import static javaapp.book.Book.READER_LIBRARY_PATH;


public class MainController implements Initializable {




    @FXML
    private HBox topPane;
    @FXML
    private Pane rightPane;
    @FXML
    private Pane leftPane;
    @FXML
    private Pane bottomPane;
    @FXML
    private ImageView selectedBookCover;
    @FXML
    private TableView<Book> table;
    private TableHelper tableHelper;
    @FXML
    private SplitMenuButton addBook;
    private MenuHelper menuHelper;



    ObservableList<Book> bookObservableList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setUI();
        tableHelper = new TableHelper(table, bookObservableList);
        menuHelper = new MenuHelper(addBook, bookObservableList, primaryStage);
        List<Path> booksPath = null;
        try {
            booksPath = Files.list(READER_LIBRARY_PATH)
                    .filter(path -> !Files.isDirectory(path))
                    .filter(path -> path.toString().endsWith(".epub"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        booksPath.forEach(path -> {
            // Each book is loaded on a separate thread, this DRASTICALLY decreases load time
            new Thread(() -> {
                Epub epub = new Epub(path);


                // Attempt to initialize the epub with its content.opf file.
                // If it was not found, log an error and continue to the next book.
                boolean result = epub.loadMetadata(false);
                if (result) {
                    bookObservableList.add(epub);
                } else {
                    System.out.println(String.format("content.opf could not be read from %s. Is the file a valid .epub? Skipping to the next book.", path.getFileName()));
                }
            }).start();
        });
    }
    private void setUI(){
        topPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
        rightPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
        leftPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
        bottomPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
    }
    @FXML
    public void addBookClick() throws Exception{


    }

    @FXML
    public void addBookSingleFolderClick() throws Exception {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Book Folder");
        File folder = directoryChooser.showDialog(this.primaryStage);
        File filesList[] = folder.listFiles();
        for (File file : filesList) {
            String fileName = file.toString();
            if (fileName.endsWith("epub")) {
                Book book = new Epub(Path.of(file.getPath()));
                bookObservableList.add(book);
            }
        }
    }

    void reviewTableSelectedBook() {
        Book selectedBook = table.getSelectionModel().getSelectedItem();
        if (selectedBook != null){
            selectedBookCover.setImage(selectedBook.getCover());
            Image img = selectedBookCover.getImage();
            double w = 0;
            double h = 0;

            double ratioX = selectedBookCover.getFitWidth() / img.getWidth();
            double ratioY = selectedBookCover.getFitHeight() / img.getHeight();

            double reducCoeff = 0;
            if (ratioX >= ratioY) {
                reducCoeff = ratioY;
            } else {
                reducCoeff = ratioX;
            }

            w = img.getWidth() * reducCoeff;
            h = img.getHeight() * reducCoeff;

            selectedBookCover.setX((selectedBookCover.getFitWidth() - w) / 2);
            selectedBookCover.setY((selectedBookCover.getFitHeight() - h) / 2);
        }
    }

    public void open() {
        Book selectedBook = table.getSelectionModel().getSelectedItem();
        FXMLLoader fxmlLoader = new FXMLLoader(eBookApp.class.getResource("read.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Stage stage = new Stage();
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
        try {
            ReadController controller = (ReadController) fxmlLoader.getController();

            controller.setPrimaryStage(stage);
            controller.setBook(selectedBook);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage){
        this.primaryStage = primaryStage;
    }


}
