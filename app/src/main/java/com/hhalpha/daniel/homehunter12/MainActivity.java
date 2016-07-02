package com.hhalpha.daniel.homehunter12;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.DefaultSyncCallback;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.AmazonS3;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 6/24/2016.
 */
public class MainActivity extends Activity {
    String profileName, loginMethod,email, salary;
    Boolean registered;
    TextView textViewMain;
    SharedPreferences preferences;

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
}
