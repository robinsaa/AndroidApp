package com.example.cuplogin.Database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Sale.class,Return_Record.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AppDao appDao();

}