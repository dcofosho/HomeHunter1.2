package com.hhalpha.daniel.homehunter12;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.DefaultSyncCallback;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.facebook.AccessToken;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 6/28/2016.
 */
public class RegisterActivity2 extends Activity {
    EditText editTextSalary,editTextEmail;
    Integer salary;
    String email,loginMethod,profileName;
    Boolean registered;
    Button reg2Button;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Bundle bundle;
    private GoogleApiClient client;
    private CognitoCachingCredentialsProvider credentialsProvider;
    CognitoSyncManager syncClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);
        bundle = new Bundle();
        editTextSalary=(EditText) findViewById(R.id.editTextSalary);
        editTextEmail=(EditText)findViewById(R.id.editTextEmail);
        reg2Button=(Button) findViewById(R.id.reg2Button);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = preferences.edit();
        try{
            profileName=preferences.getString("profileName","");
            loginMethod=preferences.getString("loginMethod","");
            registered=preferences.getBoolean("registered",false);
            bundle.putString("profileName",profileName);
            bundle.putString("loginMethod",loginMethod);
            bundle.putBoolean("registered",registered);
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            cognito();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void submit(View v){


        salary=Integer.parseInt(editTextSalary.getText().toString());
        email=editTextEmail.getText().toString();
        editor.putString("email",email);
        editor.putInt("salary",salary);
        editor.apply();
        Intent i = new Intent(RegisterActivity2.this,MainActivity.class);
        bundle = new Bundle();
        bundle.putString("email",email);
        bundle.putInt("salary",salary);
        i.putExtra("bundle",bundle);
        try{
            cognito();
        }catch (Exception e){
            e.printStackTrace();
        }
        startActivity(i);
    }
    public void cognito(){
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:f297743b-8f2b-4874-8bef-3ee300d8b4a3", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        Map<String, String> logins = new HashMap<String, String>();
        logins.put("graph.facebook.com", AccessToken.getCurrentAccessToken().getToken());
        credentialsProvider.setLogins(logins);
        syncClient = new CognitoSyncManager(
                getApplicationContext(),
                Regions.US_EAST_1, // Region
                credentialsProvider);

        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);

//        Log.v("_dan customroleARN", credentialsProvider.getCustomRoleArn());
        try {
// Create a record in a dataset and synchronize with the server
            Dataset dataset = syncClient.openOrCreateDataset("myDataset");
            dataset.put("profileName", profileName);
            dataset.put("loginMethod",loginMethod);
            dataset.put("email",email);
            dataset.put("registered",registered.toString());
            dataset.put("salary",salary+"");

            dataset.synchronize(new DefaultSyncCallback() {
                @Override
                public void onSuccess(Dataset dataset, List newRecords) {
                    //Your handler code here
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}