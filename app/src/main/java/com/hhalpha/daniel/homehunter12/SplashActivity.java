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
    String profileName, loginMethod, email;
    Integer salary;
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
            email=preferences.getString("email","");
            salary=preferences.getInt("salary",0);
            bundle.putString("email",email);
            bundle.putInt("salary",salary);
            registered = preferences.getBoolean("registered", false);
            profileName = preferences.getString("profileName", "");
            loginMethod = preferences.getString("loginMethod", "");
            bundle.putBoolean("registered", registered);
            bundle.putString("loginMethod", loginMethod);
            bundle.putString("profileName", profileName);
            i = new Intent(SplashActivity.this, MainActivity.class);
            i.putExtra("bundle",bundle);
        }catch (Exception e){
            e.printStackTrace();
        }

        if((email.isEmpty()||email.equals(""))||salary==0){
            try {
                Log.v("_dan", preferences.getAll().toString());
                registered = preferences.getBoolean("registered", false);
                profileName = preferences.getString("profileName", "");
                loginMethod = preferences.getString("loginMethod", "");
                bundle.putBoolean("registered", registered);
                bundle.putString("loginMethod", loginMethod);
                bundle.putString("profileName", profileName);
                i = new Intent(SplashActivity.this, RegisterActivity2.class);
                i.putExtra("bundle", bundle);
            }catch (Exception ex){
                ex.printStackTrace();
            }

        }
        if(registered.equals(false)||profileName.isEmpty()||profileName.equals("")||loginMethod.isEmpty()||loginMethod.equals("")){
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

    }
}
