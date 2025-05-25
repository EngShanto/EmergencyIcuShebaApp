package com.example.icusheba;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

import java.util.*;

public class UpdateActivity extends AppCompatActivity {

    TextView textCurrentName, textCurrentAddress, textCurrentNumber;
    EditText editNewName, editNewAddress, editNewNumber;
    Button UpdateName, UpdateAddress, UpdateNumber;
    TableLayout seatLayout, totalSeatLayout;

    FirebaseFirestore fStore;
    FirebaseAuth sAuth;
    String hospitalId;
    String userId;

    Map<String, Object> seatTypesData = new HashMap<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        textCurrentName = findViewById(R.id.textCurrentName);
        textCurrentAddress = findViewById(R.id.textCurrentAddress);
        textCurrentNumber = findViewById(R.id.textCurrentNumber);

        editNewName = findViewById(R.id.editNewName);
        editNewAddress = findViewById(R.id.editNewAddress);
        editNewNumber = findViewById(R.id.editNewNumber);

        UpdateName = findViewById(R.id.btnUpdateName);
        UpdateAddress = findViewById(R.id.btnUpdateAddress);
        UpdateNumber = findViewById(R.id.btnUpdateNumber);

        seatLayout = findViewById(R.id.seatLayout);
        totalSeatLayout = findViewById(R.id.TotalSeatLayout);

        fStore = FirebaseFirestore.getInstance();
        sAuth = FirebaseAuth.getInstance();

