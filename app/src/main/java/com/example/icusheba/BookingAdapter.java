package com.example.icusheba;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Callback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import org.json.JSONException;
import org.json.JSONObject;

/** @noinspection unused*/
public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> implements Filterable {
    private final String hospitalName;
    private final Context context;
    private final List<Booking> bookingList;
    private final FirebaseFirestore fStore;
    private final List<Booking> bookingListFull;
    private List<Booking> fullBookingList;
    private TableRow highlightedRow = null;
    private int updatedList;

    public BookingAdapter(Context context, List<Booking> bookingList, String hospitalName) {
        this.context = context;
        this.bookingList = bookingList;
        this.bookingListFull = new ArrayList<>( bookingList );
        this.fullBookingList = new ArrayList<>(bookingList);
        this.hospitalName = hospitalName;
        fStore = FirebaseFirestore.getInstance();

    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from( context ).inflate( R.layout.booking_item, parent, false );
        return new BookingViewHolder( view );
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        holder.serialNumber.setText(String.valueOf(booking.getSerialNumber()));
        holder.patientName.setText(booking.getName());
        holder.patientAddress.setText(booking.getAddress());
        holder.patientNumber.setText(booking.getNumber());
        holder.bookingDate.setText(booking.getDate());
        holder.bookingTime.setText(booking.getTime());
        holder.SeatType.setText(booking.getSeat_Type());
        holder.NumberOfSeat.setText(booking.getNumber_Of_Seat());
        holder.bookingStatus.setText(booking.getStatus());

        // Control button visibility
        switch (booking.getStatus()) {
            case "Pending":
                holder.ApproveButton.setVisibility(View.VISIBLE);
                holder.ApproveButton.setImageResource(R.drawable.baseline_done_24); // ✅ image set
                holder.RejectButton.setVisibility(View.VISIBLE);
                holder.RejectButton.setImageResource(R.drawable.rejected); // ✅ image set
                holder.DeleteButton.setVisibility(View.GONE);
                holder.RecoveryButton.setVisibility(View.GONE);
                break;

            case "Approved":
                holder.ApproveButton.setVisibility(View.GONE);
                holder.RejectButton.setVisibility(View.VISIBLE);
                holder.RejectButton.setImageResource(R.drawable.rejected);
                holder.DeleteButton.setVisibility(View.VISIBLE);
                holder.DeleteButton.setImageResource(R.drawable.deleted_icon);
                holder.RecoveryButton.setVisibility(View.GONE);
                break;

            case "Deleted":
            case "Rejected":
                holder.ApproveButton.setVisibility(View.GONE);
                holder.RejectButton.setVisibility(View.GONE);
                holder.DeleteButton.setVisibility(View.VISIBLE);
                holder.DeleteButton.setImageResource(R.drawable.deleted_icon); // or ic_delete_red
                holder.RecoveryButton.setVisibility(View.VISIBLE);
                holder.RecoveryButton.setImageResource(R.drawable.recovery__1_);
                break;
        }


        // To avoid "Cannot find local variable 'holder'" issue in debugger
        final BookingViewHolder finalHolder = holder;

        // Approve button
        finalHolder.ApproveButton.setOnClickListener(v -> {
            if (booking.getId() == null || booking.getId().isEmpty()) {
                Toast.makeText(context, "Invalid booking ID", Toast.LENGTH_SHORT).show();
                return;
            }
            ApproveBookingStatus(booking.getId(), position);
        });

        // Reject button
        finalHolder.RejectButton.setOnClickListener(v -> {
            if (booking.getId() == null || booking.getId().isEmpty()) {
                Toast.makeText(context, "Invalid booking ID", Toast.LENGTH_SHORT).show();
                return;
            }
            RejectBookingStatus(booking.getId(), position);
        });

        // Recovery button
        finalHolder.RecoveryButton.setOnClickListener(v -> {
            if (booking.getId() == null || booking.getId().isEmpty()) {
                Toast.makeText(context, "Invalid booking ID", Toast.LENGTH_SHORT).show();
                return;
            }
            RecoveryBookingStatus(booking.getId(), position);
        });

        // Delete button
        finalHolder.DeleteButton.setOnClickListener(v -> {
            if (booking.getId() == null || booking.getId().isEmpty()) {
                Toast.makeText(context, "Invalid booking ID", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Confirm Delete");
            builder.setMessage("Are you sure you want to delete this booking?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                String currentStatus = booking.getStatus();
                DeleteBookingStatus(booking.getId(), position, currentStatus);
            });
            builder.setNegativeButton("No", null);
            builder.create().show();
        });

        // Row highlighting
        finalHolder.tableRow.setOnClickListener(view -> {
            if (highlightedRow != null) {
                removeHighlight(highlightedRow);
            }
            highlightRow(finalHolder.tableRow);
            highlightedRow = finalHolder.tableRow;
        });
    }



    private void RecoveryBookingStatus(String id, int position) {
        if (id == null || id.isEmpty()) {
            Toast.makeText(context, "Invalid booking ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("RecoveryBooking", "Recovering booking with ID: " + id);

        // Validate position and booking
        if (bookingList == null || bookingList.isEmpty() || position < 0 || position >= bookingList.size()) {
            Toast.makeText(context, "Invalid position", Toast.LENGTH_SHORT).show();
            return;
        }

        Booking booking = bookingList.get(position); // Ensure we get the correct booking
        if (booking == null) {
            Toast.makeText(context, "Booking not found", Toast.LENGTH_SHORT).show();
            return;
        }
        booking.setStatus("Pending"); // Update the status to "Pending" (recovered state)

        Log.d("RecoveryBooking", "Hospital: " + hospitalName + " ID: " + id);
        if (fStore == null) {
            Toast.makeText(context, "FireStore instance is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Move booking back to the Pending collection (if that's your intention)
        fStore.collection("BOOKING_DATA_COLLECTION")
                .document(hospitalName)
                .collection("Pending") // Move to Pending collection
                .document(id)
                .set(booking)  // Save the updated booking
                .addOnSuccessListener(aVoid -> {
                    Log.d("RecoveryBooking", "Moved booking to 'Pending' collection");

                    // Remove booking from the 'Rejected' collection
                    fStore.collection("BOOKING_DATA_COLLECTION")
                            .document(hospitalName)
                            .collection("Rejected") // The booking was in 'Rejected'
                            .document(id)
                            .delete()
                            .addOnSuccessListener(aVoid1 -> {
                                Log.d("RecoveryBooking", "Booking removed from 'Rejected' collection");

                                // Ensure UI operations are on the main thread
                                ((Activity) context).runOnUiThread(() -> {
                                    // Make sure this part only runs if the bookingList is not modified elsewhere
                                    if (position < bookingList.size()) {
                                        bookingList.remove(position);
                                        notifyItemRemoved(position);
                                        Toast.makeText(context, "Booking recovered successfully", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            })
                            .addOnFailureListener(e -> {
                                Log.e("RecoveryBooking", "Failed to remove from 'Rejected' collection", e);
                                Toast.makeText(context, "Failed to remove from Rejected list", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("RecoveryBooking", "Failed to update booking status", e);
                    Toast.makeText(context, "Failed to recover booking", Toast.LENGTH_SHORT).show();
                });
    }


    private void DeleteBookingStatus(String id, int position, String currentStatus) {
        if (id == null || id.isEmpty()) {
            Toast.makeText(context, "Invalid booking ID", Toast.LENGTH_SHORT).show();
            return;
        }

        if (hospitalName == null || hospitalName.trim().isEmpty()) {
            Toast.makeText(context, "Hospital name missing", Toast.LENGTH_SHORT).show();
            return;
        }

        Booking booking = bookingList.get(position); // Get booking object
        String seatType = booking.getSeat_Type(); // Seat type like General, Oxygen etc.
        int seatCount;

        try {
            seatCount = Integer.parseInt(booking.getNumber_Of_Seat());
        } catch (NumberFormatException e) {
            Toast.makeText(context, "Invalid seat number", Toast.LENGTH_SHORT).show();
            return;
        }

        // First move booking to Deleted
        fStore.collection("BOOKING_DATA_COLLECTION")
                .document(hospitalName)
                .collection("Deleted")
                .document(id)
                .set(booking)
                .addOnSuccessListener(aVoid -> {
                    // Then delete from current status (like Pending, Approved etc.)
                    fStore.collection("BOOKING_DATA_COLLECTION")
                            .document(hospitalName)
                            .collection(currentStatus)
                            .document(id)
                            .delete()
                            .addOnSuccessListener(aVoid1 -> {
                                bookingList.remove(position);
                                notifyItemRemoved(position);
                                restoreAvailableSeats(seatType, seatCount);
                                Toast.makeText(context, "Booking deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to delete from current status", Toast.LENGTH_SHORT).show();
                                Log.e("DeleteBooking", "Deletion from " + currentStatus + " failed", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to move booking to Deleted", Toast.LENGTH_SHORT).show();
                    Log.e("DeleteBooking", "Moving to Deleted failed", e);
                });
    }

    private void restoreAvailableSeats(String seatType, int numberOfSeats) {
        if (hospitalName == null || hospitalName.trim().isEmpty()) {
            Log.e("restoreSeats", "Hospital name is missing");
            return;
        }

        DocumentReference docRef = fStore.collection("Hospital_Data").document(hospitalName);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Step 1: Get "Seat Type" map
                Map<String, Object> seatTypeMap = (Map<String, Object>) documentSnapshot.get("Seat Type");
                if (seatTypeMap != null && seatTypeMap.containsKey(seatType)) {
                    Map<String, Object> seatDetails = (Map<String, Object>) seatTypeMap.get(seatType);
                    if (seatDetails != null && seatDetails.containsKey("available")) {
                        try {
                            String currentAvailableStr = seatDetails.get("available").toString();
                            int currentAvailable = Integer.parseInt(currentAvailableStr);
                            int updatedAvailable = currentAvailable + numberOfSeats;

                            // Step 2: Prepare update path like "Seat Type.General.available"
                            String updatePath = "Seat Type." + seatType + ".available";
                            Map<String, Object> updateMap = new HashMap<>();
                            updateMap.put(updatePath, String.valueOf(updatedAvailable));

                            docRef.update(updateMap)
                                    .addOnSuccessListener(aVoid -> Log.d("restoreSeats", "Seat restored successfully"))
                                    .addOnFailureListener(e -> Log.e("restoreSeats", "Failed to update seat count", e));

                        } catch (NumberFormatException e) {
                            Log.e("restoreSeats", "Seat count parse failed", e);
                        }
                    } else {
                        Log.e("restoreSeats", "'available' field missing for seat type: " + seatType);
                    }
                } else {
                    Log.e("restoreSeats", "Seat type not found in hospital data: " + seatType);
                }
            }
        }).addOnFailureListener(e -> Log.e("restoreSeats", "Failed to read hospital data", e));
    }

    private void removeHighlight(TableRow tbRow) {
        // Loop through all children in the TableRow and reset background color
        for (int i = 0; i < tbRow.getChildCount(); i++) {
            View child = tbRow.getChildAt(i);
            // Check if the child is a TextView or ImageButton
            if (child instanceof TextView || child instanceof ImageButton) {
                // Reset background color to default
                child.setBackgroundResource(R.drawable.border);
            }
        }
    }

    private void highlightRow(TableRow tbRow) {
        // Loop through all children in the TableRow
        for (int i = 0; i < tbRow.getChildCount(); i++) {
            View child = tbRow.getChildAt(i);
            // Check if the child is a TextView or ImageButton
            if (child instanceof TextView || child instanceof ImageButton) {
                // Highlight background color
                child.setBackgroundColor( ContextCompat.getColor(context, R.color.Yellow));
                child.setBackgroundResource(R.drawable.selected_colore);
            }
        }
    }

    private void RejectBookingStatus(String id, int position) {
        if (id == null || id.isEmpty()) {
            Toast.makeText(context, "Invalid booking ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("DeleteBooking", "Deleting booking with ID: " + id);

        Booking booking = bookingList.get(position); // Ensure we get the correct booking
        String PhoneNumber = booking.getNumber(); // Get patient's phone number
        String PatientEmail = booking.getEmail(); // Get patient's name
        booking.setStatus("Rejected");

        // Move booking to the Delete collection
        fStore.collection("BOOKING_DATA_COLLECTION")
                .document(hospitalName)
                .collection("Rejected") // Ensure this is a valid collection
                .document(id)
                .set(booking)
                .addOnSuccessListener(aVoid -> {
                    Log.d("RejectedBooking", "Moved booking to 'Rejected' collection");

                    fStore.collection("BOOKING_DATA_COLLECTION")
                            .document(hospitalName)
                            .collection("Pending")
                            .document(id)
                            .delete()
                            .addOnSuccessListener(aVoid1 -> {
                                Log.d("RejectedBooking", "Booking deleted from 'Pending' collection");
                                bookingList.remove(position);
                                notifyItemRemoved(position);
                                Toast.makeText(context, "Booking Reject successfully", Toast.LENGTH_SHORT).show();
                                // Send SMS to the patient
                                sendDeleteSms(PhoneNumber, booking.getName(),PatientEmail);
                                String token = booking.getFcmToken();
                                String title = "Booking Cancelled";
                                String message = "Dear " + booking.getName() + ", your ICU booking has been Rejected/cancelled.";
                                sendFCMNotification(token, title, message);

                            })
                            .addOnFailureListener(e -> {
                                Log.e("RejectedBooking", "Failed to Reject from Pending", e);
                                Toast.makeText(context, "Failed to remove from pending list", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("RejectedBooking", "Failed to Rejected booking", e);
                    Toast.makeText(context, "Failed to Reject booking", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendDeleteSms(String phoneNumber, String patientName, String patientEmail) {

            String message = "Dear " + patientName + ", your ICU booking has been Rejected/Cancel.";

            if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
                showToast("SMS not supported on this device");
                return;
            }

            // Check for SEND_SMS permission
            if (ContextCompat.checkSelfPermission(context,android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                if (context instanceof Activity) {
                    // Request permission if not granted
                    ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.SEND_SMS}, 1);
                } else {
                    showToast("Unable to request permission. Please enable SMS permission in settings.");
                }
            } else {
                sendSms(phoneNumber,patientEmail, message);
            }
    }


    private void ApproveBookingStatus(String id, int position) {
        if (id == null || id.isEmpty()) {
            Toast.makeText(context, "Invalid booking ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Booking booking = bookingList.get(position); // Ensure we get the correct booking
        String PhoneNumber = booking.getNumber(); // Get patient's phone number
        String PatientEmail = booking.getEmail(); // Get patient's name
        int seatsToReduce = Integer.parseInt(booking.getNumber_Of_Seat()); // "2" → 2
        String seatType = booking.getSeat_Type();
        booking.setStatus("Approved");

        // Move booking to the approved collection
        fStore.collection("BOOKING_DATA_COLLECTION")
                .document(hospitalName) // Create an "Approved" document inside "BOOKING_DATA_COLLECTION"
                .collection("Approved") // Nested collection for approved bookings
                .document(id) // Use the correct ID
                .set(booking) // Save booking data
                .addOnSuccessListener(aVoid -> {
                    // After successfully moving, delete from Pending_List (not pending_bookings)
                    fStore.collection("BOOKING_DATA_COLLECTION")
                            .document(hospitalName)
                            .collection("Pending")
                            .document(id)
                            .delete()
                            .addOnSuccessListener(aVoid1 -> {
                                bookingList.remove(position);
                                notifyItemRemoved(position);
                                Toast.makeText(context, "Booking approved successfully", Toast.LENGTH_SHORT).show();
                                // Send SMS to the patient
                                sendApprovalSms(PhoneNumber, booking.getName(),PatientEmail);
                                String token = booking.getFcmToken();
                                String title = "Booking Approved";
                                String message = "Dear " + booking.getName() + ", your ICU booking has been approved.";

                                sendFCMNotification(token, title, message);
                                updateAvailableSeats(seatType, seatsToReduce);

                            })
                            .addOnFailureListener(e -> {
                                Log.e("FireStore", "Failed to delete from Pending_List", e);
                                Toast.makeText(context, "Failed to remove from pending list", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FireStore", "Failed to approve booking", e);
                    Toast.makeText(context, "Failed to approve booking", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendApprovalSms(String phoneNumber, String patientName,String PatientEmail) {
        String message = "Dear " + patientName + ", your ICU booking has been approved. Please arrive at the hospital as scheduled.";

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            showToast("SMS not supported on this device");
            return;
        }

        // Check for SEND_SMS permission
        if (ContextCompat.checkSelfPermission(context,android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (context instanceof Activity) {
                // Request permission if not granted
                ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.SEND_SMS}, 1);
            } else {
                showToast("Unable to request permission. Please enable SMS permission in settings.");
            }
        } else {
            sendSms(phoneNumber,PatientEmail, message);
        }
    }

    private void sendSms(String phoneNumber,String PatientEmail, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
            smsManager.sendMultipartTextMessage(PatientEmail, null, parts, null, null);

// Remove this:
// smsManager.sendMultipartTextMessage(PatientEmail, null, parts, null, null);

            showToast("Approval message sent to patient");
        } catch (Exception e) {
            Log.e(TAG, "Failed to send SMS", e);
            showToast("Failed to send message");
        }
    }


    private void showToast(String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();

    }
    private void updateAvailableSeats(String seatType, int numberOfSeats) {
        DocumentReference docRef = fStore.collection("Hospital_Data").document(hospitalName);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Step 1: Get the "Seat Type" map
                //noinspection unchecked
                Map<String, Object> seatTypeMap = (Map<String, Object>) documentSnapshot.get("Seat Type");
                if (seatTypeMap != null && seatTypeMap.containsKey(seatType)) {
                    //noinspection unchecked
                    Map<String, Object> specificSeatMap = (Map<String, Object>) seatTypeMap.get(seatType);
                    if (specificSeatMap != null && specificSeatMap.containsKey("available")) {
                        String currentAvailableStr = Objects.requireNonNull(specificSeatMap.get("available")).toString();

                        try {
                            int currentAvailable = Integer.parseInt(currentAvailableStr);
                            int updatedAvailable = Math.max(currentAvailable - numberOfSeats, 0);

                            // Step 2: Update only the nested "available" field
                            String updatePath = "Seat Type." + seatType + ".available";
                            Map<String, Object> updateMap = new HashMap<>();
                            updateMap.put(updatePath, String.valueOf(updatedAvailable));

                            docRef.update(updateMap)
                                    .addOnSuccessListener(aVoid -> Log.d("Seats", "Seat count updated to " + updatedAvailable))
                                    .addOnFailureListener(e -> Log.e("Seats", "Failed to update seat count", e));

                        } catch (NumberFormatException e) {
                            Log.e("Seats", "Invalid available seat format", e);
                        }
                    } else {
                        Log.e("Seats", "No 'available' field found for seatType: " + seatType);
                    }
                } else {
                    Log.e("Seats", "SeatType '" + seatType + "' not found");
                }
            }
        }).addOnFailureListener(e -> Log.e("FireStore", "Failed to fetch hospital data", e));
    }



    @Override
    public int getItemCount() {
        return bookingList.size();
    }
    private void sendFCMNotification(String token, String title, String message) {
        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        JSONObject notification = new JSONObject();
        try {
            notification.put("title", title);
            notification.put("body", message);

            json.put("to", token);
            json.put("notification", notification);
            // Optional: You can also add a data payload
        } catch (JSONException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .post(body)
                .addHeader("Authorization", "key=YOUR_SERVER_KEY_HERE") // Keep this key secret!
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) {
                if (!response.isSuccessful()) {
                    Log.e("FCM", "Failed to send notification: " + response.message());
                } else {
                    Log.d("FCM", "Notification sent: " + response.body());
                }
            }
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                Log.e("FCM", "Error in sending notification", e);
            }
        });
    }

    @Override
    public Filter getFilter() {
        return bookingFilter;
    }

    private final Filter bookingFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Booking> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(bookingListFull); // Return full list if search is empty
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Booking item : bookingListFull) {
                    // You can change which fields to filter by (e.g., patient name, phone, status, etc.)
                    if (item.getName().toLowerCase().contains(filterPattern)
                            || item.getNumber().toLowerCase().contains(filterPattern)
                            || item.getStatus().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            bookingList.clear();
            //noinspection unchecked,rawtypes
            bookingList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    @SuppressLint("NotifyDataSetChanged")
    public void updateFullList(List<Booking> bookingList) {
        bookingListFull.clear();
        bookingListFull.addAll(bookingList);
        this.fullBookingList = new ArrayList<>(updatedList);
        notifyDataSetChanged();
    }

    public void setUpdatedList(int updatedList) {
        this.updatedList = updatedList;
    }

    public void setFullBookingList(List<Booking> fullBookingList) {
        this.fullBookingList = fullBookingList;
    }

    public List<Booking> getFullBookingList() {
        return fullBookingList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<Booking> filteredList) {
        bookingList.clear();
        bookingList.addAll(filteredList);
        notifyDataSetChanged();
    }


    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView patientName, patientAddress, SeatType, bookingDate, bookingTime, NumberOfSeat, bookingStatus, serialNumber, patientNumber;
        ImageButton ApproveButton,RejectButton,RecoveryButton,DeleteButton;
        TableRow tableRow;

        public BookingViewHolder(View itemView) {
            super(itemView);
            patientName = itemView.findViewById(R.id.patientName);
            patientAddress = itemView.findViewById(R.id.patientAddress);
            patientNumber = itemView.findViewById(R.id.patientNumber);
            SeatType = itemView.findViewById(R.id.SeatType);
            bookingDate = itemView.findViewById(R.id.bookingDate);
            bookingTime = itemView.findViewById(R.id.bookingTime);
            NumberOfSeat = itemView.findViewById(R.id.NumberOfSeat);
            bookingStatus = itemView.findViewById(R.id.bookingStatus);
            serialNumber = itemView.findViewById(R.id.SerialNumber);
            ApproveButton = itemView.findViewById(R.id.Approve_Button);
            RejectButton = itemView.findViewById(R.id.Reject_Button);
            RecoveryButton = itemView.findViewById(R.id.Recovery_Button);
            DeleteButton = itemView.findViewById(R.id.Delete_Button);
            tableRow = itemView.findViewById(R.id.Table_Row);
        }

    }
}
