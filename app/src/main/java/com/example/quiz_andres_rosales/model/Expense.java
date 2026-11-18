package com.example.quiz_andres_rosales.model;

import java.io.Serializable;

public class Expense implements Serializable {
    private String id;
    private String title;
    private double amount;
    private String paymentMethod; // "Efectivo", "Tarjeta", "Transferencia"
    private String date;

    public Expense(String id, String title, double amount, String paymentMethod, String date) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.date = date;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getDate() {
        return date;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}