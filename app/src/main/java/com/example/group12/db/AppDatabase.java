package com.example.group12.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.group12.db.dao.ReadingProgressDao;
import com.example.group12.db.entity.ReadingProgress;

// Room database chinh cua ung dung - luu tien do doc va du lieu offline
@Database(entities = {ReadingProgress.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract ReadingProgressDao readingProgressDao();

    // Singleton pattern - dam bao chi co 1 instance duy nhat
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "group12_reader_db"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}
