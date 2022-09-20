package javaapp;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import javaapp.book.Book;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.web.WebView;

public class ReadController implements Initializable {

    private Book book;

    @FXML
    private WebView view;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
    public void setBook(Book book){
        this.book = book;
        Init();
    }
    public void Init(){
        book.getSpine().forEach(entry -> {
            String html = epub.readSection(entry);

            // HTML files have src/image tags that reference images from their perspective/directory.
            // Because our HTML file is ""moved"", the references do not link to images properly.
            // To fix this, we reference saved images which were extracted earlier in the loading pipeline.
            // Each src attribute is replaced with a src reference to the same local file in the data directory.
            Pattern pattern = Pattern.compile("src=\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(html);
            html = matcher.replaceAll(result -> {
                String group = result.group(1);
                URI x = Paths.get(epub.getImageDirectory().toString(), group.substring(group.lastIndexOf("/") + 1)).toUri();
                return String.format("src=\"%s\"", x);
            });

            // Retrieve the template (HTML without the body) from the current spine entry.
            String template = HTMLHelper.getTemplate(html);

            // Create a task to calculate the pages from our HTML.
            // This future is stored in a list so we can reference it later.
            HeightHelper helper = new HeightHelper();
            CompletableFuture<Pair<SpineEntry, String[]>> future = helper.calculatePages(entry, html, finalRoot.getHeight(), finalRoot.getWidth() * .6);
            futures.add(future);

            // Debug log
            System.out.printf("Loading %s.\n", entry.getIdref());

            // When the future is finished calculating the pages for this particular entry in the book,
            //   we iterate over each page and add a WebView element representing the page to our screen.
            // Additional setup for individual pages also occurs here.
            future.thenAccept(result -> {
                for(String page : result.getValue()) {
                    // TODO: this will break very heavily as soon as a book has %s in it
                    pages.add(template.replace("%s", page));
                }

                // If the SpineEntry is the first one in this epub's TOC, load the first page now.
                if(epub.getSpine().indexOf(result.getKey()) == 0 && !pages.isEmpty()) {
                    view.getEngine().load(pages.get(0));
                }

                System.out.printf("%s has loaded! Time taken: " + (System.currentTimeMillis() - start) + "ms%n", result.getKey().getIdref());
            }).exceptionally(error -> {
                error.printStackTrace();
                return null;
            });

            // Setup arrow-key click events for traversing through pages.
            finalRoot.setOnKeyPressed(key -> {
                // Left-key => go one page back
                if(key.getCode().equals(KeyCode.LEFT)) {
                    page = Math.max(0, page - 1);
                    view.getEngine().loadContent(pages.get(page));
                }

                // Right-key => go one page forwards
                else if (key.getCode().equals(KeyCode.RIGHT)) {
                    page = Math.min(pages.size() - 1, page + 1);
                    view.getEngine().loadContent(pages.get(page));
                }

                key.consume();
            });
        });
    }
    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage){
        this.primaryStage = primaryStage;
    }


}
