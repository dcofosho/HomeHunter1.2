package com.hhalpha.daniel.homehunter12;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Daniel on 7/3/2016.
 */
public class PropertyActivity extends Activity implements OnMapReadyCallback {
    String address, shortBlurb, bigBlurb, payPeriod, minSalary,beds, baths, sqft, rent;

    Boolean guarantor, dogs, smallDogs, cats, couples, children, smoking, securityDeposit, doorman;
    TextView textViewAddress,textViewSqft,textViewRent, textViewMinSalary, textViewShort, textViewLong, textViewBeds, textViewBaths, textViewCouples, textViewChildren, textViewSmallDogs, textViewDogs, textViewCats, textViewSmoking, textViewGuarantor, textViewSecurity, textViewDoorman;
    Bundle bundle;
    ArrayList<String> arrayList;
    private CognitoCachingCredentialsProvider credentialsProvider;
    CognitoSyncManager syncClient;
    AmazonS3 s3;
    TransferUtility transferUtility;
    DynamoDBMapper mapper;
    AmazonDynamoDB dynamoDB;
    ImageView imageView5, imageView6, imageView7, imageView8;
    File pic1, pic2, pic3, pic4;
    Double lat, lng;
    Uri uri1, uri2, uri3, uri4;
    Bitmap selectedImage, selectedImage2,selectedImage3,selectedImage4;
    Map<String, String> userMetadata;
    ObjectMetadata myObjectMetadata;
    ArrayList<String> abridgedList;
    Boolean firstTime;
    ArrayList<String> strings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property);
        abridgedList = new ArrayList<String>();
        strings= new ArrayList<String>();
        bundle=getIntent().getBundleExtra("bundle");
        firstTime=bundle.getBoolean("firstTime");

        //TODO:Handle situations where fields are left blank/less than 3 photos are added
        textViewAddress=(TextView) findViewById(R.id.textViewAddress);
        textViewRent=(TextView) findViewById(R.id.textViewRent);
        textViewSqft=(TextView) findViewById(R.id.textViewSqft);
        textViewMinSalary=(TextView) findViewById(R.id.textViewMinSalary);
        textViewShort=(TextView) findViewById(R.id.textViewShort);
        textViewLong=(TextView) findViewById(R.id.textViewLong);
        textViewBeds=(TextView) findViewById(R.id.textViewBeds);
        textViewBaths=(TextView) findViewById(R.id.textViewBaths);
        textViewCouples=(TextView) findViewById(R.id.textViewCouples);
        textViewChildren=(TextView) findViewById(R.id.textViewChildren);
        textViewSmallDogs=(TextView) findViewById(R.id.textViewSmallDogs);
        textViewDogs=(TextView) findViewById(R.id.textViewDogs);
        textViewCats=(TextView) findViewById(R.id.textViewCats);
        textViewSmoking=(TextView) findViewById(R.id.textViewSmoking);
        textViewGuarantor=(TextView) findViewById(R.id.textViewGuarantor);
        textViewSecurity=(TextView) findViewById(R.id.textViewSecurity);
        textViewDoorman=(TextView) findViewById(R.id.textViewDoorman);
        imageView5=(ImageView) findViewById(R.id.imageView5);
        imageView6=(ImageView) findViewById(R.id.imageView6);
        imageView7=(ImageView) findViewById(R.id.imageView7);
        imageView8=(ImageView) findViewById(R.id.imageView8);
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
        try {




            arrayList = bundle.getStringArrayList("arrayList");
            Log.v("_dan confirm",arrayList.toString());
//            firstTime= bundle.getBoolean("firstTime");
//            if(firstTime){
//                arrayList = bundle.getStringArrayList("arrayList");
//            }else{
//                strings=bundle.getStringArrayList("arrayList");
//                for(int i=0;i<strings.size();i++){
//                    arrayList.add(strings.get(i).replace("+",","));
//                }
//            }
//
//            Log.v("_danConfirmIntent",arrayList.toString()+firstTime.toString());
            for(int i=0;i<arrayList.size();i++){
                Log.v("_dan arraylist",arrayList.get(i));
            }
            textViewAddress.setText("Address: "+arrayList.get(0).replace("[","").replace("+",","));
            address=arrayList.get(0)
                    .replace("[","").replace("+",",");
            for(int i=0;i<arrayList.size();i++){
                abridgedList.add(arrayList.get(i).replace(",","+"));
            }
            //create a map to store user metadata
            myObjectMetadata = new ObjectMetadata();
            userMetadata = new HashMap<String,String>();
            userMetadata.put("info",abridgedList.toString());

            //call setUserMetadata on our ObjectMetadata object, passing it our map
            myObjectMetadata.setUserMetadata(userMetadata);
            Log.v("_dan",address);
            new latLngFromAddressTask().execute(address);



            textViewSqft.setText(arrayList.get(2)+" Sqft");
            textViewRent.setText("Rent:"+arrayList.get(3));
            textViewMinSalary.setText("Minimum Salary:"+arrayList.get(4));
            textViewShort.setText(arrayList.get(5));
            textViewLong.setText(arrayList.get(6));
            textViewBeds.setText(arrayList.get(7)+ " Beds");
            textViewBaths.setText(arrayList.get(8)+" Baths");
            if(arrayList.get(9).equals("true")) {
                textViewCouples.setText("Couples OK");
            } else {
                textViewCouples.setText("No Couples");
            }
            if(arrayList.get(10).equals("true")) {
                textViewChildren.setText("Children OK");
            } else {
                textViewChildren.setText("No Children");
            }
            if(arrayList.get(11).equals("true")) {
                textViewSmallDogs.setText("Small Dogs OK");
            } else if(arrayList.get(11).equals("false")) {
                textViewCouples.setText("No Small Dogs");
            }
            if(arrayList.get(12).equals("true")) {
                textViewDogs.setText("Large Dogs OK");
            } else {
                textViewDogs.setText("No Large Dogs");
            }
            if(arrayList.get(13).equals("true")) {
                textViewCats.setText("Cats OK");
            } else {
                textViewCats.setText("No Cats");
            }
            if(arrayList.get(14).equals("true")) {
                textViewSmoking.setText("Smoking OK");
            } else {
                textViewSmoking.setText("No Smoking");
            }
            if(arrayList.get(15).equals("true")) {
                textViewGuarantor.setText("Guarantor OK");
            } else {
                textViewGuarantor.setText("No Guarantors");
            }
            if(arrayList.get(16).equals("true")) {
                textViewSecurity.setText("Security Deposit Required");
            } else {
                textViewSecurity.setText("No Security Deposit");
            }
            if(arrayList.get(17).equals("true")) {
                textViewDoorman.setText("Doorman");
            } else {
                textViewDoorman.setText("No Doorman");
            }
            if(firstTime) {
                uri1 = Uri.parse(arrayList.get(18));
                uri2 = Uri.parse(arrayList.get(19));
                uri3 = Uri.parse(arrayList.get(20));
                uri4 = Uri.parse(arrayList.get(21));
                InputStream imageStream = getContentResolver().openInputStream(uri1);
                InputStream imageStream2 = getContentResolver().openInputStream(uri2);
                InputStream imageStream3 = getContentResolver().openInputStream(uri3);
                InputStream imageStream4 = getContentResolver().openInputStream(uri4);
                selectedImage = BitmapFactory.decodeStream(imageStream);
                selectedImage2 = BitmapFactory.decodeStream(imageStream2);
                selectedImage3 = BitmapFactory.decodeStream(imageStream3);
                selectedImage4 = BitmapFactory.decodeStream(imageStream4);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
                ByteArrayOutputStream bos3 = new ByteArrayOutputStream();
                ByteArrayOutputStream bos4 = new ByteArrayOutputStream();
                selectedImage.compress(Bitmap.CompressFormat.PNG, 100 /*ignored for PNG*/, bos);
                selectedImage2.compress(Bitmap.CompressFormat.PNG, 100, bos2);
                selectedImage3.compress(Bitmap.CompressFormat.PNG, 100, bos3);
                selectedImage4.compress(Bitmap.CompressFormat.PNG, 100, bos4);

                byte[] bitmapdata = bos.toByteArray();
                byte[] bitmapdata2 = bos2.toByteArray();
                byte[] bitmapdata3 = bos3.toByteArray();
                byte[] bitmapdata4 = bos4.toByteArray();

                imageView5.setImageBitmap(selectedImage);
                imageView6.setImageBitmap(selectedImage2);
                imageView7.setImageBitmap(selectedImage3);
                imageView8.setImageBitmap(selectedImage4);

                pic1 = new File(getApplicationContext().getCacheDir(), address + "pic1");
                pic1.createNewFile();
                FileOutputStream fos = new FileOutputStream(pic1);
                try {
                    fos.write(bitmapdata);
                    fos.flush();
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                pic2 = new File(getApplicationContext().getCacheDir(), address + "pic2");
                pic2.createNewFile();
                FileOutputStream fos2 = new FileOutputStream(pic2);
                try {
                    fos2.write(bitmapdata2);
                    fos2.flush();
                    fos2.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                pic3 = new File(getApplicationContext().getCacheDir(), address + "pic3");
                pic3.createNewFile();
                FileOutputStream fos3 = new FileOutputStream(pic3);
                try {
                    fos3.write(bitmapdata3);
                    fos3.flush();
                    fos3.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                pic4 = new File(getApplicationContext().getCacheDir(), address + "pic4");
                pic4.createNewFile();
                FileOutputStream fos4 = new FileOutputStream(pic4);
                try {
                    fos4.write(bitmapdata4);
                    fos4.flush();
                    fos4.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                new getImagesFromS3().execute(address);
            }
        }catch (Exception e ){
            e.printStackTrace();
        }
    }

    public void scheduleAppt(View v){

    }
    public void setupMap(){
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap map) {
        LatLng location=new LatLng(lat,lng);
        try {
            map.setMyLocationEnabled(true);
        }catch (SecurityException e){
            Log.v("_dan mapsec", e.getMessage());
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13));

        map.addMarker(new MarkerOptions()
                .title(arrayList.get(0))
                .snippet(arrayList.get(1))
                .position(location));
    }
    public class getImagesFromS3 extends AsyncTask<String, Integer, ArrayList<Bitmap>> {

        @Override
        protected ArrayList<Bitmap> doInBackground(String... params) {
            ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
            try {

                s3 = new AmazonS3Client(credentialsProvider);

                // Set the region of your S3 bucket
                s3.setRegion(Region.getRegion(Regions.US_EAST_1));
                transferUtility = new TransferUtility(s3, getApplicationContext());

                S3ObjectInputStream content = s3.getObject("hhproperties/"+address, "pic1").getObjectContent();
                byte[] bytes = IOUtils.toByteArray(content);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                bitmaps.add(bitmap);

                S3ObjectInputStream content2 = s3.getObject("hhproperties/"+address, "pic2").getObjectContent();
                byte[] bytes2 = IOUtils.toByteArray(content2);
                Bitmap bitmap2 = BitmapFactory.decodeByteArray(bytes2, 0, bytes2.length);
                bitmaps.add(bitmap2);

                S3ObjectInputStream content3 = s3.getObject("hhproperties/"+address, "pic3").getObjectContent();
                byte[] bytes3 = IOUtils.toByteArray(content3);
                Bitmap bitmap3 = BitmapFactory.decodeByteArray(bytes3, 0, bytes3.length);
                bitmaps.add(bitmap3);

                S3ObjectInputStream content4 = s3.getObject("hhproperties/"+address, "pic4").getObjectContent();
                byte[] bytes4 = IOUtils.toByteArray(content4);
                Bitmap bitmap4 = BitmapFactory.decodeByteArray(bytes4, 0, bytes4.length);
                bitmaps.add(bitmap4);
                return bitmaps;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmaps;
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> bitmaps) {
            imageView5.setImageBitmap(bitmaps.get(0));
            imageView6.setImageBitmap(bitmaps.get(1));
            imageView7.setImageBitmap(bitmaps.get(2));
            imageView8.setImageBitmap(bitmaps.get(3));
        }


    }
    private class latLngFromAddressTask extends AsyncTask<String, Void, String[]> {
        ProgressDialog dialog = new ProgressDialog(PropertyActivity.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Please wait...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected String[] doInBackground(String... params) {
            String response;
            try {
                response = getLatLongByURL("http://maps.google.com/maps/api/geocode/json?address="+params[0].replace(",","").replace(" ","+")+"&sensor=false");
                Log.d("response",""+response);
                return new String[]{response};
            } catch (Exception e) {
                Log.v("_dan gmbackground",e.getMessage());
                return new String[]{"error"};
            }
        }

        @Override
        protected void onPostExecute(String... result) {
            try {
                JSONObject jsonObject = new JSONObject(result[0]);

                lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lng");

                lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lat");

                Log.d("latitude", "" + lat);
                Log.d("longitude", "" + lng);
            } catch (JSONException e) {
                Log.v("_dan gmjson", e.getMessage());
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            setupMap();
        }
    }
    public String getLatLongByURL(String requestURL) {
        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";
            }

        } catch (Exception e) {
            Log.v("_danlatlngbyurl",e.getMessage());
            e.printStackTrace();
        }
        return response;
    }
}
