package com.example.icusheba;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import java.io.IOException;
import java.util.List;

/** @noinspection deprecation*/
public class LocationActivity extends AppCompatActivity implements
        GoogleMap.OnMyLocationButtonClickListener, OnMapReadyCallback {
    ImageView backButton, hospitalButton;
    private GoogleMap gMap;
    private static final int REQUEST_CODE = 1001;
    FusedLocationProviderClient fusedLocationClient;
    double currentLatitude, currentLongitude;
    double latitude, longitude;
    private SearchView mapSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        EdgeToEdge.enable( this );
        setContentView( R.layout.activity_location );
        backButton = findViewById( R.id.back_button );
        hospitalButton = findViewById( R.id.hospitals_nearby );
        mapSearchView = findViewById( R.id.searchView );


        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById( R.id.map );
        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync( this );
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient( this );

        mapSearchView.setOnQueryTextListener( new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = mapSearchView.getQuery().toString();
                List<Address> addressList = null;

                //noinspection ConstantValue,StringEqualsEmptyString
                if (location != null || !location.equals( "" )) {
                    Geocoder geocoder = new Geocoder( LocationActivity.this );
                    try {
                        addressList = geocoder.getFromLocationName( location, 1 );
                    } catch (IOException e) {
                        //noinspection CallToPrintStackTrace
                        e.printStackTrace();
                    }

                    if (addressList != null && !addressList.isEmpty()) {
                        Address address = addressList.get( 0 );
                        LatLng latLng = new LatLng( address.getLatitude(), address.getLongitude() );
                        gMap.addMarker( new MarkerOptions().position( latLng ).title( location ) );
                        gMap.animateCamera( CameraUpdateFactory.newLatLngZoom( latLng, 10 ) );
                    } else {
                        Toast.makeText( LocationActivity.this, "Location not found", Toast.LENGTH_SHORT ).show();
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }

        } );
        hospitalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchNearbyHospitals();
            }
        });
        //noinspection Convert2Lambda
        backButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent( getApplicationContext(), HomeActivity.class ) );
            }
        } );

        ViewCompat.setOnApplyWindowInsetsListener( findViewById( R.id.main ), (v, insets) -> {
            Insets systemBars = insets.getInsets( WindowInsetsCompat.Type.systemBars() );
            v.setPadding( systemBars.left, systemBars.top, systemBars.right, systemBars.bottom );
            return insets;
        } );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        //noinspection SwitchStatementWithTooFewBranches
        switch (REQUEST_CODE) {
            //noinspection DataFlowIssue
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d( "MapsActivity", "Location permission granted" ); // Debugging
                    // Permission granted, enable the My Location layer and button
                    enableMyLocation();
                    showCurrentLocationOnMap();
                } else {
                    Toast.makeText( this, "Location permission denied", Toast.LENGTH_SHORT ).show();
                }
        }
    }


    private void showCurrentLocationOnMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Task<Location> task = fusedLocationClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                // Get current latitude and longitude
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                LatLng currentLatLng = new LatLng(currentLatitude, currentLongitude);

                // Move camera to current location
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

                // Search for nearby hospitals
                searchNearbyHospitals();
            }
        }).addOnFailureListener(e -> {
            Log.e("Location", "Error getting location: " + e.getMessage());
            Toast.makeText(LocationActivity.this, "Error getting location", Toast.LENGTH_LONG).show();
        });
    }

    private void searchNearbyHospitals() {
        gMap.clear(); // Clear previous markers

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
                + "location=" + currentLatitude + "," + currentLongitude
                + "&radius=5000" // Search radius (5km)
                + "&type=hospital"
                + "&key=" + getString(R.string.google_api_key);

        // Logging for debugging
        Log.d("LocationActivity", "Searching for nearby hospitals with URL: " + url);

        new FetchNearbyPlaces(gMap).execute(url);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        // Check and request permissions
        checkLocationPermission();


    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE);
        } else {
            // Permission has already been granted
            enableMyLocation();
            showCurrentLocationOnMap();
            gMap.setOnMyLocationButtonClickListener(this);
        }
    }

    private void enableMyLocation() {
        Log.d( "LocationActivity", "enableMyLocation called" ); // Debugging
        if (gMap != null) {
            Log.d( "LocationActivity", "Map is not null" ); // Debugging
            if (ActivityCompat.checkSelfPermission( this,
                    Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission( this,
                    Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
                Log.d( "MapsActivity", "enableMyLocation: Permissions are not granted" );
                // Permissions are not granted, return
                return;
            }
            gMap.setMyLocationEnabled( true );
            gMap.getUiSettings().setMyLocationButtonEnabled( true );
            Log.d( "LocationActivity", "enableMyLocation finished" );
            // Debugging
        } else {

            Log.d( "LocationActivity", "Map is null" ); // Debugging
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            // Show dialog to enable location
            new AlertDialog.Builder(this)
                    .setMessage("Location is disabled. Please enable it in settings.")
                    .setPositiveButton("Enable", (dialog, which) -> {
                        Intent intent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true; // Prevent default behavior
        }

        // Location is enabled, so proceed with showing location
        Toast.makeText(this, "Fetching location...", Toast.LENGTH_SHORT).show();
        showCurrentLocationOnMap(); // Refresh location
        return false;
    }

}

