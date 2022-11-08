package javaapp.helper;

import javaapp.book.Book;
import javaapp.eBookApp;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javax.swing.*;
import java.util.*;

public class TreeHelper {
    public class FilterNode {

        private String name;
        private Integer num;

        public FilterNode() {
        }

        public FilterNode(String name, int num) {
            this.name = name;
            this.num = num;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }
    }

    private final TreeTableView<FilterNode> tree;
    private TreeTableColumn<FilterNode, String> treeCol;
    private TreeTableColumn<FilterNode, Integer> numCol;
    private TreeItem<FilterNode> root;
    private TreeItem<FilterNode> authors;
    private TreeItem<FilterNode> language;
    private TreeItem<FilterNode> publisher;
    private Set<String> tmpSet = new HashSet<>();

    Map<String, Integer> authorsMap = new HashMap<String, Integer>();
    private final ObservableList<Book> bookObservableList;
    private final Stage stage;

    public TreeHelper(TreeTableView<?> tree, ObservableList<Book> bookObservableList, Stage primaryStage) {
        this.tree = (TreeTableView<FilterNode>) tree;
        this.bookObservableList = bookObservableList;
        this.stage = primaryStage;

        Init();
    }

    private void Init() {
        setUI();
        setEvent();
    }

    void setUI() {
        treeCol = new TreeTableColumn<>("Filter");
        numCol = new TreeTableColumn<>("Num");
        treeCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        numCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("num"));
        tree.getColumns().add(treeCol);
        tree.getColumns().add(numCol);
        numCol.setPrefWidth(40);
        treeCol.setPrefWidth(tree.getPrefWidth() - numCol.getPrefWidth());
        Image iconAuthors = new Image(Objects.requireNonNull(eBookApp.class.getResourceAsStream("images/user_profile.png")), 15, 15, false, false);
        Image iconLanguage = new Image(Objects.requireNonNull(eBookApp.class.getResourceAsStream("images/languages.png")), 15, 15, false, false);
        Image iconPublisher = new Image(Objects.requireNonNull(eBookApp.class.getResourceAsStream("images/publisher.png")), 15, 15, false, false);
        root = new TreeItem<FilterNode>(new FilterNode());
        authors = new TreeItem<FilterNode>(new FilterNode("Authors", 0), new ImageView(iconAuthors));
        language = new TreeItem<FilterNode>(new FilterNode("Language", 0), new ImageView(iconLanguage));
        publisher = new TreeItem<FilterNode>(new FilterNode("Publisher", 0), new ImageView(iconPublisher));
        tree.setRoot(root);
        root.getChildren().addAll(authors, language, publisher);
        tree.setShowRoot(false);
        TreeTableView.TreeTableViewSelectionModel selectionModel = tree.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.MULTIPLE);
    }

    void setEvent() {

    }

    public void addBook(List<Book> books) {
        SwingWorker<String, Object> worker = new SwingWorker<>() {

            @Override
            public String doInBackground() {
                for (Book book : books) {
                    String author = book.getMetadata().getCreator();
                    if (authorsMap.containsKey(author)) {
                        authorsMap.put(author, authorsMap.get(author) + 1);
                    } else {
                        tmpSet.add(author);
                        authorsMap.put(author, 1);
                    }
                }
                return "done";
            }

            @Override
            protected void done() {
                authors.getChildren().forEach(child -> {
                    // Each book is loaded on a separate thread, this DRASTICALLY decreases load time
                    new Thread(() -> {
                        FilterNode node = child.getValue();
                        node.setNum(authorsMap.get(node.getName()));
                        child.setValue(node);
                    }).start();
                });

                for (String author : tmpSet) {
                    if (authorsMap.containsKey(author)) {
                        authors.getChildren().add(new TreeItem<FilterNode>(new FilterNode(author, authorsMap.get(author))));
                    }
                }
                tmpSet.clear();

                FilterNode node = authors.getValue();
                node.setNum(authorsMap.size());
                authors.setValue(node);
                tree.refresh();
            }
        };
        worker.execute();
    }

    public void deleteBook(List<Book> books) {
        Set<String> tmpSet = new HashSet<>();
        SwingWorker<String, Object> worker = new SwingWorker<>() {
            @Override
            public String doInBackground() {

                for (Book book : books) {
                    String author = book.getMetadata().getCreator();

                    if (authorsMap.get(author) == 1) {
                        authorsMap.remove(author);
                        tmpSet.add(author);
                    } else authorsMap.put(author, authorsMap.get(author) - 1);
                }
                return "done";
            }

            @Override
            protected void done() {

                for (int i = 0; i < authors.getChildren().size(); i++) {
                    TreeItem<FilterNode> child = authors.getChildren().get(i);
                    FilterNode node = child.getValue();
                    if (tmpSet.contains(node.getName())) {
                        authors.getChildren().remove(child);
                    } else {
                        if (authorsMap.containsKey(node.getName())) {
                            node.setNum(authorsMap.get(node.getName()));
                            child.setValue(node);
                        }
                    }
                }

                FilterNode node = authors.getValue();
                node.setNum(authorsMap.size());
                authors.setValue(node);
                tree.refresh();
            }
        };
        worker.execute();
    }
}
