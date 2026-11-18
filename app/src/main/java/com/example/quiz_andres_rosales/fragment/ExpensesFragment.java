package com.example.quiz_andres_rosales.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quiz_andres_rosales.R;
import com.example.quiz_andres_rosales.adapter.ExpenseAdapter;
import com.example.quiz_andres_rosales.db.entity.ExpenseEntity;
import com.example.quiz_andres_rosales.utils.CurrencyUtils;
import com.example.quiz_andres_rosales.utils.DataManager;
import com.example.quiz_andres_rosales.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExpensesFragment extends Fragment implements ExpenseAdapter.OnExpenseClickListener {

    private TextView tvTotalExpensesAmount;
    private TextView tvCashTotal;
    private TextView tvCardTotal;
    private TextView tvTransferTotal;
    private TextView tvEmptyExpenses;
    private RecyclerView rvExpenses;

    private ExpenseAdapter adapter;
    private List<ExpenseEntity> expenseList = new ArrayList<>();
    private int currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_expenses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUserId = SessionManager.getUserId(requireContext());

        tvTotalExpensesAmount = view.findViewById(R.id.tvTotalExpensesAmount);
        tvCashTotal = view.findViewById(R.id.tvCashTotal);
        tvCardTotal = view.findViewById(R.id.tvCardTotal);
        tvTransferTotal = view.findViewById(R.id.tvTransferTotal);
        tvEmptyExpenses = view.findViewById(R.id.tvEmptyExpenses);
        rvExpenses = view.findViewById(R.id.rvExpenses);
        FloatingActionButton fabAddExpense = view.findViewById(R.id.fabAddExpense);

        rvExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));

        fabAddExpense.setOnClickListener(v -> showExpenseDialog(null));

        loadExpenses();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadExpenses();
    }

    private void loadExpenses() {
        expenseList.clear();
        expenseList.addAll(DataManager.getInstance(requireContext()).getExpenses(currentUserId));

        if (adapter == null) {
            adapter = new ExpenseAdapter(expenseList, this);
            rvExpenses.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }

        updateSummary();
    }

    private void updateSummary() {
        double total = 0;
        double cash = 0;
        double card = 0;
        double transfer = 0;

        for (ExpenseEntity e : expenseList) {
            total += e.getAmount();
            if ("Efectivo".equalsIgnoreCase(e.getPaymentMethod())) {
                cash += e.getAmount();
            } else if ("Tarjeta".equalsIgnoreCase(e.getPaymentMethod())) {
                card += e.getAmount();
            } else {
                transfer += e.getAmount();
            }
        }

        tvTotalExpensesAmount.setText(CurrencyUtils.formatCurrency(total));
        tvCashTotal.setText("Efectivo: " + CurrencyUtils.formatCurrency(cash));
        tvCardTotal.setText("Tarjeta: " + CurrencyUtils.formatCurrency(card));
        tvTransferTotal.setText("Transf.: " + CurrencyUtils.formatCurrency(transfer));

        if (expenseList.isEmpty()) {
            tvEmptyExpenses.setVisibility(View.VISIBLE);
            rvExpenses.setVisibility(View.GONE);
        } else {
            tvEmptyExpenses.setVisibility(View.GONE);
            rvExpenses.setVisibility(View.VISIBLE);
        }
    }

    private void showExpenseDialog(@Nullable ExpenseEntity expenseToEdit) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_expense, null);

        TextView tvDialogExpenseTitle = dialogView.findViewById(R.id.tvDialogExpenseTitle);
        TextInputEditText etExpenseTitle = dialogView.findViewById(R.id.etExpenseTitle);
        TextInputEditText etExpenseAmount = dialogView.findViewById(R.id.etExpenseAmount);
        Spinner spExpenseCategory = dialogView.findViewById(R.id.spExpenseCategory);
        Spinner spPaymentMethod = dialogView.findViewById(R.id.spPaymentMethod);

        List<String> categories = Arrays.asList("Alimentación", "Transporte", "Papelería / Académico", "Suscripciones", "Otros");
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        spExpenseCategory.setAdapter(catAdapter);

        List<String> methods = Arrays.asList("Efectivo", "Tarjeta", "Transferencia");
        ArrayAdapter<String> methodAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, methods);
        spPaymentMethod.setAdapter(methodAdapter);

        etExpenseAmount.addTextChangedListener(CurrencyUtils.createCurrencyTextWatcher(etExpenseAmount));

        if (expenseToEdit != null) {
            tvDialogExpenseTitle.setText("Editar Gasto");
            etExpenseTitle.setText(expenseToEdit.getTitle());
            etExpenseAmount.setText(String.valueOf((long) expenseToEdit.getAmount()));
            spExpenseCategory.setSelection(categories.indexOf(expenseToEdit.getCategory()));
            spPaymentMethod.setSelection(methods.indexOf(expenseToEdit.getPaymentMethod()));
        } else {
            tvDialogExpenseTitle.setText("Registrar Nuevo Gasto");
        }

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton(expenseToEdit != null ? "Actualizar" : "Guardar Gasto", (dialog, which) -> {
                    String title = etExpenseTitle.getText() != null ? etExpenseTitle.getText().toString().trim() : "";
                    String amountStr = etExpenseAmount.getText() != null ? etExpenseAmount.getText().toString().trim() : "";
                    String category = spExpenseCategory.getSelectedItem().toString();
                    String method = spPaymentMethod.getSelectedItem().toString();

                    if (TextUtils.isEmpty(title) || TextUtils.isEmpty(amountStr)) {
                        Toast.makeText(requireContext(), "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount = CurrencyUtils.parseCurrencyValue(amountStr);
                    if (amount <= 0) {
                        Toast.makeText(requireContext(), "Monto inválido", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (expenseToEdit != null) {
                        expenseToEdit.setTitle(title);
                        expenseToEdit.setAmount(amount);
                        expenseToEdit.setCategory(category);
                        expenseToEdit.setPaymentMethod(method);
                        DataManager.getInstance(requireContext()).updateExpense(expenseToEdit);
                        Toast.makeText(requireContext(), "Gasto actualizado", Toast.LENGTH_SHORT).show();
                    } else {
                        ExpenseEntity newExpense = new ExpenseEntity(currentUserId, title, amount, method, category, System.currentTimeMillis());
                        DataManager.getInstance(requireContext()).addExpense(newExpense);
                        Toast.makeText(requireContext(), "Gasto registrado exitosamente", Toast.LENGTH_SHORT).show();
                    }

                    loadExpenses();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onExpenseClick(ExpenseEntity expense) {
        showExpenseDialog(expense);
    }

    @Override
    public void onDeleteExpense(ExpenseEntity expense) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de eliminar el gasto \"" + expense.getTitle() + "\" por " + CurrencyUtils.formatCurrency(expense.getAmount()) + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    DataManager.getInstance(requireContext()).deleteExpense(expense);
                    loadExpenses();
                    Toast.makeText(requireContext(), "Gasto eliminado correctamente", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}