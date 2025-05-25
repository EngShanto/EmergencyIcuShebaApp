package com.example.icusheba;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Add_New_Hospital extends AppCompatActivity {
    FirebaseFirestore db;
    EditText hospitalName, hospitalAddress, hospitalPhone;
    CheckBox checkManual, checkElectric, checkHighLow, checkPediatric, checkNeuro;
    EditText manualPrice, manualAvailable, manualTotal;
    EditText electricPrice, electricAvailable, electricTotal;
    EditText highLowPrice, highLowAvailable, highLowTotal;
    EditText pediatricPrice, pediatricAvailable, pediatricTotal;
    EditText neuroPrice, neuroAvailable, neuroTotal;
    Button submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_hospital);

        db = FirebaseFirestore.getInstance();

        hospitalName = findViewById(R.id.hospitalName);
        hospitalAddress = findViewById(R.id.hospitalAddress);
        hospitalPhone = findViewById(R.id.hospitalPhone);

        checkManual = findViewById(R.id.checkManualSeat);
        checkElectric = findViewById(R.id.checkElectricSeat);
        checkHighLow = findViewById(R.id.checkHigh_LawSeat);
        checkPediatric = findViewById(R.id.checkPediatricSeat);
        checkNeuro = findViewById(R.id.checkNeuroSeat);

        manualPrice = findViewById(R.id.manualSeatPrice);
        manualAvailable = findViewById(R.id.manualSeatAvailable);
        manualTotal = findViewById(R.id.manualSeatTotal);

        electricPrice = findViewById(R.id.electricSeatPrice);
        electricAvailable = findViewById(R.id.electricSeatAvailable);
        electricTotal = findViewById(R.id.electricSeatTotal);

        highLowPrice = findViewById(R.id.High_LawSeatPrice);
        highLowAvailable = findViewById(R.id.High_LawSeatAvailable);
        highLowTotal = findViewById(R.id.High_LawSeatTotal);

        pediatricPrice = findViewById(R.id.PediatricSeatPrice);
        pediatricAvailable = findViewById(R.id.PediatricSeatAvailable);
        pediatricTotal = findViewById(R.id.PediatricSeatTotal);

        neuroPrice = findViewById(R.id.NeuroSeatPrice);
        neuroAvailable = findViewById(R.id.NeuroSeatAvailable);
        neuroTotal = findViewById(R.id.NeuroSeatTotal);

        submitBtn = findViewById(R.id.submitBtn);

        setupVisibility(checkManual, manualPrice, manualTotal, manualAvailable);
        setupVisibility(checkElectric, electricPrice, electricTotal, electricAvailable);
        setupVisibility(checkHighLow, highLowPrice, highLowTotal, highLowAvailable);
        setupVisibility(checkPediatric, pediatricPrice, pediatricTotal, pediatricAvailable);
        setupVisibility(checkNeuro, neuroPrice, neuroTotal, neuroAvailable);

        submitBtn.setOnClickListener(v -> saveHospitalToFirestore());
    }

    private void setupVisibility(CheckBox checkBox, EditText priceField, EditText totalField, EditText availableField) {
        priceField.setVisibility(View.GONE);
        totalField.setVisibility(View.GONE);
        availableField.setVisibility(View.GONE);

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            priceField.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            totalField.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            availableField.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
    }

    private void saveHospitalToFirestore() {
        String name = hospitalName.getText().toString().trim();
        String address = hospitalAddress.getText().toString().trim();
        String phone = hospitalPhone.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill hospital name, address and phone", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!checkManual.isChecked() && !checkElectric.isChecked() &&
                !checkHighLow.isChecked() && !checkPediatric.isChecked() &&
                !checkNeuro.isChecked()) {
            Toast.makeText(this, "Please select at least one seat type", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> hospital = new HashMap<>();
        hospital.put("Hospital_Name", name);
        hospital.put("Hospital_Address", address);
        hospital.put("Hospital_Number", phone);

        Map<String, Map<String, Object>> beds = new HashMap<>();

        if (checkManual.isChecked()) {
            if (!validateBedFields(manualPrice, manualTotal, manualAvailable, "Manual")) return;
            beds.put("Manual", createBedMap(manualPrice, manualTotal, manualAvailable));
        }

        if (checkElectric.isChecked()) {
            if (!validateBedFields(electricPrice, electricTotal, electricAvailable, "Electric")) return;
            beds.put("Electric", createBedMap(electricPrice, electricTotal, electricAvailable));
        }

        if (checkHighLow.isChecked()) {
            if (!validateBedFields(highLowPrice, highLowTotal, highLowAvailable, "High-Low")) return;
            beds.put("High-low", createBedMap(highLowPrice, highLowTotal, highLowAvailable));
        }

        if (checkPediatric.isChecked()) {
            if (!validateBedFields(pediatricPrice, pediatricTotal, pediatricAvailable, "Pediatric")) return;
            beds.put("Pediatric", createBedMap(pediatricPrice, pediatricTotal, pediatricAvailable));
        }

        if (checkNeuro.isChecked()) {
            if (!validateBedFields(neuroPrice, neuroTotal, neuroAvailable, "Neuro")) return;
            beds.put("Neuro", createBedMap(neuroPrice, neuroTotal, neuroAvailable));
        }

        hospital.put("Seat Type", beds);

        db.collection("Hospital_Data")
                .document( name )
                .set(hospital)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Hospital added successfully!", Toast.LENGTH_SHORT).show();
                    clearInputs();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error adding hospital: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private boolean validateBedFields(EditText price, EditText total, EditText available, String type) {
        String priceText = price.getText().toString().trim();
        String totalText = total.getText().toString().trim();
        String availableText = available.getText().toString().trim();

        if (priceText.isEmpty() || totalText.isEmpty() || availableText.isEmpty()) {
            Toast.makeText(this, "Please enter price, total and availability for " + type + " seat", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            Double.parseDouble(priceText);
            Integer.parseInt(totalText);
            Integer.parseInt(availableText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format for " + type + " seat", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private Map<String, Object> createBedMap(EditText price, EditText total, EditText available) {
        Map<String, Object> map = new HashMap<>();
        map.put("price", Double.parseDouble(price.getText().toString().trim()));
        map.put("total", Integer.parseInt(total.getText().toString().trim()));
        map.put("available", Integer.parseInt(available.getText().toString().trim()));
        return map;
    }

    private void clearInputs() {
        hospitalName.setText("");
        hospitalAddress.setText("");
        hospitalPhone.setText("");

        checkManual.setChecked(false);
        checkElectric.setChecked(false);
        checkHighLow.setChecked(false);
        checkPediatric.setChecked(false);
        checkNeuro.setChecked(false);

        manualPrice.setText(""); manualTotal.setText(""); manualAvailable.setText("");
        electricPrice.setText(""); electricTotal.setText(""); electricAvailable.setText("");
        highLowPrice.setText(""); highLowTotal.setText(""); highLowAvailable.setText("");
        pediatricPrice.setText(""); pediatricTotal.setText(""); pediatricAvailable.setText("");
        neuroPrice.setText(""); neuroTotal.setText(""); neuroAvailable.setText("");
    }
}
