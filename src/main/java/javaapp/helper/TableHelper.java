package javaapp.helper;

import javaapp.book.Book;
import javaapp.book.epub.Epub;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import jfxtras.styles.jmetro.JMetroStyleClass;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javaapp.book.Book.READER_LIBRARY_PATH;

public class TableHelper {
    private final TableView<Book> table;
    private final TableColumn<Book, String> title;
    private final TableColumn<Book, String> author;
    private final TableColumn<Book, String> date;
    final private tableCallBacks callbacks;
    final private FilteredList<Book> bookFilteredList;
    public TableHelper(TableView<Book> table, FilteredList<Book> bookFilteredList, tableCallBacks callbacks) {
        this.table = table;
        this.bookFilteredList = bookFilteredList;
        this.table.setItems(bookFilteredList);
        this.callbacks = callbacks;
        this.title = new TableColumn<>("Title");
        this.author = new TableColumn<>("Author");
        this.date = new TableColumn<>("Date");
        title.prefWidthProperty().bind(table.widthProperty().multiply(0.35));
        Init();
    }

    private void Init() {
        table.getStyleClass().add(JMetroStyleClass.BACKGROUND);
        table.getColumns().addAll(title, author, date);
        title.setCellValueFactory(cell -> cell.getValue().getMetadata().titleProperty());
        author.setCellValueFactory(cell -> cell.getValue().getMetadata().creatorProperty());
        date.setCellValueFactory(cell -> cell.getValue().getMetadata().dateProperty());
        table.setItems(bookFilteredList);
        TableView.TableViewSelectionModel<Book> selectionModel = table.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.MULTIPLE);
        table.setOnMouseClicked((MouseEvent event) -> {

//            // Review book selected when mouse click
//            reviewSelectedBook();

            // Read book when double click
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2){
                Book selectedBook = table.getSelectionModel().getSelectedItem();
                callbacks.onTableOpenBook(selectedBook);
            }
        });
        table.setOnKeyPressed((KeyEvent event) -> {
            //Delete books selected when press delete
            if (event.getCode() == KeyCode.DELETE){

                SwingWorker<String, Object> worker = new SwingWorker<>() {
                    @Override
                    public String doInBackground() {
                        callbacks.onTableDeleteBook(table.getSelectionModel().getSelectedItems());
                        return "done";
                    }
                    @Override
                    protected void done() {
                        bookFilteredList.removeAll(table.getSelectionModel().getSelectedItems());
                    }

                };
                worker.execute();

//                table.getSelectionModel().clearSelection();

            }

            // Review book selected when key arrow press
//            reviewSelectedBook();
        });

    }

    public interface tableCallBacks {
        void onTableOpenBook(Book book);
        void onTableDeleteBook(List<Book> books);
    }

}
