package javaapp.book;

import javaapp.book.epub.EpubMetadata;
import javafx.scene.image.Image;

import java.nio.file.Path;
import java.nio.file.Paths;

public interface Book {

    public static final Path READER_LIBRARY_PATH = Paths.get(System.getProperty("user.home"), "ReaderLibrary");
    public static final Path READER_LIBRARY_DATA_PATH = Paths.get(System.getProperty("user.home"), "ReaderLibrary", "Data");
    public default EpubMetadata getMetadata() {
        return null;
    }
    public default Path getDataDirectory() {
        return null;
    }
    public default void initDataDirectory() {}

    public default Image getCover(){
        return null;
    };
}
