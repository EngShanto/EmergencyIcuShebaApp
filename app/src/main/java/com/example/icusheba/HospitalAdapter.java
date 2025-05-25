package com.example.icusheba;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.bumptech.glide.Glide;


public class HospitalAdapter extends FirestoreRecyclerAdapter<Hospital, HospitalAdapter.HospitalHolder> {
    private OnItemClickListener listener;
    public HospitalAdapter(@NonNull FirestoreRecyclerOptions<Hospital> options) {

        super(options);
    }

    // Bind data to view
    @Override
    protected void onBindViewHolder(@NonNull HospitalHolder holder, int position, @NonNull Hospital model) {
        holder.hospitalName.setText(model.getHospital_Name());
        holder.hospitalAddress.setText(model.getHospital_Address());
        holder.hospitalNumber.setText(model.getHospital_Number());

        // Load image using Glide or Picasso
        Glide.with(holder.hospitalImage.getContext())
                .load(model.getImageUrl())
                .placeholder(R.drawable.hospital_icon) // optional
                .into(holder.hospitalImage);



        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(model); // ক্লিক হওয়া আইটেম পাঠানো
            }
        });

    }

    // Create view holder

    @NonNull
    @Override
    public HospitalHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hospital_card_item, parent, false);
        return new HospitalHolder(view);
    }

    // ViewHolder class to hold the view items
    public class HospitalHolder extends RecyclerView.ViewHolder {
        TextView hospitalName, hospitalAddress, hospitalNumber;
        ImageView hospitalImage;

        public HospitalHolder(View itemView) {
            super(itemView);
            hospitalName = itemView.findViewById(R.id.hospitalName);
            hospitalAddress = itemView.findViewById(R.id.hospitalAddress);
            hospitalNumber = itemView.findViewById(R.id.hospitalNumber);
            hospitalImage = itemView.findViewById(R.id.hospitalImage);
        }
    }
    public static <Query> FirestoreRecyclerOptions<Hospital> getOptions(Query query) {
        return new FirestoreRecyclerOptions.Builder<Hospital>()
                .setQuery( (com.google.firebase.firestore.Query) query, Hospital.class)
                .build();
    }
    public interface OnItemClickListener {
        void onItemClick(Hospital hospital);

    }
    public void updateOptions(FirestoreRecyclerOptions<Hospital> newOptions) {
        stopListening();
        super.updateOptions(newOptions); // Call the parent method
        notifyDataSetChanged();
        startListening();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
