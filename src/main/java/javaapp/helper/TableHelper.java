package javaapp.helper;

import javaapp.book.Book;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class TableHelper {
    private final TableView<Book> table;
    private final TableColumn<Book, String> title;
    private final TableColumn<Book, String> author;
    private final TableColumn<Book, String> date;
    private final ObservableList<Book> bookObservableList;
    final private tableCallBacks callbacks;

    public TableHelper(TableView<Book> table, ObservableList<Book> bookObservableList, tableCallBacks callbacks) {
        this.table = table;
        this.bookObservableList = bookObservableList;
        this.callbacks = callbacks;
        this.title = new TableColumn<>("Title");
        this.author = new TableColumn<>("Author");
        this.date = new TableColumn<>("Date");
        title.prefWidthProperty().bind(table.widthProperty().multiply(0.35));
        Init();
    }

    private void Init() {
        table.getColumns().addAll(title, author, date);
        title.setCellValueFactory(cell -> cell.getValue().getMetadata().titleProperty());
        author.setCellValueFactory(cell -> cell.getValue().getMetadata().creatorProperty());
        date.setCellValueFactory(cell -> cell.getValue().getMetadata().dateProperty());
        table.setItems(bookObservableList);
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
                bookObservableList.removeAll(table.getSelectionModel().getSelectedItems());
                table.getSelectionModel().clearSelection();
            }

            // Review book selected when key arrow press
//            reviewSelectedBook();
        });

    }

    public interface tableCallBacks {
        void onTableOpenBook(Book book);
    }

}
