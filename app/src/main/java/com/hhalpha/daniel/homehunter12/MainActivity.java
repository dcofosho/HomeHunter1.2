package com.hhalpha.daniel.homehunter12;
//imports
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.iterable.S3Objects;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


/**
 * Created by Daniel on 6/24/2016.
 */
public class MainActivity extends Activity implements OnMapReadyCallback {
    //declarations
    String profileName, loginMethod,email;
    Boolean registered,usingGps,gettingPropLocation;
    TextView textViewMain, textViewLoading;
    SharedPreferences preferences;
    Double lat, lng, propLat, propLong;
    int salary;
    Location myLocation;
    EditText editTextLocation;
    CustomListViewAdapter adapter;
    ArrayList<PropertyListEntry> propertyListEntries;
    ListView listView;
    ArrayList<String> metadataArrayList;
    ArrayList<String> addresses;

    AmazonS3 s3;
    TransferUtility transferUtility;
    CognitoCachingCredentialsProvider credentialsProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usingGps=true;
        gettingPropLocation=false;

        //assignments
        textViewMain=(TextView) findViewById(R.id.textViewMain);
        textViewLoading=(TextView) findViewById(R.id.textViewLoading);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        addresses=new ArrayList<String>();
        editTextLocation=(EditText) findViewById(R.id.editTextLocation);
        //FB credentials for S3
        FacebookSdk.sdkInitialize(getApplicationContext());
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:f297743b-8f2b-4874-8bef-3ee300d8b4a3", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        Map<String, String> logins = new HashMap<String, String>();
        logins.put("graph.facebook.com", AccessToken.getCurrentAccessToken().getToken());
        credentialsProvider.setLogins(logins);
        s3 = new AmazonS3Client(credentialsProvider);
        // Set the region of your S3 bucket
        s3.setRegion(Region.getRegion(Regions.US_EAST_1));
        //listview adapter/etc.
        //sort List by distance
        try{
            for(int i=0;i<propertyListEntries.size();i++){
                propertyListEntries.get(i).setDistance(distance(lat,lng,propertyListEntries.get(i).getPropLat(),propertyListEntries.get(i).getPropLng()));
            }
            Collections.sort(propertyListEntries,new Comparator<PropertyListEntry>() {
                @Override
                public int compare(PropertyListEntry p1, PropertyListEntry p2) {
                    return p1.getDistance().compareTo(p2.getDistance());
                }
            });
            adapter.notifyDataSetChanged();
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            Log.v("_danoncreate",getApplicationContext().getCacheDir().listFiles().toString());
            propertyListEntries = new ArrayList<PropertyListEntry>();
            metadataArrayList=new ArrayList<String>();
            adapter = new CustomListViewAdapter(getApplicationContext(), R.layout.list_layout1, propertyListEntries);
            listView = (ListView) findViewById(R.id.listView);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.v("_dan click",metadataArrayList.toString());
                    Bundle bundle=new Bundle();
                    Intent i = new Intent(MainActivity.this, PropertyActivity.class);
                    bundle.putStringArrayList("arrayList",new ArrayList<String>(Arrays.asList(metadataArrayList.get(position).toString().split(","))));
                    bundle.putBoolean("firstTime",false);
                    i.putExtra("bundle",bundle);
                    startActivity(i);
                }

            });

        }catch(Exception e){
            e.printStackTrace();
        }
        //get registration info from bundle
        try {
            Bundle bundle = getIntent().getBundleExtra("bundle");
            email=bundle.getString("email","");

            salary=bundle.getInt("salary",0);
            Log.v("_dan",email+salary);

        }catch(Exception e){
            e.printStackTrace();
        }
        try {
            Bundle bundle = getIntent().getBundleExtra("bundle");
            profileName=bundle.getString("profileName","");
            loginMethod=bundle.getString("loginMethod","");
            registered=bundle.getBoolean("registered",false);
            textViewMain.setText(preferences.getAll().toString());
            Log.v("_danMain",bundle.toString());
        }catch(Exception e){
            e.printStackTrace();
        }

