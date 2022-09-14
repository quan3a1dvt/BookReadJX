package javaapp;

import java.net.URI;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;

public class eBook {
    private String path;

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public Double getSize() {
        return size;
    }

    private String author;
    private String title;
    private Double size;

    private String Date;
    public eBook(String path) throws IOException{
        this.path = path;
        File file = new File(path);
        InputStream inputstream = new FileInputStream(this.path);
        Book book = (new EpubReader()).readEpub(inputstream);
        this.title = book.getTitle();
        List<Author> authors = book.getMetadata().getAuthors();
        this.author = authors.get(0).getFirstname() + " " + authors.get(0).getLastname();
        this.size = Math.round((Double.valueOf(file.length()) / (1024 * 1024)) * 10.0) / 10.0;
    }
    public void setPath(String path){
        this.path = path;
    }
}
