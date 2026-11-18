package com.example.quiz_andres_rosales.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.quiz_andres_rosales.db.dao.ExpenseDao;
import com.example.quiz_andres_rosales.db.dao.ReminderDao;
import com.example.quiz_andres_rosales.db.dao.UserDao;
import com.example.quiz_andres_rosales.db.entity.ExpenseEntity;
import com.example.quiz_andres_rosales.db.entity.ReminderEntity;
import com.example.quiz_andres_rosales.db.entity.UserEntity;
import com.example.quiz_andres_rosales.utils.SecurityUtils;

import java.util.concurrent.Executors;

@Database(entities = {UserEntity.class, ExpenseEntity.class, ReminderEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract ExpenseDao expenseDao();
    public abstract ReminderDao reminderDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "finanz_app.db"
                    )
                    .allowMainThreadQueries() // Simple sync for room operations
                    .addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            Executors.newSingleThreadExecutor().execute(() -> seedInitialData(INSTANCE));
                        }
                    })
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void seedInitialData(AppDatabase database) {
        if (database == null) return;
        String passHash = SecurityUtils.hashPassword("123456");
        UserEntity defaultUser = new UserEntity(
                "estudiante",
                passHash,
                "Andrés Rosales",
                22,
                "Ingeniería de Sistemas",
                "2024-10582",
                null,
                600000.0 // Presupuesto inicial $600.000 COP
        );

        long userIdLong = database.userDao().insertUser(defaultUser);
        int userId = (int) userIdLong;
        if (userId <= 0) userId = 1;

        long now = System.currentTimeMillis();

        // Initial Expenses
        database.expenseDao().insertExpense(new ExpenseEntity(userId, "Almuerzo Universitario", 18500, "Efectivo", "Alimentación", now));
        database.expenseDao().insertExpense(new ExpenseEntity(userId, "Pasajes de Bus", 12000, "Efectivo", "Transporte", now - 86400000L));
        database.expenseDao().insertExpense(new ExpenseEntity(userId, "Fotocopias y Cuaderno", 15000, "Transferencia", "Papelería / Académico", now - 172800000L));
        database.expenseDao().insertExpense(new ExpenseEntity(userId, "Suscripción Spotify", 16900, "Tarjeta", "Suscripciones", now - 259200000L));

        // Initial Fixed Reminders
        database.reminderDao().insertReminder(new ReminderEntity(userId, "Factura del Celular", 45000, "Celular", 15, false, true));
        database.reminderDao().insertReminder(new ReminderEntity(userId, "Plataformas (Netflix / HBO)", 38000, "Plataformas", 20, false, true));
        database.reminderDao().insertReminder(new ReminderEntity(userId, "Tarjeta de Crédito Visa", 150000, "Tarjeta de Crédito", 28, false, true));
        database.reminderDao().insertReminder(new ReminderEntity(userId, "Gimnasio Universitario", 50000, "Otro", 5, true, false));
    }
}