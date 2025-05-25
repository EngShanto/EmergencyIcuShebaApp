package com.example.icusheba;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ICU_Activity extends AppCompatActivity {

    TextView tv;
    ArrayList<HashMap<String, String>> list;
    FirebaseFirestore db;
    String hospitalName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icu);

        db = FirebaseFirestore.getInstance();
        tv = findViewById(R.id.Tittle);
        ListView listView = findViewById(R.id.List_View);

        Intent intent = getIntent();
        hospitalName = intent.getStringExtra("hospitalName");

        if (hospitalName == null || hospitalName.isEmpty()) {
            Toast.makeText(this, "Hospital name not found", Toast.LENGTH_SHORT).show();
            return;
        }

        tv.setText(hospitalName);
        list = new ArrayList<>();

        // 🔽 Fetch data from Firebase
        db.collection("Hospital_Data").document(hospitalName)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data == null) return;

                        String address = (String) data.get("Hospital_Address");
                        String phone = (String) data.get("Hospital_Number");
                        @SuppressWarnings("unchecked")
                        Map<String, Object> beds = (Map<String, Object>) data.get("Seat Type");

                        if (beds != null) {
                            for (Map.Entry<String, Object> entry : beds.entrySet()) {
                                String bedType = entry.getKey();
                                @SuppressWarnings("unchecked")
                                Map<String, Object> bedInfo = (Map<String, Object>) entry.getValue();

                                //noinspection DataFlowIssue
                                String available = bedInfo.get("available").toString();
                                String total = Objects.requireNonNull(bedInfo.get("total")).toString();
                                String price = Objects.requireNonNull(bedInfo.get("price")).toString();

                                HashMap<String, String> item = new HashMap<>();
                                item.put("line_a", bedType);  // Bed Type
                                item.put("line_b", "Hospital Address: " + address);  // Address
                                item.put("line_c", "Available Seats: " + available);  // Availability
                                item.put("line_d", "Phone: " + phone);  // Mobile Number
                                item.put("line_e", "Fees: " + price + "/-");  // Price
                                item.put("line_f", "Total Seats:" + total);

                                list.add(item);
                            }

                            SimpleAdapter adapter = new SimpleAdapter(
                                    ICU_Activity.this,
                                    list,
                                    R.layout.multi_line,
                                    new String[]{"line_a", "line_b", "line_c", "line_d", "line_e", "line_f"},
                                    new int[]{R.id.line_a, R.id.line_b, R.id.line_c, R.id.line_d, R.id.line_e, R.id.line_f}
                            );
                            listView.setAdapter(adapter);

                            // 🔽 Item Click Listener
                            listView.setOnItemClickListener((adapterView, view, position, id) -> {
                                HashMap<String, String> selectedItem = list.get(position);
                                Intent bookIntent = new Intent(ICU_Activity.this, Book_AppointmentActivity.class);
                                bookIntent.putExtra("hospitalName", hospitalName);
                                bookIntent.putExtra("bedType", selectedItem.get("line_a"));
                                bookIntent.putExtra("available", selectedItem.get("line_c"));
                                bookIntent.putExtra("total", selectedItem.get("line_f"));
                                bookIntent.putExtra("address", selectedItem.get("line_b"));
                                bookIntent.putExtra("phone", selectedItem.get("line_d"));
                                bookIntent.putExtra("fees", Objects.requireNonNull(selectedItem.get("line_e")).replace("Fees: ", "").replace("/-", ""));
                                startActivity(bookIntent);
                            });
                        }

                    } else {
                        Toast.makeText(this, "Hospital data not found in Firebase", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(ICU_Activity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
