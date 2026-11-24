package com.example.group12.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.group12.db.entity.ReadingProgress;

// DAO truy van tien do doc sach tu Room database
@Dao
public interface ReadingProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(ReadingProgress progress);

    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId LIMIT 1")
    LiveData<ReadingProgress> getProgressLive(int bookId);

    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId LIMIT 1")
    ReadingProgress getProgress(int bookId);

    @Query("DELETE FROM reading_progress WHERE bookId = :bookId")
    void delete(int bookId);
}
