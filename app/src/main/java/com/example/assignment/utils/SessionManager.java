package com.example.assignment.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "app_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_FULLNAME = "fullname";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_AVATAR = "avatar_url";
    private static final String KEY_WALLET = "wallet_balance";
    public void saveUsername(String username) { prefs.edit().putString(KEY_USERNAME, username).apply(); }
    public String fetchUsername() { return prefs.getString(KEY_USERNAME, null); }
    public void saveUserId(Long userId) { prefs.edit().putLong(KEY_USER_ID, userId).apply(); }
    public Long fetchUserId() { return prefs.getLong(KEY_USER_ID, -1L); }
    private SharedPreferences prefs;

    public SessionManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        // Use commit() to ensure token is persisted immediately before navigation
        prefs.edit().putString(KEY_TOKEN, token).commit();
    }

    public String fetchToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void clear() {
        prefs.edit().remove(KEY_TOKEN).remove(KEY_FULLNAME).remove(KEY_PHONE).remove(KEY_ADDRESS).remove(KEY_AVATAR).apply();
    }

    public void saveProfile(String fullname, String phone, String address, String avatarUrl) {
        prefs.edit().putString(KEY_FULLNAME, fullname).putString(KEY_PHONE, phone).putString(KEY_ADDRESS, address).putString(KEY_AVATAR, avatarUrl).apply();
    }

    public void saveWalletBalance(Double balance) { prefs.edit().putString(KEY_WALLET, balance == null ? null : String.valueOf(balance)).apply(); }
    public Double getWalletBalance() { String s = prefs.getString(KEY_WALLET, null); if (s == null) return null; try { return Double.parseDouble(s); } catch (Exception e) { return null; } }

    public String getFullname() { return prefs.getString(KEY_FULLNAME, null); }
    public String getPhone() { return prefs.getString(KEY_PHONE, null); }
    public String getAddress() { return prefs.getString(KEY_ADDRESS, null); }
    public String getAvatarUrl() { return prefs.getString(KEY_AVATAR, null); }
}
