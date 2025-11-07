package com.example.assignment.data.api;

import android.content.Context;

import com.example.assignment.utils.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static final String BASE_URL = "http://10.0.2.2:8080";

    public static Retrofit getClient(Context ctx, String baseUrl) {
        if (retrofit == null) {
            SessionManager session = new SessionManager(ctx);
            Interceptor authInterceptor = chain -> {
                Request original = chain.request();
                Request.Builder builder = original.newBuilder();
                String token = session.fetchToken();
                if (token != null && !token.isEmpty()) {
                    builder.addHeader("Authorization", "Bearer " + token);
                    // Debug: log masked token to help diagnose Authorization header issues
                    try {
                        String masked = token.length() > 10 ? token.substring(0, 6) + "..." + token.substring(token.length()-4) : token;
                        android.util.Log.d("ApiAuth", "Adding Authorization header: Bearer " + masked);
                    } catch (Exception e) {
                        android.util.Log.d("ApiAuth", "Adding Authorization header (unable to mask)");
                    }
                }
                Request request = builder.build();
                Response response = chain.proceed(request);
                if (response.code() == 401) {
                    // optional: clear session
                    session.clear();
                }
                return response;
            };

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService(Context ctx) {
        Retrofit r = getClient(ctx, BASE_URL);
        return r.create(ApiService.class);
    }

    public static RetrofitClient getInstance(String baseUrl) {
        return new RetrofitClient();
    }

    public ApiService getApi() {
        // This method is used by PaymentRepository
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
