package com.example.rvnmrqz.firetrack;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

public class Activity_User_Settings extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Switch switch_masters_notif, switch_vibrate, switch_playsound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        switch_masters_notif = (Switch) findViewById(R.id.switch_master_notif);
        switch_playsound = (Switch) findViewById(R.id.switch_playsound);
        switch_vibrate = (Switch) findViewById(R.id.switch_notif_vibrate);

        sharedPreferences = getSharedPreferences(MySharedPref.SHAREDPREF_NAME,MODE_PRIVATE);
        setDisplay();
        switchListeners();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void switchListeners(){
        switch_masters_notif.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setSharePrefValue(MySharedPref.NOTIFICATIONS,isChecked);
                if(isChecked){
                    switch_playsound.setEnabled(true);
                    switch_vibrate.setEnabled(true);
                }else{
                    switch_playsound.setEnabled(false);
                    switch_vibrate.setEnabled(false);
                }
            }
        });

        switch_playsound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setSharePrefValue(MySharedPref.PLAY_NOTIFSOUND,isChecked);
            }
        });

        switch_vibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setSharePrefValue(MySharedPref.PLAY_VIBRATE,isChecked);
            }
        });
    }

    protected void setDisplay(){
        switch_masters_notif.setEnabled(false);
        switch_playsound.setEnabled(false);
        switch_vibrate.setEnabled(false);

        switch_masters_notif.setChecked(getSharedPrefValue(MySharedPref.NOTIFICATIONS));
        switch_vibrate.setChecked(getSharedPrefValue(MySharedPref.PLAY_VIBRATE));
        switch_playsound.setChecked(getSharedPrefValue(MySharedPref.PLAY_NOTIFSOUND));

        if(getSharedPrefValue(MySharedPref.NOTIFICATIONS)){

            switch_playsound.setEnabled(true);
            switch_vibrate.setEnabled(true);
        }else{
            switch_masters_notif.setChecked(false);
            switch_vibrate.setEnabled(false);
            switch_playsound.setEnabled(false);
        }
        switch_masters_notif.setEnabled(true);

    }

    protected boolean getSharedPrefValue(String key){
        boolean value = sharedPreferences.getBoolean(key,true);
        return value;
    }

    protected void setSharePrefValue(String key, boolean values){
        editor = sharedPreferences.edit();
        editor.putBoolean(key,values);
        editor.commit();
    }

}
