package com.example.icusheba;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    ImageView drawerButton, profileButton, locationButton;
    TextView edUsername;
    FirebaseAuth sAuth;
    FirebaseFirestore fStore;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        EdgeToEdge.enable( this );
        setContentView( R.layout.activity_home );
        drawerLayout = findViewById( R.id.drawer_Layout );
        drawerButton = findViewById( R.id.buttonDrawerToggle );
        profileButton = findViewById( R.id.button_profile );
        locationButton = findViewById( R.id.button_location );
        edUsername = findViewById( R.id.usernameView );
        sAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        //noinspection DataFlowIssue
        userId = sAuth.getCurrentUser().getUid();

        CardView cardView1 = findViewById( R.id.Card_View_1 );
        cardView1.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( HomeActivity.this, ICU_Activity.class );
                intent.putExtra( "Title", "National Heart Foundation Hospital" );
                startActivity( intent );
            }
        } );
        CardView cardView2 = findViewById(R.id.Card_View_2);
        cardView2.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( HomeActivity.this, ICU_Activity.class );
                intent.putExtra( "Title", "LabAid Hospital" );
                startActivity( intent );
            }
        } );
        CardView cardView3 =findViewById(R.id.Card_View_3);
        cardView3.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( HomeActivity.this, ICU_Activity.class );
                intent.putExtra( "Title", "Ibrahim Cardiac Hospital" );
                startActivity( intent );
            }
        } );
        CardView cardView4 = findViewById(R.id.Card_View_4);
        cardView4.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( HomeActivity.this, ICU_Activity.class );
                intent.putExtra( "Title", "Bangladesh Specialized Hospital Ltd" );
                startActivity( intent );
            }
        } );
        CardView cardView5 = findViewById(R.id.Card_View_5);
        cardView5.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( HomeActivity.this, ICU_Activity.class );
                intent.putExtra( "Title", "United Hospital" );
                startActivity( intent );
            }
        } );
        CardView cardView6 = findViewById(R.id.Card_View_6);
        cardView6.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( HomeActivity.this, ICU_Activity.class );
                intent.putExtra( "Title", "Asgar Ali Hospital" );
                startActivity( intent );
            }
        } );

        profileButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu();
            }
        } );

        locationButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( HomeActivity.this, LocationActivity.class );
                startActivity( intent );
                finish();
            }
        } );

        drawerButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.open();
            }
        } );
    }
    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu( HomeActivity.this, profileButton );
        popupMenu.getMenuInflater().inflate( R.menu.popup_menu, popupMenu.getMenu() );
        popupMenu.setOnMenuItemClickListener( new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId()==R.id.button_logout){
                    Intent intent = new Intent( HomeActivity.this,LoginActivity.class );
                    startActivity( intent );
                    finish();
                }
                Toast.makeText( HomeActivity.this, item.getTitle(), Toast.LENGTH_SHORT ).show();

                return false;
            }
        } );
        popupMenu.show();

    }
}