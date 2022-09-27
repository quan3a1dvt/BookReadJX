package javaapp.book;

import javaapp.book.epub.EpubMetadata;
import javafx.scene.image.Image;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public interface Book {

    public static final Path READER_LIBRARY_PATH = Paths.get(System.getProperty("user.home"), "ReaderLibrary");
    public static final Path READER_LIBRARY_DATA_PATH = Paths.get(System.getProperty("user.home"), "ReaderLibrary", "Data");
    public static final Path READER_LIBRARY_CONFIG_PATH = Paths.get(System.getProperty("user.home"), "ReaderLibrary", "Config");
    public default EpubMetadata getMetadata() {
        return null;
    }
    public default Path getDataDirectory() {
        return null;
    }
    public default Path getConfigDirectory(){
        return null;
    }

    public default Path getCssPath(){
        return null;
    }
    public default void initDataDirectory() {}

    public default Image getCover(){
        return null;
    };
    public default List<SpineEntry> getSpine() {return null;}

    public default Path getImageDirectory(){return null;}

    public default String readSection(SpineEntry spineEntry) {return null;}
}
