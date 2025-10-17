package com.example.assignment.presenter;

import android.content.Context;

import com.example.assignment.data.model.Listing;
import com.example.assignment.data.repository.ListingRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListingPresenter {
    public interface View {
        void showLoading(boolean show);
        void onListingsLoaded(List<Listing> listings);
        void onError(String message);
    }

    private View view;
    private ListingRepository repo;

    public ListingPresenter(View view, Context ctx, String baseUrl) {
        this.view = view;
        this.repo = new ListingRepository(ctx, baseUrl);
    }

    public void loadListings(String status) {
        view.showLoading(true);
        repo.getListings(status).enqueue(new Callback<List<Listing>>() {
            @Override
            public void onResponse(Call<List<Listing>> call, Response<List<Listing>> response) {
                view.showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    view.onListingsLoaded(response.body());
                } else {
                    view.onError("Failed to load listings");
                }
            }

            @Override
            public void onFailure(Call<List<Listing>> call, Throwable t) {
                view.showLoading(false);
                view.onError(t.getMessage());
            }
        });
    }
}
