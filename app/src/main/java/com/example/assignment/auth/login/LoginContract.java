package com.example.assignment.auth.login;

public interface LoginContract {
    interface View {
        void showProgress();
        void hideProgress();
        void showLoginSuccess(String message);
        void showLoginError(String message);
        void navigateToHome();
        void navigateToRegister();
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void login(String username, String password);
        void onRegisterClick();
    }
}
