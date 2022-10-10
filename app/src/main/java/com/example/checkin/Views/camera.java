package com.example.checkin.Views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.checkin.Database.DataBase;
import com.example.checkin.Database.Logs;
import com.example.checkin.R;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class camera extends AppCompatActivity implements View.OnClickListener, TextureView.SurfaceTextureListener {

    private TextureView textureView;
    private ImageView capture, back, change;

    DataBase dataBase;

    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;

    private boolean frontCamera;

    private Bitmap bitmap;

    private Calendar currentCalender;
    private Date currentDate;
    private String date, time;

    private List<Logs> allLogs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        textureView = findViewById(R.id.my_texture);
        capture = findViewById(R.id.capture_btn);
        capture.setOnClickListener(this);
        textureView.setSurfaceTextureListener(this);
        back = findViewById(R.id.camera_back_btn);
        back.setOnClickListener(this);
        change = findViewById(R.id.change_camera);
        change.setOnClickListener(this);
        frontCamera = true;
        dataBase = DataBase.getInstance(this);

        // openCamera(frontCamera);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.capture_btn:

                //bitmap = textureView.getBitmap();

                addData(!lastLog());
                break;

            case R.id.camera_back_btn:
                Intent intent = new Intent(camera.this, MainActivity.class);
                cameraDevice.close();
                cameraDevice = null;
                startActivity(intent);
                finish();
                break;
            case R.id.change_camera:

                if (cameraDevice != null)
                    cameraDevice.close();
                cameraDevice = null;
                //openCamera(!frontCamera);
                frontCamera = !frontCamera;

                break;

        }

    }


    @Override
    protected void onPause() {
        //stopBackgroundThread();
        if (cameraDevice != null)
            cameraDevice.close();
        cameraDevice = null;
        super.onPause();
    }


    private void openCamera(boolean change) {

        int cameraFace = 0;
        if (!change)
            cameraFace = 1;

        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        try {

            String myCameraID = cameraManager.getCameraIdList()[cameraFace];


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            cameraManager.openCamera(myCameraID, stateCallback, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {

            //frontCamera = !frontCamera;
            cameraDevice = camera;
            SurfaceTexture mySurfaceTexture = textureView.getSurfaceTexture();
            Surface mySurface = new Surface(mySurfaceTexture);

            try {
                captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                captureRequestBuilder.addTarget(mySurface);

                cameraDevice.createCaptureSession(Arrays.asList(mySurface), new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession session) {
                                cameraCaptureSession = session;
                                captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                                try {
                                    cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                            }
                        }, null
                );
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;

        }
    };


    //region ____TextureListener_____
    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            openCamera(frontCamera);
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
        if (cameraDevice == null)
            openCamera(frontCamera);
    }
    //endregion

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    private void addData(boolean state) {

        currentCalender = Calendar.getInstance();
        currentDate = currentCalender.getTime();
        date = new SimpleDateFormat( "dd-MM-yy").format(currentDate);
        time = new SimpleDateFormat( "hh:mm a").format(currentDate);
        dataBase.dao().insertLog(new Logs("Ahmad", date , time , state , BitMapToString(textureView.getBitmap()) , null ));

        List<Logs> logs =   dataBase.dao().getAll();
        Log.d("test", logs.get(0).getId()+"");

        Intent intent1 = new Intent(camera.this, MainActivity.class);
        cameraDevice.close();
        cameraDevice = null;
        startActivity(intent1);
        finish();

    }

    private boolean lastLog () {

        allLogs = dataBase.dao().getAll();
        if(allLogs.size() == 0 ){
            return false;
        }else {
            int lastLog = allLogs.size() - 1;
            boolean check = allLogs.get(lastLog).isCheckedState();
            return check;
        }
    }
}