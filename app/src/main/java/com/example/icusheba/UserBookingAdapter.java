package com.example.icusheba;
import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserBookingAdapter extends RecyclerView.Adapter<UserBookingAdapter.ViewHolder> {

    private final List<UserBooking> bookingList;

    public UserBookingAdapter(List<UserBooking> bookingList) {
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserBooking booking = bookingList.get(position);

        holder.textName.setText("Name: " + safe(booking.getName()));
        holder.textHospitalName.setText("Hospital: " + safe(booking.getHospital_Name()));
        holder.textSeatType.setText("Seat Type: " + safe(booking.getSeat_Type()));
        holder.textSeatFee.setText("Fee: " + safe(booking.getBed_Fee()));
        holder.textNumberOfBed.setText("Number of Beds: " + safe(booking.getNumber_Of_Seat()));
        holder.textBookingStatus.setText("Status: " + safe(booking.getStatus()));
        holder.textHospitalNumber.setText("Hospital Number: " + safe(booking.getHospital_Number()));
        holder.textEmail.setText("Email: " + safe(booking.getEmail()));
        holder.textDate.setText("Admission Date: " + safe(booking.getDate()));
        holder.textTime.setText("Admission Time: " + safe(booking.getTime()));
        holder.textHospitalAddress.setText("Address: " + safe(booking.getHospital_Address()));
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    private String safe(String text) {
        return text != null && !text.isEmpty() ? text : "N/A";
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textHospitalName, textSeatType, textSeatFee, textNumberOfBed,textDate, textTime,
                textBookingStatus, textHospitalNumber, textEmail,textHospitalAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textHospitalName = itemView.findViewById(R.id.textHospitalName);
            textSeatType = itemView.findViewById(R.id.textSeatType);
            textSeatFee = itemView.findViewById(R.id.textSeatFee);
            textDate = itemView.findViewById(R.id.textViewDate);
            textTime = itemView.findViewById(R.id.textViewTime);
            textNumberOfBed = itemView.findViewById(R.id.textNumberOfBed);
            textBookingStatus = itemView.findViewById(R.id.textBookingStatus);
            textHospitalNumber = itemView.findViewById(R.id.textHospitalNumber);
            textEmail = itemView.findViewById(R.id.textEmail);
            textHospitalAddress = itemView.findViewById(R.id.textHospitalAddress);

        }
    }
}
