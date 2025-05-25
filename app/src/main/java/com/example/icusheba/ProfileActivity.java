package com.example.icusheba;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView fullName, email, phone;
    private ImageView profileImage;
    private Button uploadBtn, saveBtn;
    private Uri imageUri;

    FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        fullName = findViewById(R.id.nameEditText);
        email = findViewById(R.id.emailEditText);
        phone = findViewById(R.id.phoneEditText);
        profileImage = findViewById(R.id.profileImageView);
        uploadBtn = findViewById(R.id.changeImageButton);
        saveBtn = findViewById(R.id.uploadImageButton);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("profileImages");

        // ✅ TextFields
        fullName.setEnabled(false);
        email.setEnabled(false);
        phone.setEnabled(false);

        loadUserProfile();

        // ✅ Change Image বাটনে ছবি সিলেক্ট
        uploadBtn.setOnClickListener(v -> openFileChooser());

        // ✅ Upload বাটনে Firebase এ ইমেজ আপলোড
        saveBtn.setOnClickListener(v -> {
            if (imageUri != null) {
                uploadImageToFirebase(imageUri);
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Firebase থেকে ইউজারের প্রোফাইল ডেটা লোড
    @SuppressLint("SetTextI18n")
    private void loadUserProfile() {
        String uid = mAuth.getUid();
        if (uid != null) {
            fStore.collection("Users").document(uid).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            fullName.setText("Name: "+document.getString("Full Name"));
                            email.setText("Email: "+document.getString("Email"));
                            phone.setText("Phone Number: "+document.getString("Phone Number"));

                            String imageUrl = document.getString("imageUrl");
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(this).load(imageUrl).into(profileImage);
                            }
                        }
                    });
        }
    }

    // ✅ গ্যালারি থেকে ছবি সিলেক্ট
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // ✅ সিলেক্ট করা ছবিকে ImageView-তে দেখানো
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
    }

    // ✅ Firebase Storage-এ ইমেজ আপলোড ও Firestore-এ imageUrl
    private void uploadImageToFirebase(Uri uri) {
        String uid = mAuth.getUid();
        if (uid == null) return;

        StorageReference fileRef = storageReference.child(uid + ".jpg");

        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            fStore.collection("Users").document(uid)
                                    .update("imageUrl", downloadUri.toString());
                            Toast.makeText(this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show());
    }
}
