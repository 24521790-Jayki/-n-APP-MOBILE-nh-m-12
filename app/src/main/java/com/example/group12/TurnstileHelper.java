package com.example.group12;

import android.annotation.SuppressLint;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TurnstileHelper {

    public interface TurnstileListener {
        void onVerified(String token);
        void onExpired();
    }

    // Sitekey thử nghiệm chuẩn của Cloudflare Turnstile (1x00000000000000000000AA luôn pass)
    public static final String DEMO_SITE_KEY = "1x00000000000000000000AA";

    @SuppressLint("SetJavaScriptEnabled")
    public static void setupTurnstile(WebView webView, String siteKey, TurnstileListener listener) {
        if (webView == null) return;

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient());
        webView.setBackgroundColor(0x00000000); // Nền trong suốt

        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void onVerified(String token) {
                if (listener != null) {
                    webView.post(() -> listener.onVerified(token));
                }
            }

            @JavascriptInterface
            public void onExpired() {
                if (listener != null) {
                    webView.post(listener::onExpired);
                }
            }
        }, "AndroidInterface");

        // Sử dụng CSS transform scale để co nhỏ và căn giữa widget Turnstile gọn gàng
        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\">\n" +
                "    <script src=\"https://challenges.cloudflare.com/turnstile/v0/api.js\" async defer></script>\n" +
                "    <style>\n" +
                "        body { margin: 0; padding: 0; display: flex; justify-content: center; align-items: center; background: transparent; height: 100vh; overflow: hidden; }\n" +
                "        .cf-turnstile { transform: scale(0.82); transform-origin: center; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"cf-turnstile\" data-sitekey=\"" + siteKey + "\" data-callback=\"onTurnstileSuccess\" data-expired-callback=\"onTurnstileExpired\"></div>\n" +
                "    <script>\n" +
                "        function onTurnstileSuccess(token) {\n" +
                "            if (window.AndroidInterface) {\n" +
                "                window.AndroidInterface.onVerified(token);\n" +
                "            }\n" +
                "        }\n" +
                "        function onTurnstileExpired() {\n" +
                "            if (window.AndroidInterface) {\n" +
                "                window.AndroidInterface.onExpired();\n" +
                "            }\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";

        webView.loadDataWithBaseURL("https://challenges.cloudflare.com", html, "text/html", "UTF-8", null);
    }
}
