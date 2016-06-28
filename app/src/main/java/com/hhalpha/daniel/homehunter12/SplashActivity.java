package com.hhalpha.daniel.homehunter12;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Daniel on 6/24/2016.
 */
public class SplashActivity extends Activity {
    Boolean registered;
    String profileName;
    String loginMethod;
    Bundle bundle;
    Intent i;
    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 1000;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash);
        bundle=new Bundle();
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        try{
            Log.v("_dan",preferences.getAll().toString());
            registered=preferences.getBoolean("registered",false);
            profileName=preferences.getString("profileName","");
            loginMethod=preferences.getString("loginMethod","");
            bundle.putBoolean("registered",registered);
            bundle.putString("loginMethod",loginMethod);
            bundle.putString("profileName",profileName);
            i = new Intent(SplashActivity.this, MainActivity.class);
            i.putExtra("bundle",bundle);
        }catch (Exception e){
            i = new Intent(SplashActivity.this, RegisterActivity.class);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */

                SplashActivity.this.startActivity(i);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
//        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//        if(preferences.contains("registered")) {
//        /* New Handler to start the Menu-Activity
//         * and close this Splash-Screen after some seconds.*/
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                /* Create an Intent that will start the Menu-Activity. */
//                    Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
//                    Bundle bundle = new Bundle();
//                    bundle.putString("loginMethod",preferences.getString("loginMethod",""));
//                    bundle.putString("profileName",preferences.getString("profileName",""));
//                    SplashActivity.this.startActivity(mainIntent);
//                    SplashActivity.this.finish();
//                }
//            }, SPLASH_DISPLAY_LENGTH);
//        }else{
////            preferences.edit().putBoolean("registered", true);
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                /* Create an Intent that will start the Menu-Activity. */
//                    Intent mainIntent = new Intent(SplashActivity.this, RegisterActivity.class);
//                    SplashActivity.this.startActivity(mainIntent);
//                    SplashActivity.this.finish();
//                }
//            }, SPLASH_DISPLAY_LENGTH);
//        }
    }
}
