package com.example.emotechs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

import static java.lang.Thread.sleep;

//This is the main YouTube webview activity.
public class MainActivity extends AppCompatActivity {
    private WebView youTubeView;
    private Bundle resumePageState = null, mainPageState = null;
    private Intent backgroundService;
    private FloatingActionButton FABMenuButton;
    private static final int PERMISSION_CODE_CAMERA = 1000;
    private static final int PERMISSION_CODE_STORAGE = 1001;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            String[] permission = {Manifest.permission.CAMERA};
            requestPermissions(permission, PERMISSION_CODE_CAMERA);
        }

        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(permission, PERMISSION_CODE_STORAGE);
        }


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

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_CODE_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.e("Permission", "onRequestPermissionsResult: Camera" );
                    Toast.makeText(this, "Permission Granted Thank you, Now we can spy on you (camera)", Toast.LENGTH_SHORT).show();
            }
                else {
                    finishAffinity();
                    finish();
                }
            }

            case PERMISSION_CODE_STORAGE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("Permission", "onRequestPermissionsResult: STORATGE");
                    Toast.makeText(this, "Permission Granted Thank you, Now we can spy on you(storage)", Toast.LENGTH_SHORT).show();
                }
                    else {
                    finishAffinity();
                    finish();
                }
            }

        }
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





