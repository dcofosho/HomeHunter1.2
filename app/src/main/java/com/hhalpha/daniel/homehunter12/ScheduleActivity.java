package com.hhalpha.daniel.homehunter12;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Daniel on 7/15/2016.
 */
public class ScheduleActivity extends Activity {
    String address;
    CustomCalendarView calendarView;
    Calendar currentCalendar;
    List<DayDecorator> list;
    ArrayList<Date> dates, appts, confAppts;
    DynamoDBMapper mapper;
    CognitoCachingCredentialsProvider credentialsProvider;
    CognitoSyncManager syncClient;
    Timeslot timeslot;
    AmazonDynamoDB dynamoDB;
    AmazonDynamoDBClient ddbClient;
    ArrayList<Integer> apptIndicies;
    int apptIndex;
    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        dates=new ArrayList<>();
        appts=new ArrayList<>();
        confAppts=new ArrayList<>();
        apptIndicies=new ArrayList<>();
        calendarView = (CustomCalendarView) findViewById(R.id.calendar_view);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
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
        new retrieveApptsTask().execute();
        new retrieveConfApptsTask().execute();
    }

    public class DaysDecorator implements DayDecorator {
            @Override
            public void decorate(final DayView dayView) {

                for (int i = 0; i < dates.size(); i++) {
                    apptIndex = i;
                    if (dates.get(i).toString().replace("[", "").replace("]", "").contains(dayView.getDate().toString().split(" ")[0] + " " + dayView.getDate().toString().split(" ")[1] + " " + dayView.getDate().toString().split(" ")[2])) {

                        dayView.setBackgroundColor(Color.parseColor("#FFa7a7"));
                        dayView.setText(dates.get(i).toString());
                        dayView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("address", address.replace("[", "").replace("+", ""));
                                    bundle.putString("date", dates.get(apptIndex).toString());//


                                    CustomDialogClass cdd = new CustomDialogClass(ScheduleActivity.this, bundle);
//                    cdd.setTitle(string.replace("[","").replace("+",""));
                                    cdd.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            dayView.setBackgroundColor(Color.parseColor("#a7a7a7"));
                                            dayView.setText("Awaiting confirmation for " + dayView.getText().toString());
                                            SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd hh:mm a yyyy", Locale.US);
                                            Toast.makeText(ScheduleActivity.this, df.format(dayView.getDate()), Toast.LENGTH_SHORT).show();

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

                    if (isPastDay(dayView.getDate())) {
                        dayView.setBackgroundColor(Color.parseColor("#a7a7FF"));
                    }

                }
                for (int y = 0; y < appts.size(); y++) {
                    apptIndex = y;
                    if (appts.get(y).toString().replace("[", "").replace("]", "").contains(dayView.getDate().toString().split(" ")[0] + " " + dayView.getDate().toString().split(" ")[1] + " " + dayView.getDate().toString().split(" ")[2])) {

                        dayView.setBackgroundColor(Color.parseColor("#a7a7bb"));
                        dayView.setText("Awaiting confirmation for " + appts.get(y).toString());
                        dayView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
//                                try {
//                                    Bundle bundle = new Bundle();
//                                    bundle.putString("address", address.replace("[", "").replace("+", ""));
//                                    bundle.putString("date", dates.get(apptIndex).toString());//
//
//
//                                    CustomDialogClass cdd = new CustomDialogClass(ScheduleActivity.this, bundle);
////                    cdd.setTitle(string.replace("[","").replace("+",""));
//                                    cdd.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                                        @Override
//                                        public void onDismiss(DialogInterface dialog) {
//                                            dayView.setBackgroundColor(Color.parseColor("#a7a7a7"));
//                                            dayView.setText("Awaiting confirmation for "+dayView.getText().toString());
//                                            SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd hh:mm a yyyy", Locale.US);
//                                            Toast.makeText(ScheduleActivity.this, df.format(dayView.getDate()), Toast.LENGTH_SHORT).show();
//
//                                            Intent i = getIntent();
//                                            startActivity(i);
//                                        }
//                                    });
//
//                                    cdd.show();
//                                }catch(Exception e){
//                                    e.printStackTrace();
//                                }
                            }
                        });
                    }


                }
                for (int z = 0; z < confAppts.size(); z++) {
//                    statusList.add("confirmed");
                    apptIndex = z;
                    Log.v("_dan confAppts" + z, confAppts.get(z).toString());
                    Log.v("dan dayview get date", dayView.getDate().toString());
                    if (confAppts.get(z).toString().replace("[", "").replace("]", "").contains(dayView.getDate().toString().split(" ")[0] + " " + dayView.getDate().toString().split(" ")[1] + " " + dayView.getDate().toString().split(" ")[2])) {
//                        confirmed=true;
//                        numConfAppts++;
                        dayView.setBackgroundColor(Color.parseColor("#00FF00"));
                        dayView.setText("Appointment confirmed for "+confAppts.get(z).toString().split(" ")[3]);
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
                    if(result.get(i).getTime().split("@")[1].contains(address.replace("[","").replace("]","").replace("+","").replace(",",""))) {
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
}
