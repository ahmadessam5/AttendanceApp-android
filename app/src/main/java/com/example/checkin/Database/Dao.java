package com.example.checkin.Database;

import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@androidx.room.Dao
public interface Dao {

    @Insert
    void insertLog (Logs log );

    @Query("select * from logs_table")
    List<Logs> getAll ();


}
