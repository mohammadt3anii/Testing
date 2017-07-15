package com.example.rvnmrqz.firetrack;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.security.Permission;

/**
 * Created by Rvn Mrqz on 2/19/2017.
 */

public class Fragment_report_options extends Fragment {
    View myview;
    Button btnOnline,btnMessage;
    int PERMISSION_SMS = 10,PERMISSION_CALL=20;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myview = inflater.inflate(R.layout.fragment_report_options, container, false);
        return myview;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnOnline = (Button) getActivity().findViewById(R.id.btnOnlineReport);
        btnMessage = (Button) getActivity().findViewById(R.id.btnMessage);

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
            }else{
                Log.wtf("RequestResult","denied");
                Toast.makeText(getActivity(),"Grant the permission before using this",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCreateMessage(){
       MainActivity_user.addToBackStack(new Fragment_create_message(),"sms_reporting");
    }


}
