package com.example.rvnmrqz.firetrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(MySharedPref.SHAREDPREF_NAME,Context.MODE_PRIVATE);
        String logged = sharedPreferences.getString(MySharedPref.LOGGED,"");
        if(!logged.trim().equals("")){
            Intent myIntent = new Intent(context, Service_Notification.class);
            context.startService(myIntent);
        }
    }
}