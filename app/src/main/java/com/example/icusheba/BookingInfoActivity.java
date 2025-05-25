package com.example.icusheba;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BookingInfoActivity extends AppCompatActivity {
    TextView tv, Info_Type;
    CardView Pending_Card, Approve_Card, Delete_Card, Reject_Card;
    FirebaseFirestore fStore;
    FirebaseAuth sAuth;
    String hospitalName;
    SearchView searchView;
    private BookingAdapter bookingAdapter;
    private List<Booking> bookingList;
    String userId;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        EdgeToEdge.enable( this );
        CreateNotification.createChannel(this);
        setContentView( R.layout.activity_booking_info );
        Pending_Card = findViewById( R.id.Pending_Info );
        Approve_Card = findViewById( R.id.Approve_Info );
        Delete_Card = findViewById( R.id.Delete_Info );
        Reject_Card = findViewById( R.id.Reject_Info );
        tv = findViewById( R.id.Hospital_Title );
        Info_Type = findViewById( R.id.Info_Type );
        TextView headerText = findViewById( R.id.header );
        fStore = FirebaseFirestore.getInstance();
        RecyclerView recyclerView = findViewById( R.id.recyclerView );
        searchView = findViewById(R.id.search);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookingList = new ArrayList<>();
        bookingAdapter = new BookingAdapter(this, bookingList, hospitalName);
        recyclerView.setAdapter(bookingAdapter);
        sAuth = FirebaseAuth.getInstance();
        SearchView searchView = findViewById(R.id.search);
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor( ContextCompat.getColor(this, R.color.white)); // or your desired color
        searchEditText.setHintTextColor(ContextCompat.getColor(this, R.color.white)); // or an
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            searchEditText.setTextCursorDrawable(R.drawable.cursor_colore);
        }
        userId = Objects.requireNonNull(sAuth.getCurrentUser()).getUid();
        if (sAuth.getCurrentUser() != null) {
            userId = sAuth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch Hospital Name Assigned to User
        DocumentReference documentReference = fStore.collection("Users").document(userId);
        documentReference.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("FireStore", "Error fetching user data", error);
                Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null && value.exists()) {
                hospitalName = value.getString("Hospital");
                if (hospitalName == null || hospitalName.trim().isEmpty()) {
                    Log.e("BookingInfoActivity", "Hospital name is empty");
                    tv.setText("No hospital assigned");
                    return;
                }

                Log.d("BookingInfoActivity", "User's hospitalName: " + hospitalName);
                tv.setText(hospitalName);
                bookingAdapter = new BookingAdapter(this, bookingList, hospitalName);
                recyclerView.setAdapter(bookingAdapter);

                // Load initial pending data
                fetchBookings("Status");
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle search submit if needed, e.g., do nothing or show a message
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter the data based on the entered text
                filterData(newText);
                return true;
            }
        });

        Pending_Card.setOnClickListener(v -> {
            headerText.setText("Approved/Reject");
            Info_Type.setText("Pending Information");

            // Fetch the bookings with status 'Pending'
            fetchBookings("Pending");
        });

        Approve_Card.setOnClickListener(v -> {
            Info_Type.setText("Approved Information");
            headerText.setText("Completed/Reject");


            // Fetch the bookings with status 'Approved'
            fetchBookings("Approved");
        });

        Delete_Card.setOnClickListener(v -> {
            headerText.setText("Completed");

            // Fetch the bookings with status 'Deleted'
            fetchBookings("Deleted");
        });

        Reject_Card.setOnClickListener(v -> {
            headerText.setText("Recovery/Delete");
            fetchBookings("Rejected");
        });

        ViewCompat.setOnApplyWindowInsetsListener( findViewById( R.id.main ), (v, insets) -> {
            Insets systemBars = insets.getInsets( WindowInsetsCompat.Type.systemBars() );
            v.setPadding( systemBars.left, systemBars.top, systemBars.right, systemBars.bottom );
            return insets;
        } );
    }

    private void filterData(String newText) {
        if (bookingAdapter != null) {
            bookingAdapter.getFilter().filter(newText);  // Use newText directly
        } else {
            Toast.makeText(this, "Adapter not initialized", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void fetchBookings(String status) {
        if (hospitalName == null || hospitalName.trim().isEmpty()) {
            Log.e("BookingInfoActivity", "Hospital name is empty. Cannot fetch bookings.");
            return;
        }

        Log.d("BookingInfoActivity", "Fetching from: BOOKING_DATA_COLLECTION/" + hospitalName + "/" + status);

        Info_Type.setText(status + " Information");

        fStore.collection("BOOKING_DATA_COLLECTION")
                .document(hospitalName)
                .collection(status) // ✅ Use correct status here
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bookingList.clear();

                        if (task.getResult().isEmpty()) {
                            Log.d("BookingInfoActivity", "No " + status + " bookings found.");
                            Toast.makeText(this, "No " + status + " bookings found.", Toast.LENGTH_SHORT).show();
                        } else {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("FireStore", "Document data: " + document.getData());

                                Booking booking = document.toObject(Booking.class);
                                booking.setId(document.getId());

                                bookingList.add(booking);
                            }
                            bookingAdapter.notifyDataSetChanged();
                            bookingAdapter.updateFullList(bookingList);
                        }
                    } else {
                        Log.w("BookingInfoActivity", "Error getting " + status + " bookings.", task.getException());
                        Toast.makeText(this, "Error fetching " + status + " bookings", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
