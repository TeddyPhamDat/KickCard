package com.example.assignment.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment.R;
import com.example.assignment.data.model.Card;
import com.example.assignment.data.model.User;
import com.example.assignment.data.repository.AdminRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminFragment extends Fragment {
    private Button btnPendingCards, btnUsers, btnAdminInventory;
    private RecyclerView recycler;
    private AdminCardAdapter adapter;
    private AdminRepository repo;
    private static final String BASE_URL = "http://10.0.2.2:8080";
    private enum Mode { CARDS, USERS }
    private Mode currentMode = Mode.CARDS;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    btnPendingCards = view.findViewById(R.id.btnPendingCards);
    btnUsers = view.findViewById(R.id.btnUsers);
    btnAdminInventory = view.findViewById(R.id.btnAdminInventory);
        recycler = view.findViewById(R.id.recyclerAdmin);

        adapter = new AdminCardAdapter(new AdminCardAdapter.Listener() {
            @Override
            public void onApprove(Object item) { handleApprove(item); }

            @Override
            public void onReject(Object item) { handleReject(item); }

            @Override
            public void onEdit(Object item) { handleEdit(item); }

            @Override
            public void onDelete(Object item) { handleDelete(item); }
        });

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);

        repo = new AdminRepository(getContext(), BASE_URL);

        btnPendingCards.setOnClickListener(v -> loadPendingCards());
        btnUsers.setOnClickListener(v -> loadUsers());
        btnAdminInventory.setOnClickListener(v -> openAdminInventory());
        // load pending cards by default (backend listings endpoint returned 500 in logs)
        loadPendingCards();
    }

    private void openAdminInventory() {
        // Replace fragment with InventoryFragment for admin
        requireActivity().getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragmentContainer, new InventoryFragment())
            .addToBackStack(null)
            .commit();
    }

    // listings admin endpoints are not present on the backend; we only support card/user admin APIs

    private void loadPendingCards() {
        currentMode = Mode.CARDS;
        repo.getPendingCards().enqueue(new Callback<java.util.List<Card>>() {
            @Override
            public void onResponse(Call<java.util.List<Card>> call, Response<java.util.List<Card>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Object> list = new ArrayList<>(response.body());
                    adapter.setItems(list);
                } else {
                    String msg = "Failed to load pending cards (" + response.code() + ")";
                    safeToast(msg);
                    android.util.Log.w("AdminFragment", msg + " - " + (response.errorBody() == null ? "no body" : response.message()));
                }
            }

            @Override
            public void onFailure(Call<java.util.List<Card>> call, Throwable t) { safeToast("Network error: " + t.getMessage()); android.util.Log.w("AdminFragment", "loadPendingCards failure", t); }
        });
    }

    private void loadUsers() {
        currentMode = Mode.USERS;
        repo.listUsers().enqueue(new Callback<java.util.List<User>>() {
            @Override
            public void onResponse(Call<java.util.List<User>> call, Response<java.util.List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Object> list = new ArrayList<>(response.body());
                    adapter.setItems(list);
                } else {
                    String msg = "Failed to load users (" + response.code() + ")";
                    safeToast(msg);
                    android.util.Log.w("AdminFragment", msg + " - " + (response.errorBody() == null ? "no body" : response.message()));
                }
            }

            @Override
            public void onFailure(Call<java.util.List<User>> call, Throwable t) { safeToast("Network error: " + t.getMessage()); android.util.Log.w("AdminFragment", "loadUsers failure", t); }
        });
    }

    private void handleApprove(Object item) {
        if (item instanceof Card) {
            repo.approveCard(((Card) item).getId()).enqueue(new Callback<java.util.Map<String, String>>() {
                @Override
                public void onResponse(Call<java.util.Map<String, String>> call, Response<java.util.Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        removeItemFromAdapter(item);
                        safeToast("Approved card");
                    } else {
                        safeToast("Approve failed (" + response.code() + ")");
                    }
                }

                @Override
                public void onFailure(Call<java.util.Map<String, String>> call, Throwable t) {
                    safeToast("Network error: " + t.getMessage());
                }
            });
        }
    }

    private void handleReject(Object item) {
        View v = getLayoutInflater().inflate(R.layout.dialog_admin_reject, null);
        EditText etReason = v.findViewById(R.id.etReason);
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Reject with reason")
                .setView(v)
                .setPositiveButton("Reject", (d, w) -> {
                    String reason = etReason.getText().toString().trim();
                    if (item instanceof Card) {
                        repo.rejectCard(((Card) item).getId(), reason).enqueue(new Callback<java.util.Map<String, String>>() {
                            @Override
                            public void onResponse(Call<java.util.Map<String, String>> call, Response<java.util.Map<String, String>> response) {
                                if (response.isSuccessful()) {
                                    removeItemFromAdapter(item);
                                    safeToast("Rejected card");
                                } else {
                                    safeToast("Reject failed (" + response.code() + ")");
                                }
                            }

                            @Override
                            public void onFailure(Call<java.util.Map<String, String>> call, Throwable t) {
                                safeToast("Network error: " + t.getMessage());
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // User edit/delete
    private void handleEditUser(Object item) {
        if (!(item instanceof User)) return;
        User u = (User) item;
        View v = getLayoutInflater().inflate(R.layout.dialog_admin_edit_user, null);
        EditText etFullname = v.findViewById(R.id.etFullname);
        EditText etRole = v.findViewById(R.id.etRole);
        etFullname.setText(u.getFullname());
        etRole.setText(u.getRole());
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Edit User")
                .setView(v)
                .setPositiveButton("Save", (d, w) -> {
                    u.setFullname(etFullname.getText().toString().trim());
                    u.setRole(etRole.getText().toString().trim());
                    repo.updateUser(u.getId(), u).enqueue(simpleCallback("User updated"));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleDeleteUser(Object item) {
        if (!(item instanceof User)) return;
        User u = (User) item;
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Delete User")
                .setMessage("Delete user " + (u.getFullname() != null ? u.getFullname() : u.getUsername()) + "?")
                .setPositiveButton("Delete", (d, w) -> {
                    int idx = removeItemFromAdapter(item);
                    final Call<java.util.Map<String, String>> call = repo.deleteUser(u.getId());
                    call.enqueue(new Callback<java.util.Map<String, String>>() {
                        @Override
                        public void onResponse(Call<java.util.Map<String, String>> call, Response<java.util.Map<String, String>> response) {
                            if (response.isSuccessful()) safeToast("User deleted"); else { safeToast("Delete failed (" + response.code() + ")"); restoreItemToAdapter(item, idx); }
                        }

                        @Override
                        public void onFailure(Call<java.util.Map<String, String>> call, Throwable t) { if (call.isCanceled()) { android.util.Log.d("AdminFragment","delete user call canceled"); } else { safeToast("Network error: " + t.getMessage()); restoreItemToAdapter(item, idx); } }
                    });

                    com.google.android.material.snackbar.Snackbar sb = com.google.android.material.snackbar.Snackbar.make(recycler, "User deleted", com.google.android.material.snackbar.Snackbar.LENGTH_LONG);
                    sb.setAction("Undo", v1 -> {
                        if (!call.isExecuted() || (call.isExecuted() && !call.isCanceled())) call.cancel();
                        restoreItemToAdapter(item, idx);
                    });
                    sb.show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleEdit(Object item) {
        if (item instanceof User) { handleEditUser(item); return; }
        if (!(item instanceof Card)) return;
        Card c = (Card) item;
        View v = getLayoutInflater().inflate(R.layout.dialog_admin_edit_card, null);
        EditText etName = v.findViewById(R.id.etName);
        EditText etTeam = v.findViewById(R.id.etTeam);
        android.widget.Spinner spStatus = v.findViewById(R.id.spStatus);
        etName.setText(c.getName());
        etTeam.setText(c.getTeam());
        String[] statuses = new String[] {"PENDING", "APPROVED", "REJECTED"};
        android.widget.ArrayAdapter<String> aa = new android.widget.ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, statuses);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatus.setAdapter(aa);
        int sel = 0;
        for (int i = 0; i < statuses.length; i++) if (statuses[i].equalsIgnoreCase(c.getStatus())) { sel = i; break; }
        spStatus.setSelection(sel);

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Edit Card")
                .setView(v)
                .setPositiveButton("Save", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String team = etTeam.getText().toString().trim();
                    String status = spStatus.getSelectedItem().toString();
                    c.setName(name);
                    c.setTeam(team);
                    c.setStatus(status);
                    repo.updateCard(c.getId(), c).enqueue(simpleCallback("Card updated"));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleDelete(Object item) {
        if (item instanceof User) { handleDeleteUser(item); return; }
        if (!(item instanceof Card)) return;
        Card c = (Card) item;
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Delete Card")
                .setMessage("Delete card " + c.getName() + "?")
                .setPositiveButton("Delete", (d, w) -> {
                    int idx = removeItemFromAdapter(item);
                    final Call<java.util.Map<String, String>> call = repo.deleteCard(c.getId());
                    call.enqueue(new Callback<java.util.Map<String, String>>() {
                        @Override
                        public void onResponse(Call<java.util.Map<String, String>> call, Response<java.util.Map<String, String>> response) {
                            if (response.isSuccessful()) {
                                safeToast("Card deleted");
                            } else {
                                safeToast("Delete failed (" + response.code() + ")");
                                restoreItemToAdapter(item, idx);
                            }
                        }

                        @Override
                        public void onFailure(Call<java.util.Map<String, String>> call, Throwable t) {
                            if (call.isCanceled()) {
                                // user undid deletion locally
                                android.util.Log.d("AdminFragment", "delete call canceled by undo");
                            } else {
                                safeToast("Network error: " + t.getMessage());
                                restoreItemToAdapter(item, idx);
                            }
                        }
                    });

                    // show undo snackbar
                    com.google.android.material.snackbar.Snackbar sb = com.google.android.material.snackbar.Snackbar.make(recycler, "Card deleted", com.google.android.material.snackbar.Snackbar.LENGTH_LONG);
                    sb.setAction("Undo", v1 -> {
                        // cancel network call if still running and restore item
                        if (!call.isExecuted() || (call.isExecuted() && !call.isCanceled())) {
                            call.cancel();
                        }
                        restoreItemToAdapter(item, idx);
                    });
                    sb.show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private <T> Callback<T> simpleCallback(String successToast) {
        return new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful()) {
                    safeToast(successToast);
                    // refresh current view - reload the current tab only
                    refreshCurrentView();
                } else {
                    String msg = "Operation failed (" + response.code() + ")";
                    safeToast(msg);
                    android.util.Log.w("AdminFragment", msg + " - " + (response.errorBody() == null ? "no body" : response.message()));
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) { safeToast("Network error: " + t.getMessage()); android.util.Log.w("AdminFragment", "operation failure", t); }
        };
    }

    // helper to avoid Toast NPE when fragment is detached
    private void safeToast(String msg) {
        if (getContext() != null && isAdded()) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        } else {
            android.util.Log.w("AdminFragment", "safeToast skipped: " + msg);
        }
    }

    private void refreshCurrentView() {
        // Reload the current admin view (cards or users)
        if (currentMode == Mode.USERS) {
            loadUsers();
        } else {
            loadPendingCards();
        }
    }

    // Optimistic UI helpers
    private int removeItemFromAdapter(Object item) {
        if (adapter != null) {
            try {
                return adapter.removeItem(item);
            } catch (Exception ex) { android.util.Log.w("AdminFragment", "removeItemFromAdapter failed", ex); }
        }
        return -1;
    }

    private void restoreItemToAdapter(Object item, int index) {
        if (adapter != null) {
            try {
                adapter.insertItem(item, index);
            } catch (Exception ex) { android.util.Log.w("AdminFragment", "restoreItemToAdapter failed", ex); refreshCurrentView(); }
        } else {
            refreshCurrentView();
        }
    }
}
