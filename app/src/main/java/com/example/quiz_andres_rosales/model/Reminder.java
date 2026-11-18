package com.example.quiz_andres_rosales.model;

import java.io.Serializable;

public class Reminder implements Serializable {
    private String id;
    private String title;
    private double amount;
    private String category; // "Celular", "Plataformas", "Tarjeta de Crédito", "Otro"
    private String dueDate;
    private boolean isPaid;
    private boolean isFixed; // true for standard fixed expenses, false for extra user reminders

    public Reminder(String id, String title, double amount, String category, String dueDate, boolean isPaid, boolean isFixed) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.dueDate = dueDate;
        this.isPaid = isPaid;
        this.isFixed = isFixed;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public String getDueDate() {
        return dueDate;
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
}