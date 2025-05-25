package com.example.icusheba;

import android.os.AsyncTask;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

    String googlePlacesData;
    GoogleMap gMap;
    String url;

    @Override
    protected String doInBackground(Object... objects) {
        gMap = (GoogleMap)objects[0];
        url = (String)objects[1];
        DownloadUrl downloadUrl = new DownloadUrl();
        try {
            googlePlacesData = downloadUrl.readUrl(url);
        } catch (IOException e) {
            throw new RuntimeException( e );
        }
        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String s) {
        List<HashMap<String,String>> nearbyPlaceList = null;
        DataParser dataParser = new DataParser();
        nearbyPlaceList = dataParser.parse(s);
        showNearbyPlaces(nearbyPlaceList);
    }
    private void showNearbyPlaces(List<HashMap<String,String>>nearbyPlacesList){
        for (int i = 0;i<nearbyPlacesList.size();i++){
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String,String>googlePlace = nearbyPlacesList.get(i);
            String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));
            LatLng latLng = new LatLng(lat,lng);
            markerOptions.position(latLng);
            markerOptions.title(placeName+" : "+vicinity);
            markerOptions.icon( BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE) );
            gMap.addMarker(markerOptions);
            gMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            gMap.animateCamera( CameraUpdateFactory.zoomTo(10));
        }
    }
}
