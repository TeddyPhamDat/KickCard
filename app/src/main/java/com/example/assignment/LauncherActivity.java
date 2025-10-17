package com.example.assignment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.assignment.utils.SessionManager;
import com.example.assignment.ui.MainActivity;
import com.example.assignment.auth.login.LoginActivity;
import android.util.Log;
import android.widget.Toast;

/**
 * LauncherActivity decides initial navigation based on whether the user has a saved JWT.
 * If no token -> open LoginActivity so the user can sign in.
 * If token exists -> open ListingActivity (main screen).
 */
public class LauncherActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            SessionManager session = new SessionManager(this);
            String token = session.fetchToken();

            Intent intent;
            if (token == null || token.isEmpty()) {
                // No token: show login
                intent = new Intent(this, LoginActivity.class);
                Toast.makeText(this, "No token found -> opening Login", Toast.LENGTH_SHORT).show();
                Log.d("LauncherActivity", "No token found, routing to LoginActivity");
            } else {
                // Has token: go to main activity
                intent = new Intent(this, MainActivity.class);
                String masked = token.length() > 10 ? token.substring(0, 8) + "..." : token;
                Toast.makeText(this, "Token found -> opening Main (" + masked + ")", Toast.LENGTH_SHORT).show();
                Log.d("LauncherActivity", "Token found, routing to MainActivity; token=" + masked);
            }
            startActivity(intent);
            finish();
    }
}
