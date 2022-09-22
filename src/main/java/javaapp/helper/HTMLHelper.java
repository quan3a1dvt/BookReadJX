package javaapp.helper;

import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class HTMLHelper {

    // TODO: this method assumes the given HTML is valid.
    /**
     * @return a list of body elements from the given HTML document text.
     */
    public static List<String> getBody(String html) {
        try {
            Document doc = Jsoup.parse(html);


            Elements elements = doc.select("body").first().children();
            //or only `<p>` elements
            //Elements elements = doc.select("p");
            List<String> ret = new ArrayList<>();
            for (Element el : elements){
                ret.add(String.valueOf(el));
            }
//            for (String s : ret){
//                System.out.println(s);
//            }

//            List<String> ret = new ArrayList<>();
//
//            // First, locate the <body> content inside the given HTML document.
//            String startTag = getStartBodyTag(html);
//            String bodyText = html.substring(html.indexOf(startTag), html.lastIndexOf("</body>") + "</body>".length());
//
//            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//            Document parsed = builder.parse(new InputSource(new StringReader(bodyText)));
//            parsed.normalize();
//            Node body = parsed.getElementsByTagName("body").item(0);
//
//            // source: https://stackoverflow.com/questions/3300839/get-a-nodes-inner-xml-as-string-in-java-dom
//            DOMImplementationLS lsImpl = (DOMImplementationLS) body.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
//            LSSerializer lsSerializer = lsImpl.createLSSerializer();
//            lsSerializer.getDomConfig().setParameter("xml-declaration", false);
//            NodeList childNodes = body.getChildNodes();
//            for (int i = 0; i < childNodes.getLength(); i++) {
//                String e = lsSerializer.writeToString(childNodes.item(i));
//
//                // parsing is dumb, and most lines have skewed whitespace.
//                // for more reliable testing, we will simply strip all whitespace on each line
//                String result = Arrays.stream(e.split("\n"))
//                        .map(String::trim)
//                        .filter(line -> !line.isBlank())
//                        .collect(Collectors.joining("\n"));
//
//                // Append the trimmed result to the list
//                if(!result.isBlank()) {
//                    ret.add(result);
//                }
//            }
//            // end source
//
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

            subPage += body.get(i);
        }
        subPage += "\n";
        subPage += getTemplateEnd(html);

        return subPage;
    }
    public static List<String> getSubPages(String html, WebView view){
        List<String> body = getBody(html);
        System.out.println(body.size());
        List<String> subPages = new ArrayList<>();
//        WebView view = vie;

//        view.setPrefHeight(1.0);
//        view.setPrefWidth(506.0);
//        view.setMaxHeight(1.0);
        final int[] start = {0};
        final int[] now = {0};
        final AtomicReference<Double>[] htmlHeight = new AtomicReference[]{new AtomicReference<>((double) 0)};
        final double[] preHtmlHeight = {-1};
        AtomicInteger check = new AtomicInteger();
        view.getEngine().loadContent(getSubPage(html, start[0], now[0]));
        AtomicInteger dam = new AtomicInteger();
        view.getEngine().getLoadWorker().stateProperty().addListener((ov,oldState,newState)->{
            if(newState== Worker.State.SCHEDULED){
//                System.out.println("state: scheduled");
            } else if(newState== Worker.State.RUNNING){
//                System.out.println("state: running");
            } else if(newState== Worker.State.SUCCEEDED){
//                System.out.println("state: succeeded");
                dam.addAndGet(1);

                if (check.get() == 0) {
                    view.setPrefHeight(-1);
                    String heightText = view.getEngine().executeScript(
                            "window.getComputedStyle(document.body, null).getPropertyValue('height')"
                    ).toString();
                    htmlHeight[0].set(Double.valueOf(heightText.replace("px", "")));
                    System.out.println(htmlHeight[0] + " " + now[0]);

                    double viewHeight = 506.0;


                    if (htmlHeight[0].get() > viewHeight) {
                        subPages.add(getSubPage(html, start[0], now[0] - 1));
                        System.out.println(start[0] + " " + (now[0] - 1));
                        start[0] = now[0];
                    } else {
                        if (now[0] >= body.size() - 1) {
                            subPages.add(getSubPage(html, start[0], now[0]));
                            System.out.println(start[0] + " " + (now[0] - 1));
                            start[0] = now[0];
                            check.set(1);
                        }

                        now[0] += 1;

                    }
                    if (check.get() == 0){
                        String x = getSubPage(html, start[0], now[0]);

              

                        view.getEngine().loadContent(x);

                    }

                }
            }
        });


//
//        while(true) {
//
////            String htmlHeightString = view.getEngine().executeScript("window.getComputedStyle(document.body, null).getPropertyValue('height')").toString();
////            System.out.println(htmlHeightString);
////            double htmlHeight = Integer.parseInt(htmlHeightString.substring(0, htmlHeightString.length() - 2));
////            engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
////                if (newState == Worker.State.SUCCEEDED) {
////                    check.set(1);
////                }
////            });
//
////            ExecutorService executor = Executors.newFixedThreadPool(10);
////            CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(view.getEngine().loadContent(getSubPage(html, start, now)));
////            Async.await(future1);
//            String heightText = view.getEngine().executeScript(
//                    "window.getComputedStyle(document.body, null).getPropertyValue('height')"
//            ).toString();
//            htmlHeight[0].set(Double.valueOf(heightText.replace("px", "")));
//            System.out.println(htmlHeight[0].get());
//            if (preHtmlHeight[0] != htmlHeight[0].get()) {
//                preHtmlHeight[0] = htmlHeight[0].get();
//                check = 1;
//                view.getEngine().loadContent(getSubPage(html, start[0], now[0]));
//
//
//                double viewHeight = view.getHeight();
////                System.out.println(htmlHeight + " " + viewHeight + " " + start + " " + now);
//                if (htmlHeight[0].get() == preHtmlHeight[0]) {
//                    continue;
//                }
//                if (htmlHeight[0].get() > viewHeight) {
//                    //                System.out.println(start + " " + now);
//                    subPages.add(getSubPage(html, start[0], now[0] - 1));
//                    start[0] = now[0];
//                } else {
//                    if (now[0] >= body.size() - 1) {
//                        //                    System.out.println(start + " " + now);
//                        subPages.add(getSubPage(html, start[0], now[0]));
//                        start[0] = now[0];
//                        break;
//                    }
//
//                    now[0] += 1;
//
//
//                }
//            }
//        }
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
