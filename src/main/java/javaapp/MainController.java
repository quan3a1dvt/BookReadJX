package javaapp;

import java.io.File;

import com.ea.async.Async;
import javaapp.book.Book;
import javaapp.helper.MenuHelper;
import javaapp.helper.TableHelper;
import javaapp.helper.TreeHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import java.net.URL;
import java.util.ResourceBundle;

import javaapp.book.epub.Epub;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.JMetroStyleClass;
import jfxtras.styles.jmetro.Style;

import javax.swing.*;

import static javaapp.book.Book.READER_LIBRARY_DATA_PATH;
import static javaapp.book.Book.READER_LIBRARY_CONFIG_PATH;
import static javaapp.book.Book.READER_LIBRARY_PATH;


public class MainController implements Initializable, TableHelper.tableCallBacks, MenuHelper.menuCallBacks, TreeHelper.treeCallBacks {

    @FXML
    private Pane filterPane;
    @FXML
    private TextField filterBox;
    @FXML
    private HBox topPane;
    @FXML
    private Pane rightPane;
    @FXML
    private Pane bottomPane;
    @FXML
    private ImageView selectedBookCover;
    @FXML
    private TableView<Book> table;
    private TableHelper tableHelper;
    @FXML
    private SplitMenuButton addBook;
    @FXML
    private SplitMenuButton viewBook;
    @FXML
    private SplitMenuButton removeBook;
    @FXML
    private SplitMenuButton saveBook;
    private MenuHelper menuHelper;
    @FXML
    private Text description;
    @FXML
    private TreeTableView<?> tree;

    private TreeHelper treeHelper;

    private ObservableList<Book> bookObservableList;

    private FilteredList<Book> bookFilteredList;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setUI();
        bookObservableList = FXCollections.observableArrayList();
        bookFilteredList = new FilteredList<>(bookObservableList);
        tableHelper = new TableHelper(table, bookFilteredList, bookObservableList, this);
        menuHelper = new MenuHelper(addBook, viewBook, removeBook, saveBook, bookObservableList, primaryStage,this);
        treeHelper = new TreeHelper(tree, bookFilteredList, bookObservableList, primaryStage, this);
        SwingWorker<String, Object> worker = new SwingWorker<>() {
            List<Book> books = new ArrayList<>();
            @Override
            public String doInBackground() {
                List<Path> booksPath = null;
                File file = new File(READER_LIBRARY_PATH.toUri());
                if (!file.exists()){
                    file.mkdir();
                    READER_LIBRARY_DATA_PATH.toFile().mkdir();
                    READER_LIBRARY_CONFIG_PATH.toFile().mkdir();
                }
                try {
                    booksPath = Files.list(READER_LIBRARY_PATH)
                            .filter(path -> !Files.isDirectory(path))
                            .filter(path -> path.toString().endsWith(".epub"))
                            .collect(Collectors.toList());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                booksPath.forEach(path ->

                {
                    // Each book is loaded on a separate thread, this DRASTICALLY decreases load time
                    Epub epub = new Epub(path);
                    // Attempt to initialize the epub with its content.opf file.
                    // If it was not found, log an error and continue to the next book.
                    boolean result = epub.loadMetadata(false);
                    if (result) {
                        bookObservableList.add(epub);
                        books.add(epub);
                    } else {
                        System.out.println(String.format("content.opf could not be read from %s. Is the file a valid .epub? Skipping to the next book.", path.getFileName()));
                    }
                });

                return "done";
            }
            @Override
            protected void done() {
                treeHelper.addBook(books);
            }

        };
        worker.execute();
    }

    private void setUI() {
//        filterPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
//        filterBox.getStyleClass().clear();
//        filterBox.getStyleClass().add("-fx-control-inner-background: rgb(205, 20, 20)");
//        topPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
//        rightPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
////        leftPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
//        bottomPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
    }


    public void onTableOpenBook(List<Book> books) {
        onOpenBook(books);
    }
    private void onOpenBook(List<Book> books){
        for (Book book: books){
            FXMLLoader fxmlLoader = new FXMLLoader(eBookApp.class.getResource("read.fxml"));
            Scene scene = null;
            try {
                scene = new Scene(fxmlLoader.load());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            JMetro jmetro = new JMetro(scene, Style.DARK);
            Stage stage = new Stage();
            stage.setTitle(book.getMetadata().getTitle());
            try {
                stage.getIcons().add(new Image(eBookApp.class.getResource("images/viewer.png").openStream()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            stage.setScene(scene);
            stage.show();
            try {
                ReadController controller = (ReadController) fxmlLoader.getController();
                controller.setPrimaryStage(stage);
                controller.setBook(book);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    @Override
    public void onTableRemoveBook(List<Book> books){
        treeHelper.removeBook(books);
    }

    @Override
    public void onTableReviewBook() {
        Book book = table.getSelectionModel().getSelectedItem();
        if (book == null){
            return;
        }
        selectedBookCover.setImage(book.getCover());
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
        description.setText(book.getMetadata().getDescription().replaceAll("<.+?>", ""));

    }

    @Override
    public void onMenuAddBook(List<Book> books) {
        treeHelper.addBook(books);
    }
    @Override
    public void onMenuRemoveBook() {tableHelper.onRemoveBook();}

    @Override
    public void onMenuSaveBook() {
        Book book = table.getSelectionModel().getSelectedItem();
        menuHelper.onMenuSaveBook(book);
    }

    @Override
    public void onMenuOpenBook() {
        tableHelper.onOpenBookFromMenu();
    }

    @Override
    public void onMenuOpenBook(List<Book> books) {
        onOpenBook(books);
    }

    @Override
    public void onTreeFilter(String filter) {
        filterBox.setText(filter);

    }
}
