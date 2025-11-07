package com.example.assignment.ui;

import android.os.Bundle;
import android.util.Log;
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
import com.google.gson.Gson;

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

        // Search button click
        btnSearch.setOnClickListener(v -> doSearch());

        // Enter key on name field
        etName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                doSearch();
                // Hide keyboard
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) 
                    requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        swipeRefresh.setOnRefreshListener(() -> doSearch());

        // initial load
        doSearch();
    }

    private void doSearch() {
        if (!isAdded() || getContext() == null) return;
        
        String name = etName.getText().toString().trim();
        Double min = null;
        Double max = null;
        
        // Parse min price
        try {
            String smin = etMinPrice.getText().toString().trim();
            if (!smin.isEmpty()) {
                min = Double.parseDouble(smin);
                if (min < 0) {
                    Toast.makeText(getContext(), "Giá tối thiểu phải >= 0", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Giá tối thiểu không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Parse max price
        try {
            String smax = etMaxPrice.getText().toString().trim();
            if (!smax.isEmpty()) {
                max = Double.parseDouble(smax);
                if (max < 0) {
                    Toast.makeText(getContext(), "Giá tối đa phải >= 0", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Giá tối đa không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate price range
        if (min != null && max != null && min > max) {
            Toast.makeText(getContext(), "Giá tối thiểu không được lớn hơn giá tối đa", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progress.setVisibility(View.VISIBLE);
        HomeRepository repo = new HomeRepository(getContext(), BASE_URL);
        repo.getHomeListings(name.isEmpty() ? null : name, min, max).enqueue(new Callback<List<HomeListing>>() {
            @Override
            public void onResponse(Call<List<HomeListing>> call, Response<List<HomeListing>> response) {
                if (!isAdded() || getContext() == null) return;
                
                progress.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<HomeListing> listings = response.body();
                    Log.d("HomeFragment", "Listings received: " + listings.size() + " items");
                    
                    adapter.setItems(listings);
                    
                    if (listings.isEmpty()) {
                        Toast.makeText(getContext(), "🔍 Không tìm thấy thẻ phù hợp", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "✓ Tìm thấy " + listings.size() + " thẻ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e("HomeFragment", "Response error: " + response.code() + " " + errorBody);
                        Toast.makeText(getContext(), "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("HomeFragment", "Response error: " + response.code());
                        Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<HomeListing>> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                
                progress.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                Log.e("HomeFragment", "Network error", t);
                Toast.makeText(getContext(), "❌ Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
