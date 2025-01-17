package com.example.icusheba;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
public class LoginActivity extends AppCompatActivity {
    EditText edEmail, edPassword;
    TextView tv;
    TextView ForgetPassword;
    Button btn;
    FirebaseAuth sAuth;
    FirebaseFirestore sStore;
    boolean valid = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        EdgeToEdge.enable( this );
        setContentView( R.layout.activity_login );
        sAuth = FirebaseAuth.getInstance();
        sStore = FirebaseFirestore.getInstance();
        edEmail = findViewById( R.id.editTextEmail );
        edPassword = findViewById( R.id.editTextPassword );
        tv = findViewById( R.id.CreateAccount);
        ForgetPassword = findViewById( R.id.Forget_Password );
        btn = findViewById( R.id.Login_button );

        ForgetPassword.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailAddress = edEmail.getText().toString().trim();
                if (emailAddress == null || emailAddress.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter your email address", Toast.LENGTH_SHORT).show();
                    return;
                }
                sAuth.sendPasswordResetEmail(emailAddress).addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(LoginActivity.this,"Check your email",Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(LoginActivity.this,"Error"+ Objects.requireNonNull( task.getException() ).getMessage(),Toast.LENGTH_SHORT).show();
                        }

                    }
                } );
            }
        } );

        btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkField(edEmail);
                checkField(edPassword);
                if (valid) {
                    sAuth.signInWithEmailAndPassword(edEmail.getText().toString(),edPassword.getText().toString()).addOnSuccessListener( new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                            checkUserAccessLevel(sAuth.getCurrentUser().getUid());


                        }
                    } ).addOnFailureListener( new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();

                        }
                    } );
                }

            }
        } );



        tv.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),RegisterActivity.class) );
            }
        } );
        ViewCompat.setOnApplyWindowInsetsListener( findViewById( R.id.main ), (v, insets) -> {
            Insets systemBars = insets.getInsets( WindowInsetsCompat.Type.systemBars() );
            v.setPadding( systemBars.left, systemBars.top, systemBars.right, systemBars.bottom );
            return insets;
        } );
    }

    private void checkUserAccessLevel(String Uid) {
        DocumentReference df = sStore.collection("Users").document(Uid);
        //Extract the data for the document
        df.get().addOnSuccessListener( new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d("Tag","onSuccess: "+ documentSnapshot.getData());
                //identify User
                if (documentSnapshot.getString("isUser") != null) {
                    startActivity( new Intent( getApplicationContext(), HomeActivity.class ) );
                    finish();
                }
                if (documentSnapshot.getString("isAdmin") != null) {
                    startActivity( new Intent( getApplicationContext(), AdminActivity.class ) );
                    finish();
                }
            }
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