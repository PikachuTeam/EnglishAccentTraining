package com.tatteam.android.englishaccenttraining;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class PrivacyActivity extends AppCompatActivity {
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        mWebView = (WebView) this.findViewById(R.id.webView);
        mWebView.loadUrl("https://essential-studio.000webhostapp.com/essential_privacy_policy.html");
    }
}
