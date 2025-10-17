package com.example.assignment.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.assignment.R;
import com.example.assignment.presenter.AuthPresenter;

public class LoginActivity extends AppCompatActivity implements AuthPresenter.View {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private AuthPresenter presenter;

    // set your backend base url here
    private static final String BASE_URL = "http://10.0.2.2:8080"; // emulator -> host machine

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        presenter = new AuthPresenter(this, this, BASE_URL);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String u = etUsername.getText().toString().trim();
                String p = etPassword.getText().toString().trim();
                if (u.isEmpty() || p.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Enter credentials", Toast.LENGTH_SHORT).show();
                    return;
                }
                presenter.login(u, p);
            }
        });

        findViewById(R.id.tvRegisterHint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    @Override
    public void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onLoginSuccess() {
        Toast.makeText(this, "Login success", Toast.LENGTH_SHORT).show();
        // open main navigation activity
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
    }
}
