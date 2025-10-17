package com.example.assignment.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment.R;
import com.example.assignment.data.model.HomeListing;
import com.example.assignment.data.repository.HomeRepository;
import com.example.assignment.presenter.ListingPresenter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private EditText etName;
    private EditText etMinPrice;
    private EditText etMaxPrice;
    private Button btnSearch;
    private RecyclerView recycler;
    private ProgressBar progress;
    private HomeListingAdapter adapter;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh;

    private static final String BASE_URL = "http://10.0.2.2:8080";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etName = view.findViewById(R.id.etSearchName);
    etMinPrice = view.findViewById(R.id.etMinPrice);
    etMaxPrice = view.findViewById(R.id.etMaxPrice);
        btnSearch = view.findViewById(R.id.btnSearch);
        recycler = view.findViewById(R.id.recyclerHomeListings);
        progress = view.findViewById(R.id.progressHome);
    swipeRefresh = view.findViewById(R.id.swipeRefresh);

    adapter = new HomeListingAdapter(new java.util.ArrayList<>());
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> doSearch());

    swipeRefresh.setOnRefreshListener(() -> doSearch());

        // initial load
        doSearch();
    }

    private void doSearch() {
        String name = etName.getText().toString().trim();
        Double min = null;
        Double max = null;
        try {
            String smin = etMinPrice.getText().toString().trim();
            if (!smin.isEmpty()) min = Double.parseDouble(smin);
        } catch (NumberFormatException ignored) {}
        try {
            String smax = etMaxPrice.getText().toString().trim();
            if (!smax.isEmpty()) max = Double.parseDouble(smax);
        } catch (NumberFormatException ignored) {}
        progress.setVisibility(View.VISIBLE);
        HomeRepository repo = new HomeRepository(getContext(), BASE_URL);
        repo.getHomeListings(name.isEmpty() ? null : name, min, max).enqueue(new Callback<java.util.List<HomeListing>>() {
            @Override
            public void onResponse(Call<java.util.List<HomeListing>> call, Response<java.util.List<HomeListing>> response) {
                progress.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body());
                } else {
                    Toast.makeText(getContext(), "Failed to load home listings", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<java.util.List<HomeListing>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
