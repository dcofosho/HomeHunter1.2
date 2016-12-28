package com.hhalpha.daniel.homehunter12;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;



/**
 * Created by Daniel on 10/12/2016.
 */

public class NewScheduleActivity2 extends Activity {

    String address;
    DynamoDBMapper mapper;
    AmazonDynamoDB dynamoDB;
    AmazonDynamoDBClient ddbClient;
    String idPool;
    CognitoSyncManager syncClient;
    Timeslot timeslot;
    CheckBox checkBoxAM, checkBoxPM;
    String amPm,profile,phoneNum;

    Bundle bundle;
    SharedPreferences preferences;
    Showing showing;
    ArrayList<Showing> showings;
    ArrayList<String> timeSlots;
    File pic1;
    private CognitoCachingCredentialsProvider credentialsProvider;
    ArrayAdapter<String> timeSlotAdapter;
    ListView listView;
    int numShowings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newschedule2);

        FacebookSdk.sdkInitialize(getApplicationContext());

        try{
            TelephonyManager tMgr = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            phoneNum = tMgr.getLine1Number();
            credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),
                    "us-east-1:f297743b-8f2b-4874-8bef-3ee300d8b4a3", // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );
            Map<String, String> logins = new HashMap<String, String>();
            logins.put("graph.facebook.com", AccessToken.getCurrentAccessToken().getToken());
            credentialsProvider.setLogins(logins);
        }catch (Exception e){
            e.printStackTrace();
        }
        numShowings=0;
        address=getIntent().getExtras().getString("address");
        showing=new Showing();
        showings=new ArrayList<Showing>();
        timeSlots=new ArrayList<String>();
        timeSlotAdapter=new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, timeSlots);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(timeSlotAdapter);
        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try{
                    Log.v("_dan item selected", listView.getItemAtPosition(position).toString());
                    bundle.putString("address",listView.getItemAtPosition(position).toString());
                    RequestShowingDialog dia = new RequestShowingDialog(NewScheduleActivity2.this,bundle);
                    dia.show();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listView.setSelection(position);
                try{
                    Log.v("_dan item selected", address);
                    bundle=new Bundle();
                    bundle.putString("address",address);
                    bundle.putString("date",listView.getItemAtPosition(position).toString());
                    RequestShowingDialog dia = new RequestShowingDialog(NewScheduleActivity2.this,bundle);
                    dia.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            try {
                                Log.v("_danDis", dialog.getClass().getDeclaredField("requested").toString());
                                if(preferences.getBoolean("requested",false)){
                                    Log.v("danDisReq","Success");
                                    Log.v("danDisReq2",preferences.getString("myRequestedDate","000"));
                                    sendSMS(preferences.getString("myRequestedDate","000").split(" ")[preferences.getString("myRequestedDate","000").split(" ").length-1],"HH test1");
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                    dia.show();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });


        try {
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            profile = preferences.getString("profileName", "");
            new retrieveTimeSlots().execute();
        }catch(Exception e){
            e.printStackTrace();
        }
    }



//    public void createAvailableTimeSlot(View v){
//        txt_dia.setText(address+hours+":"+mins+" "+amPm+" "+day+" "+month+" "+year+" "+profile);
//        //create a map to store user metadata
//        showing.setAddress(address);
//        showing.setInfoString("Available "+hours+":"+mins+" "+amPm+" "+day+" "+month+" "+year+" "+profile);
//        new timeSlotTask().execute(showing);
//    }

    public class timeSlotTask extends AsyncTask<Showing,Integer,String>{

        @Override
        protected String doInBackground(Showing... params) {
            syncClient = new CognitoSyncManager(
                    getApplicationContext(),
                    Regions.US_EAST_1, // Region
                    credentialsProvider);
            credentialsProvider.refresh();
            ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            mapper = new DynamoDBMapper(ddbClient);
            mapper.save(params[0]);
            return "";
        }
    }

    public class retrieveTimeSlots extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... params) {
            syncClient = new CognitoSyncManager(
                    getApplicationContext(),
                    Regions.US_EAST_1, // Region
                    credentialsProvider);
            credentialsProvider.refresh();
            ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            mapper = new DynamoDBMapper(ddbClient);
            try{
                DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
                PaginatedScanList<Showing> result = mapper.scan(Showing.class, scanExpression);
                for(int i=0;i<result.size();i++) {
                    //populate listview with showing timeslots for the selected address
                    if (result.get(i).getAddress().contains(address)){
                        showings.add(result.get(i));
                        timeSlots.add(result.get(i).getInfoString());
                        numShowings++;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            timeSlotAdapter.notifyDataSetChanged();

        }
    }
    public void sendSMS(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }
}
