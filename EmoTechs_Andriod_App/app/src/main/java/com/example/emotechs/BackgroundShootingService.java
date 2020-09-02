package com.example.emotechs;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.SessionConfiguration;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class BackgroundShootingService extends Service {

    protected static final int CAMERA_CALIBRATION_DELAY = 500;
    protected static final String TAG = "myLog";
    protected static final int CAMERACHOICE = CameraCharacteristics.LENS_FACING_BACK;
    protected static long cameraCaptureStartTime;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession session;
    protected ImageReader imageReader;
    protected int j;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String VideoURL = intent.getStringExtra("VideoURL");
        Log.e("Background Service", "The Video URL is:" + VideoURL);
        try{
            ServerCommunicationHandler sendURL = new ServerCommunicationHandler();
            String result = sendURL.execute(VideoURL).get();
            Log.e("Service", "Result: " + result);
        }catch (Exception e){
        }



        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        j = 0;
        readyCamera();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
            if(session != null){
                session.close();
                session=null;
            }

            if (cameraDevice != null){
                cameraDevice.close();
                cameraDevice = null;
            }

            if(imageReader != null){
                imageReader.close();
                imageReader=null;
            }

        Log.e("Background Service", "Service has been destroyed!");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


//////////////////////////////////////////////Camera Device Error Check/////////////////////////////////////////

    protected CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "CameraDevice.StateCallback onOpened");
            cameraDevice = camera;
            actOnReadyCameraDevice();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.w(TAG, "CameraDevice.StateCallback onDisconnected");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "CameraDevice.StateCallback onError " + error);
        }
    };


    public void actOnReadyCameraDevice()
    {
        try {
            cameraDevice.createCaptureSession(Arrays.asList(imageReader.getSurface()), sessionStateCallback, null);
        } catch (CameraAccessException e){
            Log.e(TAG, e.getMessage());
        }
    }

    //////////////////////////////////////////Camera Device Error Check Availability/////////////////////////////////


    //////////////////////////////////////////Camera Capture Session ////////////////////////////////////////////////
    protected CameraCaptureSession.StateCallback sessionStateCallback = new CameraCaptureSession.StateCallback() {

        @Override
        public void onReady(CameraCaptureSession session) {
            BackgroundShootingService.this.session = session;
            try {
                session.setRepeatingRequest(createCaptureRequest(), null, null);
                cameraCaptureStartTime = System.currentTimeMillis ();
            } catch (CameraAccessException e) {
                Log.e(TAG, e.getMessage());
            }
        }


        @Override
        public void onConfigured(CameraCaptureSession session) {

        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
        }
    };

    protected CaptureRequest createCaptureRequest() {
        try {
            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            builder.addTarget(imageReader.getSurface());
            return builder.build();
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    //////////////////////////////////////////////Camera Capture Session//////////////////////////////////////////////////////

    /////////////////////////////////////////////Image Reader Listener //////////////////////////////////////////////////////
    protected ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d(TAG, "onImageAvailable");
            Image img = reader.acquireLatestImage();
            if (img != null) {
                if (System.currentTimeMillis () > cameraCaptureStartTime + CAMERA_CALIBRATION_DELAY) {
                    processImage(img);
                }
                img.close();
            }
        }
    };


    /////////////////////////////////////////// Image Reader Listener ////////////////////////////////////////////////////////



    //////////////////////////////////////// Camera Reading Procedure ////////////////////////////////////////////////////////
    public void readyCamera() {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String pickedCamera = getCamera(manager);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.openCamera(pickedCamera, cameraStateCallback, null);
            imageReader = ImageReader.newInstance(299, 299, ImageFormat.JPEG, 1 /* images buffered */);
            imageReader.setOnImageAvailableListener(onImageAvailableListener, null);
            Log.d(TAG, "imageReader created");
        } catch (CameraAccessException e){
            Log.e(TAG, e.getMessage());
        }
    }

    public String getCamera(CameraManager manager){
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation == CAMERACHOICE) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e){
            e.printStackTrace();
        }
        return null;
    }

////////////////////////////////////////////////Camera Reading Procedure /////////////////////////////////////////////////


/////////////////////////////////////////////////Process Images //////////////////////////////////////////////////////////////



    private void processImage(Image image){
        //Process image data
        Log.e(TAG, "processImage: hahaha");

        ByteBuffer buffer;



        if(image.getFormat() == ImageFormat.JPEG) {

            buffer = image.getPlanes()[0].getBuffer();
            Log.e("Image size", "processImage: " + Integer.toString(buffer.remaining()));
            Log.e("Image size", buffer.toString());

            byte[] bytes = new byte[buffer.remaining()]; // makes byte array large enough to hold image
            Log.e("Bytes size", Integer.toString(bytes.length));

            String encoded = Base64.encodeToString(bytes, Base64.DEFAULT);


            try{
                ImageUploadTask uploadTask = new ImageUploadTask();
                uploadTask.execute(encoded).get();
            }catch (Exception e){

            }





        }


    }

}
