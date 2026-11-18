package com.example.quiz_andres_rosales.utils;

import android.content.Context;

import com.example.quiz_andres_rosales.db.AppDatabase;
import com.example.quiz_andres_rosales.db.entity.ExpenseEntity;
import com.example.quiz_andres_rosales.db.entity.ReminderEntity;
import com.example.quiz_andres_rosales.db.entity.UserEntity;

import java.util.List;

public class DataManager {

    private static DataManager instance;
    private final AppDatabase database;

    private DataManager(Context context) {
        database = AppDatabase.getInstance(context.getApplicationContext());
    }

    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context);
        }
        return instance;
    }

    // User Operations
    public UserEntity getUserById(int userId) {
        UserEntity user = database.userDao().getUserById(userId);
        if (user == null) {
            // Seed check if db was empty
            UserEntity defaultUser = new UserEntity("estudiante", SecurityUtils.hashPassword("123456"), "Andrés Rosales", 22, "Ingeniería de Sistemas", "2024-10582", null, 600000.0);
            long id = database.userDao().insertUser(defaultUser);
            user = database.userDao().getUserById((int) id);
        }
        return user;
    }

    public UserEntity login(String username, String password) {
        String hash = SecurityUtils.hashPassword(password);
        UserEntity user = database.userDao().login(username, hash);
        if (user == null) {
            // Allow fallback check for "estudiante" / "123456"
            if ("estudiante".equalsIgnoreCase(username) && "123456".equals(password)) {
                user = database.userDao().getUserByUsername("estudiante");
                if (user == null) {
                    UserEntity defaultUser = new UserEntity("estudiante", hash, "Andrés Rosales", 22, "Ingeniería de Sistemas", "2024-10582", null, 600000.0);
                    long id = database.userDao().insertUser(defaultUser);
                    user = database.userDao().getUserById((int) id);
                }
            }
        }
        return user;
    }

    public boolean registerUser(UserEntity user) {
        UserEntity existing = database.userDao().getUserByUsername(user.getUsername());
        if (existing != null) {
            return false; // Already exists
        }
        database.userDao().insertUser(user);
        return true;
    }

    public void updateUser(UserEntity user) {
        database.userDao().updateUser(user);
    }

    // Expenses Operations
    public List<ExpenseEntity> getExpenses(int userId) {
        return database.expenseDao().getExpensesForUser(userId);
    }

    public void addExpense(ExpenseEntity expense) {
        database.expenseDao().insertExpense(expense);
    }

    public void updateExpense(ExpenseEntity expense) {
        database.expenseDao().updateExpense(expense);
    }

    public void deleteExpense(ExpenseEntity expense) {
        database.expenseDao().deleteExpense(expense);
    }

    public double getTotalExpenses(int userId) {
        Double total = database.expenseDao().getTotalExpensesForUser(userId);
        return total != null ? total : 0.0;
    }

    // Reminders Operations
    public List<ReminderEntity> getReminders(int userId) {
        return database.reminderDao().getRemindersForUser(userId);
    }

    public void addReminder(ReminderEntity reminder) {
        database.reminderDao().insertReminder(reminder);
    }

    public void updateReminder(ReminderEntity reminder) {
        database.reminderDao().updateReminder(reminder);
    }

    public void deleteReminder(ReminderEntity reminder) {
        database.reminderDao().deleteReminder(reminder);
    }

    public int getPendingRemindersCount(int userId) {
        return database.reminderDao().getPendingCountForUser(userId);
    }
}