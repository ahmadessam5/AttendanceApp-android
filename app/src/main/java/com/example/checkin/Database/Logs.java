package com.example.checkin.Database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.File;
import java.util.Date;

@Entity(tableName = "logs_table")
public class Logs {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String date;
    private String time;
    private boolean checkedState;
    //private File file;
    private String img;
    private String record;



/*    public Logs(String name, String date , String time , boolean checkedState) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.checkedState = checkedState;
    }*/

    public Logs(String name, String date, String time, boolean checkedState, String img , String record ) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.checkedState = checkedState;
        this.img = img;
        this.record = record;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isCheckedState() {
        return checkedState;
    }

    public void setCheckedState(boolean checkedState) {
        this.checkedState = checkedState;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
