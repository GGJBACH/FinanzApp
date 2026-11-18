package com.example.quiz_andres_rosales.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quiz_andres_rosales.db.entity.ExpenseEntity;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    long insertExpense(ExpenseEntity expense);

    @Update
    void updateExpense(ExpenseEntity expense);

    @Delete
    void deleteExpense(ExpenseEntity expense);

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY dateMillis DESC")
    List<ExpenseEntity> getExpensesForUser(int userId);

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId")
    Double getTotalExpensesForUser(int userId);
}