package com.hhalpha.daniel.homehunter12;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by Daniel on 6/24/2016.
 */
public class MainActivity extends Activity {
    String profileName, loginMethod;
    Boolean registered;
    TextView textViewMain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewMain=(TextView) findViewById(R.id.textViewMain);
        try {
            Bundle bundle = getIntent().getBundleExtra("bundle");
            profileName=bundle.getString("profileName","");
            loginMethod=bundle.getString("loginMethod","");
            registered=bundle.getBoolean("registered",false);
            textViewMain.setText(profileName+" "+loginMethod + " " +registered.toString());
            Log.v("_danMain",bundle.toString());
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
