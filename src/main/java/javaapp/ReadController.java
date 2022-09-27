package javaapp;

import javaapp.book.SpineEntry;
import javaapp.helper.HTMLHelper;
import javaapp.helper.HeightHelper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import javaapp.book.Book;

import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.web.WebView;
import javafx.util.Pair;
import org.w3c.dom.Document;

public class ReadController implements Initializable {

    private Book book;
    @FXML
    private WebView view;


    private static int page = 0;
    /**
     * Scrolls to the specified position.
     * @param view web view that shall be scrolled
     * @param x horizontal scroll value
     * @param y vertical scroll value
     */
    public void scrollTo(WebView view, int x, int y) {
        view.getEngine().executeScript("window.scrollTo(" + x + ", " + y + ")");
    }

    /**
     * Returns the vertical scroll value, i.e. thumb position.
     * This is equivalent to {@link javafx.scene.control.ScrollBar#getValue().
     * @param view
     * @return vertical scroll value
     */
    public int getVScrollValue(WebView view) {
        return (Integer) view.getEngine().executeScript("document.body.scrollTop");
    }

    /**
     * Returns the horizontal scroll value, i.e. thumb position.
     * This is equivalent to {@link javafx.scene.control.ScrollBar#getValue()}.
     * @param view
     * @return horizontal scroll value
     */
    public int getHScrollValue(WebView view) {
        return (Integer) view.getEngine().executeScript("document.body.scrollLeft");
    }

    /**
     * Returns the maximum vertical scroll value.
     * This is equivalent to {@link javafx.scene.control.ScrollBar#getMax()}.
     * @param view
     * @return vertical scroll max
     */
    public int getHScrollMax(WebView view) {
        return (Integer) view.getEngine().executeScript("document.body.scrollWidth");
    }

    /**
     * Returns the maximum horizontal scroll value.
     * This is equivalent to {@link javafx.scene.control.ScrollBar#getMax()}.
     * @param view
     * @return horizontal scroll max
     */
    public int getVScrollMax(WebView view) {
        return (Integer) view.getEngine().executeScript("document.body.scrollHeight");
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        view.setOnKeyPressed((KeyEvent event) -> {
            // Left-key => go one page back
            if(event.getCode().equals(KeyCode.LEFT)) {
                page = Math.max(0, page - 1);
                view.getEngine().loadContent(pages.get(page));
            }
            // Right-key => go one page forwards
            else if (event.getCode().equals(KeyCode.RIGHT)) {
//                System.out.println(getVScrollValue(view));
//                System.out.println(getVScrollMax(view));
                int tmp = getHScrollValue(view) + (int) view.getWidth() ;
                System.out.print(getHScrollValue(view));
                System.out.print(" ");
                System.out.print(view.getWidth());
                System.out.print(" ");
                System.out.print(tmp);
                System.out.print(" ");
                System.out.print(getHScrollMax(view));
                System.out.println();

                if (tmp < getHScrollMax(view)){
                    System.out.println("nice");
                    scrollTo(view, tmp, 0);
                }
                else{
                    page = Math.min(page + 1, pages.size() - 1);
                    view.getEngine().loadContent(pages.get(page));
                }

            }
        });
        //view.getEngine().setUserStyleSheetLocation("path/to/style.css");
        view.getChildrenUnmodifiable().addListener(new ListChangeListener<Node>() {
            @Override public void onChanged(Change<? extends Node> change) {
                Set<Node> deadSeaScrolls = view.lookupAll(".scroll-bar");
                for (Node scroll : deadSeaScrolls) {
                    scroll.setVisible(false);
                }
            }
        });
    }
    public void setBook(Book book){
        this.book = book;
        page = 0;
        view.getEngine().setUserStyleSheetLocation(Paths.get(book.getConfigDirectory().toString(),"bookview.css").toString());
        Init();

    }
    // Keep track of all pages & all futures
    List<String> pages = new ArrayList<>();
    List<CompletableFuture<Pair<SpineEntry, String[]>>> futures = new ArrayList<>();


    public void Init(){
        book.getSpine().forEach(entry -> {
            String html = book.readSection(entry);

            // HTML files have src/image tags that reference images from their perspective/directory.
            // Because our HTML file is ""moved"", the references do not link to images properly.
            // To fix this, we reference saved images which were extracted earlier in the loading pipeline.
            // Each src attribute is replaced with a src reference to the same local file in the data directory.

//            System.out.println(HTMLHelper.getBody(html));

            Pattern pattern = Pattern.compile("src=\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(html);
            html = matcher.replaceAll(result -> {
                String group = result.group(1);
                URI x = Paths.get(book.getImageDirectory().toString(), group.substring(group.lastIndexOf("/") + 1)).toUri();
                return String.format("src=\"%s\"", x);
            });
            pattern = Pattern.compile("href=\"([^\"]+)\"");
            matcher = pattern.matcher(html);
            html = matcher.replaceAll(result -> {
                String x = book.getCssPath().toString();
                return String.format("href=\"%s\"", x);
            });
            pattern = Pattern.compile("<!DOCTYPE[^>]+>");
            matcher = pattern.matcher(html);
            html = matcher.replaceAll(result -> {
                return "";
            });
            pages.add(html);
//            // Retrieve the template (HTML without the body) from the current spine entry.
//            String template = HTMLHelper.getTemplate(html);
//
//            // Create a task to calculate the pages from our HTML.
//            // This future is stored in a list so we can reference it later.
//            HeightHelper helper = new HeightHelper();
//            CompletableFuture<Pair<SpineEntry, String[]>> future = helper.calculatePages(entry, html, stage.getHeight(), stage.getWidth() * .6);
//            futures.add(future);
//
//            // Debug log
////            System.out.printf("Loading %s.\n", entry.getIdref());
//
//            // When the future is finished calculating the pages for this particular entry in the book,
//            //   we iterate over each page and add a WebView element representing the page to our screen.
//            // Additional setup for individual pages also occurs here.
//            future.thenAccept(result -> {
//                for(String page : result.getValue()) {
//                    // TODO: this will break very heavily as soon as a book has %s in it
//                    pages.add(template.replace("%s", page));
//                }
//
//                // If the SpineEntry is the first one in this epub's TOC, load the first page now.
//                if(book.getSpine().indexOf(result.getKey()) == 0 && !pages.isEmpty()) {
//                    view.getEngine().load(pages.get(0));
//                }
//
//
//            }).exceptionally(error -> {
//                error.printStackTrace();
//                return null;
//            });

            // Setup arrow-key click events for traversing through pages.
//            stage.addEventHandler(KeyEvent.KEY_RELEASED, event->{
//                System.out.println(page);
//                // Left-key => go one page back
//                if(event.getCode().equals(KeyCode.LEFT)) {
//                    page = Math.max(0, page - 1);
//                    view.getEngine().loadContent(pages.get(page));
//                }
//
//                // Right-key => go one page forwards
//                else if (event.getCode().equals(KeyCode.RIGHT)) {
//                    page = Math.min(pages.size() - 1, page + 1);
//                    view.getEngine().loadContent(pages.get(page));
//                }
//            });

        });
    }
    private Stage stage;

    public void setPrimaryStage(Stage stage){
        this.stage = stage;
    }


}