//        //initialize map
//        lat=0.0;
//        lng=0.0;
        new retrieveTask().execute();

    }

    //clear registration info
    public void clear (View v){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("registered",false);
        editor.putString("loginMethod","");
        editor.putString("profileName","");
        editor.putString("email","");
        editor.putInt("salary",0);
        editor.apply();
        Intent i = new Intent(MainActivity.this,SplashActivity.class);
        startActivity(i);
    }
    public void gpsBtn(View v){
        usingGps=true;
        setupMap();
    }
    //setupMap
    public void setupMap(){
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    //onMapReady
    @Override
    public void onMapReady(GoogleMap map) {
        try {
            // Enable MyLocation Layer of Google Map
            map.setMyLocationEnabled(true);
        }catch (SecurityException e){
            e.printStackTrace();
        }
        if(usingGps) {
            // Get LocationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // Create a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            // Get the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);
            try {
                // Get Current Location
                myLocation = locationManager.getLastKnownLocation(provider);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            try {
                //set map type
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

                // Get latitude of the current location
                lat = myLocation.getLatitude();

                // Get longitude of the current location
                lng = myLocation.getLongitude();


            } catch (Exception e) {
                e.printStackTrace();
            }



        }
        //sort List by distance
        try{
            for(int i=0;i<propertyListEntries.size();i++){
                propertyListEntries.get(i).setDistance(distance(lat,lng,propertyListEntries.get(i).getPropLat(),propertyListEntries.get(i).getPropLng()));
            }
            for(int i=0;i<propertyListEntries.size()-1;i++){
                if(propertyListEntries.get(i).getDistance()>propertyListEntries.get(i+1).getDistance()){
                    Collections.swap(propertyListEntries,i,i+1);
                }
            }
            Collections.sort(propertyListEntries,new Comparator<PropertyListEntry>() {
                @Override
                public int compare(PropertyListEntry p1, PropertyListEntry p2) {
                    return p1.getDistance().compareTo(p2.getDistance());
                }
            });
            adapter.notifyDataSetChanged();
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            LatLng latLng = new LatLng(lat, lng);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            map.setMyLocationEnabled(true);
        }catch (SecurityException e){
            Log.v("_dan mapsec", e.getMessage());
        }
        textViewLoading.setText("");

    }
    //async task for getting location from address string
    private class latLngFromAddressTask extends AsyncTask<String, Void, String[]> {


        @Override
        protected String[] doInBackground(String... params) {
            String response;
            try {
                response = getLatLongByURL("http://maps.google.com/maps/api/geocode/json?address="+params[0].replace(",","").replace(" ","+")+"&sensor=false");
                Log.d("response",""+response);
                return new String[]{response};
            } catch (Exception e) {
                Log.v("_dan gmbackground",e.getMessage());
                return new String[]{"error"};
            }
        }


        @Override
        protected void onPostExecute(String... result) {
            try {
                JSONObject jsonObject = new JSONObject(result[0]);
//                if(!gettingPropLocation) {
                    lng = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                            .getJSONObject("geometry").getJSONObject("location")
                            .getDouble("lng");

                    lat = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                            .getJSONObject("geometry").getJSONObject("location")
                            .getDouble("lat");
                    Log.d("latitude", "" + lat);
                    Log.d("longitude", "" + lng);
//                }
//                if(gettingPropLocation){
//                    propLong = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
//                            .getJSONObject("geometry").getJSONObject("location")
//                            .getDouble("lng");
//
//                    propLat = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
//                            .getJSONObject("geometry").getJSONObject("location")
//                            .getDouble("lat");
//                    Log.d("latitude", "" + propLat);
//                    Log.d("longitude", "" + propLong);
//                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            try{
                for(int i=0;i<propertyListEntries.size();i++){
                    propertyListEntries.get(i).setDistance(distance(lat,lng,propertyListEntries.get(i).getPropLat(),propertyListEntries.get(i).getPropLng()));
                }
                for(int i=0;i<propertyListEntries.size()-1;i++){
                    if(propertyListEntries.get(i).getDistance()>propertyListEntries.get(i+1).getDistance()){
                        Collections.swap(propertyListEntries,i,i+1);
                    }
                }
                Collections.sort(propertyListEntries,new Comparator<PropertyListEntry>() {
                    @Override
                    public int compare(PropertyListEntry p1, PropertyListEntry p2) {
                        return p1.getDistance().compareTo(p2.getDistance());
                    }
                });
                adapter.notifyDataSetChanged();
            }catch (Exception e){
                e.printStackTrace();
            }
            setupMap();
        }
    }
    //method for getting lat and lng from a location URL
    public String getLatLongByURL(String requestURL) {
        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";
            }

        } catch (Exception e) {
            Log.v("_danlatlngbyurl",e.getMessage());
            e.printStackTrace();
        }
        return response;
    }
    public void goToTextLocation(View v){
        usingGps=false;
        new latLngFromAddressTask().execute(editTextLocation.getText().toString());
    }
    //retreive S3
    public class retrieveTask extends AsyncTask<String,Integer,ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            try {


                transferUtility = new TransferUtility(s3, getApplicationContext());
                for (S3ObjectSummary summary : S3Objects.inBucket(s3, "hhproperties")) {
                    try {
                        Log.v("_dan", summary.getKey());
                        String key = summary.getKey();
                        S3ObjectInputStream content = s3.getObject("hhproperties", key).getObjectContent();
                        ObjectMetadata metadata = s3.getObject("hhproperties", key).getObjectMetadata();
                        Log.v("_dan meta",metadata.getUserMetaDataOf("info").toString());
                        metadataArrayList.add(metadata.getUserMetaDataOf("info").toString());
                        Log.v("_dan meta",metadata.getUserMetaDataOf("coords").toString());
                        metadataArrayList.add(metadata.getUserMetaDataOf("coords").toString());
                        Double propertyLatitude=Double.parseDouble(metadata.getUserMetaDataOf("coords").toString().replace("[","").replace("]","").split(",")[0]);
                        Double propertyLongitude=Double.parseDouble(metadata.getUserMetaDataOf("coords").toString().replace("[","").replace("]","").split(",")[1]);
                        Double propertyDistance = distance(lat,lng,propertyLatitude,propertyLongitude);
                        byte[] bytes = IOUtils.toByteArray(content);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                        PropertyListEntry propertyListEntry = new PropertyListEntry(key, bitmap,propertyDistance,propertyLatitude,propertyLongitude);
                        propertyListEntry.setPic(bitmap);
                        propertyListEntry.setPropertyText(key);
                        if(!addresses.toString().contains(summary.getKey().split("/")[0])) {
                            propertyListEntries.add(propertyListEntry);
                            addresses.add(summary.getKey());
                        }
                        Log.v("_dan", addresses.toString());
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            return metadataArrayList;
        }

        @Override
        protected void onPreExecute() {
            setupMap();
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            try {
                Log.v("_danPostExecute",strings.toString());
                try{
                    for(int i=0;i<propertyListEntries.size();i++){
                        propertyListEntries.get(i).setDistance(distance(lat,lng,propertyListEntries.get(i).getPropLat(),propertyListEntries.get(i).getPropLng()));
                    }
                    for(int i=0;i<propertyListEntries.size()-1;i++){
                        if(propertyListEntries.get(i).getDistance()>propertyListEntries.get(i+1).getDistance()){
                            Collections.swap(propertyListEntries,i,i+1);
                        }
                    }
                    Collections.sort(propertyListEntries,new Comparator<PropertyListEntry>() {
                        @Override
                        public int compare(PropertyListEntry p1, PropertyListEntry p2) {
                            return p1.getDistance().compareTo(p2.getDistance());
                        }
                    });
                    adapter.notifyDataSetChanged();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }


    private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

    public static double distance(double startLat, double startLong,
                                  double endLat, double endLong) {

        double dLat  = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat   = Math.toRadians(endLat);

        double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // <-- d
    }

    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
}