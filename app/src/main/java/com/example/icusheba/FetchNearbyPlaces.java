package com.example.icusheba;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class FetchNearbyPlaces extends AsyncTask<String, Void, String> {
    private GoogleMap gMap;

    public FetchNearbyPlaces(GoogleMap gMap) {
        this.gMap = gMap;
    }

        @Override
        protected String doInBackground(String... strings) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
            } catch (Exception e) {
                Log.e("FetchError", "Error fetching data", e);
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONArray resultsArray = jsonObject.getJSONArray("results");

                if (resultsArray.length() == 0) {
                    Log.e("FetchNearbyPlaces", "No results found.");
                    return;
                }

                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject place = resultsArray.getJSONObject(i);
                    JSONObject location = place.getJSONObject("geometry").getJSONObject("location");
                    String name = place.getString("name");
                    double lat = location.getDouble("lat");
                    double lng = location.getDouble("lng");

                    LatLng latLng = new LatLng(lat, lng);
                    gMap.addMarker(new MarkerOptions().position(latLng).title(name));
                }

            } catch (Exception e) {
                Log.e("FetchError", "Error parsing JSON", e);
            }
        }
    }