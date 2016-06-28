package com.hhalpha.daniel.homehunter12;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by Daniel on 6/24/2016.
 */
public class MainActivity extends Activity {
    String profileName, loginMethod,email, salary;
    Boolean registered;
    TextView textViewMain;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewMain=(TextView) findViewById(R.id.textViewMain);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        try {
            Bundle bundle = getIntent().getBundleExtra("bundle");
            email=bundle.getString("email","");
            salary=bundle.getString("salary","");
            Log.v("_dan",email+salary);

        }catch(Exception e){
            e.printStackTrace();
        }
        try {
            Bundle bundle = getIntent().getBundleExtra("bundle");
            profileName=bundle.getString("profileName","");
            loginMethod=bundle.getString("loginMethod","");
            registered=bundle.getBoolean("registered",false);
            textViewMain.setText(preferences.getAll().toString());
            Log.v("_danMain",bundle.toString());
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
