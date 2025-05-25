package com.example.icusheba;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ImageView drawerButton, profileButton, locationButton;
    SearchView search_hospital;
    FirebaseAuth sAuth;
    private RecyclerView recyclerView;
    private HospitalAdapter hospitalAdapter;

    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        drawerLayout = findViewById(R.id.drawer_Layout);
        drawerButton = findViewById(R.id.buttonDrawerToggle);
        profileButton = findViewById(R.id.button_profile);
        locationButton = findViewById(R.id.button_location);
        search_hospital = findViewById(R.id.HospitalSearch);
        recyclerView = findViewById(R.id.hospitalRecyclerView);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // ✅ Clean and safe way to access the search text field
        EditText searchEditText = search_hospital.findViewById(androidx.appcompat.R.id.search_src_text);

        if (searchEditText != null) {
            searchEditText.setTextColor(ContextCompat.getColor(this, R.color.white));
            searchEditText.setHintTextColor(ContextCompat.getColor(this, R.color.white));
        }



    // Header view bindings
        View headerView = navigationView.getHeaderView(0);
        TextView usernameView = headerView.findViewById(R.id.UsernameView);
        ImageView userImageView = headerView.findViewById(R.id.UserImage);

        FirebaseFirestore.getInstance().collection("Users")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("Full Name");
                        if (username != null && !username.isEmpty()) {
                            usernameView.setText(username);
                        }

                        String imageUrl = documentSnapshot.getString("imageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(HomeActivity.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_baseline_person_24)
                                    .into(userImageView);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(HomeActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show());

        // Firebase init
        sAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        setupRecyclerView("");

        // Drawer button
        drawerButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Profile button popup
        profileButton.setOnClickListener(v -> showPopupMenu());

        // Location button
        locationButton.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, LocationActivity.class));
            finish();
        });

        // Search hospital
        search_hospital.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                setupRecyclerView(query != null ? query.trim() : "");
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                setupRecyclerView(newText != null ? newText.trim() : "");
                return true;
            }
        });

        // Drawer item click listener
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();

            if (id == R.id.profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            } else if (id == R.id.home) {
                Toast.makeText(HomeActivity.this, "Already on Home", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.settings) {
                Toast.makeText(HomeActivity.this, "Settings clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.bookingInfo) {
                startActivity(new Intent(HomeActivity.this, BookingConfirmation.class));
                Toast.makeText(HomeActivity.this, "AllBookingInfo", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.AllBookingInfo) {
                startActivity(new Intent(HomeActivity.this, ContractActivity.class));
                Toast.makeText(HomeActivity.this, "AllBookingInfo", Toast.LENGTH_SHORT).show();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void setupRecyclerView(String searchText) {
        String search = searchText.toUpperCase();  // **এখানে কেস কনভার্ট করা হয়েছে**

        Query query;
        if (search.isEmpty()) {
            query = fStore.collection("Hospital_Data").orderBy("Hospital_Name");
        } else {
            query = fStore.collection("Hospital_Data")
                    .orderBy("Hospital_Name")
                    .startAt(search)
                    .endAt(search + "\uf8ff");
        }

        FirestoreRecyclerOptions<Hospital> options =
                new FirestoreRecyclerOptions.Builder<Hospital>()
                        .setQuery(query, Hospital.class)
                        .build();

        if (hospitalAdapter != null) {
            hospitalAdapter.updateOptions(options);
            hospitalAdapter.startListening();
        } else {
            hospitalAdapter = new HospitalAdapter(options);
            recyclerView.setAdapter(hospitalAdapter);

            hospitalAdapter.setOnItemClickListener(hospital -> {
                if (hospital != null) {
                    Intent intent = new Intent(HomeActivity.this, ICU_Activity.class);
                    intent.putExtra("hospitalName", hospital.getHospital_Name());
                    intent.putExtra("hospitalAddress", hospital.getHospital_Address());
                    intent.putExtra("hospitalNumber", hospital.getHospital_Number());
                    intent.putExtra("hospitalImage", hospital.getImageUrl());
                    startActivity(intent);
                } else {
                    Toast.makeText(HomeActivity.this, "Hospital data missing!", Toast.LENGTH_SHORT).show();
                }
            });

            hospitalAdapter.startListening();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (hospitalAdapter != null) hospitalAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (hospitalAdapter != null) hospitalAdapter.stopListening();
    }

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(HomeActivity.this, profileButton);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.button_logout) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(HomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                finish();
                return true;
            }else if (item.getItemId() == R.id.button_changepassword) {
                showChangePasswordDialog(); 
                return true;
            }else if (item.getItemId() == R.id.button_share) {
                shareApp();
                return true;

            } else if (item.getItemId() == R.id.button_about) {
                showAboutDialog();
                return true;
            }

            return false;
        });
        popupMenu.show();
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About This App");
        builder.setMessage("ICU Sheba\n\nVersion: 1.0.0\nDeveloped by:Student Of European University of Bangladesh (EUB)\n Department Of CSE \n Batch: 21 \n\nThis app helps users find and book ICU seats in hospitals.");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    private void shareApp() {
        String shareText = "Check out this ICU Booking App:\nhttps://play.google.com/store/apps/details?id=" + getPackageName();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "ICU Booking App");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }


    private void showChangePasswordDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.change_password_dialog, null);

        EditText oldPassword = view.findViewById(R.id.oldPassword);
        EditText newPassword = view.findViewById(R.id.newPassword);
        EditText confirmPassword = view.findViewById(R.id.confirmPassword);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setTitle("Change Password");
        builder.setPositiveButton("Change", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button changeButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            changeButton.setOnClickListener(v -> {
                String oldPwd = oldPassword.getText().toString().trim();
                String newPwd = newPassword.getText().toString().trim();
                String confirmPwd = confirmPassword.getText().toString().trim();

                if (oldPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
                    Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPwd.equals(confirmPwd)) {
                    Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null || user.getEmail() == null) {
                    Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPwd);
                user.reauthenticate(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.updatePassword(newPwd).addOnCompleteListener(passwordTask -> {
                            if (passwordTask.isSuccessful()) {
                                // ✅ Firestore এ Password ফিল্ড আপডেট
                                FirebaseFirestore.getInstance()
                                        .collection("Users")
                                        .document(user.getUid())
                                        .update("Password", newPwd)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Password changed but Firestore update failed", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        });
                            } else {
                                Toast.makeText(this, "Password update failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        dialog.show();
    }
}
