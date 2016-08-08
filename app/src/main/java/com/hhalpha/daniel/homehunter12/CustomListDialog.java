package com.hhalpha.daniel.homehunter12;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
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

/**
 * Created by Daniel on 7/20/2016.
 */
public class CustomListDialog extends Dialog implements
        View.OnClickListener {

    public Activity c;

    public Button yes;
    TextView txt_dia, txt_dia2, txt_dia3;
    AmazonDynamoDBClient ddbClient;
    String date, address;
    AmazonDynamoDB dynamoDB;
    CognitoCachingCredentialsProvider credentialsProvider;
    CognitoSyncManager syncClient;
    Timeslot timeslot;
    Appointment appointment;
    ConfirmedAppointment confAppt;
    ListView list, list2, list3;
    ArrayList<String> dateArrayList, apptArrayList, confApptArrayList, statusList;
    CustomListViewAdapter2 adapter, adapter2, adapter3;
    int numDates, numAppts, numConfAppts;
    Bundle bundle;
    Boolean available, requested,confirmed;
    public CustomListDialog(Activity a, Bundle args) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        this.bundle=args;
        try {
            address = bundle.getString("address");
            date = bundle.getString("date");
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            dateArrayList = new ArrayList<>();
            if(bundle.containsKey("dateArrayList")) {
                for (int i = 0; i < bundle.getStringArrayList("dateArrayList").size(); i++) {
                    if (bundle.getStringArrayList("dateArrayList").get(i).contains(date.split(" ")[0] + " " + date.split(" ")[1] + " " + date.split(" ")[2])) {
                        dateArrayList.add(bundle.getStringArrayList("dateArrayList").get(i));
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            apptArrayList = new ArrayList<>();
            if(bundle.containsKey("apptArrayList")) {
                for (int i = 0; i < bundle.getStringArrayList("apptArrayList").size(); i++) {
                    if (bundle.getStringArrayList("apptArrayList").get(i).contains(date.split(" ")[0] + " " + date.split(" ")[1] + " " + date.split(" ")[2])) {
                        apptArrayList.add(bundle.getStringArrayList("apptArrayList").get(i));
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            confApptArrayList = new ArrayList<>();
            if(bundle.containsKey("confApptArrayList")) {
                for (int i = 0; i < bundle.getStringArrayList("confApptArrayList").size(); i++) {
                    if (bundle.getStringArrayList("confApptArrayList").get(i).contains(date.split(" ")[0] + " " + date.split(" ")[1] + " " + date.split(" ")[2])) {
                        confApptArrayList.add(bundle.getStringArrayList("confApptArrayList").get(i));
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            available = bundle.getBoolean("available");
            Log.v("_dan cust list avail",available.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            requested = bundle.getBoolean("requested");
            Log.v("_dan cust list req",requested.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            confirmed = bundle.getBoolean("confirmed");
            Log.v("_dan cust list conf",confirmed.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.v("_dan list dialog info",dateArrayList.toString()+apptArrayList.toString()+confApptArrayList.toString());
        Log.v("_dan list dialog info",date+address);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog3);
        FacebookSdk.sdkInitialize(getContext());
        try{
            credentialsProvider = new CognitoCachingCredentialsProvider(
                    getContext(),
                    "us-east-1:db3a6e00-7c35-4f48-b956-eaf3375a024f", // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );
            Map<String, String> logins = new HashMap<String, String>();
            logins.put("graph.facebook.com", AccessToken.getCurrentAccessToken().getToken());
            credentialsProvider.setLogins(logins);
        }catch (Exception e){
            e.printStackTrace();
        }
        txt_dia=(TextView)findViewById(R.id.txt_dia);
        txt_dia2=(TextView)findViewById(R.id.txt_dia2);
        txt_dia3=(TextView)findViewById(R.id.txt_dia3);
        list = (ListView) findViewById(R.id.list);
        list2 = (ListView) findViewById(R.id.list2);
        list3 = (ListView) findViewById(R.id.list3);
        if(available) {
            try {
                txt_dia.setVisibility(View.VISIBLE);
                list.setVisibility(View.VISIBLE);
                adapter = new CustomListViewAdapter2(c.getBaseContext(), R.layout.list_layout2, dateArrayList);
                list.setAdapter(adapter);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        bundle.putString("date",list.getItemAtPosition(position).toString());
                        bundle.putString("address",address);
                        bundle.putString("status","available");
                        bundle.putBoolean("edit",true);

                        CustomDialogClass cdd = new CustomDialogClass(c,bundle);
                        cdd.setOnDismissListener(new OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                CustomListDialog.this.dismiss();
                            }
                        });
                        cdd.show();
                    }
                });
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(requested) {
            try {
                txt_dia2.setVisibility(View.VISIBLE);
                list2.setVisibility(View.VISIBLE);
                adapter2 = new CustomListViewAdapter2(c.getBaseContext(), R.layout.list_layout2, apptArrayList);
                list2.setAdapter(adapter2);
                list2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        bundle.putString("date",list2.getItemAtPosition(position).toString());
                        bundle.putString("address",address);
                        bundle.putString("status","requested");
                        bundle.putBoolean("edit",true);
                        CustomDialogClass cdd = new CustomDialogClass(c,bundle);
                        cdd.setOnDismissListener(new OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                CustomListDialog.this.dismiss();
                            }
                        });
                        cdd.show();
                    }
                });
                adapter2.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(confirmed) {
            try {
                txt_dia3.setVisibility(View.VISIBLE);
                list3.setVisibility(View.VISIBLE);
                adapter3 = new CustomListViewAdapter2(c.getBaseContext(), R.layout.list_layout2, confApptArrayList);
                list3.setAdapter(adapter3);
                list3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        bundle.putString("date",list3.getItemAtPosition(position).toString());
                        bundle.putString("address",address);
                        bundle.putString("status","confirmed");
                        bundle.putBoolean("edit",true);
                        CustomDialogClass cdd = new CustomDialogClass(c,bundle);
                        cdd.setOnDismissListener(new OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                CustomListDialog.this.dismiss();
                            }
                        });
                        cdd.show();
                    }
                });
                adapter3.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        yes = (Button) findViewById(R.id.btn_yes);
        yes.setOnClickListener(this);

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
        switch (v.getId()) {
            case R.id.btn_yes:
                //TODO add intent to activity to add new timeslot
                CustomDialogClass cdd = new CustomDialogClass(c,bundle);
                cdd.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        CustomListDialog.this.dismiss();
                    }
                });
                cdd.show();
        }

    }


}