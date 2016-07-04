package com.hhalpha.daniel.homehunter12;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Daniel on 6/24/2016.
 */
public class MainActivity extends Activity implements OnMapReadyCallback {
    String profileName, loginMethod,email, salary;
    Boolean registered;
    TextView textViewMain;
    SharedPreferences preferences;
    Double lat, lng;

    Location myLocation;

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
        textViewMain=(TextView) findViewById(R.id.textViewMain);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        addresses=new ArrayList<String>();
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
                    bundle.putStringArrayList("arrayList",new ArrayList<String>(Arrays.asList(metadataArrayList.toString().split(","))));
                    bundle.putBoolean("firstTime",false);
                    i.putExtra("bundle",bundle);
                    startActivity(i);
                }

            });

        }catch(Exception e){
            e.printStackTrace();
        }

        try {
            Bundle bundle = getIntent().getBundleExtra("bundle");
            email=bundle.getString("email","");

//            salary=bundle.getString("salary","");
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
        lat=0.0;
        lng=0.0;
        setupMap();
        new retrieveTask().execute();
    }


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
    public void setupMap(){
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        try {
            // Enable MyLocation Layer of Google Map
            map.setMyLocationEnabled(true);
        }catch (SecurityException e){
            e.printStackTrace();
        }
        // Get LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);
        try {
            // Get Current Location
            myLocation = locationManager.getLastKnownLocation(provider);
        }catch (SecurityException e){
            e.printStackTrace();
        }
        try {
            //set map type
            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

            // Get latitude of the current location
            lat = myLocation.getLatitude();

            // Get longitude of the current location
            lng = myLocation.getLongitude();

            // Create a LatLng object for the current location
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


    }
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
                        byte[] bytes = IOUtils.toByteArray(content);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                        PropertyListEntry propertyListEntry = new PropertyListEntry(key, bitmap);
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
        protected void onPostExecute(ArrayList<String> strings) {
            try {
                Log.v("_danPostExecute",strings.toString());
                adapter.notifyDataSetChanged();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}