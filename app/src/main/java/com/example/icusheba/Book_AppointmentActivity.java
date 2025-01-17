package com.example.icusheba;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Book_AppointmentActivity extends AppCompatActivity {

    EditText ed1,ed2,ed3,ed4;
    TextView tv;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        EdgeToEdge.enable( this );
        setContentView( R.layout.activity_book_appointment );
        tv = findViewById( R.id.textViewAPTitle);
        ed1 = findViewById( R.id.editTextPatientName );
        ed2 = findViewById( R.id.editTextAddress );
        ed3 = findViewById( R.id.editTextNumber );
        ed4 = findViewById( R.id.editTextGardian );

        ed1.setKeyListener(null);
        ed2.setKeyListener(null);
        ed3.setKeyListener(null);
        ed4.setKeyListener(null);
        Intent it = getIntent();
        String title = it.getStringExtra( "text1" );
        String name = it.getStringExtra( "text2" );
        String address = it.getStringExtra( "text3" );
        String number = it.getStringExtra( "text4" );
        String gardian = it.getStringExtra( "text5" );
        tv.setText( title );
        ed1.setText( name );
        ed2.setText( address );
        ed3.setText( number );
        ed4.setText("Cons Fees:"+ gardian+"/-" );


        ViewCompat.setOnApplyWindowInsetsListener( findViewById( R.id.main ), (v, insets) -> {
            Insets systemBars = insets.getInsets( WindowInsetsCompat.Type.systemBars() );
            v.setPadding( systemBars.left, systemBars.top, systemBars.right, systemBars.bottom );
            return insets;
        } );
    }
}