package com.example.assignment.presenter;

import android.content.Context;

import com.example.assignment.data.model.LoginRequest;
import com.example.assignment.data.repository.AuthRepository;
import com.example.assignment.utils.SessionManager;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthPresenter {
    public interface View {
        void showLoading(boolean show);
        void onLoginSuccess();
        void onError(String message);
    }

    private View view;
    private AuthRepository repo;
    private SessionManager session;

    public AuthPresenter(View view, Context ctx, String baseUrl) {
        this.view = view;
        this.repo = new AuthRepository(ctx, baseUrl);
        this.session = new SessionManager(ctx);
    }

    public void login(String username, String password) {
        view.showLoading(true);
        LoginRequest req = new LoginRequest(username, password);
        repo.signin(req).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                view.showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, String> map = response.body();
                    String token = map.get("token");
                    if (token != null) {
                        session.saveToken(token);
                        android.util.Log.d("AuthPresenter", "Saved token, length=" + (token != null ? token.length() : 0));
                        view.onLoginSuccess();
                        return;
                    }
                }
                view.onError("Login failed");
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                view.showLoading(false);
                view.onError(t.getMessage());
            }
        });
    }
}
