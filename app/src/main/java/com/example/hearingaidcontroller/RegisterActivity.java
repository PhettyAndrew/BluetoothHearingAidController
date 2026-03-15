package com.example.hearingaidcontroller;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etHearingLoss, etPassword, etConfirm;
    private Button btnRegister;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        etFullName = findViewById(R.id.editTextFullName);
        etEmail = findViewById(R.id.editTextEmail);
        etHearingLoss = findViewById(R.id.editTextHearingLoss);
        etPassword = findViewById(R.id.editTextPassword);
        etConfirm = findViewById(R.id.editTextConfirmPassword);
        btnRegister = findViewById(R.id.buttonRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = etFullName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String hearingLoss = etHearingLoss.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirm = etConfirm.getText().toString().trim();

                if (fullName.isEmpty() || email.isEmpty() || hearingLoss.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(confirm)) {
                    Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                User user = new User(fullName, email, hearingLoss, password);
                long id = dbHelper.addUser(user);
                if (id != -1) {
                    Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Email already exists", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}