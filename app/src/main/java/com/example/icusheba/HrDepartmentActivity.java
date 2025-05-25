package com.example.icusheba;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HrDepartmentActivity extends AppCompatActivity {

    TextView tv, name1, address1, number1, update_Button, booking_info;
    ImageView profileBtn;
    FirebaseFirestore fStore;
    FirebaseAuth sAuth;
    String hospitalName;
    private RecyclerView recyclerView;
    private BookingAdapter bookingAdapter;
    private List<Booking> bookingList;
    SearchView searchButton;
    private ListenerRegistration hospitalDataListener;
    String userId;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hr_department);

        tv = findViewById(R.id.Hospital_Tittle);
        name1 = findViewById(R.id.Name1);
        address1 = findViewById(R.id.Address1);
        number1 = findViewById(R.id.Number1);
        profileBtn = findViewById(R.id.button_profile);
        searchButton = findViewById(R.id.Booking_Search);
        SearchView searchView = findViewById(R.id.Booking_Search);
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(ContextCompat.getColor(this, R.color.white));
        searchEditText.setHintTextColor(ContextCompat.getColor(this, R.color.white));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            searchEditText.setTextCursorDrawable(R.drawable.cursor_colore);
        }
        update_Button = findViewById(R.id.Update_Button);
        booking_info = findViewById(R.id.Booking_info);

        fStore = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bookingList = new ArrayList<>();
        bookingAdapter = new BookingAdapter(this, bookingList, hospitalName);
        recyclerView.setAdapter(bookingAdapter);

        sAuth = FirebaseAuth.getInstance();
        if (sAuth.getCurrentUser() != null) {
            userId = sAuth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Fetch the user's hospital name and set it to tv
        DocumentReference documentReference = fStore.collection("Users").document(userId);
        @SuppressLint("SetTextI18n") ListenerRegistration userListListener = documentReference.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error fetching user data", error);
                Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null && value.exists()) {
                this.hospitalName = value.getString("Hospital");

                if (hospitalName == null || hospitalName.trim().isEmpty()) {
                    Log.e("HrDepartmentActivity", "Hospital name is empty");
                    tv.setText("No hospital assigned");
                    return;
                }

                Log.d("HrDepartmentActivity", "User's hospitalName: " + hospitalName);
                tv.setText(hospitalName);

                HospitalData(hospitalName);
                fetchBookings(hospitalName);
            }
        });

        profileBtn.setOnClickListener(this::showPopupMenu);

        searchButton.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Optionally handle submit
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterData(newText);
                return true;
            }
        });

        update_Button.setOnClickListener(view -> {
            Intent intent = new Intent(HrDepartmentActivity.this, UpdateActivity.class);
            intent.putExtra("hospitalName", hospitalName);
            startActivity(intent);
        });

        booking_info.setOnClickListener(view -> {
            Intent intent = new Intent(HrDepartmentActivity.this, BookingInfoActivity.class);
            intent.putExtra("hospitalName", hospitalName);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(HrDepartmentActivity.this, profileBtn);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.button_logout) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(HrDepartmentActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(HrDepartmentActivity.this, LoginActivity.class));
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

    private void filterData(String newText) {
        List<Booking> filteredList = new ArrayList<>();

        for (Booking booking : bookingAdapter.getFullBookingList()) {
            if (booking.getName() != null && booking.getName().toLowerCase().contains(newText.toLowerCase())) {
                filteredList.add(booking);
            }
        }

        bookingAdapter.updateList(filteredList);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchBookings(String hospitalName) {
        if (hospitalName == null || hospitalName.trim().isEmpty()) {
            return;
        }

        Log.d("HrDepartmentActivity", "Fetching pending bookings for hospital: " + hospitalName);

        fStore.collection("BOOKING_DATA_COLLECTION")
                .document(hospitalName)
                .collection("Pending")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            Log.d("HrDepartmentActivity", "No pending bookings found for hospital: " + hospitalName);
                            Toast.makeText(this, "No pending bookings found.", Toast.LENGTH_SHORT).show();
                        } else {
                            bookingList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Firestore", "Document data: " + document.getData());

                                Booking booking = document.toObject(Booking.class);
                                booking.setId(document.getId());

                                bookingList.add(booking);
                            }
                            bookingAdapter = new BookingAdapter(this, bookingList, hospitalName);
                            recyclerView.setAdapter(bookingAdapter);
                            bookingAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.w("HrDepartmentActivity", "Error getting documents.", task.getException());
                        Toast.makeText(this, "Error fetching bookings", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void HospitalData(String hospitalName) {
        if (hospitalName == null || hospitalName.trim().isEmpty()) {
            return;
        }

        CollectionReference hospitalRef = fStore.collection("Hospital_Data");

        hospitalRef.whereEqualTo("Hospital_Name", hospitalName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            String name = document.getString("Hospital_Name");
                            String address = document.getString("Hospital_Address");
                            String number = document.getString("Hospital_Number");

                            name1.setText(name);
                            address1.setText(address);
                            number1.setText(number);
                        } else {
                            Log.e("Firestore", "No data found for hospital: " + hospitalName);
                            Toast.makeText(this, "No data found for this hospital.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("Firestore", "Error fetching hospital data", task.getException());
                        Toast.makeText(this, "Error fetching hospital data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
