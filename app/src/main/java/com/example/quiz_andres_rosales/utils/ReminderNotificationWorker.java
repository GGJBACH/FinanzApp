package com.example.quiz_andres_rosales.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.quiz_andres_rosales.R;

public class ReminderNotificationWorker extends Worker {

    public static final String CHANNEL_ID = "reminder_notifications_channel";
    public static final String KEY_TITLE = "title";
    public static final String KEY_AMOUNT = "amount";

    public ReminderNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String title = getInputData().getString(KEY_TITLE);
        double amount = getInputData().getDouble(KEY_AMOUNT, 0);

        if (title == null) title = "Recordatorio de Pago";

        showNotification(title, "Recordatorio: Mañana vence el pago de " + title + " por " + CurrencyUtils.formatCurrency(amount));

        return Result.success();
    }

    private void showNotification(String title, String message) {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Recordatorios Financieros",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notificaciones para vencimiento de gastos fijos");
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_reminders)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        if (manager != null) {
            try {
                manager.notify((int) System.currentTimeMillis(), builder.build());
            } catch (Exception e) {
                // Ignore permission exception if not granted
            }
        }
    }
}