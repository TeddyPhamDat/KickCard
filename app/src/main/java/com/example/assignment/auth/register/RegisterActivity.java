package com.example.assignment.auth.register;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.assignment.R;

public class RegisterActivity extends AppCompatActivity implements RegisterContract.View {

    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnRegister;
    private ProgressBar progressBar;
    private TextView tvLogin; // For the "Sign In" link

    private RegisterContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views from the new layout
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvLogin = findViewById(R.id.tvLogin);

        presenter = new RegisterPresenter(this);
        presenter.attachView(this);

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            presenter.register(username, email, password);
        });

        // Set listener for the "Sign In" link to go back
        tvLogin.setOnClickListener(v -> {
            // Simply finish this activity to return to the previous one (LoginActivity)
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    @Override
    public void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
        // Hide the button's text and disable it
        btnRegister.setText("");
        btnRegister.setEnabled(false);
    }

    @Override
    public void hideProgress() {
        progressBar.setVisibility(View.GONE);
        // Restore the button's text and re-enable it
        btnRegister.setText("Register");
        btnRegister.setEnabled(true);
    }

    @Override
    public void showRegisterSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showRegisterError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToLogin() {
        // This is called after a SUCCESSFUL registration
        // We finish the current activity so the user can't press "back" to it
        finish();
    }
}