package com.example.assignment.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment.R;
import com.example.assignment.data.model.Listing;
import com.example.assignment.presenter.ListingPresenter;

import java.util.ArrayList;
import java.util.List;

public class ListingFragment extends Fragment implements ListingPresenter.View {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ListingAdapter adapter;
    private ListingPresenter presenter;

    private static final String BASE_URL = "http://10.0.2.2:8080";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_listing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerListings);
        progressBar = view.findViewById(R.id.progressBarListings);

        adapter = new ListingAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        presenter = new ListingPresenter(this, getContext(), BASE_URL);
        presenter.loadListings(null);
    }

    @Override
    public void showLoading(boolean show) {
        if (getActivity() == null) return;
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onListingsLoaded(List<Listing> listings) {
        adapter.setItems(listings);
    }

    @Override
    public void onError(String message) {
        if (getActivity() == null) return;
        Toast.makeText(getActivity(), "Error: " + message, Toast.LENGTH_LONG).show();
    }
}
