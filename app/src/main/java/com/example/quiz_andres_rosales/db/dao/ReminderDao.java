package com.example.quiz_andres_rosales.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quiz_andres_rosales.db.entity.ReminderEntity;

import java.util.List;

@Dao
public interface ReminderDao {

    @Insert
    long insertReminder(ReminderEntity reminder);

    @Update
    void updateReminder(ReminderEntity reminder);

    @Delete
    void deleteReminder(ReminderEntity reminder);

    @Query("SELECT * FROM reminders WHERE userId = :userId ORDER BY isPaid ASC, dueDay ASC")
    List<ReminderEntity> getRemindersForUser(int userId);

    @Query("SELECT COUNT(*) FROM reminders WHERE userId = :userId AND isPaid = 0")
    int getPendingCountForUser(int userId);
}