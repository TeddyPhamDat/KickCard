package com.example.assignment.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.assignment.R;
import com.example.assignment.data.model.Listing;
import com.example.assignment.data.repository.ListingRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateListingActivity extends AppCompatActivity {
    private TextInputEditText etTitle, etPrice, etQty, etDesc;
    private MaterialButton btnCreate;
    private static final String BASE_URL = "http://10.0.2.2:8080";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_listing);

        etTitle = findViewById(R.id.etTitle);
        etPrice = findViewById(R.id.etPrice);
        etQty = findViewById(R.id.etQty);
        etDesc = findViewById(R.id.etDesc);
        btnCreate = findViewById(R.id.btnCreate);

        ListingRepository repo = new ListingRepository(this, BASE_URL);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String t = etTitle.getText().toString().trim();
                String p = etPrice.getText().toString().trim();
                String q = etQty.getText().toString().trim();
                String d = etDesc.getText().toString().trim();
                if (t.isEmpty() || p.isEmpty()) {
                    Toast.makeText(CreateListingActivity.this, "Title and price required", Toast.LENGTH_SHORT).show();
                    return;
                }
                Listing l = new Listing();
                l.setTitle(t);
                l.setPrice(Double.parseDouble(p));
                try { l.setQuantity(Integer.parseInt(q)); } catch (Exception ex) { l.setQuantity(1); }
                l.setDescription(d);
                repo.getApi().createListing(l).enqueue(new Callback<Listing>() {
                    @Override
                    public void onResponse(Call<Listing> call, Response<Listing> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(CreateListingActivity.this, "Created (pending approval)", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(CreateListingActivity.this, "Create failed", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Listing> call, Throwable t) {
                        Toast.makeText(CreateListingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
