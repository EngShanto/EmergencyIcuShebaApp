package com.example.icusheba;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    EditText edUsername, edUseremail, edNumber, edCurrentPassword, edConfirmPassword;
    TextView tv;
    Button btn;
    ImageView backButton, infoButton;
    boolean valid = true;
    FirebaseAuth sAuth;
    FirebaseFirestore sStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        EdgeToEdge.enable( this );
        setContentView( R.layout.activity_register );
        sAuth = FirebaseAuth.getInstance();
        sStore = FirebaseFirestore.getInstance();
        edUsername = findViewById( R.id.editTextUsername );
        edUseremail = findViewById( R.id.editTextUseremail );
        edNumber = findViewById( R.id.editTextNumber );
        edCurrentPassword = findViewById( R.id.editTextCurrentPassword );
        edConfirmPassword = findViewById( R.id.editTextConfirmPassword );
        tv = findViewById( R.id.textView );
        btn = findViewById( R.id.SignUp_button );
        backButton = findViewById( R.id.back_login );
        infoButton = findViewById( R.id.button_info );

        btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkField( edUsername );
                checkField( edUseremail );
                checkField( edNumber );
                checkField( edCurrentPassword );
                checkField( edConfirmPassword );
                if (valid) {
                    //Start Registation Process
                    sAuth.createUserWithEmailAndPassword( edUseremail.getText().toString(), edConfirmPassword.getText().toString() ).addOnSuccessListener( new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            FirebaseUser User = sAuth.getCurrentUser();
                            Toast.makeText( RegisterActivity.this, "Record Inserted", Toast.LENGTH_SHORT ).show();
                            DocumentReference df = sStore.collection( "Users" ).document( User.getUid() );
                            Map<String, Object> userInfo = new HashMap<>();
                            userInfo.put( "Full Name", edUsername.getText().toString() );
                            userInfo.put( "Email", edUseremail.getText().toString() );
                            userInfo.put( "Phone Number", edNumber.getText().toString() );
                            userInfo.put( "Password", edConfirmPassword.getText().toString() );

                            userInfo.put("isUser","1");
                            df.set(userInfo);

                            startActivity( new Intent( getApplicationContext(), LoginActivity.class ) );
                            finish();


                        }
                    } ).addOnFailureListener( new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText( RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT ).show();
                        }
                    } );
                }
            }
        } );
        tv.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            }
        } );
        backButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            }
        } );

        ViewCompat.setOnApplyWindowInsetsListener( findViewById( R.id.main ), (v, insets) -> {
            Insets systemBars = insets.getInsets( WindowInsetsCompat.Type.systemBars() );
            v.setPadding( systemBars.left, systemBars.top, systemBars.right, systemBars.bottom );
            return insets;
        } );
    }

    public boolean checkField(EditText textField) {
        if (textField.getText().toString().isEmpty()) {
            textField.setError( "Error" );
            valid = false;
        } else {
            valid = true;
    }
        return valid;
    }

}