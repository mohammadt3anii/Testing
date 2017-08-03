package com.example.rvnmrqz.firetrack;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

/**
 * Created by Rvn Mrqz on 2/19/2017.
 */

public class Fragment_report_options extends Fragment {
    View myview;
    Button btnOnline,btnMessage;
    int PERMISSION_SMS = 10,PERMISSION_CALL=20;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myview = inflater.inflate(R.layout.fragment_report_options, container, false);
        return myview;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Reporting");
        btnOnline = (Button) getActivity().findViewById(R.id.btnOnlineReport);
        btnMessage = (Button) getActivity().findViewById(R.id.btnMessage);
        sharedPreferences = getActivity().getSharedPreferences(MySharedPref.SHAREDPREF_NAME, Context.MODE_PRIVATE);
        checkShowDialog();
        buttonListeners();
    }

    protected void buttonListeners(){

        btnOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity_user.addToBackStack(new Fragment_online_reporting(),"online_reporting");
            }
        });


        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    Log.wtf("Location","PERMISSION CHECK FOR M AND HIGHER");
                    //provider,minimum time refresh in milisecond, minimum distance refresh in meter,location listener
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        Log.wtf("Location","NOT GRANTED");
                        Log.wtf("Location","Requesting permission");
                        requestPermissions(new String[]{Manifest.permission.SEND_SMS},PERMISSION_SMS);
                        return;
                    }else{
                        Log.wtf("REQUEST PERMISSION"," Already GRANTED");
                       openCreateMessage();
                    }
                }else{
                    //get the location
                    Log.wtf("Location","LOWER ANDROID VERSION");
                    Log.wtf("Location","No need to request permission");
                    openCreateMessage();
                }

            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==PERMISSION_SMS){
            Log.wtf("RequestResult","code is "+PERMISSION_SMS);
            if(permissions.length>0){
                for(int x=0;x<permissions.length;x++){
                    Log.wtf("Permission ["+x+"]",permissions[x]);
                    Log.wtf("Grant Result ["+x+"]",grantResults[x]+"");
                }
            }
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.wtf("RequestResult","Granted");
                Toast.makeText(getActivity(), "You can now use SMS function", Toast.LENGTH_SHORT).show();

                //to fix, ERROR: Can not perform this action after onSaveInstanceState
                new Handler().post(new Runnable() {
                    public void run() {
                       openCreateMessage();
                    }
                });

            }else{
                Log.wtf("RequestResult","denied");
                Toast.makeText(getActivity(),"Grant the permission before using this",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCreateMessage(){
       MainActivity_user.addToBackStack(new Fragment_sms_reporting(),"sms_reporting");
    }

    //REMINDER
    protected void checkShowDialog(){
        Log.wtf("checkshowdialog","called");
        if(MainActivity_user.reminderIsShown==false){
            String show = getSharedPrefData(MySharedPref.REMINDER);
            if(show!=null){
                show = show.trim();
                switch (show){
                    case "":
                        Log.wtf("checkShowDialog","reminder is not empty");
                        showReminder();
                        break;
                    case "no":
                        //user selected dont show again
                        Log.wtf("checkShowDialog","reminder is set to don't show again");
                        break;
                }
            }else{
                Log.wtf("checkShowDialog","show is null");
                showReminder();
            }
        }
    }
    protected void showReminder(){
        Log.wtf("ShowReminder","called");
        MainActivity_user.reminderIsShown=true;
        Log.wtf("showReminder","reminderisshown = "+MainActivity_user.reminderIsShown);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialog =  inflater.inflate(R.layout.dialog_reminder, null);
        final CheckBox chk = (CheckBox) dialog.findViewById(R.id.chkDontShowAgain);
        chk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    setSharedPrefData(MySharedPref.REMINDER,"no");
                }else{
                    setSharedPrefData(MySharedPref.REMINDER,"");
                }
            }
        });
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setTitle("Reminder");
        builder.setMessage(R.string.reminder);
        builder.setView(dialog);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //do nothing

            }
        });
        builder.show();
    }

    //SHARED PREFERENCE GET AND SET
    protected String getSharedPrefData(String key){
        try {
            String value = sharedPreferences.getString(key,"");
            return value;
        }catch (Exception ee){
            Toast.makeText(getActivity(), "Error in getSharedPrefData", Toast.LENGTH_SHORT).show();
            Log.wtf("getSharedPrefData: ERROR ",ee.getMessage());
        }
        return null;
    }
    protected void setSharedPrefData(String key, String value){
        try{
            editor = sharedPreferences.edit();
            editor.putString(key,value);
            editor.apply();
        }catch (Exception ee){
            Toast.makeText(getActivity(), "Error in setSharedPrefData", Toast.LENGTH_SHORT).show();
            Log.wtf("setSharedPrefData: ERROR ",ee.getMessage());
        }
    }

}
