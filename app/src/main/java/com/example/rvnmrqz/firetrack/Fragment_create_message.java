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

public class Fragment_create_message extends Fragment {

    View myview;
    DBHelper dbHelper;
    String number=null;
    TextView txtCounter, txtMessage,txtNumber, txtLocationResult;
    AutoCompleteTextView auto_barangay;
    CheckBox chkLocations;
    Button btnSend;
    int ctr = 0;
    private LocationManager locationManager;
    private LocationListener locationListener;
    String coordinates = null;

    ArrayList<String> barangays;
    ArrayList<String> cell;
    int barangay_local_id;
    String selectedBarangay=null;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String results[] = new String[]{"", "Getting Location . . .", "Location Added ✓", "Failed To Get Location X"};
    int colors[] = new int[]{Color.WHITE, Color.DKGRAY, Color.BLUE, Color.RED};
    int LOCATION_PERMISSION = 10;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myview = inflater.inflate(R.layout.fragment_create_message, container, false);
        return myview;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper  = new DBHelper(getActivity());
        txtCounter = (TextView) getActivity().findViewById(R.id.txtCharCounter);
        txtMessage = (TextView) getActivity().findViewById(R.id.txtMessageBody);
        txtNumber = (TextView) getActivity().findViewById(R.id.txtSMSNumber);
        auto_barangay = (AutoCompleteTextView) getActivity().findViewById(R.id.autoComplete_Barangay);
        btnSend = (Button) getActivity().findViewById(R.id.btnSendMessage);
        chkLocations = (CheckBox) getActivity().findViewById(R.id.checkboxLocation);
        txtLocationResult = (TextView) getActivity().findViewById(R.id.txtLocationResult);
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
        addCheckLocationListener();
        btnSendListener();
        populateAutoCompleteBarangay();
        setDefaultBarangay();

       // checkShowDialog();

        chkLocations.performClick();
    }

    @Override
    public void onDestroyView() {
        Log.wtf("Fragment_create_message","onDestroyView");
        Log.wtf("OnDestoryView","Location manager updates removed");

        stopLocationListener();

        super.onDestroyView();
    }


    //BARANGAY LIST
    protected void populateAutoCompleteBarangay(){
        barangays = new ArrayList<>();
        cell = new ArrayList<>();

        //Popualting arraylist
        Cursor c = dbHelper.getSqliteData("SELECT * FROM "+dbHelper.TABLE_BARANGAY+";");
        if(c!=null){
            Log.wtf("getBarangay","cursor is not null");
            c.moveToFirst();
            do {
                barangays.add(c.getString(c.getColumnIndex(dbHelper.BARANGAY_NAME)));
                cell.add(c.getString(c.getColumnIndex(dbHelper.BARANGAY_CEL)));
            }while (c.moveToNext());

        }else Log.wtf("getBarangay","cursor is null");

        //passing arraylist to adapter
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, barangays);
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
        if(c!=null){
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
             if(continueSend()) {
                 Log.wtf("btnClicked","continue is true");
                 //check if contact is not null
                 if(number!=null){
                    Log.wtf("btnClicked","Number is not null: "+number);
                     //check if message is not null
                     if(!txtMessage.getText().toString().trim().equals("")){
                         Log.wtf("btnClicked","Message is not null");
                         String msg = txtMessage.getText().toString().trim();
                         if(coordinates!=null&&chkLocations.isChecked()){
                             msg+="\n{"+coordinates+"}";
                         }
                         sendSMS(number,msg);
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

             }else Log.wtf("btn clicked","continue send is false");
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
    private boolean continueSend(){
        if(chkLocations.isChecked() && coordinates!=null){
            return true;
        }
        else if(chkLocations.isChecked() && coordinates==null){
            Toast.makeText(getActivity(), "Location is not yet captured", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!chkLocations.isChecked()){
            return true;
        }
        return false;
    }

    //LOCATION
    protected void getLocation() {
        try {
            locationManagerInitialize();
            txtLocationResult.setText(results[1]);
            txtLocationResult.setTextColor(colors[1]);
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Log.wtf("getLocation","Called");
            locationManager.requestLocationUpdates("gps", 10000, 0, locationListener);

        }catch (Exception e){
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.wtf("getLocation Error",e.getMessage());
        }
    }
    protected void stopLocationListener(){
        if(locationManager!=null) {
            locationManager.removeUpdates(locationListener);
            locationManager=null;
        }
    }
    protected void locationManagerInitialize(){
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                coordinates = location.getLatitude()+","+location.getLongitude();
                txtLocationResult.setText(results[2]);
                txtLocationResult.setTextColor(colors[2]);
                if(getActivity()!=null) {
                    Toast.makeText(getActivity(), "Location Added, Thanks!", Toast.LENGTH_SHORT).show();
                    stopLocationListener();
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.wtf("locationListener","onStatusChanged, status: "+status);
                checkPermission();
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.wtf("locationListener","onProviderEnabled: "+provider);
                checkPermission();
            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }
    protected void addCheckLocationListener() {
        chkLocations.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if(isLocationEnabled(getActivity())){
                        txtLocationResult.setText(results[1]);
                        txtLocationResult.setTextColor(colors[1]);
                        locationManagerInitialize();
                        checkPermission();
                    }else{
                        txtLocationResult.setText(results[3]);
                        txtLocationResult.setTextColor(colors[3]);
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Turn On Location")
                                .setMessage("This function Requires Location")
                                .setCancelable(false)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.wtf("TurnOnGPSTracking","Settings intent is called");
                                        openGPSinSettings();
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.wtf("TurnOnGPSTracking","User clicked Cancel");
                                        txtLocationResult.setText(results[0]);
                                        chkLocations.setChecked(false);
                                    }
                                })
                                .show();
                    }
                } else {
                    txtLocationResult.setText(results[0]);
                    txtLocationResult.setTextColor(colors[0]);
                }
            }
        });
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
        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),100);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==100){
            //check if the location is enabled
            if(isLocationEnabled(getActivity())){
                checkPermission();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //PERMISSIONS
    protected void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.wtf("Location_service", "PERMISSION CHECK FOR M AND HIGHER");
            //provider,minimum time refresh in milisecond, minimum distance refresh in meter,location listener
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                Log.wtf("Location", "NOT GRANTED");
                txtLocationResult.setText(results[3]);
                txtLocationResult.setTextColor(colors[3]);
                Toast.makeText(getActivity(), "Location Permission Not Granted", Toast.LENGTH_SHORT).show();
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION);
                return;
            } else {
                Log.wtf("location_service", "REQUEST PERMISSION GRANTED");
                getLocation();
            }
        } else {
            //get the location
            Log.wtf("Location_service", "LOWER ANDROID VERSION");
            getLocation();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==LOCATION_PERMISSION){
            Log.wtf("OnRequestResult","request code is Location Permission");
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.wtf("RequestResult","Request is granted");
                getLocation();
            }else{
                Log.wtf("RequestResult","Request is not granted");
            }
        }
    }



    //REMINDER
    protected void checkShowDialog(){
        Log.wtf("checkshowdialog","called");
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
    protected void showReminder(){
        Log.wtf("ShowReminder","called");
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
