package com.example.icusheba;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Book_AppointmentActivity extends AppCompatActivity {

    EditText ed1, ed2, ed3, ed4;
    TextView tv, tv1, tv2, tv3, tv4, btn3;
    TextView seatInfoBox;
    Button btn, btn1, btn2;
    private Calendar selectedDate;
    private Calendar selectedTime;
    FirebaseFirestore sStore;
    private static final String PENDING_STATUS = "Pending";
    String hospitalName, bedType, address, phone, fees, totalSeats, availableSeats;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_appointment);

        tv = findViewById(R.id.textViewAPTitle);
        tv1 = findViewById(R.id.Hospital_Address);
        tv2 = findViewById(R.id.Hospital_Number);
        tv3 = findViewById(R.id.Selected_Bead);
        tv4 = findViewById(R.id.Bead_Fee);
        ed1 = findViewById(R.id.editTextPatientName);
        ed2 = findViewById(R.id.editTextAddress);
        ed3 = findViewById(R.id.editTextNumber);
        ed4 = findViewById(R.id.editTextBed);
        btn = findViewById(R.id.Booking_button);
        btn1 = findViewById(R.id.Date_button);
        btn2 = findViewById(R.id.Time_button);
        btn3 = findViewById(R.id.Back_Button);
        seatInfoBox = findViewById(R.id.Seat_Info_Box);

        selectedDate = Calendar.getInstance();
        selectedTime = Calendar.getInstance();
        sStore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        hospitalName = intent.getStringExtra("hospitalName");
        bedType = intent.getStringExtra("bedType");
        address = intent.getStringExtra("address");
        phone = intent.getStringExtra("phone");
        fees = intent.getStringExtra("fees");
        totalSeats = intent.getStringExtra("total");
        availableSeats = intent.getStringExtra("available");

        tv.setText(hospitalName);
        tv3.setText(bedType);
        tv1.setText(address);
        tv2.setText(phone);
        seatInfoBox.setText(totalSeats + " | " + availableSeats);
        tv4.setText("Cons Fees:" + fees + "/-");

        btn3.setOnClickListener(v -> finish());
        btn1.setOnClickListener(v -> showDatePicker());
        btn2.setOnClickListener(v -> showTimePicker());

        btn.setOnClickListener(v -> {
            String seatInput = ed4.getText().toString().trim();
            String availableSeatString = availableSeats != null ? availableSeats.trim() : "";

            if (btn1.getText().toString().isEmpty() || btn2.getText().toString().isEmpty() ||
                    ed1.getText().toString().isEmpty() || ed2.getText().toString().isEmpty() ||
                    ed3.getText().toString().isEmpty() || seatInput.isEmpty()) {
                Toast.makeText(Book_AppointmentActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // Ensure only digits are extracted from availableSeats
                availableSeatString = availableSeatString.replaceAll("[^0-9]", "");
                int requestedSeat = Integer.parseInt(seatInput);
                int availableSeat = Integer.parseInt(availableSeatString);

                if (requestedSeat <= 0) {
                    Toast.makeText(this, "Seat must be more than 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (requestedSeat > availableSeat) {
                    Toast.makeText(this, "No Seat Available", Toast.LENGTH_SHORT).show();
                } else {
                    addNewBooking();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid seat number", Toast.LENGTH_SHORT).show();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @OptIn(markerClass = UnstableApi.class)
    private void addNewBooking() {
        String name = ed1.getText().toString().trim();
        String userAddress = ed2.getText().toString().trim();
        String number = ed3.getText().toString().trim();
        String bed = ed4.getText().toString().trim();
        String Hospital_Address = tv1.getText().toString().trim();
        String Hospital_Number = tv2.getText().toString().trim();
        String date = btn1.getText().toString().trim();
        String time = btn2.getText().toString().trim();
        String bedFee = tv4.getText().toString().trim();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = currentUser != null ? currentUser.getEmail() : "Not logged in";

        CollectionReference bookingRef = sStore.collection("BOOKING_DATA_COLLECTION")
                .document(hospitalName)
                .collection("Pending");

        bookingRef.orderBy("SerialNumber", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int nextSerialNumber = 1;

                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot lastDocument = queryDocumentSnapshots.getDocuments().get(0);
                        Long lastSerialNumber = lastDocument.getLong("SerialNumber");
                        if (lastSerialNumber != null) {
                            nextSerialNumber = lastSerialNumber.intValue() + 1;
                        }
                    }

                    Map<String, Object> newBooking = new HashMap<>();
                    newBooking.put("Hospital_Name", hospitalName);
                    newBooking.put("Hospital_Address", Hospital_Address);
                    newBooking.put("Hospital_Number", Hospital_Number);
                    newBooking.put("Email", userEmail);
                    newBooking.put("Name", name);
                    newBooking.put("Address", userAddress);
                    newBooking.put("Number", number);
                    newBooking.put("Number_Of_Seat", bed);
                    newBooking.put("Seat_Type", bedType);
                    newBooking.put("Date", date);
                    newBooking.put("Time", time);
                    newBooking.put("Bed_Fee", bedFee);
                    newBooking.put("Status", PENDING_STATUS);
                    newBooking.put("SerialNumber", nextSerialNumber);
                    newBooking.put("timestamp", FieldValue.serverTimestamp());

                    bookingRef.add(newBooking)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(Book_AppointmentActivity.this, "Successfully booked", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Book_AppointmentActivity.this,BookingConfirmation.class);
                                intent.putExtra("hospital", hospitalName);
                                intent.putExtra("email", userEmail);
                                startActivity(intent);
                                finish();
                                clearFields();
                                sendMessageToHospitalHR(hospitalName, newBooking);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(Book_AppointmentActivity.this, "Error adding booking", Toast.LENGTH_SHORT).show();
                                Log.e("BookAppointment", "Error adding booking", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Book_AppointmentActivity.this, "Error getting last serial number", Toast.LENGTH_SHORT).show();
                    Log.e("BookAppointment", "Error getting last serial number", e);
                });
    }

    @UnstableApi
    private void sendMessageToHospitalHR(String hospitalName, Map<String, Object> newBooking) {
        // Optional: implement messaging to HR
    }

    private void clearFields() {
        ed1.setText("");
        ed2.setText("");
        ed3.setText("");
        ed4.setText("");
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute1) -> {
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute1);
                    updateSelectedTime();
                    Toast.makeText(this, "Time selected", Toast.LENGTH_SHORT).show();
                },
                hour, minute, false);
        timePickerDialog.show();
    }

    private void updateSelectedTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String formattedTime = timeFormat.format(selectedTime.getTime());
        btn2.setText(formattedTime);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year1);
                    selectedDate.set(Calendar.MONTH, monthOfYear);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateSelectedDate();
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void updateSelectedDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(selectedDate.getTime());
        btn1.setText(formattedDate);
    }
}
