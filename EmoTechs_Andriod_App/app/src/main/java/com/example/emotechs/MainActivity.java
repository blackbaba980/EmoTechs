package com.example.emotechs;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

import static java.lang.Thread.sleep;

//This is the main YouTube webview activity.
public class MainActivity extends AppCompatActivity {
    private WebView youTubeView;
    private Bundle resumePageState = null, mainPageState = null;
    private Intent backgroundService;
    private FloatingActionButton FABMenuButton;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FABMenuButton = findViewById(R.id.fab_menu_button);
        youTubeView = findViewById(R.id.youTubeMainPageViewer);

        WebSettings youTubeViewSettings = youTubeView.getSettings();
        youTubeViewSettings.setJavaScriptEnabled(true);


        youTubeView.setWebViewClient(new WebViewClient(){
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                //Take the URL and check if it is a video for watching.
                int videoToWatch = request.getUrl().toString().indexOf("watch?v");
                String watchingVideoURL;
                if( videoToWatch > 0){
                    //update the UI when the user click on a video
                    Handler refresh = new Handler(Looper.getMainLooper());
                    refresh.post(new Runnable() {
                        public void run()
                        { FABMenuButton.setVisibility(View.INVISIBLE); }});

                    //get the video URL and send it to a service that should run in the background
                    watchingVideoURL = request.getUrl().toString();
                    watchingVideoURL = watchingVideoURL.split("=1")[0];
                    backgroundService = new Intent(MainActivity.this, BackgroundShootingService.class);
                    backgroundService.putExtra("VideoURL", watchingVideoURL);
                    startService(backgroundService);
                }
                return null;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        youTubeView.restoreState(resumePageState);
        youTubeView.loadUrl("https://m.youtube.com/");
        Log.e("Systems called", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        youTubeView.saveState(resumePageState);
        //stopService(backgroundService);
        Log.e("Systems called", "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (backgroundService != null)
            stopService(backgroundService);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (Objects.requireNonNull(youTubeView.getUrl()).indexOf("watch?v") > 0) {
                        youTubeView.loadUrl("https://m.youtube.com/");

                        Handler refresh = new Handler(Looper.getMainLooper());
                        refresh.post(new Runnable() {
                            public void run()
                            {
                                FABMenuButton.setVisibility(View.VISIBLE);
                            }
                        });
                        stopService(backgroundService);

                    } else if (!(Objects.requireNonNull(youTubeView.getUrl()).equals("https://m.youtube.com/"))){
                        youTubeView.goBack();

                    } else{
                        finishAffinity();
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}





