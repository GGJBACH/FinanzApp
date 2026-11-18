package com.example.quiz_andres_rosales.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.quiz_andres_rosales.R;
import com.example.quiz_andres_rosales.adapter.ReminderAdapter;
import com.example.quiz_andres_rosales.db.entity.ExpenseEntity;
import com.example.quiz_andres_rosales.db.entity.ReminderEntity;
import com.example.quiz_andres_rosales.utils.CurrencyUtils;
import com.example.quiz_andres_rosales.utils.DataManager;
import com.example.quiz_andres_rosales.utils.ReminderNotificationWorker;
import com.example.quiz_andres_rosales.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RemindersFragment extends Fragment implements ReminderAdapter.OnReminderActionListener {

    private TextView tvRemindersPendingBadge;
    private RecyclerView rvReminders;

    private ReminderAdapter adapter;
    private List<ReminderEntity> reminderList = new ArrayList<>();
    private int currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUserId = SessionManager.getUserId(requireContext());

        tvRemindersPendingBadge = view.findViewById(R.id.tvRemindersPendingBadge);
        MaterialButton btnAddCustomReminder = view.findViewById(R.id.btnAddCustomReminder);
        rvReminders = view.findViewById(R.id.rvReminders);

        rvReminders.setLayoutManager(new LinearLayoutManager(requireContext()));

        btnAddCustomReminder.setOnClickListener(v -> showAddReminderDialog());

        loadReminders();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadReminders();
    }

    private void loadReminders() {
        reminderList.clear();
        reminderList.addAll(DataManager.getInstance(requireContext()).getReminders(currentUserId));

        if (adapter == null) {
            adapter = new ReminderAdapter(reminderList, this);
            rvReminders.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }

        updatePendingBadge();
    }

    private void updatePendingBadge() {
        int pending = DataManager.getInstance(requireContext()).getPendingRemindersCount(currentUserId);
        int total = reminderList.size();
        tvRemindersPendingBadge.setText("Gastos Fijos y Extras: " + (total - pending) + " de " + total + " pagados");
    }

    private void showAddReminderDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_reminder, null);

        TextInputEditText etReminderTitle = dialogView.findViewById(R.id.etReminderTitle);
        TextInputEditText etReminderAmount = dialogView.findViewById(R.id.etReminderAmount);
        Spinner spReminderCategory = dialogView.findViewById(R.id.spReminderCategory);
        NumberPicker npDueDay = dialogView.findViewById(R.id.npDueDay);

        npDueDay.setMinValue(1);
        npDueDay.setMaxValue(31);
        npDueDay.setValue(15);

        List<String> categories = Arrays.asList("Celular", "Plataformas", "Tarjeta de Crédito", "Otro");
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        spReminderCategory.setAdapter(catAdapter);

        etReminderAmount.addTextChangedListener(CurrencyUtils.createCurrencyTextWatcher(etReminderAmount));

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Guardar Recordatorio", (dialog, which) -> {
                    String title = etReminderTitle.getText() != null ? etReminderTitle.getText().toString().trim() : "";
                    String amountStr = etReminderAmount.getText() != null ? etReminderAmount.getText().toString().trim() : "";
                    String category = spReminderCategory.getSelectedItem().toString();
                    int dueDay = npDueDay.getValue();

                    if (TextUtils.isEmpty(title) || TextUtils.isEmpty(amountStr)) {
                        Toast.makeText(requireContext(), "Por favor complete el título y monto", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount = CurrencyUtils.parseCurrencyValue(amountStr);
                    if (amount <= 0) {
                        Toast.makeText(requireContext(), "Monto inválido", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ReminderEntity newReminder = new ReminderEntity(currentUserId, title, amount, category, dueDay, false, false);
                    DataManager.getInstance(requireContext()).addReminder(newReminder);

                    scheduleReminderNotification(title, amount);

                    loadReminders();
                    Toast.makeText(requireContext(), "Recordatorio extra agregado exitosamente", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void scheduleReminderNotification(String title, double amount) {
        try {
            Data inputData = new Data.Builder()
                    .putString(ReminderNotificationWorker.KEY_TITLE, title)
                    .putDouble(ReminderNotificationWorker.KEY_AMOUNT, amount)
                    .build();

            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ReminderNotificationWorker.class)
                    .setInitialDelay(5, TimeUnit.SECONDS) // Demonstration delay for testing notification
                    .setInputData(inputData)
                    .build();

            WorkManager.getInstance(requireContext()).enqueue(workRequest);
        } catch (Exception ignored) {}
    }

    @Override
    public void onReminderStatusChanged(ReminderEntity reminder, boolean isPaid) {
        reminder.setPaid(isPaid);
        DataManager.getInstance(requireContext()).updateReminder(reminder);

        if (isPaid) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Registrar Pago como Gasto")
                    .setMessage("¿Deseas registrar este pago de \"" + reminder.getTitle() + "\" por " + CurrencyUtils.formatCurrency(reminder.getAmount()) + " como un nuevo egreso en tu historial de gastos?")
                    .setPositiveButton("Sí, Registrar Gasto", (dialog, which) -> {
                        ExpenseEntity expense = new ExpenseEntity(
                                currentUserId,
                                reminder.getTitle(),
                                reminder.getAmount(),
                                "Transferencia",
                                reminder.getCategory() != null ? reminder.getCategory() : "Suscripciones",
                                System.currentTimeMillis()
                        );
                        DataManager.getInstance(requireContext()).addExpense(expense);
                        Toast.makeText(requireContext(), "Gasto registrado en el historial", Toast.LENGTH_SHORT).show();
                        loadReminders();
                    })
                    .setNegativeButton("No", (dialog, which) -> loadReminders())
                    .setCancelable(false)
                    .show();
        } else {
            loadReminders();
            Toast.makeText(requireContext(), reminder.getTitle() + ": Marcado como Pendiente", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteReminder(ReminderEntity reminder) {
        if (reminder.isFixed()) {
            Toast.makeText(requireContext(), "Los gastos fijos base no se pueden eliminar", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de eliminar el recordatorio \"" + reminder.getTitle() + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    DataManager.getInstance(requireContext()).deleteReminder(reminder);
                    loadReminders();
                    Toast.makeText(requireContext(), "Recordatorio eliminado correctamente", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}