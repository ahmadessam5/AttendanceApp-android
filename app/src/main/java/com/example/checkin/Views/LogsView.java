package com.example.checkin.Views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.checkin.Database.DataBase;
import com.example.checkin.Database.Logs;
import com.example.checkin.R;
import com.example.checkin.Adapters.RecyclerViewAdapter;

import java.util.List;

public class LogsView extends AppCompatActivity implements View.OnClickListener {

    RecyclerView recyclerView;
    ImageView imageView;
    RecyclerViewAdapter adapter;

    DataBase dataBase;
    List<Logs> logs;
    LinearLayoutManager layoutManager;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logs);
        initUi();

    }

    private void initUi() {
        recyclerView = findViewById(R.id.logs_recycler);
        imageView = findViewById(R.id.logs_back_btn);
        imageView.setOnClickListener(this);
        dataBase = DataBase.getInstance(this);

        setRecycler();
    }

    private void setRecycler() {

        logs = dataBase.dao().getAll();
        layoutManager = new LinearLayoutManager(this);

        adapter = new RecyclerViewAdapter(this , logs );
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.logs_back_btn:

                Intent intent = new Intent(LogsView.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }
}
