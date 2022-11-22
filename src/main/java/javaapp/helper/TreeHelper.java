package javaapp.helper;

import javaapp.book.Book;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import javax.swing.*;
import java.util.*;
import java.util.function.Predicate;

public class TreeHelper {
    public class FilterNode {

        private String name;
        private Integer num;

        private String state = "";

        public FilterNode() {
        }

        public FilterNode(String name, int num, String state) {
            this.name = name;
            this.num = num;
            this.state = state;
        }

        public void changeState() {
            if (state == "") state = "➕";
            else if (state == "➕") state = "➖";
            else state = "";
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

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        private void resetState() {
            this.state = "";
        }
    }

    private final TreeTableView<FilterNode> tree;
    private TreeTableColumn<FilterNode, String> treeCol;
    private TreeTableColumn<FilterNode, Integer> numCol;
    private TreeTableColumn<FilterNode, String> stateCol;
    private TreeItem<FilterNode> root;
    private TreeItem<FilterNode> authors;
    private TreeItem<FilterNode> languages;
    private TreeItem<FilterNode> publishers;
    private final Set<String> tmpAuthorsSet = new HashSet<>();
    private final Set<String> tmpLanguageSet = new HashSet<>();
    private final Set<String> tmpPublisherSet = new HashSet<>();

    Map<String, Integer> authorsMap = new HashMap<String, Integer>();
    Map<String, Integer> languageMap = new HashMap<String, Integer>();
    Map<String, Integer> publisherMap = new HashMap<String, Integer>();

    private FilterNode preFilterNodeAuthors;
    private FilterNode preFilterNodeLanguages;
    private FilterNode preFilterNodePublishers;
    private final FilteredList<Book> bookFilteredList;
    private final ObservableList<Book> bookObservableList;
    private Predicate<Book> filterAuthors;
    private Predicate<Book> filterLanguages;
    private Predicate<Book> filterPublishers;
    private String filterAuthorsString = "ALL";
    private String filterLanguagesString = "ALL";
    private String filterPublishersString = "ALL";
    private final Stage stage;

    private treeCallBacks callbacks;

    public TreeHelper(TreeTableView<?> tree, FilteredList<Book> bookFilteredList, ObservableList<Book> bookObservableList, Stage primaryStage, treeCallBacks callbacks) {
        this.tree = (TreeTableView<FilterNode>) tree;
        this.bookFilteredList = bookFilteredList;
        this.bookObservableList = bookObservableList;
        this.stage = primaryStage;
        this.callbacks = callbacks;
        Init();
    }

    private void Init() {
        setUI();
        setEvent();
    }

    void setUI() {
        treeCol = new TreeTableColumn<>("Filter");
        numCol = new TreeTableColumn<>("Num");
        stateCol = new TreeTableColumn<>("");
        treeCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        numCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("num"));
        stateCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("state"));
        tree.getColumns().add(treeCol);
        tree.getColumns().add(numCol);
        tree.getColumns().add(stateCol);
        double treeWidth = tree.getPrefWidth();
        numCol.setPrefWidth(treeWidth * 0.15);
        stateCol.setPrefWidth(treeWidth * 0.08);
        treeCol.setPrefWidth(tree.getPrefWidth() - numCol.getPrefWidth() - stateCol.getPrefWidth() - treeWidth * 0.07);
        root = new TreeItem<FilterNode>(new FilterNode());
        authors = new TreeItem<FilterNode>(new FilterNode("Authors", 0, ""));
        languages = new TreeItem<FilterNode>(new FilterNode("Language", 0, ""));
        publishers = new TreeItem<FilterNode>(new FilterNode("Publisher", 0, ""));

