package com.hhalpha.daniel.homehunter12;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;

import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Daniel on 6/24/2016.
 */
public class RegisterActivity extends FragmentActivity {
    LoginButton loginButton;
    CallbackManager callbackManager;
    TextView textViewFB;
    AccessToken accessToken;
    Profile profile;
    SharedPreferences preferences;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
//        AppEventsLogger.activateApp(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
//        printHash();
        setContentView(R.layout.activity_register);
        textViewFB=(TextView) findViewById(R.id.textViewFB);

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
//                textViewFB.setText("Success!");
                Log.v("_dan","success");
                try {
//            accessToken = AccessToken.getCurrentAccessToken();
//            Log.v("_dan", accessToken.getUserId());
                    profile = Profile.getCurrentProfile();
                    Log.v("_dan",profile.getName());
                }catch (Exception e){
                    e.printStackTrace();
                }
                preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("registered",true);
                editor.putString("loginMethod","FB");
                editor.putString("profileName",profile.getName());
                editor.apply();
                Intent i = new Intent(RegisterActivity.this, RegisterActivity2.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("registered",preferences.getBoolean("registered",false));
                bundle.putString("loginMethod",preferences.getString("loginMethod",""));
                bundle.putString("loginMethod",preferences.getString("profileName",""));
                i.putExtra("bundle",bundle);
                startActivity(i);
            }

            @Override
            public void onCancel() {
//                textViewFB.setText("Cancel");
                Log.v("_dan","cancel");
            }

            @Override
            public void onError(FacebookException exception) {
//                textViewFB.setText("Issue logging in to Facebook");
                Log.v("_dan","error");
            }

        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        try{
            printHash();
        }catch (Exception e){
             e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void printHash(){
        // code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
//                    "com.facebook.samples.hellofacebook",
                    "com.hhalpha.daniel.homehunter12",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("_dan's KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
