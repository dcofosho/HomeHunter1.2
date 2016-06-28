package com.hhalpha.daniel.homehunter12;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Daniel on 6/28/2016.
 */
public class RegisterActivity2 extends Activity {
    EditText editTextSalary,editTextEmail;
    Integer salary;
    String email,loginMethod,profileName;
    Boolean registered;
    Button reg2Button;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Bundle bundle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);
        bundle = new Bundle();
        editTextSalary=(EditText) findViewById(R.id.editTextSalary);
        editTextEmail=(EditText)findViewById(R.id.editTextEmail);
        reg2Button=(Button) findViewById(R.id.reg2Button);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = preferences.edit();
        try{
            profileName=preferences.getString("profileName","");
            loginMethod=preferences.getString("loginMethod","");
            registered=preferences.getBoolean("registered",false);
            bundle.putString("profileName",profileName);
            bundle.putString("loginMethod",loginMethod);
            bundle.putBoolean("registered",registered);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void submit(View v){
        salary=Integer.parseInt(editTextSalary.getText().toString());
        email=editTextEmail.getText().toString();
        editor.putString("email",email);
        editor.putInt("salary",salary);
        editor.apply();
        Intent i = new Intent(RegisterActivity2.this,MainActivity.class);
        bundle = new Bundle();
        bundle.putString("email",email);
        bundle.putInt("salary",salary);
        i.putExtra("bundle",bundle);
        startActivity(i);
    }
}
