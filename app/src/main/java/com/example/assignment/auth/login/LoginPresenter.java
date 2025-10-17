package com.example.assignment.auth.login;

import android.content.Context;
import com.example.assignment.data.api.ApiService;
import com.example.assignment.data.api.RetrofitClient;
import com.example.assignment.data.model.LoginRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginPresenter implements LoginContract.Presenter {

    private LoginContract.View view;
    private ApiService apiService;

    public LoginPresenter(Context context) {
        this.apiService = RetrofitClient.getApiService(context);
    }

    @Override
    public void attachView(LoginContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void login(String username, String password) {
        if (view != null) {
            view.showProgress();
        }

        LoginRequest loginRequest = new LoginRequest(username, password);
        apiService.signin(loginRequest).enqueue(new Callback<java.util.Map<String, String>>() {
            @Override
            public void onResponse(Call<java.util.Map<String, String>> call, Response<java.util.Map<String, String>> response) {
                if (view != null) {
                    view.hideProgress();
                    if (response.isSuccessful() && response.body() != null) {
                        // Handle successful login
                        String message = response.body().get("message");
                        String token = response.body().get("token");
                        view.showLoginSuccess(message != null ? message : "Login successful");
                        if (token != null && view instanceof android.content.Context) {
                            // Save token synchronously
                            com.example.assignment.utils.SessionManager session = new com.example.assignment.utils.SessionManager((android.content.Context) view);
                            session.saveToken(token);
                            android.util.Log.d("LoginPresenter", "Saved token from response, len=" + token.length());
                        }
                        view.navigateToHome(); // Navigate to home after successful login
                    } else {
                        // Handle login error
                        String errorMessage = "Login failed!";
                        if (response.errorBody() != null) {
                            try {
                                errorMessage = response.errorBody().string();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        view.showLoginError(errorMessage);
                    }
                }
            }

            @Override
            public void onFailure(Call<java.util.Map<String, String>> call, Throwable t) {
                if (view != null) {
                    view.hideProgress();
                    view.showLoginError("Network error: " + t.getMessage());
                }
            }
        });
    }

    @Override
    public void onRegisterClick() {
        if (view != null) {
            view.navigateToRegister();
        }
    }
}
