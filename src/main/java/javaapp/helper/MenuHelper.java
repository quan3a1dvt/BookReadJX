package javaapp.helper;

import javaapp.book.Book;
import javaapp.book.epub.Epub;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;

public class MenuHelper {
    private final SplitMenuButton addBook;
    private final ObservableList<Book> bookObservableList;
    private final Stage stage;
    private MenuItem addBook_1;
    private MenuItem addBook_2;

    public MenuHelper(SplitMenuButton addBook, ObservableList<Book> bookObservableList, Stage stage) {
        this.addBook = addBook;
        this.bookObservableList = bookObservableList;
        this.stage = stage;
        Init();
    }

    private void Init() {
        setUI();
        setEvent();
    }

    private void setUI() {
        addBook_1 = new MenuItem("Add books from single folder");
        addBook_2 = new MenuItem("Add books from folder and sub-folder");
        addBook.getItems().addAll(addBook_1, addBook_2);
    }

    private void setEvent() {
        addBook.setOnAction((e) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Book File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("EPUB", "*.epub")
            );
            File file = fileChooser.showOpenDialog(stage);
            if (file == null) return;
            Epub book = new Epub(Path.of(file.getPath()));
            bookObservableList.add(book);
        });
        addBook_1.setOnAction((e) -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Open Book Folder");
            File folder = directoryChooser.showDialog(stage);
            if (folder == null) return;
            File[] filesList = folder.listFiles();
            assert filesList != null;
            for (File file : filesList) {
                String fileName = file.toString();
                if (fileName.endsWith("epub")) {
                    Book book = new Epub(Path.of(file.getPath()));
                    bookObservableList.add(book);
                }
            }
        });
    }
}
