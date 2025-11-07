package com.example.assignment.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.assignment.R;
import com.example.assignment.data.repository.PaymentRepository;
import com.example.assignment.data.api.RetrofitClient;
import com.example.assignment.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentHistoryFragment extends Fragment {
    private static final String BASE_URL = "http://10.0.2.2:8080";

    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvEmpty;

    private PaymentRepository paymentRepository;
    private SessionManager sessionManager;
    private PaymentHistoryAdapter adapter;
    private List<Map<String, Object>> payments = new ArrayList<>();

    private int currentTab = 0; // 0 = purchases, 1 = sales

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRepositories();
        setupRecyclerView();
        setupTabLayout();
        setupSwipeRefresh();

        loadPayments();
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tabLayout);
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        tvEmpty = view.findViewById(R.id.tvEmpty);
    }

    private void setupRepositories() {
        paymentRepository = new PaymentRepository(RetrofitClient.getInstance(BASE_URL).getApi());
        sessionManager = new SessionManager(getContext());
    }

    private void setupRecyclerView() {
        adapter = new PaymentHistoryAdapter(payments, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Đã mua"));
        tabLayout.addTab(tabLayout.newTab().setText("Đã bán"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                loadPayments();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(this::loadPayments);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
    }

    private void loadPayments() {
        String token = "Bearer " + sessionManager.fetchToken();

        Call<Map<String, Object>> call;
        if (currentTab == 0) {
            call = paymentRepository.getMyPurchases(token);
        } else {
            // getMySales is disabled for VNPay
            Toast.makeText(getContext(), "Chức năng bán thẻ tạm thời không khả dụng với VNPay", Toast.LENGTH_SHORT).show();
            swipeRefresh.setRefreshing(false);
            return;
            // call = paymentRepository.getMySales(token);
        }

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    handlePaymentsResponse(response.body());
                } else {
                    showError("Không thể tải lịch sử giao dịch");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                showError("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void handlePaymentsResponse(Map<String, Object> response) {
        try {
            Boolean success = (Boolean) response.get("success");
            if (success != null && success) {
                List<Map<String, Object>> paymentList = (List<Map<String, Object>>) response.get("payments");
                if (paymentList != null) {
                    payments.clear();
                    payments.addAll(paymentList);
                    adapter.notifyDataSetChanged();

                    updateEmptyState();
                }
            }
        } catch (Exception e) {
            showError("Lỗi xử lý dữ liệu");
        }
    }

    private void updateEmptyState() {
        if (payments.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText(currentTab == 0 ? "Chưa có giao dịch mua nào" : "Chưa có giao dịch bán nào");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
