package com.example.icusheba;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataParser {
    public HashMap<String,String>getPlace(JSONObject googlePlaceJson)
    {
        HashMap<String,String>googlePlaceMap=new HashMap<>();
        String NameOfPlace="name";
        String vicinity="vicinity";
        String latitude="lat";
        String longitude="lng";
        String reference="reference";
        try {
            if (!googlePlaceJson.isNull("name")){
                NameOfPlace = googlePlaceJson.getString("name");
            }
            if (!googlePlaceJson.isNull("vicinity")){
                vicinity = googlePlaceJson.getString("vicinity");
            }
             latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
             longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");
             reference = googlePlaceJson.getString("reference");

             googlePlaceMap.put("Place_name",NameOfPlace);
             googlePlaceMap.put("Place_vicinity",vicinity);
             googlePlaceMap.put("Place_lat",latitude);
             googlePlaceMap.put("Place_lng",longitude);
             googlePlaceMap.put("Place_reference",reference);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return googlePlaceMap;
    }
    private List<HashMap<String,String>> getPlaces(JSONArray jsonArray)
    {
        int count = jsonArray.length();
        List<HashMap<String,String>>placeList=new ArrayList<>();
        HashMap<String,String>placeMap = null;
        for (int i = 0;i<count;i++)
        {
            try {
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                placeList.add(placeMap);
            } catch (JSONException e) {
                throw new RuntimeException( e );
            }
        }
        return placeList;
    }
    public List<HashMap<String,String>>parse(String jsonData)
    {
        JSONArray jsonArray = null;
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            throw new RuntimeException( e );
    }
        return getPlaces(jsonArray);
    }
}
