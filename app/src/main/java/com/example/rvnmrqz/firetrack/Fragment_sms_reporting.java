package com.example.rvnmrqz.firetrack;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by Rvn Mrqz on 2/19/2017.
 */

public class Fragment_sms_reporting extends Fragment {

    View myview;
    static DBHelper dbHelper;
    static String number=null;
    static TextView txtCounter, txtMessage,txtNumber;
    static AutoCompleteTextView auto_barangay;
    Button btnSend;
    int ctr = 0;
    TextView txtLocation;
    static ArrayList<String> barangays;
    static ArrayList<String> cell;
    public static Activity context;
    int barangay_local_id;
    static String selectedBarangay=null;

    LocationManager locationManager;
    LocationListener locationListener;
    String coordinates = null;

    int
            LOCATION_PERMISSION=2,
            OPEN_GPS_SETTINGS_REQUEST=30,
            OPEN_PERMISSION_REQUEST=40;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myview = inflater.inflate(R.layout.fragment_create_message, container, false);
        return myview;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getActivity();
        dbHelper  = new DBHelper(getActivity());
        txtCounter = (TextView) getActivity().findViewById(R.id.txtCharCounter);
        txtMessage = (TextView) getActivity().findViewById(R.id.txtMessageBody);
        txtNumber = (TextView) getActivity().findViewById(R.id.txtSMSNumber);
        auto_barangay = (AutoCompleteTextView) getActivity().findViewById(R.id.autoComplete_Barangay);
        btnSend = (Button) getActivity().findViewById(R.id.btnSendMessage);

        txtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ctr = txtMessage.getText().length();
                txtCounter.setText(ctr + "/160");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        auto_barangay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(selectedBarangay!=null){
                    number=null;
                    txtNumber.setText("");
                }
                selectedBarangay=null;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        txtLocation = (TextView) getActivity().findViewById(R.id.txtSMSLocation);
        txtLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocationChoices();
            }
        });

        btnSendListener();
        populateAutoCompleteBarangay();
        setDefaultBarangay();
        showLocationChoices();
    }

    @Override
    public void onDestroyView() {
        Log.wtf("Fragment_sms_reporting","onDestroyView");
        Log.wtf("OnDestoryView","Location manager updates removed");
        stopLocationListener();
        super.onDestroyView();
    }

    protected void showLocationChoices(){

        Log.wtf("Dialog","Location Choices shown");
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setTitle("Location to use");
        builder.setItems(R.array.location_pop_up_menu, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                 /*
                     [0]  Registered Location
                     [1]  Location Now
                     */
                txtLocation.setError(null);
                switch (item){
                    case 0:
                        //get saved coordinates in sqlite
                        Log.wtf("Dialog","Registered Location Selected");
                        stopLocationListener();

                        dbHelper  = new DBHelper(getActivity());

                        Cursor c = dbHelper.getSqliteData("SELECT "+dbHelper.COL_COORDINATES +" FROM "+dbHelper.TABLE_USER+" WHERE "+dbHelper.COL_USER_LOC_ID+" = 1;");
                        if(c!=null){
                            c.moveToFirst();
                            coordinates = c.getString(c.getColumnIndex(dbHelper.COL_COORDINATES));
                            txtLocation.setText("{"+coordinates+"}");
                        }else{
                            Toast.makeText(getActivity(), "No Coordinates Saved", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1:
                        //get current location
                        Log.wtf("Dialog","Current Location Selected");
                        txtLocation.setText("Tap to Set");
                        coordinates=null;
                        getCurrentLocation();
                        break;
                    case 2:
                        //do nothing
                        break;
                }
            }
        });
        android.app.AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                auto_barangay.requestFocus();
            }
        });
        alert.show();

    }

    //LOCATION
    protected void getCurrentLocation(){
        if(isLocationPermissionGranted()){
            Log.wtf("get current location","Permission is Granted");
            if(isLocationEnabled(getActivity())){
                requestLocationUpdate();
                Log.wtf("get current location","Location is enabled");
            }else{
                Log.wtf("get current location","Location is disabled");
                openGPSinSettings();
            }
        }else{
            //request permission
            Log.wtf("getCurrentLocation","Permission not granted");
            Toast.makeText(getActivity(), "Grant Permission", Toast.LENGTH_SHORT).show();
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }
    }
    protected void locationManagerInitialize(){
        Log.wtf("locationInitialize","called");
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.wtf("onLocationChange","Location is changed "+location);
                coordinates = location.getLatitude()+","+location.getLongitude();
                if(getActivity()!=null) {
                    txtLocation.setError(null);
                    txtLocation.setText("{"+coordinates+"}");
                    Toast.makeText(getActivity(), "Location Added, Thanks!", Toast.LENGTH_SHORT).show();
                    stopLocationListener();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }
    protected void requestLocationUpdate() {
        try {
            locationManagerInitialize();
            txtLocation.setText("Waiting for location...");
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Log.wtf("request Location","Called");
            locationManager.requestLocationUpdates("gps", 10000, 0, locationListener);

        }catch (Exception e){
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.wtf("getLocation Error",e.getMessage());
        }
    }
    protected boolean isLocationPermissionGranted(){
        int locationCheck = ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION);

        if(locationCheck == PackageManager.PERMISSION_GRANTED){
            return true;
        }else{
            return false;
        }
    }
    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }
    protected void openGPSinSettings(){
        new android.app.AlertDialog.Builder(getActivity())
                .setTitle("Turn On Location")
                .setMessage("This function Requires Location")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.wtf("TurnOnGPSTracking","Settings intent is called");
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),OPEN_GPS_SETTINGS_REQUEST);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.wtf("TurnOnGPSTracking","User clicked Cancel");
                    }
                })
                .show();
    }
    protected void stopLocationListener(){
        if(locationManager!=null) {
            locationManager.removeUpdates(locationListener);
            locationManager=null;
        }
    }


    //BARANGAY LIST
    protected static void populateAutoCompleteBarangay(){
        barangays = new ArrayList<>();
        cell = new ArrayList<>();

        //Popualting arraylist
        Cursor c = dbHelper.getSqliteData("SELECT * FROM "+dbHelper.TABLE_BARANGAY+";");
        if(c!=null && c.getCount()>0){
            Log.wtf("getBarangay","cursor is not null");
            c.moveToFirst();
            do{
                barangays.add(c.getString(c.getColumnIndex(dbHelper.BARANGAY_NAME)));
                cell.add(c.getString(c.getColumnIndex(dbHelper.BARANGAY_CEL)));
            }while (c.moveToNext());
        }
        else{
            Log.wtf("getBarangay","cursor is null");
            Toast.makeText(context, "There are No Barangay contact details saved", Toast.LENGTH_SHORT).show();
            new AlertDialog.Builder(context)
                    .setTitle("Sync Barangay Details")
                    .setMessage("Do you want to sync barangay details?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new SyncBarangay(context,1,true);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
        }

        //passing arraylist to adapter
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item, barangays);
        auto_barangay.setAdapter(adapter);

        //select listener
        auto_barangay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                int index = barangays.indexOf(adapter.getItem(position)); // to get the original position of item in arraylist
                if(cell.get(index).trim().equals("null")){
                    number=null;
                    txtNumber.setText("<NO NUMBER>");
                    auto_barangay.setError("Barangay has NO number");
                }
                else{
                    txtNumber.setText(cell.get(index)+"");
                    txtMessage.requestFocus();
                    number=cell.get(index).trim();
                }
                selectedBarangay=auto_barangay.getText().toString();

            }
        });
    }
    protected void setDefaultBarangay(){
        //get the barangay id of user logged
        //set the barangay details in textviews
        Cursor c = dbHelper.getSqliteData("SELECT "+dbHelper.BARANGAY_LOC_ID+" FROM "+dbHelper.TABLE_BARANGAY+" u INNER JOIN "+dbHelper.TABLE_USER +" b ON u."+dbHelper.COL_BARANGAY_ID+" = b."+dbHelper.COL_BARANGAY_ID+";" );
        if(c!=null && c.getCount()>0){
                Log.wtf("setDefualtBarangay","c is not null");
                c.moveToFirst();
                barangay_local_id = Integer.parseInt(c.getString(c.getColumnIndex(dbHelper.BARANGAY_LOC_ID)));
                Log.wtf("setDefualtBarangay","barangay_local_id: "+barangay_local_id);

                auto_barangay.setText(barangays.get(barangay_local_id));
                String number = cell.get(barangay_local_id);
                if(number.equals("null")){
                    txtNumber.setText("<NO NUMBER>");
                    auto_barangay.setError("Barangay has NO number");
                    this.number=null;
                }else{
                    txtNumber.setText(number);
                    this.number = number;
                }
                selectedBarangay = auto_barangay.getText().toString();
                auto_barangay.requestFocus();
                try{
                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                }catch (Exception e){
                    Toast.makeText(getActivity(), "Failed to open keyboard", Toast.LENGTH_SHORT).show();
                }
            }
    }


    //SENDING
    protected void btnSendListener(){

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Log.wtf("btnClicked","continue is true");
                 //check if contact is not null
                 if(number!=null){
                    Log.wtf("btnClicked","Number is not null: "+number);
                     //check if message is not null
                     if(!txtMessage.getText().toString().trim().equals("")){
                         Log.wtf("btnClicked","Message is not null");
                         String msg = txtMessage.getText().toString().trim();
                         if(coordinates!=null){
                             msg+="\n{"+coordinates+"}";
                             sendSMS(number,msg);
                         }else{
                             txtLocation.setError("No Location Given");
                             txtLocation.requestFocus();
                         }
                     }
                     else{
                         //no message yet
                         txtMessage.requestFocus();
                         txtMessage.setError("Message should not be empty");
                     }
                 }else{
                     Log.wtf("btnClicked","number is null");
                     auto_barangay.requestFocus();
                     auto_barangay.setError("Plese fill up with correct details");
                 }
            }
        });
    }
    public void sendSMS(String phonenumber, String message){
        try {
            if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getActivity(), "App permission not granted", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.SEND_SMS},1);
            }
            else{

                String SENT = "sent";
                String DELIVERED = "delivered";

                Intent sentIntent = new Intent(SENT);
     /*Create Pending Intents*/
                PendingIntent sentPI = PendingIntent.getBroadcast(
                        getActivity(), 0, sentIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                Intent deliveryIntent = new Intent(DELIVERED);

                PendingIntent deliverPI = PendingIntent.getBroadcast(
                        getActivity(), 0, deliveryIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
     /* Register for SMS send action */
                getActivity().registerReceiver(new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String result = "";

                        switch (getResultCode()) {

                            case Activity.RESULT_OK:
                                result = "Sent successful";
                                //PN.setText("");
                                //MSG.setText("");
                                break;
                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                result = "Sending failed";
                                break;
                            case SmsManager.RESULT_ERROR_RADIO_OFF:
                                result = "Turn off Airplane mode";
                                break;
                            case SmsManager.RESULT_ERROR_NULL_PDU:
                                result = "No PDU defined";
                                break;
                            case SmsManager.RESULT_ERROR_NO_SERVICE:
                                result = "No service";
                                break;
                        }

                        Toast.makeText(getActivity(), result,
                                Toast.LENGTH_LONG).show();
                    }

                }, new IntentFilter(SENT));
     /* Register for Delivery event */
                getActivity().registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Toast.makeText(getActivity(), "Deliverd",
                                Toast.LENGTH_LONG).show();
                    }

                }, new IntentFilter(DELIVERED));


                //SENDING THE MESSAGE
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(phonenumber, null, message, sentPI, deliverPI);
                Toast.makeText(getActivity(), "Sending ...", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception ee){
            Log.wtf("Send SMS Error",ee.getMessage());
            Toast.makeText(getActivity(), ee.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode ==  OPEN_GPS_SETTINGS_REQUEST){
            Log.wtf("activity result","Result for opening gps location");
            if(isLocationEnabled(getActivity())){
                Log.wtf("activity_result","location is not opened");
                requestLocationUpdate();
            }else{
                Log.wtf("activity_result","location is not opened");
            }
        }
        else if(requestCode == OPEN_PERMISSION_REQUEST){
            if(isLocationPermissionGranted()){
                getCurrentLocation();
            }else{
                Toast.makeText(getActivity(), "Permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==LOCATION_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.wtf("permissionResult", "Fine Location is granted");
                getCurrentLocation();
            } else {
                Log.wtf("permissionResult", "Fine Location is NOT granted");
                //NOT GRANTED
                boolean showRationale = shouldShowRequestPermissionRationale(permissions[0]);
                if (!showRationale) {
                    //USER SELECTED DO NOT SHOW AGAIN
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                    builder.setTitle("Allow Permission");
                    builder.setMessage("You cannot use this function without permission. Please allow the permission.");
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivityForResult(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)), OPEN_PERMISSION_REQUEST);
                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getActivity(), "You need to grant permission to use this", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.show();
                }
            }
        }
    }
}