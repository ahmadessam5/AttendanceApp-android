package com.example.checkin.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = Logs.class , version = 4)
public abstract class DataBase extends RoomDatabase {

    private static DataBase instance;
    public abstract Dao dao() ;

    public static synchronized DataBase getInstance(Context context){
        if (instance == null){

            instance = Room.databaseBuilder(context.getApplicationContext() , DataBase.class , "logs_database2")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return instance;
    }



}