        tree.setRoot(root);
        root.getChildren().addAll(authors, languages, publishers);
        tree.setShowRoot(false);


    }

    void setEvent() {
        filterAuthors = i -> i.getMetadata().getCreator() != null;
        filterLanguages = i -> i.getMetadata().getLanguage() != null;
        filterPublishers = i -> i.getMetadata().getPublisher() != null;
        tree.setOnKeyPressed((KeyEvent event) -> {
            //Delete books selected when press delete
            if (event.getCode() == KeyCode.SHIFT){
                tree.refresh();
            }
        });
        tree.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && tree.getSelectionModel().getSelectedItem() != null) {
                TreeItem<FilterNode> item = tree.getSelectionModel().getSelectedItem();
                FilterNode node = item.getValue();
                if (item == authors || item.getParent() == authors) {
                    if (preFilterNodeAuthors != null && !Objects.equals(preFilterNodeAuthors.getName(), node.getName())) {
                        preFilterNodeAuthors.resetState();
                    }
                    node.changeState();
                    preFilterNodeAuthors = node;
                    if (item == authors) {
                        if (Objects.equals(node.getState(), "")) {
                            filterAuthors = i -> i.getMetadata().getCreator() != null;
                            filterAuthorsString = "ALL";
                        } else if (Objects.equals(node.getState(), "➕")) {
                            filterAuthors = i -> !Objects.equals(i.getMetadata().getCreator(), "");
                            filterAuthorsString = "TRUE";
                        } else {
                            filterAuthors = i -> Objects.equals(i.getMetadata().getCreator(), "");
                            filterAuthorsString = "FALSE";
                        }
                    } else {
                        if (Objects.equals(node.getState(), "")) {
                            filterAuthors = i -> i.getMetadata().getCreator() != null;
                            filterAuthorsString = "ALL";
                        } else if (Objects.equals(node.getState(), "➕")) {
                            filterAuthors = i -> Objects.equals(i.getMetadata().getCreator(), node.getName());
                            filterAuthorsString = "=" + node.getName();
                        } else {
                            filterAuthors = i -> !Objects.equals(i.getMetadata().getCreator(), node.getName());
                            filterAuthorsString = "!=" + node.getName();
                        }
                    }
                }

                if (item == languages || item.getParent() == languages) {
                    if (preFilterNodeLanguages != null && !Objects.equals(preFilterNodeLanguages.getName(), node.getName())) {
                        preFilterNodeLanguages.resetState();
                    }
                    node.changeState();
                    preFilterNodeLanguages = node;
                    if (item == languages) {
                        if (Objects.equals(node.getState(), "")) {
                            filterLanguages = i -> i.getMetadata().getLanguage() != null;
                            filterLanguagesString = "ALL";
                        } else if (Objects.equals(node.getState(), "➕")) {
                            filterLanguages = i -> !Objects.equals(i.getMetadata().getLanguage(), "");
                            filterLanguagesString = "TRUE";
                        } else {
                            filterLanguages = i -> Objects.equals(i.getMetadata().getLanguage(), "");
                            filterLanguagesString = "FALSE";
                        }
                    } else {
                        if (Objects.equals(node.getState(), "")) {
                            filterLanguages = i -> i.getMetadata().getLanguage() != null;
                            filterLanguagesString = "ALL";
                        } else if (Objects.equals(node.getState(), "➕")) {
                            filterLanguages = i -> Objects.equals(i.getMetadata().getLanguage(), node.getName());
                            filterLanguagesString = "=" + node.getName();
                        } else {
                            filterLanguages = i -> !Objects.equals(i.getMetadata().getLanguage(), node.getName());
                            filterLanguagesString = "!=" + node.getName();
                        }
                    }
                }

                if (item == publishers || item.getParent() == publishers) {
                    if (preFilterNodePublishers != null && !Objects.equals(preFilterNodePublishers.getName(), node.getName())) {
                        preFilterNodePublishers.resetState();
                    }
                    node.changeState();
                    preFilterNodePublishers = node;
                    if (item == publishers) {
                        if (Objects.equals(node.getState(), "")) {
                            filterPublishers = i -> i.getMetadata().getPublisher() != null;
                            filterPublishersString = "ALL";
                        } else if (Objects.equals(node.getState(), "➕")) {
                            filterPublishers = i -> !Objects.equals(i.getMetadata().getPublisher(), "");
                            filterPublishersString = "TRUE";
                        } else {
                            filterPublishers = i -> Objects.equals(i.getMetadata().getPublisher(), "");
                            filterPublishersString = "FALSE";
                        }
                    } else {
                        if (Objects.equals(node.getState(), "")) {
                            filterPublishers = i -> i.getMetadata().getPublisher() != null;
                            filterPublishersString = "ALL";
                        } else if (Objects.equals(node.getState(), "➕")) {
                            filterPublishers = i -> Objects.equals(i.getMetadata().getPublisher(), node.getName());
                            filterPublishersString = "=" + node.getName();
                        } else {
                            filterPublishers = i -> !Objects.equals(i.getMetadata().getPublisher(), node.getName());
                            filterPublishersString = "!=" + node.getName();
                        }
                    }
                }

                Predicate<Book> filter = filterAuthors.and(filterLanguages.and(filterPublishers));
                bookFilteredList.setPredicate(filter);
                tree.getSelectionModel().clearSelection();
                tree.refresh();
                String filterString = "";
                if (filterAuthorsString != "ALL") {
                    if (filterString != "") {
                        filterString += " and ";
                    }
                    filterString += "author:" + "\"" + filterAuthorsString + "\"";
                }
                if (filterLanguagesString != "ALL") {
                    if (filterString != "") {
                        filterString += " and ";
                    }
                    filterString += "language:" + "\"" + filterLanguagesString + "\"";
                }
                if (filterPublishersString != "ALL") {
                    if (filterString != "") {
                        filterString += " and ";
                    }
                    filterString += "publisher:" + "\"" + filterPublishersString + "\"";
                }
//                String filterString = "author:" + "\"" + filterAuthorsString + "\"" + " and " + "language:" + "\"" + filterLanguagesString + "\"" + " and " + "publisher:" + "\"" + filterPublishersString + "\"";
                callbacks.onTreeFilter(filterString);
            }
        });
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
                        tmpAuthorsSet.add(author);
                        authorsMap.put(author, 1);
                    }

                    String language = book.getMetadata().getLanguage();
                    if (languageMap.containsKey(language)) {
                        languageMap.put(language, languageMap.get(language) + 1);
                    } else {
                        tmpLanguageSet.add(language);
                        languageMap.put(language, 1);
                    }

                    String publisher = book.getMetadata().getPublisher();
                    if (!publisher.equals("")) {
                        if (publisherMap.containsKey(publisher)) {
                            publisherMap.put(publisher, publisherMap.get(publisher) + 1);
                        } else {
                            tmpPublisherSet.add(publisher);
                            publisherMap.put(publisher, 1);
                        }
                    }

                }
                return "done";
            }

            @Override
            protected void done() {

                tree.setDisable(true);

                FilterNode node;

                authors.getChildren().forEach(child -> {
                    // Each book is loaded on a separate thread, this DRASTICALLY decreases load time
                    new Thread(() -> {
                        FilterNode tmpNode = child.getValue();
                        if (authorsMap.containsKey(tmpNode.getName()))
                            tmpNode.setNum(authorsMap.get(tmpNode.getName()));
                        child.setValue(tmpNode);
                    }).start();
                });

                for (String author : tmpAuthorsSet) {
                    if (authorsMap.containsKey(author)) {
                        authors.getChildren().add(new TreeItem<FilterNode>(new FilterNode(author, authorsMap.get(author), "")));
                    }
                }
                tmpAuthorsSet.clear();

                node = authors.getValue();
                node.setNum(authorsMap.size());
                authors.setValue(node);


                languages.getChildren().forEach(child -> {
                    // Each book is loaded on a separate thread, this DRASTICALLY decreases load time
                    new Thread(() -> {
                        FilterNode tmpNode = child.getValue();
                        if (languageMap.containsKey(tmpNode.getName()))
                            tmpNode.setNum(languageMap.get(tmpNode.getName()));
                        child.setValue(tmpNode);
                    }).start();
                });

                for (String language : tmpLanguageSet) {
                    if (languageMap.containsKey(language)) {
                        languages.getChildren().add(new TreeItem<FilterNode>(new FilterNode(language, languageMap.get(language), "")));
                    }
                }
                tmpLanguageSet.clear();

                node = languages.getValue();
                node.setNum(languageMap.size());
                languages.setValue(node);

                publishers.getChildren().forEach(child -> {
                    // Each book is loaded on a separate thread, this DRASTICALLY decreases load time
                    new Thread(() -> {
                        FilterNode tmpNode = child.getValue();
                        if (publisherMap.containsKey(tmpNode.getName()))
                            tmpNode.setNum(publisherMap.get(tmpNode.getName()));
                        child.setValue(tmpNode);
                    }).start();
                });

                for (String publisher : tmpPublisherSet) {
                    if (publisherMap.containsKey(publisher)) {
                        publishers.getChildren().add(new TreeItem<FilterNode>(new FilterNode(publisher, publisherMap.get(publisher), "")));
                    }
                }
                tmpPublisherSet.clear();

                node = publishers.getValue();
                node.setNum(publisherMap.size());
                publishers.setValue(node);


                tree.refresh();

                tree.setDisable(false);
            }
        };
        worker.execute();
    }

    public void removeBook(List<Book> books) {

        Set<String> tmpSetAuthor = new HashSet<>();
        Set<String> tmpSetLanguage = new HashSet<>();
        Set<String> tmpSetPublisher = new HashSet<>();
        Map<String, Integer> tmpMapAuthor = new HashMap<>();
        Map<String, Integer> tmpMapLanguage = new HashMap<>();
        Map<String, Integer> tmpMapPublisher = new HashMap<>();

        SwingWorker<String, Object> worker = new SwingWorker<>() {
            @Override
            public String doInBackground() {
                for (Book book : books) {
                    String author = book.getMetadata().getCreator();
                    if(authorsMap.containsKey(author)) {
                        if (authorsMap.get(author) == 1) {
                            authorsMap.remove(author);
                            tmpSetAuthor.add(author);
                        } else authorsMap.put(author, authorsMap.get(author) - 1);
                    }
                    String language = book.getMetadata().getLanguage();
                    if(languageMap.containsKey(language)){
                        if (languageMap.get(language) == 1) {
                            languageMap.remove(language);
                            tmpSetLanguage.add(language);
                        } else languageMap.put(language, languageMap.get(language) - 1);
                    }


                    String publisher = book.getMetadata().getPublisher();
                    if(publisherMap.containsKey(publisher)){
                        if (publisherMap.get(publisher) == 1) {
                            publisherMap.remove(publisher);
                            tmpSetPublisher.add(publisher);
                        } else publisherMap.put(publisher, publisherMap.get(publisher) - 1);
                    }

                }
                return "done";
            }

            @Override
            protected void done() {
                FilterNode node;
                List<TreeItem> removeChild = new ArrayList<>();
                for (int i = 0; i < authors.getChildren().size(); i++) {
                    TreeItem<FilterNode> child = authors.getChildren().get(i);
                    node = child.getValue();
                    if (tmpSetAuthor.contains(node.getName())) {
                        removeChild.add(child);
                    } else {
                        if (authorsMap.containsKey(node.getName())) {
                            node.setNum(authorsMap.get(node.getName()));
                            child.setValue(node);
                        }
                    }
                }
                for (TreeItem child: removeChild){
                    authors.getChildren().removeAll(removeChild);
                }

                node = authors.getValue();
                node.setNum(authorsMap.size());
                authors.setValue(node);

                removeChild = new ArrayList<>();
                for (int i = 0; i < languages.getChildren().size(); i++) {
                    TreeItem<FilterNode> child = languages.getChildren().get(i);
                    node = child.getValue();
                    if (tmpSetLanguage.contains(node.getName())) {
                        removeChild.add(child);
                    } else {
                        if (languageMap.containsKey(node.getName())) {
                            node.setNum(languageMap.get(node.getName()));
                            child.setValue(node);
                        }
                    }
                }
                for (TreeItem child: removeChild){
                    languages.getChildren().removeAll(removeChild);
                }

                node = languages.getValue();
                node.setNum(languageMap.size());
                languages.setValue(node);

                removeChild = new ArrayList<>();
                for (int i = 0; i < publishers.getChildren().size(); i++) {
                    TreeItem<FilterNode> child = publishers.getChildren().get(i);
                    node = child.getValue();
                    if (tmpSetPublisher.contains(node.getName())) {
                        removeChild.add(child);
                    } else {
                        if (publisherMap.containsKey(node.getName())) {
                            node.setNum(publisherMap.get(node.getName()));
                            child.setValue(node);
                        }
                    }
                }
                for (TreeItem child: removeChild){
                    publishers.getChildren().removeAll(removeChild);
                }
                node = publishers.getValue();
                node.setNum(publisherMap.size());
                publishers.setValue(node);

                tree.refresh();
            }
        };
        worker.execute();
    }

    public interface treeCallBacks {
        void onTreeFilter(String filter);

    }

}
