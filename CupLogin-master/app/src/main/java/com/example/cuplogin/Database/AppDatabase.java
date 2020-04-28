package com.example.cuplogin.Database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Sale.class,Return_Record.class,Cup.class}, version = 5)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AppDao appDao();
    public abstract CupDao cupDao();
}