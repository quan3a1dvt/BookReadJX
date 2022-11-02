package javaapp.helper;

import javaapp.book.Book;
import javaapp.eBookApp;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.util.Objects;

public class TreeHelper {

    private final TreeView<String> tree;
    private final ObservableList<Book> bookObservableList;
    private final Stage stage;

    public TreeHelper(TreeView<String> tree, ObservableList<Book> bookObservableList, Stage primaryStage) {
        this.tree = tree;
        this.bookObservableList = bookObservableList;
        this.stage = primaryStage;

        Init();
    }
    private void Init() {
        setUI();
        setEvent();
    }
    void setUI(){
        Image iconAuthors = new Image(Objects.requireNonNull(eBookApp.class.getResourceAsStream("images/user_profile.png")), 15, 15, false, false);
        Image iconLanguage = new Image(Objects.requireNonNull(eBookApp.class.getResourceAsStream("images/languages.png")), 15, 15, false, false);
        Image iconPublisher = new Image(Objects.requireNonNull(eBookApp.class.getResourceAsStream("images/publisher.png")), 15, 15, false, false);
        TreeItem<String> root = new TreeItem<>("Bruh");
        TreeItem<String> authors = new TreeItem<String>("Authors", new ImageView(iconAuthors));
        TreeItem<String> language = new TreeItem<String>("Language", new ImageView(iconLanguage));
        TreeItem<String> publisher = new TreeItem<String>("Publisher", new ImageView(iconPublisher));
        tree.setRoot(root);
        root.getChildren().addAll(authors, language, publisher);
        tree.setShowRoot(false);
        System.out.println(bookObservableList.size());
        for (Book book:bookObservableList){
            boolean found = false;
            for(TreeItem<String> item: authors.getChildren()){
                if (item.getValue() == book.getMetadata().getCreator()){
                    found = true;
                    break;
                }
            }
            if (!found){
                authors.getChildren().add(new TreeItem<String>(book.getMetadata().getCreator()));
            }
        }
    };
    void setEvent(){};

    void addbook(Book book){

    }

}
