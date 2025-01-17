package com.example.icusheba;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    ImageView backButton, hospitalButton;
    private GoogleMap gMap;
    private SearchView mapSearchView;
    private String googleplaceData, url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        EdgeToEdge.enable( this );
        setContentView( R.layout.activity_location );
        backButton = findViewById( R.id.back_button );
        hospitalButton = findViewById( R.id.hospitals_nearby );
        mapSearchView = findViewById( R.id.searchView );

        backButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),HomeActivity.class));
            }
        } );


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById( R.id.map );
        mapFragment.getMapAsync(this );
        mapSearchView.setOnQueryTextListener( new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = mapSearchView.getQuery().toString();
                List<Address> addressList = null;
                if (location != null) {
                    Geocoder geocoder = new Geocoder(LocationActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(location,1);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
                    gMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    gMap.animateCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(latLng,10));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        } );

        ViewCompat.setOnApplyWindowInsetsListener( findViewById( R.id.main ), (v, insets) -> {
            Insets systemBars = insets.getInsets( WindowInsetsCompat.Type.systemBars() );
            v.setPadding( systemBars.left, systemBars.top, systemBars.right, systemBars.bottom );
            return insets;
        } );
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        gMap.getUiSettings().setZoomControlsEnabled( true );
        LatLng location = new LatLng( 23.6850, 90.3563 );
        googleMap.addMarker(new MarkerOptions().position(location).title("Bangladesh"));
        googleMap.moveCamera( CameraUpdateFactory.newLatLngZoom(location,12));

    }
}