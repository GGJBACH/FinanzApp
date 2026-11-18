package com.example.quiz_andres_rosales.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "expenses")
public class ExpenseEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int userId;
    private String title;
    private double amount;
    private String paymentMethod; // "Efectivo", "Tarjeta", "Transferencia"
    private String category;      // "Alimentación", "Transporte", "Papelería / Académico", "Suscripciones", "Otros"
    private long dateMillis;

    public ExpenseEntity(int userId, String title, double amount, String paymentMethod, String category, long dateMillis) {
        this.userId = userId;
        this.title = title;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.category = category;
        this.dateMillis = dateMillis;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getDateMillis() {
        return dateMillis;
    }

    public void setDateMillis(long dateMillis) {
        this.dateMillis = dateMillis;
    }
}