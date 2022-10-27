package javaapp.helper;

import javaapp.book.Book;
import javaapp.book.epub.Epub;
import javafx.collections.ObservableList;
import javafx.scene.control.SplitMenuButton;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;

public class MenuHelper {
    private final SplitMenuButton addBook;
    private final ObservableList<Book> bookObservableList;
    private final Stage stage;

    public MenuHelper(SplitMenuButton addBook, ObservableList<Book> bookObservableList, Stage stage){
        this.addBook = addBook;
        this.bookObservableList = bookObservableList;
        this.stage = stage;
        Init();
    }
    private void Init(){
        addBook.setOnAction((e) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Book File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("EPUB", "*.epub")
            );
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                Epub book = new Epub(Path.of(file.getPath()));
                bookObservableList.add(book);
            }
        });
    }
}
