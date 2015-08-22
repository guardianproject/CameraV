package org.witness.informacam.app;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class WebActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);  
        WebView wv;  
        wv = (WebView) findViewById(R.id.webview1);  
        wv.loadUrl("file:///android_asset/guide/en/index.html");   // now it will not fail here
    }  
    
}
