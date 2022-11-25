package javaapp.helper;

import javaapp.book.Book;
import javaapp.book.epub.Epub;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;

import static javaapp.book.Book.READER_LIBRARY_DATA_PATH;
import static javaapp.book.Book.READER_LIBRARY_CONFIG_PATH;
import static javaapp.book.Book.READER_LIBRARY_PATH;
public class MenuHelper {

    private final ObservableList<Book> bookObservableList;
    private final Stage stage;
    private final SplitMenuButton addBook;
    private MenuItem addBook_1;
    private MenuItem addBook_2;
    private final SplitMenuButton viewBook;

    private MenuItem viewBook_1;
    private MenuItem viewBook_2;
    private final SplitMenuButton removeBook;
    private final SplitMenuButton saveBook;
    Random rand = new Random();
    final private menuCallBacks callbacks;
    public MenuHelper(SplitMenuButton addBook, SplitMenuButton viewBook, SplitMenuButton removeBook, SplitMenuButton saveBook, ObservableList<Book> bookObservableList, Stage stage, menuCallBacks callbacks) {
        this.addBook = addBook;
        this.viewBook = viewBook;
        this.removeBook = removeBook;
        this.saveBook = saveBook;
        this.bookObservableList = bookObservableList;
        this.stage = stage;
        this.callbacks = callbacks;
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
        viewBook_1 = new MenuItem("View");
        viewBook_2 = new MenuItem("Read a random book");
        viewBook.getItems().addAll(viewBook_1, viewBook_2);

    }

    private void setEvent() {
        addBook.setOnAction((e) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Book File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("EPUB", "*.epub")
            );
            List<File> files = fileChooser.showOpenMultipleDialog(stage);
            if (files == null) return;
            for (File file: files){
                if (file == null) return;
                if (!READER_LIBRARY_PATH.resolve(file.toPath().getFileName()).toFile().exists()) {
                    try {
                        Files.copy(file.toPath(), READER_LIBRARY_PATH.resolve(file.toPath().getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    Epub book = new Epub(Path.of(file.getPath()));
                    bookObservableList.add(book);
                    List<Book> books = new ArrayList<>();
                    books.add(book);
                    callbacks.onMenuAddBook(books);
                }
            }



        });
        addBook_1.setOnAction((e) -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Open Book Folder");
            File folder = directoryChooser.showDialog(stage);
            if (folder == null) return;
            File[] filesList = folder.listFiles();
            assert filesList != null;
            List<Book> books = new ArrayList<>();

            for (File file : filesList) {
                String fileName = file.toString();
                if (fileName.endsWith("epub")) {
                    if (!READER_LIBRARY_PATH.resolve(file.toPath().getFileName()).toFile().exists()){
                        try {
                            Files.copy(file.toPath(), READER_LIBRARY_PATH.resolve(file.toPath().getFileName()), StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        Book book = new Epub(Path.of(file.getPath()));
                        bookObservableList.add(book);
                        books.add(book);
                    }

                }
            }
            callbacks.onMenuAddBook(books);
        });

        addBook_2.setOnAction((e) -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Open Book Folder");
            File folder = directoryChooser.showDialog(stage);
            if (folder == null) return;
            File[] filesList = folder.listFiles();
            assert filesList != null;
            List<Book> books = new ArrayList<>();
            Deque<File> queue = new ArrayDeque<File>();
            queue.addAll(List.of(filesList));
            while(!queue.isEmpty()){
                File file = queue.peek();
                queue.pop();
                if (file.isDirectory()){
                    filesList = file.listFiles();
                    if (filesList == null) continue;
                    queue.addAll(List.of(filesList));
                    continue;
                }
                if (file.toString().endsWith("epub")) {
                    if (!READER_LIBRARY_PATH.resolve(file.toPath().getFileName()).toFile().exists()){
                        try {
                            Files.copy(file.toPath(), READER_LIBRARY_PATH.resolve(file.toPath().getFileName()), StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        Book book = new Epub(Path.of(file.getPath()));
                        bookObservableList.add(book);
                        books.add(book);
                    }

                }
            }
            callbacks.onMenuAddBook(books);
        });
        viewBook.setOnAction((e) -> {
            callbacks.onMenuOpenBook();
        });
        viewBook_1.setOnAction((e) -> {
            callbacks.onMenuOpenBook();
        });
        viewBook_2.setOnAction((e) -> {
            List<Book> books = new ArrayList<>();
            books.add(bookObservableList.get(rand.nextInt(bookObservableList.size())));
            callbacks.onMenuOpenBook(books);
        });
        removeBook.setOnAction((e) -> {
            callbacks.onMenuRemoveBook();
        });
        saveBook.setOnAction((e) -> {
            callbacks.onMenuSaveBook();
        });


    }

    public void onMenuSaveBook(Book book) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose Save Folder");
        File folder = directoryChooser.showDialog(stage);
        if (folder == null) return;
        File folderauthor = Paths.get(String.valueOf(folder), book.getMetadata().getCreator()).toFile();
        if (!folderauthor.exists()){
            folderauthor.mkdir();
        }
        File folderbook = Paths.get(folderauthor.getPath(), book.getMetadata().getTitle()).toFile();
        if (!folderbook.exists()){
            folderbook.mkdir();
        }
        try {
            Files.copy(book.getPath(), Paths.get(String.valueOf(folderbook.toPath()), String.valueOf(book.getPath().getFileName())), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        for (File file: book.getDataDirectory().toFile().listFiles()){

            try {
                Files.copy(file.toPath(), Paths.get(String.valueOf(folderbook.toPath()), file.getName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            if (file.isDirectory()){
                for (File file1: file.listFiles()) {

                    try {
                        Files.copy(file1.toPath(), Paths.get(file.getPath(), file1.getName()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
        Desktop.getDesktop().browseFileDirectory(folderbook);
    }

    public interface menuCallBacks {
        void onMenuAddBook(List<Book> books);
        void onMenuRemoveBook();
        void onMenuSaveBook();
        void onMenuOpenBook();
        void onMenuOpenBook(List<Book> books);
//        void onTableDeleteBook(List<Book> books);
    }
}
