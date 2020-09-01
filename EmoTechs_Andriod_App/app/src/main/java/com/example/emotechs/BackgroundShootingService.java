package com.example.emotechs;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class BackgroundShootingService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String VideoURL = intent.getStringExtra("VideoURL");
        Log.e("Background Service", "The Video URL is:" + VideoURL);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("Background Service", "Service has been destroyed!");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
