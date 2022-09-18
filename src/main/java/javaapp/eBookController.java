package javaapp;
import java.io.File;

import javaapp.book.Book;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import java.net.URL;
import java.util.ResourceBundle;

import javaapp.book.epub.Epub;
import javafx.stage.Stage;



public class eBookController implements Initializable {

    public static final Path READER_LIBRARY_PATH = Paths.get(System.getProperty("user.home"), "ReaderLibrary");
    public static final Path READER_LIBRARY_DATA_PATH = Paths.get(System.getProperty("user.home"), "ReaderLibrary", "Data");

    @FXML
    private ImageView add_book;
    @FXML
    private ImageView selectedBookCover;
    @FXML
    private TableView<Book> book_table;

    @FXML
    private TableColumn<Book, String> author;

    @FXML
    private TableColumn<Book, Double> size;

    @FXML
    private TableColumn<Book, String> title;


    ObservableList<Book> eBookObservableList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        author.setCellValueFactory(cell -> cell.getValue().getMetadata().creatorProperty());
//        size.setCellValueFactory(cell -> cell.getValue().getMetadata().creatorProperty());
        title.setCellValueFactory(cell -> cell.getValue().getMetadata().titleProperty());
        TableView.TableViewSelectionModel<Book> selectionModel = book_table.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.MULTIPLE);
        book_table.setItems(eBookObservableList);
        try {
            Init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void Init() throws Exception{
        List<Path> booksPath = Files.list(READER_LIBRARY_PATH)
                    .filter(path -> !Files.isDirectory(path))
                    .filter(path -> path.toString().endsWith(".epub"))
                    .collect(Collectors.toList());

        booksPath.forEach(path -> {
            // Each book is loaded on a separate thread, this DRASTICALLY decreases load time
            new Thread(() -> {
                Epub epub = new Epub(path);



                // Attempt to initialize the epub with its content.opf file.
                // If it was not found, log an error and continue to the next book.
                boolean result = epub.loadMetadata(false);
                if (result) {
                    eBookObservableList.add(epub);

                    // add epub book
//                    Platform.runLater(() -> {
//                        BookIconNode node = new BookIconNode(epub);
//                        allBooks.getChildren().add(node);
//
//                        // makeshift recent section
//                        if(eBookObservableList.indexOf(epub) < 6) {
//                            recentBooks.getChildren().add(node);
//                        }
//
//                        // on click, open the book
//                        node.setOnMouseClicked(event -> {
//                            sidebar.display(node.getBook()); // ??????
//                        });
//                    });
                } else {
                    // TODO: still add book, but have invalid cover/warning marker on it?
                    System.out.println(String.format("content.opf could not be read from %s. Is the file a valid .epub? Skipping to the next book.", path.getFileName()));
                }
            }).start();
        });
    }
    //    String[] Extension={"epub"};
    @FXML
    public void addBookClick() throws Exception{
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Book File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("EPUB", "*.epub")
        );
        File file = fileChooser.showOpenDialog(this.primaryStage);
        if (file != null) {
            Epub book = new Epub(Path.of(file.getPath()));
            eBookObservableList.add(book);
        }

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
                eBookObservableList.add(book);
            }
        }
    }
    @FXML
    void removeBook(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE){
            eBookObservableList.removeAll(book_table.getSelectionModel().getSelectedItems());
            book_table.getSelectionModel().clearSelection();
        }
    }
    @FXML
    void selectBook(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY){
            Book selectedBook = book_table.getSelectionModel().getSelectedItem();
            if (selectedBook != null)
                selectedBookCover.setImage(selectedBook.getCover());
        }
    }
    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage){
        this.primaryStage = primaryStage;
    }


}
