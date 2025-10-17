package com.example.assignment.auth.register;

import android.content.Context;
import com.example.assignment.data.api.ApiService;
import com.example.assignment.data.api.RetrofitClient;
import com.example.assignment.data.model.RegisterRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterPresenter implements RegisterContract.Presenter {

    private RegisterContract.View view;
    private ApiService apiService;

    public RegisterPresenter(Context context) {
        this.apiService = RetrofitClient.getApiService(context);
    }

    @Override
    public void attachView(RegisterContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void register(String username, String email, String password) {
        if (view != null) {
            view.showProgress();
        }

        RegisterRequest registerRequest = new RegisterRequest(username, email, password);
        apiService.signup(registerRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (view != null) {
                    view.hideProgress();
                    if (response.isSuccessful()) {
                        view.showRegisterSuccess("Registration successful!");
                        view.navigateToLogin();
                    } else {
                        String errorMessage = "Registration failed!";
                        if (response.errorBody() != null) {
                            try {
                                errorMessage = response.errorBody().string();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        view.showRegisterError(errorMessage);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (view != null) {
                    view.hideProgress();
                    view.showRegisterError("Network error: " + t.getMessage());
                }
            }
        });
    }
}
