package com.example.icusheba;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    EditText editTextEmail, editTextPassword;
    Button loginButton;
    TextView createAccount, forgetPassword;
    ImageView eyeIcon;

    boolean isPasswordVisible = false;
    FirebaseAuth sAuth;
    FirebaseFirestore sStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sAuth = FirebaseAuth.getInstance();
        sStore = FirebaseFirestore.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.Login_button);
        createAccount = findViewById(R.id.CreateAccount);
        forgetPassword = findViewById(R.id.Forget_Password);
        eyeIcon = findViewById(R.id.eyeIcon);

        // Password show/hide toggle
        eyeIcon.setOnClickListener(v -> {
            if (isPasswordVisible) {
                editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eyeIcon.setImageResource(R.drawable.ic_eye_show);
            } else {
                editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                eyeIcon.setImageResource(R.drawable.ic_eye_hide);
            }
            editTextPassword.setSelection(editTextPassword.getText().length());
            isPasswordVisible = !isPasswordVisible;
        });

        // Login button click handler
        loginButton.setOnClickListener(v -> {
            if (!isInternetAvailable()) {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                return;
            }

            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (email.isEmpty()) {
                editTextEmail.setError("Email required");
                editTextEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                editTextPassword.setError("Password required");
                editTextPassword.requestFocus();
                return;
            }

            sAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                        checkUserAccessLevel(Objects.requireNonNull(sAuth.getCurrentUser()).getUid());
                    })
                    .addOnFailureListener(e -> {
                        if (e instanceof FirebaseAuthInvalidUserException) {
                            Toast.makeText(LoginActivity.this, "Invalid Email", Toast.LENGTH_SHORT).show();
                            editTextEmail.requestFocus();
                        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(LoginActivity.this, "Invalid Password", Toast.LENGTH_SHORT).show();
                            editTextPassword.requestFocus();
                        } else {
                            Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Create account click
        createAccount.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        // Forgot password click
        forgetPassword.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter email to reset password", Toast.LENGTH_SHORT).show();
                return;
            }

            sAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm != null ? cm.getActiveNetworkInfo() : null;
        //noinspection deprecation
        return netInfo != null && netInfo.isConnected();
    }

    private void checkUserAccessLevel(String uid) {
        sStore.collection("Users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        // Check User role
                        if (documentSnapshot.getString("isUser") != null) {
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                            return;
                        }

                        // Check Admin role
                        if (documentSnapshot.getString("isAdmin") != null) {
                            startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                            finish();
                            return;
                        }

                        // Dynamic Hospital access check
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data != null) {
                            for (Map.Entry<String, Object> entry : data.entrySet()) {
                                String key = entry.getKey();
                                Object value = entry.getValue();

                                if (key.startsWith("is") && !key.equals("isUser") && !key.equals("isAdmin")) {
                                    if ((value instanceof Boolean && (Boolean) value) ||
                                            (value instanceof String && !((String) value).isEmpty())) {
                                        startActivity(new Intent(LoginActivity.this, HrDepartmentActivity.class));
                                        finish();
                                        return;
                                    }
                                }
                            }
                        }

                        Toast.makeText(LoginActivity.this, "No valid access role found.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(LoginActivity.this, "Access check failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Auto-login if already logged in user detected
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = sAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserAccessLevel(currentUser.getUid());
        }
    }
}
