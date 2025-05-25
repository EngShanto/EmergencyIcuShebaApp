package com.example.icusheba;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ContractActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private ProgressBar loadingBar;

    private List<UserBooking> bookingList;
    private UserBookingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseFirestore.setLoggingEnabled(true);
        setContentView(R.layout.activity_contract);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerBookings);
        loadingBar = findViewById(R.id.loadingBar);

        bookingList = new ArrayList<>();
        adapter = new UserBookingAdapter(bookingList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadUserBookings();
    }

    private void loadUserBookings() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = currentUser.getEmail();
        loadingBar.setVisibility(View.VISIBLE);
        bookingList.clear();

        String[] statusCollections = {"Approved", "Pending", "Rejected", "Completed"};

        for (String status : statusCollections) {
            db.collectionGroup(status)
                    .whereEqualTo("Email", userEmail)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            UserBooking booking = doc.toObject(UserBooking.class);
                            if (booking != null) {
                                bookingList.add(booking);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        loadingBar.setVisibility(View.GONE);

                        if (bookingList.isEmpty()) {
                            Toast.makeText(this, "No bookings found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        loadingBar.setVisibility(View.GONE);
                        Log.e("FIRESTORE_ERROR", "Error loading bookings", e);
                        Toast.makeText(this, "Error loading bookings: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }
}
