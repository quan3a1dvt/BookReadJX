package javaapp;

import java.net.URI;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;

public class eBook {
    private String path;
    private String author;
    private String title;
    private Long size;
    public eBook(String path) throws IOException{
        this.path = path;
        InputStream inputstream = new FileInputStream(this.path);
        Book book = (new EpubReader()).readEpub(inputstream);
    }
    public void setPath(String path){
        this.path = path;
    }
}
