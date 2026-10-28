package com.example.mco2_itismob_gym_app_ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegName, etRegEmail, etRegPassword;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegName = findViewById(R.id.etRegName);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> handleRegistration());
    }

    private void handleRegistration() {
        String name = etRegName.getText().toString().trim();
        String email = etRegEmail.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();

        if (name.isEmpty()) {
            etRegName.setError("Full name is required");
            etRegName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etRegEmail.setError("Email is required");
            etRegEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etRegEmail.setError("Please enter a valid email address");
            etRegEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etRegPassword.setError("Password is required");
            etRegPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etRegPassword.setError("Password must be at least 6 characters");
            etRegPassword.requestFocus();
            return;
        }

        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}