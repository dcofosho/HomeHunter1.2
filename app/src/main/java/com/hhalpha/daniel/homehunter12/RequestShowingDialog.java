package com.hhalpha.daniel.homehunter12;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Daniel on 7/20/2016.
 */
public class RequestShowingDialog extends Dialog implements
        View.OnClickListener {

    public Activity c;
    public Button buttonRequest;
    public TextView textViewRequest;
    String date, address, profile,phoneNum;
    AmazonDynamoDBClient ddbClient;
    CognitoCachingCredentialsProvider credentialsProvider;
    ListView list;
    Bundle bundle;
    DynamoDBMapper mapper;
    AmazonDynamoDB dynamoDB;
    CognitoSyncManager syncClient;
    SharedPreferences preferences;
    Boolean requested;
    SharedPreferences.Editor editor;
    public RequestShowingDialog(Activity a, Bundle args) {
        super(a);
        this.c = a;
        this.bundle=args;
        try {
            address = bundle.getString("address");
            date=bundle.getString("date");
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.requestdialog1);
        requested=false;
        buttonRequest=(Button)findViewById(R.id.buttonRequest);
        textViewRequest=(TextView)findViewById(R.id.textViewRequest);
        FacebookSdk.sdkInitialize(getContext());
        try {
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            profile = preferences.getString("profileName", "");
            editor = preferences.edit();
            TelephonyManager tMgr = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            phoneNum = tMgr.getLine1Number();
            editor.putString("myPhoneNum",phoneNum);
        }catch (Exception e){
            e.printStackTrace();
        }
        buttonRequest.setOnClickListener(this);
        try{
            //set text view to ask "Do you want to request this time slot?" and show the date/time
            textViewRequest.setText("Do you want to request this time slot?"+"\n"+date.split(" ")[1]+" "+date.split(" ")[2]+" "+date.split(" ")[3]+" "+date.split(" ")[4]+" "+date.split(" ")[5]);
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

    }

    @Override
    public void onClick(View v) {
        //textViewRequest.setText("Go");
        new resquestTask().execute();
    }

    public class resquestTask extends AsyncTask<String,Integer,String> {

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
                    if (result.get(i).getInfoString().contains(date)&&result.get(i).getAddress().contains(address)){
                        //make updated showing set to "Requested" instead of "Available"
                        Showing updatedShowing=new Showing();
                        updatedShowing.setAddress(address);
                        updatedShowing.setInfoString(date.replace("Available","Requested")+" "+profile+" "+phoneNum);
                        //save updated showing
                        mapper.save(updatedShowing);
                        //delete old showing
                        mapper.delete(result.get(i));

                        requested=true;

                        //break for loop
                        //break;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                editor.putBoolean("requested",requested);
                editor.putString("myRequestedDate",date);
                editor.commit();
                dismiss();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

}