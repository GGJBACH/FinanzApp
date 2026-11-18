package com.example.quiz_andres_rosales.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "reminders")
public class ReminderEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int userId;
    private String title;
    private double amount;
    private String category;
    private int dueDay; // 1 to 31 NumberPicker
    private boolean isPaid;
    private boolean isFixed;

    public ReminderEntity(int userId, String title, double amount, String category, int dueDay, boolean isPaid, boolean isFixed) {
        this.userId = userId;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.dueDay = dueDay;
        this.isPaid = isPaid;
        this.isFixed = isFixed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getDueDay() {
        return dueDay;
    }

    public void setDueDay(int dueDay) {
        this.dueDay = dueDay;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public boolean isFixed() {
        return isFixed;
    }

    public void setFixed(boolean fixed) {
        isFixed = fixed;
    }
}