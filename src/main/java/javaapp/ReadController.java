package javaapp;

import javaapp.book.SpineEntry;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javaapp.book.Book;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.web.WebView;
import javafx.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;

import static java.lang.Math.min;

public class ReadController implements Initializable {

    private Book book;
    @FXML
    private WebView view;
    private static int pageID = 0;

    /**
     * Scrolls to the specified position.
     *
     * @param view web view that shall be scrolled
     * @param x    horizontal scroll value
     * @param y    vertical scroll value
     */
    public void scrollTo(WebView view, int x, int y) {
        view.getEngine().executeScript("window.scrollTo(" + x + ", " + y + ")");
    }

    /**
     * Returns the vertical scroll value, i.e. thumb position.
     * This is equivalent to {@link javafx.scene.control.ScrollBar#getValue().
     *
     * @param view
     * @return vertical scroll value
     */
    public int getVScrollValue(WebView view) {
        return (Integer) view.getEngine().executeScript("document.body.scrollTop");
    }

    /**
     * Returns the horizontal scroll value, i.e. thumb position.
     * This is equivalent to {@link javafx.scene.control.ScrollBar#getValue()}.
     *
     * @param view
     * @return horizontal scroll value
     */
    public int getHScrollValue(WebView view) {
        return (Integer) view.getEngine().executeScript("document.body.scrollLeft");
    }

    /**
     * Returns the maximum vertical scroll value.
     * This is equivalent to {@link javafx.scene.control.ScrollBar#getMax()}.
     *
     * @param view
     * @return vertical scroll max
     */
    public int getHScrollMax(WebView view) {
        return (Integer) view.getEngine().executeScript("document.body.scrollWidth");
    }

    /**
     * Returns the maximum horizontal scroll value.
     * This is equivalent to {@link javafx.scene.control.ScrollBar#getMax()}.
     *
     * @param view
     * @return horizontal scroll max
     */
    public int getVScrollMax(WebView view) {
        return (Integer) view.getEngine().executeScript("document.body.scrollHeight");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        view.setOnKeyPressed((KeyEvent event) -> {
            System.out.println(pages.size());
            view.getEngine().executeScript("document.onkeydown = function(e) {\n" +
                    "    var key = e.which;\n" +
                    "    if(key==35 || key == 36 || key == 37 || key == 39) {\n" +
                    "          e.preventDefault();\n" +
                    "          return false;\n" +
                    "    }\n" +
                    "    return true;\n" +
                    "};");
            // Left-key => go one-page back
            if (event.getCode().equals(KeyCode.LEFT)) {
                int tmp = getHScrollValue(view) - (int) view.getWidth();
                if (tmp >= 0) {
                    scrollTo(view, tmp, 0);
                } else {
                    pageID = Math.max(0, pageID - 1);
                    view.getEngine().loadContent(pages.get(pageID));
                }
            }
            // Right-key => go one-page forwards
            else if (event.getCode().equals(KeyCode.RIGHT)) {
                int tmp = getHScrollValue(view) + (int) view.getWidth();
                if (tmp < getHScrollMax(view) - view.getWidth()) {
                    scrollTo(view, tmp, 0);
                } else {
                    pageID = min(pageID + 1, pages.size() - 1);
                    view.getEngine().loadContent(pages.get(pageID));

                }
            }

        });
        view.getChildrenUnmodifiable().addListener(new ListChangeListener<Node>() {
            @Override
            public void onChanged(Change<? extends Node> change) {
                Set<Node> deadSeaScrolls = view.lookupAll(".scroll-bar");
                for (Node scroll : deadSeaScrolls) {
                    scroll.setVisible(false);
                }
            }
        });
    }

    public void setBook(Book book) {
        this.book = book;
        pageID = 1;
        view.getEngine().setUserStyleSheetLocation(Paths.get(book.getConfigDirectory().toString(), "bookview.css").toUri().toString());
        Init();
        view.getEngine().loadContent(pages.get(pageID));

    }

    // Keep track of all pages & all futures
    List<String> pages = new ArrayList<>();
    List<CompletableFuture<Pair<SpineEntry, String[]>>> futures = new ArrayList<>();


    public void Init() {
        book.getSpine().forEach(entry -> {
            String html = book.readSection(entry);

            // Remove DOCTYPE in html, don't know why app won't work with it?
            Document doc = Jsoup.parse(html);
            doc.childNodes()
                    .stream()
                    .filter(node -> node instanceof DocumentType)
                    .findFirst()
                    .ifPresent(org.jsoup.nodes.Node::remove);

            // set style for html
            for (Element e : doc.select("link[href$=.css]")) {
                try {
                    e.attr("href", Paths.get(eBookApp.class.getResource("config/stylesheet.css").toURI()).toUri().toString());
                } catch (URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
            }
//            for (Element e: doc.select("img")){
//                e.attr("style", "width:100%;height:100%;margin:0;padding:0;");
//            }

//            try {
//                doc.appendElement(String.format("link href=\"%s\" rel=\"stylesheet\" type=\"text/css\"", Paths.get(eBookApp.class.getResource("config/stylesheet.css").toURI()).toUri().toString()));
//            } catch (URISyntaxException e) {
//                throw new RuntimeException(e);
//            }
//            System.out.println(doc);
            html = doc.toString();

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
//            pattern = Pattern.compile("href=\"([^\"]+)\"");
//            matcher = pattern.matcher(html);
//            html = matcher.replaceAll(result -> {
//                String x = book.getCssPath().toUri().toString();
//                return String.format("href=\"%s\"", x);
//            });

//            pattern = Pattern.compile("<!DOCTYPE[^>]+>");
//            matcher = pattern.matcher(html);
//            html = matcher.replaceAll(result -> {
//                return "";
//            });
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

        });
    }

    private Stage stage;

    public void setPrimaryStage(Stage stage) {
        this.stage = stage;
        stage.widthProperty().addListener(new ChangeListener<Number>() {
            private Point2D stageSize = null;
            private Point2D previousStageSize = new Point2D(stage.getWidth(), stage.getHeight());

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (stageSize == null) {
                    Platform.runLater(() -> {
                        int tmp = getHScrollValue(view) / (int) view.getWidth();
                        if (stageSize.getX() > previousStageSize.getX()) {
                            tmp = min((tmp + 1) * (int) view.getWidth(), getHScrollMax(view));
                        } else {
                            tmp = min(tmp * (int) view.getWidth(), getHScrollMax(view));
                        }
                        scrollTo(view, tmp, 0);
//                        System.out.printf("Old: (%.1f, %.1f); new: (%.1f, %.1f)%n",
//                                previousStageSize.getX(), previousStageSize.getY(),
//                                stageSize.getX(), stageSize.getY());
                        previousStageSize = stageSize;
                        stageSize = null;
                    });
                }
                stageSize = new Point2D(stage.getWidth(), stage.getHeight());
            }
        });
    }


}
