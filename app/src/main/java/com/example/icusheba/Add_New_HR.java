package com.example.icusheba;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Add_New_HR extends AppCompatActivity {
    private static final String TAG = "AddNewHrActivity";
    private static final String FULL_NAME_KEY = "Full Name";
    private static final String EMAIL_KEY = "Email";
    private static final String PHONE_NUMBER_KEY = "Phone Number";
    private static final String PASSWORD_KEY = "Password";
    private static final String HOSPITAL_KEY = "Hospital";
    private static final String IS_USER_VALUE = "1";

    private AutoCompleteTextView autoCompleteTextView;
    private EditText edUsername, edUseremail, edNumber, edConfirmPassword;
    private Button btnAddHr;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_new_hr);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        initializeViews();
        loadHospitalListFromFirestore();
        setupButtonClickListener();
        setupWindowInsets();
    }

    private void initializeViews() {
        edUsername = findViewById(R.id.Hr_Name);
        edUseremail = findViewById(R.id.Hr_Email);
        edNumber = findViewById(R.id.Hr_Number);
        autoCompleteTextView = findViewById(R.id.Auto_complete_text);
        edConfirmPassword = findViewById(R.id.Hr_Password);
        btnAddHr = findViewById(R.id.Hr_Add_Button);
    }

    private void loadHospitalListFromFirestore() {
        firestore.collection("Hospital_Data")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> hospitalList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String hospitalName = document.getId(); // Document ID নিচ্ছি
                        hospitalList.add(hospitalName);
                        Log.d(TAG, "Hospital Loaded: " + hospitalName);
                    }

                    ArrayAdapter<String> adapterItems = new ArrayAdapter<>(this, R.layout.list_item, hospitalList);
                    autoCompleteTextView.setAdapter(adapterItems);

                    // Optional: Auto show dropdown on focus
                    autoCompleteTextView.setOnFocusChangeListener((v, hasFocus) -> {
                        if (hasFocus) {
                            autoCompleteTextView.showDropDown();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Add_New_HR.this, "Failed to load hospital list", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading hospital list", e);
                });
    }

    private void setupButtonClickListener() {
        btnAddHr.setOnClickListener(v -> {
            if (validateFields()) {
                registerNewHr();
            }
        });
    }

    private boolean validateFields() {
        boolean isValid = true;

        isValid &= checkField(edUsername, "Name is required");
        isValid &= checkField(edUseremail, "Email is required");
        isValid &= checkField(edNumber, "Phone number is required");
        isValid &= checkField(edConfirmPassword, "Password is required");
        isValid &= checkField(autoCompleteTextView);

        if (isValid && !isEmailValid(edUseremail.getText().toString())) {
            edUseremail.setError("Invalid email format");
            isValid = false;
        }
        if (isValid && edConfirmPassword.length() < 6) {
            edConfirmPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }
        return isValid;
    }

    private boolean checkField(EditText textField, String errorMessage) {
        if (TextUtils.isEmpty(textField.getText().toString())) {
            textField.setError(errorMessage);
            return false;
        }
        return true;
    }

    private boolean checkField(AutoCompleteTextView textField) {
        if (TextUtils.isEmpty(textField.getText().toString())) {
            textField.setError("Hospital is required");
            return false;
        }
        return true;
    }

    private void registerNewHr() {
        String email = edUseremail.getText().toString();
        String password = edConfirmPassword.getText().toString();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(this::onRegistrationSuccess)
                .addOnFailureListener(this::onRegistrationFailure);
    }

    private void onRegistrationSuccess(AuthResult authResult) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            Toast.makeText(Add_New_HR.this, "HR Registered Successfully", Toast.LENGTH_SHORT).show();
            saveHrDataToFirestore(user);
            clearInputFields();
        }
    }

    private void saveHrDataToFirestore(FirebaseUser user) {
        String uid = user.getUid();
        DocumentReference documentReference = firestore.collection("Users").document(uid);
        Map<String, Object> hrInfo = new HashMap<>();
        hrInfo.put(FULL_NAME_KEY, edUsername.getText().toString());
        hrInfo.put(EMAIL_KEY, edUseremail.getText().toString());
        hrInfo.put(PHONE_NUMBER_KEY, edNumber.getText().toString());
        hrInfo.put(PASSWORD_KEY, edConfirmPassword.getText().toString());
        hrInfo.put(HOSPITAL_KEY, autoCompleteTextView.getText().toString());
        hrInfo.put("is" + autoCompleteTextView.getText().toString(), IS_USER_VALUE);

        documentReference.set(hrInfo)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "HR data saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving HR data", e));
    }

    private void onRegistrationFailure(@NonNull Exception e) {
        Log.e(TAG, "Registration failed", e);
        if (e instanceof FirebaseAuthWeakPasswordException) {
            edConfirmPassword.setError("Weak password. Please use a stronger password.");
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            edUseremail.setError("Invalid email format.");
        } else if (e instanceof FirebaseAuthUserCollisionException) {
            edUseremail.setError("Email already in use.");
        } else {
            Toast.makeText(Add_New_HR.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void clearInputFields() {
        edUsername.setText("");
        edUseremail.setText("");
        edNumber.setText("");
        edConfirmPassword.setText("");
        autoCompleteTextView.setText("");
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
