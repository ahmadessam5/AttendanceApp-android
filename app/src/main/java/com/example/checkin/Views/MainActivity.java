package com.example.checkin.Views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.checkin.Database.DataBase;
import com.example.checkin.Database.Logs;
import com.example.checkin.R;

import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.sql.Blob;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private TextView in, out, logs;
    private Calendar currentCalender;
    private Date currentDate;
    private LinearLayout btnCamera;
    private ImageView btnRecord;

    private String date, time;
    private DataBase dataBase;
    private List<Logs> allLogs = new ArrayList<>();

    private MediaRecorder recorder;
    private String recordOutput;

    private Chronometer recordTime;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataBase = DataBase.getInstance(this);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        intaiUi();

    }

    @SuppressLint("ClickableViewAccessibility")
    private void intaiUi() {


        Log.d("test", "intial");
        in = findViewById(R.id.check_in_btn);
        in.setOnClickListener(this);
        out = findViewById(R.id.check_out_btn);
        out.setOnClickListener(this);
        logs = findViewById(R.id.logs_btn);
        logs.setOnClickListener(this);
        btnCamera = findViewById(R.id.camera_check_in_btn);
        btnCamera.setOnClickListener(this);
        btnRecord = findViewById(R.id.record_check_in_btn);
        btnRecord.setOnTouchListener(this);

        recordTime = findViewById(R.id.record_timer);


        if (!lastLog())
            in.setVisibility(View.VISIBLE);
        else out.setVisibility(View.VISIBLE);


    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.check_in_btn:
                in.setVisibility(View.GONE);
                out.setVisibility(View.VISIBLE);
                currentCalender = Calendar.getInstance();
                currentDate = currentCalender.getTime();
                addData(true, null);
                break;

            case R.id.check_out_btn:
                out.setVisibility(View.GONE);
                in.setVisibility(View.VISIBLE);
                currentCalender = Calendar.getInstance();
                currentDate = currentCalender.getTime();
                //  Log.d("test",currentDate + "");
                addData(false, null);
                break;

            case R.id.logs_btn:

                Intent intent = new Intent(MainActivity.this, LogsView.class);
                startActivity(intent);
                //finish();
                break;


            case R.id.camera_check_in_btn:

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent2 = new Intent(MainActivity.this, camera.class);
                    startActivity(intent2);


                }
                break;

        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

                    recordTime.setVisibility(View.VISIBLE);
                    record(true);
                }
                // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

                break;
            case MotionEvent.ACTION_UP:


                recordTime.stop();
                recordTime.setVisibility(View.GONE);
                record(false);

                break;
        }
        return false;
    }
    /*    @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            isRecording = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            record(true);

        }*/

    private void record(boolean state) {
        if (state) {
            startRecording();
        } else {

            stopRecording();
        }
    }

    private void startRecording() {

        currentCalender = Calendar.getInstance();
        currentDate = currentCalender.getTime();

        recordOutput = getExternalCacheDir().getAbsolutePath() + "/audioRecording" + new SimpleDateFormat("ddMMyyhhmmss").format(currentDate) + ".3gp";
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(recordOutput);
        try {
            recorder.prepare();
            recordTime.setBase(SystemClock.elapsedRealtime());
            recordTime.start();
            Toast.makeText(this, "Start Recording", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorder.start();



    }

    private void stopRecording() {

        recorder.stop();
        recorder.release();
        Toast.makeText(this, "stop Recording", Toast.LENGTH_SHORT).show();
        recorder = null;
        addData(!lastLog(), recordOutput);
        if (lastLog()) {
            in.setVisibility(View.GONE);
            out.setVisibility(View.VISIBLE);
        } else {
            out.setVisibility(View.GONE);
            in.setVisibility(View.VISIBLE);

        }

    }

    @SuppressLint("SimpleDateFormat")
    private void addData(boolean state, String path) {

        date = new SimpleDateFormat("dd-MM-yy").format(currentDate);
        time = new SimpleDateFormat("hh:mm a").format(currentDate);
        dataBase.dao().insertLog(new Logs("Ahmad", date, time, state, null, path));

        List<Logs> logs = dataBase.dao().getAll();
       // Log.d("test", logs.get(0).getRecord());

    }

    private boolean lastLog() {

        allLogs = dataBase.dao().getAll();
        if (allLogs.size() == 0) {
            return false;
        } else {
            int lastLog = allLogs.size() - 1;
            boolean check = allLogs.get(lastLog).isCheckedState();
            return check;
        }
    }

}


