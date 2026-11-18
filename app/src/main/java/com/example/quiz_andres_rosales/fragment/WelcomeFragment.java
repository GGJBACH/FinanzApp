package com.example.quiz_andres_rosales.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quiz_andres_rosales.LoginActivity;
import com.example.quiz_andres_rosales.R;
import com.example.quiz_andres_rosales.db.entity.ExpenseEntity;
import com.example.quiz_andres_rosales.db.entity.UserEntity;
import com.example.quiz_andres_rosales.utils.CurrencyUtils;
import com.example.quiz_andres_rosales.utils.DataManager;
import com.example.quiz_andres_rosales.utils.SessionManager;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WelcomeFragment extends Fragment {

    private ImageView ivStudentPhoto;
    private ImageView btnChangePhoto;
    private TextView tvStudentName;
    private TextView tvStudentAgeCareer;
    private TextView tvStudentId;
    private MaterialButton btnEditProfile;
    private MaterialButton btnLogout;

    private TextView tvMonthlyBudget;
    private TextView tvTotalExpensesSummary;
    private TextView tvRemainingBalance;
    private MaterialButton btnEditBudget;

    private PieChart pieChartCategory;

    private UserEntity currentUser;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null && currentUser != null) {
                            try {
                                requireContext().getContentResolver().takePersistableUriPermission(
                                        imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );
                            } catch (Exception ignored) {}
                            currentUser.setProfileImageUri(imageUri.toString());
                            DataManager.getInstance(requireContext()).updateUser(currentUser);
                            ivStudentPhoto.setImageURI(imageUri);
                            Toast.makeText(requireContext(), "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivStudentPhoto = view.findViewById(R.id.ivStudentPhoto);
        btnChangePhoto = view.findViewById(R.id.btnChangePhoto);
        tvStudentName = view.findViewById(R.id.tvStudentName);
        tvStudentAgeCareer = view.findViewById(R.id.tvStudentAgeCareer);
        tvStudentId = view.findViewById(R.id.tvStudentId);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        tvMonthlyBudget = view.findViewById(R.id.tvMonthlyBudget);
        tvTotalExpensesSummary = view.findViewById(R.id.tvTotalExpensesSummary);
        tvRemainingBalance = view.findViewById(R.id.tvRemainingBalance);
        btnEditBudget = view.findViewById(R.id.btnEditBudget);

        pieChartCategory = view.findViewById(R.id.pieChartCategory);

        btnChangePhoto.setOnClickListener(v -> openGallery());
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnEditBudget.setOnClickListener(v -> showEditBudgetDialog());

        btnLogout.setOnClickListener(v -> {
            SessionManager.clearSession(requireContext());
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        loadUserData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void loadUserData() {
        int userId = SessionManager.getUserId(requireContext());
        currentUser = DataManager.getInstance(requireContext()).getUserById(userId);

        if (currentUser == null) return;

        tvStudentName.setText(currentUser.getFullName());
        tvStudentAgeCareer.setText(currentUser.getAge() + " años | " + currentUser.getCareer());
        tvStudentId.setText("Código ID: " + currentUser.getStudentCode());

        if (currentUser.getProfileImageUri() != null) {
            try {
                ivStudentPhoto.setImageURI(Uri.parse(currentUser.getProfileImageUri()));
            } catch (Exception e) {
                ivStudentPhoto.setImageResource(R.drawable.ic_student_avatar);
            }
        } else {
            ivStudentPhoto.setImageResource(R.drawable.ic_student_avatar);
        }

        double totalExpenses = DataManager.getInstance(requireContext()).getTotalExpenses(userId);
        double budget = currentUser.getMonthlyBudget();
        double remaining = budget - totalExpenses;

        tvMonthlyBudget.setText(CurrencyUtils.formatCurrency(budget));
        tvTotalExpensesSummary.setText(CurrencyUtils.formatCurrency(totalExpenses));
        tvRemainingBalance.setText(CurrencyUtils.formatCurrency(remaining));

        setupPieChart(userId);
    }

    private void setupPieChart(int userId) {
        List<ExpenseEntity> expenses = DataManager.getInstance(requireContext()).getExpenses(userId);
        Map<String, Double> categoryTotals = new HashMap<>();

        for (ExpenseEntity e : expenses) {
            String cat = e.getCategory() != null ? e.getCategory() : "Otros";
            categoryTotals.put(cat, categoryTotals.getOrDefault(cat, 0.0) + e.getAmount());
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            if (entry.getValue() > 0) {
                entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            }
        }

        if (entries.isEmpty()) {
            pieChartCategory.setNoDataText("No hay gastos para mostrar en el gráfico");
            pieChartCategory.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#1565C0")); // Blue
        colors.add(Color.parseColor("#2E7D32")); // Green
        colors.add(Color.parseColor("#6A1B9A")); // Purple
        colors.add(Color.parseColor("#EF6C00")); // Orange
        colors.add(Color.parseColor("#C62828")); // Red
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChartCategory));
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.WHITE);

        pieChartCategory.setUsePercentValues(true);
        pieChartCategory.getDescription().setEnabled(false);
        pieChartCategory.setHoleRadius(45f);
        pieChartCategory.setTransparentCircleRadius(50f);
        pieChartCategory.setData(data);
        pieChartCategory.animateY(800);
        pieChartCategory.invalidate();
    }

    private void showEditProfileDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null);

        TextInputEditText etEditName = dialogView.findViewById(R.id.etEditName);
        TextInputEditText etEditAge = dialogView.findViewById(R.id.etEditAge);
        TextInputEditText etEditCareer = dialogView.findViewById(R.id.etEditCareer);
        TextInputEditText etEditCode = dialogView.findViewById(R.id.etEditCode);

        if (currentUser != null) {
            etEditName.setText(currentUser.getFullName());
            etEditAge.setText(String.valueOf(currentUser.getAge()));
            etEditCareer.setText(currentUser.getCareer());
            etEditCode.setText(currentUser.getStudentCode());
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Editar Perfil Estudiantil")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String name = etEditName.getText() != null ? etEditName.getText().toString().trim() : "";
                    String ageStr = etEditAge.getText() != null ? etEditAge.getText().toString().trim() : "";
                    String career = etEditCareer.getText() != null ? etEditCareer.getText().toString().trim() : "";
                    String code = etEditCode.getText() != null ? etEditCode.getText().toString().trim() : "";

                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int age = 20;
                    try {
                        age = Integer.parseInt(ageStr);
                    } catch (NumberFormatException ignored) {}

                    currentUser.setFullName(name);
                    currentUser.setAge(age);
                    currentUser.setCareer(career);
                    currentUser.setStudentCode(code);

                    DataManager.getInstance(requireContext()).updateUser(currentUser);
                    loadUserData();
                    Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showEditBudgetDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_budget, null);
        TextInputEditText etBudgetAmount = dialogView.findViewById(R.id.etBudgetAmount);

        if (currentUser != null) {
            etBudgetAmount.setText(String.valueOf((long) currentUser.getMonthlyBudget()));
        }

        etBudgetAmount.addTextChangedListener(CurrencyUtils.createCurrencyTextWatcher(etBudgetAmount));

        new AlertDialog.Builder(requireContext())
                .setTitle("Presupuesto / Mesada Mensual")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String valStr = etBudgetAmount.getText() != null ? etBudgetAmount.getText().toString().trim() : "";
                    double budget = CurrencyUtils.parseCurrencyValue(valStr);

                    if (currentUser != null) {
                        currentUser.setMonthlyBudget(budget);
                        DataManager.getInstance(requireContext()).updateUser(currentUser);
                        loadUserData();
                        Toast.makeText(requireContext(), "Presupuesto actualizado", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}