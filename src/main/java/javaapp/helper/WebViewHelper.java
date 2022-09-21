package javaapp.helper;

import javafx.scene.web.WebView;

public class WebViewHelper {

    public static WebView from(String html) {
        WebView webView = new WebView();
        webView.getEngine().loadContent(html);
        webView.getEngine().reload();
        return webView;
    }
}
