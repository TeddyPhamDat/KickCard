package com.example.assignment.auth.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.assignment.ui.MainActivity;
import com.example.assignment.R;
import com.example.assignment.auth.register.RegisterActivity;

public class LoginActivity extends AppCompatActivity implements LoginContract.View {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    // FIX: Changed variable to represent the correct view
    private TextView tvRegister;
    private ProgressBar progressBar;

    private LoginContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        // FIX: Finding the correct clickable TextView ID
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);

        presenter = new LoginPresenter(this);
        presenter.attachView(this);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            presenter.login(username, password);
        });

        // FIX: Setting the listener on the correct view
        tvRegister.setOnClickListener(v -> presenter.onRegisterClick());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    @Override
    public void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setText("");
        btnLogin.setEnabled(false);
    }

    @Override
    public void hideProgress() {
        progressBar.setVisibility(View.GONE);
        btnLogin.setText("Enter the Pitch");
        btnLogin.setEnabled(true);
    }

    @Override
    public void showLoginSuccess(String message) {
        String text = (message == null || message.trim().isEmpty()) ? "Login Successful" : message;
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoginError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void navigateToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}