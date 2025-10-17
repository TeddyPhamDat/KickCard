package com.example.assignment.auth.register;

public interface RegisterContract {
    interface View {
        void showProgress();
        void hideProgress();
        void showRegisterSuccess(String message);
        void showRegisterError(String message);
        void navigateToLogin();
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void register(String username, String email, String password);
    }
}
