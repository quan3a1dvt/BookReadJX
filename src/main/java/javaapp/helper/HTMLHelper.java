package javaapp.helper;

import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class HTMLHelper {

    // TODO: this method assumes the given HTML is valid.
    /**
     * @return a list of body elements from the given HTML document text.
     */
    public static List<String> getBody(String html) {
        try {
            List<String> ret = new ArrayList<>();

            // First, locate the <body> content inside the given HTML document.
            String startTag = getStartBodyTag(html);
            String bodyText = html.substring(html.indexOf(startTag), html.lastIndexOf("</body>") + "</body>".length());

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document parsed = builder.parse(new InputSource(new StringReader(bodyText)));
            parsed.normalize();
            Node body = parsed.getElementsByTagName("body").item(0);

            // source: https://stackoverflow.com/questions/3300839/get-a-nodes-inner-xml-as-string-in-java-dom
            DOMImplementationLS lsImpl = (DOMImplementationLS) body.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
            LSSerializer lsSerializer = lsImpl.createLSSerializer();
            lsSerializer.getDomConfig().setParameter("xml-declaration", false);
            NodeList childNodes = body.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                String e = lsSerializer.writeToString(childNodes.item(i));

                // parsing is dumb, and most lines have skewed whitespace.
                // for more reliable testing, we will simply strip all whitespace on each line
                String result = Arrays.stream(e.split("\n"))
                        .map(String::trim)
                        .filter(line -> !line.isBlank())
                        .collect(Collectors.joining("\n"));

                // Append the trimmed result to the list
                if(!result.isBlank()) {
                    ret.add(result);
                }
            }
            // end source

            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    public static String getTemplate(String html) {
        String startTag = getStartBodyTag(html);
        int endIndex = html.indexOf(startTag) + startTag.length();
        int restartIndex = html.lastIndexOf("</body>");
        return html.substring(0, endIndex) + "%s" + html.substring(restartIndex);
    }
    public static String getTemplateStart(String html) {
        String startTag = getStartBodyTag(html);
        int endIndex = html.indexOf(startTag) + startTag.length();
        int restartIndex = html.lastIndexOf("</body>");
        return html.substring(0, endIndex);
    }
    public static String getTemplateEnd(String html) {
        String startTag = getStartBodyTag(html);
        int endIndex = html.indexOf(startTag) + startTag.length();
        int restartIndex = html.lastIndexOf("</body>");
        return html.substring(restartIndex);
    }
    public static String getSubPage(String html, int start, int end){
        List<String> body = getBody(html);
        String subPage = getTemplateStart(html);
        for (int i = start; i <= end; i++){
            subPage += "\n";
            subPage += body.get(i);
        }
        subPage += getTemplateEnd(html);
        return subPage;
    }
    public static List<String> getSubPages(String html, WebView view){
        List<String> body = getBody(html);
        List<String> subPages = new ArrayList<>();
        int start = 0;
        int now = 0;
        double htmlHeight = 0;
        double preHtmlHeight = 0;
        WebEngine engine = view.getEngine();
        AtomicReference<String> state = new AtomicReference<>("succeeded");
//        engine.documentProperty().addListener(new ChangeListener<Document>() {
//            @Override
//            public void changed(ObservableValue<? extends Document> prop, Document oldDoc, Document newDoc) {
//                String heightText = view.getEngine().executeScript(
//                        "window.getComputedStyle(document.body, null).getPropertyValue('height')"
//                ).toString();
//                htmlHeight = Double.valueOf(heightText.replace("px", ""));
//                System.out.println(heightText);
//            }
//        });
        AtomicBoolean ended = new AtomicBoolean(false);
        view.getEngine().getLoadWorker().stateProperty().addListener((ov,oldState,newState)->{
            if(newState== Worker.State.SCHEDULED){
                System.out.println("state: scheduled");
            } else if(newState== Worker.State.RUNNING){
                System.out.println("state: running");
            } else if(newState== Worker.State.SUCCEEDED){
                System.out.println("state: succeeded");
                ended.set(true);
            }
        });
        int finalStart = start;
        int finalNow = now;
        new Thread(new Runnable() {
            @Override
            public void run() {
                view.getEngine().loadContent(getSubPage(html, finalStart, finalNow));
            }
        }).start();

        while(true) {

//            String htmlHeightString = view.getEngine().executeScript("window.getComputedStyle(document.body, null).getPropertyValue('height')").toString();
//            System.out.println(htmlHeightString);
//            double htmlHeight = Integer.parseInt(htmlHeightString.substring(0, htmlHeightString.length() - 2));
//            engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
//                if (newState == Worker.State.SUCCEEDED) {
//                    check.set(1);
//                }
//            });

//            ExecutorService executor = Executors.newFixedThreadPool(10);
//            CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(view.getEngine().loadContent(getSubPage(html, start, now)));
//            Async.await(future1);
            if (ended.get() == true) {
                int finalStart1 = start;
                int finalNow1 = now;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        view.getEngine().loadContent(getSubPage(html, finalStart1, finalNow1));
                    }
                }).start();
                ended.set(false);
            }

            String heightText = view.getEngine().executeScript(
                    "window.getComputedStyle(document.body, null).getPropertyValue('height')"
            ).toString();
            htmlHeight = Double.valueOf(heightText.replace("px", ""));
            double viewHeight = view.getHeight();
//            System.out.println(htmlHeight + " " + viewHeight + " " + start + " " + now);
            if (htmlHeight == preHtmlHeight){
                continue;
            }
            if (htmlHeight > viewHeight) {
                //                System.out.println(start + " " + now);
                subPages.add(getSubPage(html, start, now - 1));
                start = now;
            } else {
                if (now >= body.size() - 1) {
                    //                    System.out.println(start + " " + now);
                    subPages.add(getSubPage(html, start, now));
                    start = now;
                    break;
                }
                if (htmlHeight > preHtmlHeight)
                    now += 1;
            }

        }
        return subPages;
    }


    public static String getStartBodyTag(String html) {
        int index = html.indexOf("<body");

        // if '<body' was found, begin operation
        if(index != -1) {
            int end = html.indexOf(">", index);
            return html.substring(index, end + 1);
        }

        return "<body>";
    }
}
