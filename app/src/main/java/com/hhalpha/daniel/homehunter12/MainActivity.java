package com.hhalpha.daniel.homehunter12;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewMain=(TextView) findViewById(R.id.textViewMain);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        try {
            Bundle bundle = getIntent().getBundleExtra("bundle");
            email=bundle.getString("email","");
            salary=bundle.getString("salary","");
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
        //set map type
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Get latitude of the current location
        double latitude = myLocation.getLatitude();

        // Get longitude of the current location
        double longitude = myLocation.getLongitude();

        // Create a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);

        try {
            map.setMyLocationEnabled(true);
        }catch (SecurityException e){
            Log.v("_dan mapsec", e.getMessage());
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

    }

}
