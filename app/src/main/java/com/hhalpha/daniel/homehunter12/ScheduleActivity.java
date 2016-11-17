package com.hhalpha.daniel.homehunter12;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.http.HttpResponse;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.BatchGetItemRequest;
import com.amazonaws.services.dynamodbv2.model.BatchGetItemResult;
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemRequest;
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemResult;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableResult;
import com.amazonaws.services.dynamodbv2.model.DescribeLimitsRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeLimitsResult;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeysAndAttributes;
import com.amazonaws.services.dynamodbv2.model.ListTablesRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import com.amazonaws.services.dynamodbv2.model.UpdateTableRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateTableResult;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.imanoweb.calendarview.CalendarListener;
import com.imanoweb.calendarview.CustomCalendarView;
import com.imanoweb.calendarview.DayDecorator;
import com.imanoweb.calendarview.DayView;
import com.twilio.client.Twilio;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Daniel on 7/15/2016.
 */
public class ScheduleActivity extends AppCompatActivity {
    String address;
    SharedPreferences prefs;
    String string;
    CustomCalendarView calendarView;
    Calendar currentCalendar;
    List<DayDecorator> list;
    ArrayList<Date> dates, appts, confAppts;
    ArrayList<ArrayList<Date>> dateArrayLists;
    DynamoDBMapper mapper;
    CognitoCachingCredentialsProvider credentialsProvider;
    CognitoSyncManager syncClient;
    Timeslot timeslot;
    AmazonDynamoDB dynamoDB;
    AmazonDynamoDBClient ddbClient;
    ArrayList<Integer> apptIndicies;
    int apptIndex, numSlots, numAppts, numConfAppts;
    String status;
    ArrayList<String> dateArrayList, apptArrayList, confApptArrayList, statusList;
    Boolean available, requested, confirmed;
    Bundle bundle;
    private OkHttpClient mClient=new OkHttpClient();
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
//        Twilio.initialize(getApplicationContext(), new Twilio.InitListener() {
//            @Override
//            public void onInitialized() {
//
//            }
//
//            @Override
//            public void onError(Exception e) {
//                e.printStackTrace();
//            }
//        });
        mContext=getApplicationContext();
        dates=new ArrayList<>();
        appts=new ArrayList<>();
        confAppts=new ArrayList<>();
        apptIndicies=new ArrayList<>();
        calendarView = (CustomCalendarView) findViewById(R.id.calendar_view);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        bundle=new Bundle();
        available=false;
        requested=false;
        confirmed=false;
        dateArrayList = new ArrayList<>();
        dateArrayLists=new ArrayList<>();
        apptArrayList=new ArrayList<>();
        confApptArrayList=new ArrayList<>();
        FacebookSdk.sdkInitialize(getApplicationContext());
        try{
            credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),
                    "us-east-1:f297743b-8f2b-4874-8bef-3ee300d8b4a3", // Identity Pool for shoppers
                    Regions.US_EAST_1 // Region
            );
            Map<String, String> logins = new HashMap<String, String>();
            logins.put("graph.facebook.com", AccessToken.getCurrentAccessToken().getToken());
            credentialsProvider.setLogins(logins);
        }catch (Exception e){
            e.printStackTrace();
        }
        dynamoDB=new AmazonDynamoDB() {
            @Override
            public void setEndpoint(String endpoint) throws IllegalArgumentException {

            }

            @Override
            public void setRegion(Region region) throws IllegalArgumentException {

            }

            @Override
            public CreateTableResult createTable(CreateTableRequest createTableRequest) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public ScanResult scan(ScanRequest scanRequest) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public DescribeLimitsResult describeLimits(DescribeLimitsRequest describeLimitsRequest) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public BatchGetItemResult batchGetItem(BatchGetItemRequest batchGetItemRequest) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public GetItemResult getItem(GetItemRequest getItemRequest) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public ListTablesResult listTables(ListTablesRequest listTablesRequest) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public BatchWriteItemResult batchWriteItem(BatchWriteItemRequest batchWriteItemRequest) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public DeleteTableResult deleteTable(DeleteTableRequest deleteTableRequest) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public DeleteItemResult deleteItem(DeleteItemRequest deleteItemRequest) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public UpdateTableResult updateTable(UpdateTableRequest updateTableRequest) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public UpdateItemResult updateItem(UpdateItemRequest updateItemRequest) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public DescribeTableResult describeTable(DescribeTableRequest describeTableRequest) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public QueryResult query(QueryRequest queryRequest) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public PutItemResult putItem(PutItemRequest putItemRequest) throws AmazonServiceException, AmazonClientException {
                return null;
            }

//            @Override
//            public DescribeLimitsResult describeLimits() throws AmazonServiceException, AmazonClientException {
//                return null;
//            }

            @Override
            public ListTablesResult listTables() throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public CreateTableResult createTable(List<AttributeDefinition> attributeDefinitions, String tableName, List<KeySchemaElement> keySchema, ProvisionedThroughput provisionedThroughput) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public ScanResult scan(String tableName, List<String> attributesToGet) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public ScanResult scan(String tableName, Map<String, Condition> scanFilter) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public ScanResult scan(String tableName, List<String> attributesToGet, Map<String, Condition> scanFilter) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public BatchGetItemResult batchGetItem(Map<String, KeysAndAttributes> requestItems, String returnConsumedCapacity) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public BatchGetItemResult batchGetItem(Map<String, KeysAndAttributes> requestItems) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public GetItemResult getItem(String tableName, Map<String, AttributeValue> key) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public GetItemResult getItem(String tableName, Map<String, AttributeValue> key, Boolean consistentRead) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public ListTablesResult listTables(String exclusiveStartTableName) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public ListTablesResult listTables(String exclusiveStartTableName, Integer limit) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public ListTablesResult listTables(Integer limit) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public BatchWriteItemResult batchWriteItem(Map<String, List<WriteRequest>> requestItems) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public DeleteTableResult deleteTable(String tableName) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public DeleteItemResult deleteItem(String tableName, Map<String, AttributeValue> key) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public DeleteItemResult deleteItem(String tableName, Map<String, AttributeValue> key, String returnValues) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public UpdateTableResult updateTable(String tableName, ProvisionedThroughput provisionedThroughput) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public UpdateItemResult updateItem(String tableName, Map<String, AttributeValue> key, Map<String, AttributeValueUpdate> attributeUpdates) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public UpdateItemResult updateItem(String tableName, Map<String, AttributeValue> key, Map<String, AttributeValueUpdate> attributeUpdates, String returnValues) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public DescribeTableResult describeTable(String tableName) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public PutItemResult putItem(String tableName, Map<String, AttributeValue> item) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public PutItemResult putItem(String tableName, Map<String, AttributeValue> item, String returnValues) throws AmazonServiceException, AmazonClientException {
                return null;
            }

            @Override
            public void shutdown() {

            }

            @Override
            public ResponseMetadata getCachedResponseMetadata(AmazonWebServiceRequest request) {
                return null;
            }
        };
        try {
            address = getIntent().getStringExtra("address");
            Log.v("_dan address sched",address);
        }catch (Exception e){
            e.printStackTrace();
        }

        new retrieveTask().execute();

    }

    public class DaysDecorator implements DayDecorator {

        @Override
        public void decorate(final DayView dayView) {
            numSlots=0;
            numAppts=0;
            numConfAppts=0;
            if (isPastDay(dayView.getDate())) {
                dayView.setBackgroundColor(Color.parseColor("#a7a7FF"));
            }else {//determine if each date includes available, requested, or confirmed time slots, and if so, how many.
                for (int i = 0; i < dates.size(); i++) {
                    apptIndex = i;
                    if (dates.get(i).toString().replace("[", "").replace("]", "").contains(dayView.getDate().toString().split(" ")[0] + " " + dayView.getDate().toString().split(" ")[1] + " " + dayView.getDate().toString().split(" ")[2])) {
//                        statusList.add("available");
//                        available=true;
                        Log.v("_dan deco date"+dates.get(i).toString().replace("[", "").replace("]", ""),dayView.getDate().toString().split(" ")[0] + " " + dayView.getDate().toString().split(" ")[1] + " " + dayView.getDate().toString().split(" ")[2]);
                        numSlots++;
                        dayView.setBackgroundColor(Color.parseColor("#cca7a7"));
                    }


                }
                for (int y = 0; y < appts.size(); y++) {
                    apptIndex = y;
                    if (appts.get(y).toString().replace("[", "").replace("]", "").contains(dayView.getDate().toString().split(" ")[0] + " " + dayView.getDate().toString().split(" ")[1] + " " + dayView.getDate().toString().split(" ")[2])) {
//                        statusList.add("requested");
//                        requested=true;
                        Log.v("_dan deco appt"+appts.get(y).toString().replace("[", "").replace("]", ""),dayView.getDate().toString().split(" ")[0] + " " + dayView.getDate().toString().split(" ")[1] + " " + dayView.getDate().toString().split(" ")[2]);
                        numAppts++;
                        dayView.setBackgroundColor(Color.parseColor("#a7a7bb"));
                    }
                }

                for (int z = 0; z < confAppts.size(); z++) {
//                    statusList.add("confirmed");
                    apptIndex = z;
//                    Log.v("_dan confAppts" + z, confAppts.get(z).toString());
//                    Log.v("dan dayview get d ate", dayView.getDate().toString());
                    if (confAppts.get(z).toString().replace("[", "").replace("]", "").contains(dayView.getDate().toString().split(" ")[0] + " " + dayView.getDate().toString().split(" ")[1] + " " + dayView.getDate().toString().split(" ")[2])) {
//                        confirmed=true;
                        Log.v("_dan deco conf"+confAppts.get(z).toString().replace("[", "").replace("]", ""),dayView.getDate().toString().split(" ")[0] + " " + dayView.getDate().toString().split(" ")[1] + " " + dayView.getDate().toString().split(" ")[2]);
                        numConfAppts++;
                        dayView.setBackgroundColor(Color.parseColor("#00FF00"));
                    }

                }

                //
                if(numSlots>0||numAppts>0||numConfAppts>0) {
                    //
                    // dayView.setText(dayView.getDate().toString().split(" ")[2]+"\n"+numConfAppts + " appointments confirmed!"+ "\n"+numSlots + " timeslots set as available" + "\n" + numAppts + " timeslots awaiting confirmation");
                    dayView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try{
                                for(int x=0;x<dates.toString().split(",").length;x++){

                                    if(dates.toString().split(",")[x].contains(dayView.getDate().toString().split(" ")[0]+" "+dayView.getDate().toString().split(" ")[1]+" "+dayView.getDate().toString().split(" ")[2])&&!dateArrayList.toString().contains(dayView.getDate().toString().split(" ")[3])){
//                                        Log.v("_dan sched dates",dates.toString().split(",")[x]);
//                                        Log.v("_dan sched dayview",dayView.getDate().toString());
                                        dateArrayList.add(dates.toString().split(",")[x]);
                                    }
                                }
//                                bundle.putBoolean("available",available);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            try{
                                for(int x=0;x<appts.toString().split(",").length;x++){

                                    if(appts.toString().split(",")[x].contains(dayView.getDate().toString().split(" ")[0]+" "+dayView.getDate().toString().split(" ")[1]+" "+dayView.getDate().toString().split(" ")[2])&&!apptArrayList.toString().contains(dayView.getDate().toString().split(" ")[3])){
//                                        Log.v("_dan sched appts",appts.toString().split(",")[x]);
//                                        Log.v("_dan sched dayview",dayView.getDate().toString());
                                        apptArrayList.add(appts.toString().split(",")[x]);
//                                        requested=true;
                                    }
                                }
//                                bundle.putBoolean("requested",requested);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            try{
                                for(int x=0;x<confAppts.toString().split(",").length;x++){

                                    if(confAppts.toString().split(",")[x].contains(dayView.getDate().toString().split(" ")[0]+" "+dayView.getDate().toString().split(" ")[1]+" "+dayView.getDate().toString().split(" ")[2])&&!confApptArrayList.contains(dayView.getDate().toString().split(" ")[3])){
//                                        Log.v("_dan sched conf appts",confAppts.toString().split(",")[x]);
//                                        Log.v("_dan sched dayview",dayView.getDate().toString());
                                        confApptArrayList.add(confAppts.toString().split(",")[x]);
//                                        confirmed=true;
                                    }
                                }
//                                bundle.putBoolean("confirmed",confirmed);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            try {
                                bundle = new Bundle();
                                bundle.putString("date", dayView.getDate().toString());//
                                if(!address.isEmpty()){bundle.putString("address", address.replace("[", "").replace("]","").replace("+", ""));}
//                                if(!statusList.isEmpty()){bundle.putStringArrayList("statusList",statusList);}
                                if(!dateArrayList.isEmpty()){bundle.putStringArrayList("dateArrayList",dateArrayList);
                                    available= true;}
                                bundle.putBoolean("available",available);
                                Log.v("_dan sched dates2",dateArrayList.toString());
                                Log.v("_dan sched avail",available.toString());
                                if(!apptArrayList.isEmpty()){bundle.putStringArrayList("apptArrayList",apptArrayList);
                                    requested= true;}
                                bundle.putBoolean("requested",requested);
                                Log.v("_dan sched appts2",apptArrayList.toString());
                                Log.v("_dan sched avail",requested.toString());
                                if(!confApptArrayList.isEmpty()){bundle.putStringArrayList("confApptArrayList",confApptArrayList);
                                    confirmed= true;}
                                bundle.putBoolean("confirmed",confirmed);
                                Log.v("_dan sched confAppt2",confApptArrayList.toString());
                                Log.v("_dan sched conf",confirmed.toString());
                                CustomListDialog cdd = new CustomListDialog(ScheduleActivity.this, bundle);
                                cdd.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        sendSMS("+19146298713","dannyboy sms text 114");
//                                        try {
//                                            post("http://d4439893.ngrok.io/sms", new  Callback(){
//                                                @Override
//                                                public void onFailure(Call call, IOException e) {
//                                                    e.printStackTrace();
//                                                }
//                                                @Override
//                                                public void onResponse(Call call, final Response response) throws IOException {
//                                                    runOnUiThread(new Runnable() {
//                                                        @Override
//                                                        public void run() {
//                                                            Toast.makeText(getApplicationContext(),"SMS Sent!"+response.toString(),Toast.LENGTH_SHORT).show();
//                                                        }
//                                                    });
//                                                }
//                                            });
//                                        } catch (IOException e) {
//                                            e.printStackTrace();
//                                        }
                                        Intent i = getIntent();
                                        startActivity(i);
                                    }
                                });
                                cdd.show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }

        }
    }
    private boolean isPastDay(Date date) {
        Calendar c = Calendar.getInstance();

        // set the calendar to start of today
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        // and get that as a Date
        Date today = c.getTime();

        // test your condition, if Date specified is before today
        if (date.before(today)) {
            return true;
        }
        return false;
    }
    public void initializeCalendar(){
        try {
            list=new ArrayList<>();
            list.add(new DaysDecorator());

        }catch (Exception e){
            e.printStackTrace();
        }
        try{

//Initialize calendar with date
            currentCalendar = Calendar.getInstance(Locale.getDefault());

            calendarView.setDecorators(list);

//Show/hide overflow days of a month
            calendarView.setShowOverflowDate(false);

//call refreshCalendar to update calendar the view
            calendarView.refreshCalendar(currentCalendar);

//Handling custom calendar events
            calendarView.setCalendarListener(new CalendarListener() {
                @Override
                public void onDateSelected(Date date) {
//
                }

                @Override
                public void onMonthChanged(Date date) {
                    SimpleDateFormat df = new SimpleDateFormat("MM-yyyy");
                    Toast.makeText(ScheduleActivity.this, df.format(date), Toast.LENGTH_SHORT).show();
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public class retrieveTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            syncClient = new CognitoSyncManager(
                    getApplicationContext(),
                    Regions.US_EAST_1, // Region
                    credentialsProvider);
            credentialsProvider.refresh();
            ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            mapper = new DynamoDBMapper(ddbClient);


            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
            PaginatedScanList<Timeslot> result = mapper.scan(Timeslot.class, scanExpression);
            for(int i=0;i<result.size();i++) {
                try{
                    if(result.get(i).getTime().split("@")[1].contains(address.replace("[","").replace("]","").replace("+","").replace(",",""))) {
                        dates.add(new SimpleDateFormat("EEE MMM dd hh:mm a yyyy", Locale.US).parse(result.get(i).getTime().split("@")[0]));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                Log.v("_dan ddbScan", result.get(i).getTime().toString());

            }
            Log.v("_dan dates after scan", dates.toString());
//            initializeCalendar();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            new retrieveApptsTask().execute();

        }


    }

    public class retrieveApptsTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            syncClient = new CognitoSyncManager(
                    getApplicationContext(),
                    Regions.US_EAST_1, // Region
                    credentialsProvider);
            credentialsProvider.refresh();
            ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            mapper = new DynamoDBMapper(ddbClient);


            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
            PaginatedScanList<Appointment> result = mapper.scan(Appointment.class, scanExpression);
            for(int i=0;i<result.size();i++) {
                try{
                    if(result.get(i).getTime().contains(address.replace("[","").replace("]","").replace("+","").replace(",",""))) {
                        appts.add(new SimpleDateFormat("EEE MMM dd hh:mm a yyyy", Locale.US).parse(result.get(i).getTime().split("@")[0]));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                Log.v("_dan ddbScan", result.get(i).getTime().toString());

            }
            Log.v("_dan dates after scan", dates.toString());
//            initializeCalendar();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            new retrieveConfApptsTask().execute();
        }


    }

    public class retrieveConfApptsTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            syncClient = new CognitoSyncManager(
                    getApplicationContext(),
                    Regions.US_EAST_1, // Region
                    credentialsProvider);
            credentialsProvider.refresh();
            ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            mapper = new DynamoDBMapper(ddbClient);


            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
            PaginatedScanList<ConfirmedAppointment> result = mapper.scan(ConfirmedAppointment.class, scanExpression);
            for(int i=0;i<result.size();i++) {
                try{
                    if(result.get(i).getTime().split("@")[1].contains(address.replace("[","").replace("]","").replace("+","").replace(",",""))&&result.get(i).getHost().split("@")[1].contains(prefs.getString("profileName","profile name not found"))) {
                        confAppts.add(new SimpleDateFormat("EEE MMM dd hh:mm a yyyy", Locale.US).parse(result.get(i).getTime().split("@")[0]));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                Log.v("_dan ddbScan", result.get(i).getTime().toString());

            }
            Log.v("_dan dates after scan", dates.toString());
//            initializeCalendar();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            initializeCalendar();
        }


    }
//    Call post(String url, Callback callback) throws IOException {
//        RequestBody formBody = new FormBody.Builder()
//                .add("To", "+19146298713")
//                .add("Body", "dan's twilio test numero1")
//                .build();
//        Request request = new Request.Builder()
//                .url(url)
//                .post(formBody)
//                .build();
//        Call response = mClient.newCall(request);
//        response.enqueue(callback);
//        return response;
//    }

    //---sends an SMS message to another device---
    private void sendSMS(String phoneNumber, String message) {
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
