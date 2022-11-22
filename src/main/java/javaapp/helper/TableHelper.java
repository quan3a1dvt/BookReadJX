package javaapp.helper;

import javaapp.book.Book;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import jfxtras.styles.jmetro.JMetroStyleClass;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class TableHelper {
    private final TableView<Book> table;
    private final TableColumn<Book, String> index;
    private final TableColumn<Book, String> title;
    private final TableColumn<Book, String> author;
    private final TableColumn<Book, String> date;
    final private tableCallBacks callbacks;
    final private FilteredList<Book> bookFilteredList;

    final private ObservableList<Book> bookObservableList;

    SortedList<Book> bookSortedList;
    public TableHelper(TableView<Book> table, FilteredList<Book> bookFilteredList, ObservableList<Book> bookObservableList, tableCallBacks callbacks) {
        this.table = table;
        this.bookFilteredList = bookFilteredList;
        this.bookObservableList =  bookObservableList;
        this.callbacks = callbacks;
        this.index = new TableColumn<>("#");
        this.title = new TableColumn<>("Title");
        this.author = new TableColumn<>("Author");
        this.date = new TableColumn<>("Date");
        Init();
    }

    private void Init() {
        index.setPrefWidth(25);
        table.getColumns().addAll(index, title, author, date);
        bookSortedList = new SortedList<>(bookFilteredList);
        table.setItems(this.bookSortedList);
        bookSortedList.comparatorProperty().bind(table.comparatorProperty());
        title.prefWidthProperty().bind(table.widthProperty().multiply(0.35));
        index.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Book, String>, ObservableValue<String>>() {
            @Override public ObservableValue<String> call(TableColumn.CellDataFeatures<Book, String> p) {
                return new ReadOnlyObjectWrapper<>(table.getItems().indexOf(p.getValue()) + "");
            }
        });
        index.setSortable(false);
        title.setCellValueFactory(cell -> cell.getValue().getMetadata().titleProperty());
        author.setCellValueFactory(cell -> cell.getValue().getMetadata().creatorProperty());
        date.setCellValueFactory(cell -> cell.getValue().getMetadata().dateProperty());
        TableView.TableViewSelectionModel<Book> selectionModel = table.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.MULTIPLE);

        table.setOnMouseClicked((MouseEvent event) -> {

//            // Review book selected when mouse click
            callbacks.onTableReviewBook();

            // Read book when double click
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2){
                List<Book> selectedBooks = table.getSelectionModel().getSelectedItems();
                if (!selectedBooks.isEmpty()){
                    callbacks.onTableOpenBook(selectedBooks);
                }

            }
        });
        table.setOnKeyPressed((KeyEvent event) -> {
            //Delete books selected when press delete
            if (event.getCode() == KeyCode.DELETE){
                onRemoveBook();
            }
        });

    }
    private void onRemoveBook(){
        List<Book> selectedBooks = table.getSelectionModel().getSelectedItems();
        SwingWorker<String, Object> worker = new SwingWorker<>() {
            @Override
            public String doInBackground() {
                callbacks.onTableRemoveBook(selectedBooks);
                return "done";
            }
            @Override
            protected void done() {
                for (Book book: selectedBooks){
                    book.getPath().toFile().delete();
                    deleteDir(book.getDataDirectory().toFile());
                }
                bookObservableList.removeAll(selectedBooks);
            }
        };
        worker.execute();
    }
    public void onOpenBookFromMenu(){
        List<Book> selectedBook = table.getSelectionModel().getSelectedItems();
        if (!selectedBook.isEmpty()){
            callbacks.onTableOpenBook(selectedBook);
        }
    }
    public void onRemoveBookFromMenu(){
        callbacks.onTableRemoveBook(table.getSelectionModel().getSelectedItems());
//        bookObservableList.removeAll(table.getSelectionModel().getSelectedItems());
    }
    public interface tableCallBacks {
        void onTableOpenBook(List<Book> book);
        void onTableRemoveBook(List<Book> books);
        void onTableReviewBook();
    }
    void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (! Files.isSymbolicLink(f.toPath())) {
                    deleteDir(f);
                }
            }
        }
        file.delete();
    }
}
