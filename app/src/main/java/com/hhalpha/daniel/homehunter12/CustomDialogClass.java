package com.hhalpha.daniel.homehunter12;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

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
import com.facebook.Profile;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Daniel on 7/15/2016.
 */
public class CustomDialogClass extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;

    public Button yes;
    TextView txt_dia;
    AmazonDynamoDBClient ddbClient;
    Spinner spinnerHours, spinnerMinutes;
    String hours, mins, date, address, myHost;
    DynamoDBMapper mapper;
    AmazonDynamoDB dynamoDB;
    String status;
    CognitoCachingCredentialsProvider credentialsProvider;
    CognitoSyncManager syncClient;
    Timeslot timeslot;
    Appointment appointment;
    CheckBox checkBoxAM, checkBoxPM;
    String amPm;
    SharedPreferences prefs;
    ConfirmedAppointment confirmedAppointment;
    public CustomDialogClass(Activity a, Bundle args) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        address=args.getString("address");
        date=args.getString("date");
        status=args.getString("status");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.custom_dialog);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        txt_dia=(TextView)findViewById(R.id.txt_dia);
        txt_dia.setText(date.split(" ")[0].toString()+" "+date.split(" ")[1].toString()+"\n"+date.split(" ")[2].toString());
        yes = (Button) findViewById(R.id.btn_yes);


        if (!status.equals("available")) {
            yes.setText("Cancel appointment");
        }
        yes.setOnClickListener(this);
        FacebookSdk.sdkInitialize(getContext());
        try{
            credentialsProvider = new CognitoCachingCredentialsProvider(
                    getContext(),
                    "us-east-1:f297743b-8f2b-4874-8bef-3ee300d8b4a3", // Identity Pool ID
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
//        new dynamoTask().execute();
//        new retrieveTask().execute();
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btn_yes) {
            Log.v("_dandia", address + "hours=" + hours + "mins=" + mins);


            new dynamoTask().execute();
        }



    }
    public class dynamoTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            syncClient = new CognitoSyncManager(
                    getContext(),
                    Regions.US_EAST_1, // Region
                    credentialsProvider);
            credentialsProvider.refresh();
            ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            mapper = new DynamoDBMapper(ddbClient);
            if(status.equals("available")) {
                appointment = new Appointment();
                timeslot=new Timeslot();
                try {
//                timeslot = mapper.load(Timeslot.class,new SimpleDateFormat("EEE MMM dd hh:mm a yyyy", Locale.US).parse(date)+" @ "+address);
                    DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
                    PaginatedScanList<Timeslot> result = mapper.scan(Timeslot.class, scanExpression);
                    for (int i = 0; i < result.size(); i++) {
                        if (result.get(i).getTime().split("@")[1].contains(address.replace("[", "").replace("]", "").replace("+", "").replace(",", ""))) {
                            Log.v("_dan custdialog result", result.get(i).getTime().split("@")[1]);
                            appointment.setHost(result.get(i).getHost() + "@" + prefs.getString("profileName", ""));
                            appointment.setTime(result.get(i).getTime());
                            timeslot = result.get(i);
                            myHost=timeslot.getHost().split("@")[0];
                            Log.v("_dan host",myHost);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    mapper.save(appointment);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    mapper.delete(timeslot);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(status.equals("requested")){
                appointment = new Appointment();
                timeslot=new Timeslot();
                try {
//                timeslot = mapper.load(Timeslot.class,new SimpleDateFormat("EEE MMM dd hh:mm a yyyy", Locale.US).parse(date)+" @ "+address);
                    DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
                    PaginatedScanList<Appointment> result = mapper.scan(Appointment.class, scanExpression);
                    for (int i = 0; i < result.size(); i++) {
                        if (result.get(i).getTime().split("@")[1].contains(address.replace("[", "").replace("]", "").replace("+", "").replace(",", ""))) {
                            Log.v("_dan custdialog result", result.get(i).getTime().split("@")[1]);
                            timeslot.setHost(result.get(i).getHost() + "@" + prefs.getString("profileName", ""));
                            timeslot.setTime(result.get(i).getTime());
                            appointment = result.get(i);

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    mapper.save(timeslot);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    mapper.delete(appointment);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(status.equals("confirmed")){
                confirmedAppointment = new ConfirmedAppointment();
                timeslot=new Timeslot();
                try {
//                timeslot = mapper.load(Timeslot.class,new SimpleDateFormat("EEE MMM dd hh:mm a yyyy", Locale.US).parse(date)+" @ "+address);
                    DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
                    PaginatedScanList<ConfirmedAppointment> result = mapper.scan(ConfirmedAppointment.class, scanExpression);
                    for (int i = 0; i < result.size(); i++) {
                        if (result.get(i).getTime().split("@")[1].contains(address.replace("[", "").replace("]", "").replace("+", "").replace(",", ""))) {
                            Log.v("_dan custdialog result", result.get(i).getTime().split("@")[1]);
                            timeslot.setHost(result.get(i).getHost() + "@" + prefs.getString("profileName", ""));
                            timeslot.setTime(result.get(i).getTime());
                            confirmedAppointment = result.get(i);

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    mapper.save(timeslot);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    mapper.delete(confirmedAppointment);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            dismiss();
        }

    }



}