        if (sAuth.getCurrentUser() != null) {
            userId = sAuth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        DocumentReference userRef = fStore.collection("Users").document(userId);
        userRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "User data error", Toast.LENGTH_SHORT).show();
                return;
            }
            if (value != null && value.exists()) {
                String hospitalName = value.getString("Hospital");
                if (hospitalName != null && !hospitalName.trim().isEmpty()) {
                    fetchHospitalData(hospitalName);
                }
            }
        });

        UpdateName.setOnClickListener(v ->
                showPasswordPromptAndUpdate("Hospital_Name", editNewName.getText().toString().trim(), editNewName));
        UpdateAddress.setOnClickListener(v ->
                showPasswordPromptAndUpdate("Hospital_Address", editNewAddress.getText().toString().trim(), editNewAddress));
        UpdateNumber.setOnClickListener(v ->
                showPasswordPromptAndUpdate("Hospital_Number", editNewNumber.getText().toString().trim(), editNewNumber));
    }

    private void fetchHospitalData(String hospitalName) {
        fStore.collection("Hospital_Data")
                .whereEqualTo("Hospital_Name", hospitalName)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        hospitalId = doc.getId();

                        textCurrentName.setText(doc.getString("Hospital_Name"));
                        textCurrentAddress.setText(doc.getString("Hospital_Address"));
                        textCurrentNumber.setText(doc.getString("Hospital_Number"));

                        seatTypesData.clear();
                        //noinspection unchecked
                        Map<String, Object> seatTypeMap = (Map<String, Object>) doc.get("Seat Type");
                        if (seatTypeMap != null) {
                            for (Map.Entry<String, Object> entry : seatTypeMap.entrySet()) {
                                if (entry.getValue() instanceof Map) {
                                    //noinspection unchecked
                                    Map<String, Object> seatDetail = (Map<String, Object>) entry.getValue();
                                    if (seatDetail.containsKey("available")) {
                                        seatTypesData.put(entry.getKey(), seatDetail.get("available"));
                                    }
                                }
                            }
                        }

                        displaySeatTypeUI();
                        displayTotalSeatUI();
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void displaySeatTypeUI() {
        seatLayout.removeAllViews();
        seatLayout.setBackgroundColor(0xFFFFFFFF);

        for (Map.Entry<String, Object> entry : seatTypesData.entrySet()) {
            String type = entry.getKey();
            String available = entry.getValue().toString();

            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            row.setPadding(0, 10, 0, 10);

            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(type);
            row.addView(checkBox);

            TextView availableSeat = new TextView(this);
            availableSeat.setText("Available:" + available);
            availableSeat.setTextSize(12);
            availableSeat.setPadding(10, 10, 10, 10);
            availableSeat.setVisibility(View.GONE);
            row.addView(availableSeat);

            EditText newSeatInput = new EditText(this);
            newSeatInput.setHint("New Seat");
            newSeatInput.setTextSize(14);
            newSeatInput.setInputType(InputType.TYPE_CLASS_NUMBER);
            newSeatInput.setBackground(null);
            newSeatInput.setPadding(10, 10, 10, 10);
            newSeatInput.setVisibility(View.GONE);
            row.addView(newSeatInput);

            Button updateButton = new Button(this);
            updateButton.setText("Update");
            updateButton.setTextColor(0xFFFFFFFF);
            updateButton.setAllCaps(false);
            updateButton.setPadding(20, 10, 20, 10);
            updateButton.setVisibility(View.GONE);
            updateButton.setBackgroundResource(R.drawable.button_colore);

            TableRow.LayoutParams buttonParams = new TableRow.LayoutParams(dpToPx(90), dpToPx(40));
            buttonParams.setMargins(10, 0, 0, 0);
            updateButton.setLayoutParams(buttonParams);

            row.addView(updateButton);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int visibility = isChecked ? View.VISIBLE : View.GONE;
                availableSeat.setVisibility(visibility);
                newSeatInput.setVisibility(visibility);
                updateButton.setVisibility(visibility);
            });

            updateButton.setOnClickListener(v -> {
                String newVal = newSeatInput.getText().toString().trim();
                if (newVal.isEmpty()) {
                    Toast.makeText(this, "Enter seat value for " + type, Toast.LENGTH_SHORT).show();
                } else {
                    showPasswordPromptAndUpdate("Seat Type." + type + ".available", newVal, newSeatInput);
                }
            });

            seatLayout.addView(row);
        }
    }

    @SuppressLint("SetTextI18n")
    private void displayTotalSeatUI() {
        totalSeatLayout.removeAllViews();
        totalSeatLayout.setBackgroundColor(0xFFFFFFFF);

        for (Map.Entry<String, Object> entry : seatTypesData.entrySet()) {
            String type = entry.getKey();

            fStore.collection("Hospital_Data").document(hospitalId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            //noinspection unchecked
                            Map<String, Object> seatTypeMap = (Map<String, Object>) doc.get("Seat Type");
                            if (seatTypeMap != null && seatTypeMap.containsKey(type)) {
                                //noinspection unchecked
                                Map<String, Object> seatDetail = (Map<String, Object>) seatTypeMap.get(type);
                                assert seatDetail != null;
                                Object totalObj = seatDetail.get("total");
                                String total = totalObj != null ? totalObj.toString() : "0";

                                TableRow row = new TableRow(this);
                                row.setLayoutParams(new TableRow.LayoutParams(
                                        TableRow.LayoutParams.MATCH_PARENT,
                                        TableRow.LayoutParams.WRAP_CONTENT));
                                row.setPadding(0, 10, 0, 10);

                                CheckBox checkBox = new CheckBox(this);
                                checkBox.setText(type);
                                row.addView(checkBox);

                                TextView totalSeat = new TextView(this);
                                totalSeat.setText("Total:" + total);
                                totalSeat.setTextSize(12);
                                totalSeat.setPadding(10, 10, 10, 10);
                                totalSeat.setVisibility(View.GONE);
                                row.addView(totalSeat);

                                EditText newSeatInput = new EditText(this);
                                newSeatInput.setHint("New Seat");
                                newSeatInput.setTextSize(14);
                                newSeatInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                                newSeatInput.setBackground(null);
                                newSeatInput.setPadding(10, 10, 10, 10);
                                newSeatInput.setVisibility(View.GONE);
                                row.addView(newSeatInput);

                                Button updateButton = new Button(this);
                                updateButton.setText("Update");
                                updateButton.setTextColor(0xFFFFFFFF);
                                updateButton.setAllCaps(false);
                                updateButton.setPadding(20, 10, 20, 10);
                                updateButton.setVisibility(View.GONE);
                                updateButton.setBackgroundResource(R.drawable.button_colore);

                                TableRow.LayoutParams buttonParams = new TableRow.LayoutParams(dpToPx(90), dpToPx(40));
                                buttonParams.setMargins(10, 0, 0, 0);
                                updateButton.setLayoutParams(buttonParams);

                                row.addView(updateButton);

                                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                    int visibility = isChecked ? View.VISIBLE : View.GONE;
                                    totalSeat.setVisibility(visibility);
                                    newSeatInput.setVisibility(visibility);
                                    updateButton.setVisibility(visibility);
                                });

                                updateButton.setOnClickListener(v -> {
                                    String newVal = newSeatInput.getText().toString().trim();
                                    if (newVal.isEmpty()) {
                                        Toast.makeText(this, "Enter seat value for " + type, Toast.LENGTH_SHORT).show();
                                    } else {
                                        showPasswordPromptAndUpdate("Seat Type." + type + ".total", newVal, newSeatInput);
                                    }
                                });

                                totalSeatLayout.addView(row);
                            }
                        }
                    });
        }
    }

    private void showPasswordPromptAndUpdate(String field, String newValue, EditText editTextToClear) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Password");

        final EditText passwordInput = new EditText(this);
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(passwordInput);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String password = passwordInput.getText().toString().trim();
            reauthenticateAndUpdate(field, newValue, password, editTextToClear);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void reauthenticateAndUpdate(String field, String newValue, String password, EditText editTextToClear) {
        FirebaseUser user = sAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) return;

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
        user.reauthenticate(credential)
                .addOnSuccessListener(unused -> performUpdate(field, newValue, editTextToClear))
                .addOnFailureListener(e -> Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show());
    }

    private void performUpdate(String field, String newValue, EditText editTextToClear) {
        if (hospitalId == null) {
            Toast.makeText(this, "Hospital ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        fStore.collection("Hospital_Data").document(hospitalId)
                .update(field, newValue)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show();
                    editTextToClear.setText(""); // ✅ EditText
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
