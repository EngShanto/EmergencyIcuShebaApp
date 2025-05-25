package com.example.icusheba;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText edUsername, edUseremail, edNumber, edCurrentPassword, edConfirmPassword;
    ImageView eyeIconCurrent, eyeIconConfirm;
    TextView tv;
    Button btn;
    ImageView backButton, infoButton;
    boolean valid = true;
    FirebaseAuth sAuth;
    FirebaseFirestore sStore;

    boolean isCurrentVisible = false;
    boolean isConfirmVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Firebase
        sAuth = FirebaseAuth.getInstance();
        sStore = FirebaseFirestore.getInstance();

        // Bind views
        edUsername = findViewById(R.id.editTextUsername);
        edUseremail = findViewById(R.id.editTextUseremail);
        edNumber = findViewById(R.id.editTextNumber);
        edCurrentPassword = findViewById(R.id.editTextCurrentPassword);
        edConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        eyeIconCurrent = findViewById(R.id.eyeIconCurrent);
        eyeIconConfirm = findViewById(R.id.eyeIconConfirm);
        tv = findViewById(R.id.textView);
        btn = findViewById(R.id.SignUp_button);
        backButton = findViewById(R.id.back_login);
        infoButton = findViewById(R.id.button_info);

        // Show/hide toggle for current password
        eyeIconCurrent.setOnClickListener(v -> {
            if (isCurrentVisible) {
                edCurrentPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eyeIconCurrent.setImageResource(R.drawable.ic_eye_show);
            } else {
                edCurrentPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                eyeIconCurrent.setImageResource(R.drawable.ic_eye_hide);
            }
            edCurrentPassword.setSelection(edCurrentPassword.getText().length());
            isCurrentVisible = !isCurrentVisible;
        });

        // Show/hide toggle for confirm password
        eyeIconConfirm.setOnClickListener(v -> {
            if (isConfirmVisible) {
                edConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eyeIconConfirm.setImageResource(R.drawable.ic_eye_show);
            } else {
                edConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                eyeIconConfirm.setImageResource(R.drawable.ic_eye_hide);
            }
            edConfirmPassword.setSelection(edConfirmPassword.getText().length());
            isConfirmVisible = !isConfirmVisible;
        });

        // Sign Up
        btn.setOnClickListener(v -> {
            checkField(edUsername);
            checkField(edUseremail);
            checkField(edNumber);
            checkField(edCurrentPassword);
            checkField(edConfirmPassword);

            String currentPassword = edCurrentPassword.getText().toString().trim();
            String confirmPassword = edConfirmPassword.getText().toString().trim();

            if (!currentPassword.equals(confirmPassword)) {
                edConfirmPassword.setError("Password does not match");
                Toast.makeText(this, "Password incorrect", Toast.LENGTH_SHORT).show();
                return;
            }

            if (valid) {
                sAuth.createUserWithEmailAndPassword(edUseremail.getText().toString(), confirmPassword)
                        .addOnSuccessListener(authResult -> {
                            FirebaseUser user = sAuth.getCurrentUser();
                            Toast.makeText(RegisterActivity.this, "Record Inserted", Toast.LENGTH_SHORT).show();
                            assert user != null;
                            DocumentReference df = sStore.collection("Users").document(user.getUid());
                            Map<String, Object> userInfo = new HashMap<>();
                            userInfo.put("Full Name", edUsername.getText().toString());
                            userInfo.put("Email", edUseremail.getText().toString());
                            userInfo.put("Phone Number", edNumber.getText().toString());
                            userInfo.put("Password", confirmPassword);
                            userInfo.put("isUser", "1");
                            df.set(userInfo);

                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        // Navigation
        tv.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), LoginActivity.class)));
        backButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), LoginActivity.class)));

        // Padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void checkField(EditText textField) {
        if (textField.getText().toString().trim().isEmpty()) {
            textField.setError("Field is required");
            valid = false;
        } else {
            valid = true;
        }
    }
}
