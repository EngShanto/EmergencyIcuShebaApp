package com.example.icusheba;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Comparator;
import java.util.List;

public class BookingConfirmation extends AppCompatActivity {

    TextView bookingInfoText, btn;
    FirebaseFirestore db;
    String hospitalName;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);

        bookingInfoText = findViewById(R.id.booking_info_text);
        btn = findViewById(R.id.booking_listBtn);
        db = FirebaseFirestore.getInstance();

        hospitalName = getIntent().getStringExtra("hospital");
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getEmail()
                : null;

        if (currentUserEmail == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            bookingInfoText.setText("No user found.");
            return;
        }

        if (hospitalName != null) {
            // ✅ Use hospitalName from intent if present
            loadLatestBooking(hospitalName, currentUserEmail);
        } else {
            // ⚠️ No hospital passed → search all hospitals and find latest
            fetchLatestBookingAcrossAllHospitals(currentUserEmail);
        }

        btn.setOnClickListener(v -> {
            Intent intent = new Intent(BookingConfirmation.this, ContractActivity.class);
            if (hospitalName != null) {
                intent.putExtra("hospital", hospitalName);
            }
            startActivity(intent);
        });
    }

    private void loadLatestBooking(String hospitalName, String email) {
        db.collection("BOOKING_DATA_COLLECTION")
                .document(hospitalName)
                .collection("Pending")
                .whereEqualTo("Email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();

                    if (!docs.isEmpty()) {
                        DocumentSnapshot latestDoc = docs.stream()
                                .filter(doc -> doc.contains("timestamp"))
                                .max(Comparator.comparing(doc -> doc.getTimestamp("timestamp")))
                                .orElse(docs.get(0));

                        displayBooking(latestDoc);
                    } else {
                        bookingInfoText.setText("No booking found for this hospital.");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch booking: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    bookingInfoText.setText("Error retrieving booking info.");
                });
    }

    private void fetchLatestBookingAcrossAllHospitals(String email) {
        db.collection("BOOKING_DATA_COLLECTION")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<DocumentSnapshot> hospitalDocs = snapshot.getDocuments();

                    if (hospitalDocs.isEmpty()) {
                        bookingInfoText.setText("No hospital data found.");
                        return;
                    }

                    // Loop through all hospitals and find latest booking
                    final DocumentSnapshot[] latestDocHolder = {null};
                    final String[] latestHospitalName = {null};

                    final int[] remaining = {hospitalDocs.size()};

                    for (DocumentSnapshot hospitalDoc : hospitalDocs) {
                        String hName = hospitalDoc.getId();

                        db.collection("BOOKING_DATA_COLLECTION")
                                .document(hName)
                                .collection("Pending")
                                .whereEqualTo("Email", email)
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(pendingSnapshot -> {
                                    if (!pendingSnapshot.isEmpty()) {
                                        DocumentSnapshot doc = pendingSnapshot.getDocuments().get(0);
                                        if (latestDocHolder[0] == null ||
                                                doc.getTimestamp("timestamp") != null &&
                                                        (latestDocHolder[0].getTimestamp("timestamp") == null ||
                                                                doc.getTimestamp("timestamp").compareTo(latestDocHolder[0].getTimestamp("timestamp")) > 0)) {
                                            latestDocHolder[0] = doc;
                                            latestHospitalName[0] = hName;
                                        }
                                    }

                                    remaining[0]--;
                                    if (remaining[0] == 0) {
                                        if (latestDocHolder[0] != null) {
                                            hospitalName = latestHospitalName[0]; // save for button intent
                                            displayBooking(latestDocHolder[0]);
                                        } else {
                                            bookingInfoText.setText("No recent booking found.");
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    remaining[0]--;
                                    if (remaining[0] == 0 && latestDocHolder[0] == null) {
                                        bookingInfoText.setText("No recent booking found.");
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading hospital list", Toast.LENGTH_SHORT).show();
                    bookingInfoText.setText("Error loading hospitals.");
                });
    }

    @SuppressLint("SetTextI18n")
    private void displayBooking(DocumentSnapshot doc) {
        String info = ""
                + "Name: " + doc.getString("Name") + "\n"
                + "Email: " + doc.getString("Email") + "\n"
                + "Hospital: " + doc.getString("Hospital_Name") + "\n"
                + "Address: " + doc.getString("Hospital_Address") + "\n"
                + "Seat Type: " + doc.getString("Seat_Type") + "\n"
                + "Admission Date: " + doc.getString("Date") + "\n"
                + "Admission Time: " + doc.getString("Time") + "\n"
                + "Fee: Cons Fees: " + doc.getString("Bed_Fee") + "/-\n"
                + "Number of Beds: " + doc.getString("Number_Of_Seat") + "\n"
                + "Status: " + doc.getString("Status") + "\n"
                + "Hospital Number: " + doc.getString("Hospital_Number");

        bookingInfoText.setText(info);
    }
}
