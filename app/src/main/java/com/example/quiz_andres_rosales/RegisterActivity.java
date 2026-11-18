package com.example.quiz_andres_rosales;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quiz_andres_rosales.db.entity.UserEntity;
import com.example.quiz_andres_rosales.utils.CurrencyUtils;
import com.example.quiz_andres_rosales.utils.DataManager;
import com.example.quiz_andres_rosales.utils.SecurityUtils;
import com.example.quiz_andres_rosales.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etRegUsername;
    private TextInputEditText etRegPassword;
    private TextInputEditText etRegFullName;
    private TextInputEditText etRegAge;
    private TextInputEditText etRegCode;
    private TextInputEditText etRegCareer;
    private TextInputEditText etRegBudget;

    private MaterialButton btnRegisterSubmit;
    private MaterialButton btnBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegUsername = findViewById(R.id.etRegUsername);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegFullName = findViewById(R.id.etRegFullName);
        etRegAge = findViewById(R.id.etRegAge);
        etRegCode = findViewById(R.id.etRegCode);
        etRegCareer = findViewById(R.id.etRegCareer);
        etRegBudget = findViewById(R.id.etRegBudget);

        etRegBudget.addTextChangedListener(CurrencyUtils.createCurrencyTextWatcher(etRegBudget));

        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnRegisterSubmit.setOnClickListener(v -> handleRegister());
        btnBackToLogin.setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        String username = etRegUsername.getText() != null ? etRegUsername.getText().toString().trim() : "";
        String password = etRegPassword.getText() != null ? etRegPassword.getText().toString().trim() : "";
        String fullName = etRegFullName.getText() != null ? etRegFullName.getText().toString().trim() : "";
        String ageStr = etRegAge.getText() != null ? etRegAge.getText().toString().trim() : "";
        String code = etRegCode.getText() != null ? etRegCode.getText().toString().trim() : "";
        String career = etRegCareer.getText() != null ? etRegCareer.getText().toString().trim() : "";
        String budgetStr = etRegBudget.getText() != null ? etRegBudget.getText().toString().trim() : "";

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(fullName)) {
            Toast.makeText(this, "Complete usuario, contraseña y nombre", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = 20;
        if (!TextUtils.isEmpty(ageStr)) {
            try {
                age = Integer.parseInt(ageStr);
            } catch (NumberFormatException ignored) {}
        }

        double budget = CurrencyUtils.parseCurrencyValue(budgetStr);
        if (budget <= 0) budget = 500000.0;

        String passHash = SecurityUtils.hashPassword(password);
        UserEntity newUser = new UserEntity(username, passHash, fullName, age, career, code, null, budget);

        boolean success = DataManager.getInstance(this).registerUser(newUser);

        if (success) {
            UserEntity registered = DataManager.getInstance(this).login(username, password);
            int userId = registered != null ? registered.getId() : 1;

            SessionManager.saveSession(this, userId, username);
            Toast.makeText(this, "¡Registro exitoso! Bienvenido " + fullName, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "El nombre de usuario ya existe", Toast.LENGTH_SHORT).show();
        }
    }
}