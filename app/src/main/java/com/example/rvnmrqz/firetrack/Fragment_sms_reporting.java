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
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import static android.content.Context.LOCATION_SERVICE;
import static com.google.android.gms.internal.zzip.runOnUiThread;

/**
 * Created by Rvn Mrqz on 2/19/2017.
 */

public class Fragment_sms_reporting extends Fragment {

    View myview;
    String msg;
    static DBHelper dbHelper;

    static TextView txtBarangayName,txtCounter, txtMessage,txtNumber;
  //  static AutoCompleteTextView auto_barangay;
    Button btnSend;
    int ctr = 0;
    TextView txtLocation;

    static ArrayList<Integer> b_local_id;
    static ArrayList<String> b_name;
    static ArrayList<String> b_cell;
    static ArrayList<LatLng> b_coordinates;

    public static Activity context;

    int barangay_local_id=-1;
    static String selectedBarangay=null;
    static String number=null;
    String coordinates = null;


    LocationManager locationManager;
    LocationListener locationListener;


    LinearLayout loadinglayout;
    ProgressBar loadingprogressbar;
    TextView loadingTextview;

    int
            LOCATION_PERMISSION=2,
            OPEN_GPS_SETTINGS_REQUEST=30,
            OPEN_PERMISSION_REQUEST=40;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myview = inflater.inflate(R.layout.fragment_sms_reporting, container, false);
        return myview;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("SMS Reporting");
        context = getActivity();
        Activity_main_user.somethingisnotyetdone=true;
        dbHelper  = new DBHelper(getActivity());
        txtBarangayName = (TextView) getActivity().findViewById(R.id.txtSMSBarangayName);
        txtCounter = (TextView) getActivity().findViewById(R.id.txtCharCounter);
        txtMessage = (TextView) getActivity().findViewById(R.id.txtMessageBody);
        txtNumber = (TextView) getActivity().findViewById(R.id.txtSMSNumber);


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

        loadinglayout = (LinearLayout) getActivity().findViewById(R.id.sms_loading_layout);
        loadingprogressbar = (ProgressBar) getActivity().findViewById(R.id.sms_loading_progressbar);
        loadingTextview = (TextView) getActivity().findViewById(R.id.sms_loading_textview);

        txtLocation = (TextView) getActivity().findViewById(R.id.txtSMSLocation);
        txtLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocationChoices();
            }
        });

        populateBarangayArrayListDetails();
        btnSendListener();

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
                            showLoadingLayout(true,true,"Finding nearest fire station");
                            barangay_local_id=-1;
                            number=null;
                            txtBarangayName.setText("");
                            txtNumber.setText("");
                            findNearestBarangay();
                        }else{
                            Toast.makeText(getActivity(), "No Coordinates Saved", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1:
                        //get current location
                        Log.wtf("Dialog","Current Location Selected");
                        txtLocation.setText("Tap to Set");
                        coordinates=null;
                        barangay_local_id=-1;
                        number=null;
                        txtBarangayName.setText("");
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
             //   auto_barangay.requestFocus();
            }
        });
        alert.show();

    }
    protected void showLoadingLayout(boolean showlayout, boolean showprogress, String textviewMsg){
        if(showlayout){
            loadinglayout.setVisibility(View.VISIBLE);
            if(showprogress){
                loadingprogressbar.setVisibility(View.VISIBLE);
            }else {
                loadingprogressbar.setVisibility(View.GONE);
            }
            loadingTextview.setText(textviewMsg);
        }else{
            loadinglayout.setVisibility(View.GONE);
        }

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
                    //scan nearest fire station
                    showLoadingLayout(true,true,"Finding nearest fire station");
                    //method here for scanning nearest fire station
                    findNearestBarangay();
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



    //SENDING
    protected void btnSendListener(){
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Log.wtf("btnClicked","continue is true");
                 //check if contact is not null
                if(coordinates==null){
                    //location not set
                    if(loadinglayout.getVisibility() == View.VISIBLE){
                        //still capturing the location
                        Toast.makeText(getActivity(), "Please Wait for the location", Toast.LENGTH_SHORT).show();
                    }else{
                        //location not yet tapped or selected
                        Toast.makeText(getActivity(), "Location not set", Toast.LENGTH_SHORT).show();
                        showLocationChoices();
                    }
                }
                else if(coordinates!=null && barangay_local_id!=-1 && number==null){
                    Toast.makeText(getActivity(), "Barangay has no number", Toast.LENGTH_SHORT).show();
                }
                else if(coordinates!=null && barangay_local_id!=-1 && number!=null){
                    //ready to send
                    new android.support.v7.app.AlertDialog.Builder(getActivity())
                            .setTitle("Confirmation")
                            .setMessage("Send your report now?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    msg +="\n{"+coordinates+"}";
                                    sendSMS(number, msg);
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
                else if (coordinates!=null && barangay_local_id==-1){
                    Toast.makeText(getActivity(), "No Barangay Pointed", Toast.LENGTH_SHORT).show();
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
                                Activity_main_user.somethingisnotyetdone=false;
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
                        Toast.makeText(getActivity(), "Delivered",
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


    //FINDING NEAREST FIRE STATION
    private void populateBarangayArrayListDetails(){
        b_local_id = new ArrayList<>();
        b_name = new ArrayList<>();
        b_cell = new ArrayList<>();
        b_coordinates = new ArrayList<>();

        Log.wtf("populateBarangayArrayList()","CALLED");
        dbHelper = new DBHelper(getActivity());
        Cursor c = dbHelper.getSqliteData("SELECT * FROM "+dbHelper.TABLE_BARANGAY);
        if(c!=null){
            if(c.getCount()>0){
                c.moveToFirst();
                do{
                    int barangay_loc_id = c.getInt(c.getColumnIndex(dbHelper.BARANGAY_LOC_ID));
                    String name = c.getString(c.getColumnIndex(dbHelper.BARANGAY_NAME));
                    String cell = c.getString(c.getColumnIndex(dbHelper.BARANGAY_CEL));
                    String coor = c.getString(c.getColumnIndex(dbHelper.BARANGAY_COORDINATES));
                    Log.wtf("Index: "+c.getString(c.getColumnIndex(dbHelper.BARANGAY_LOC_ID)),"Name: "+name+"\nCellphone: "+cell+"\nCoordinates: "+coor);

                    b_local_id.add(barangay_loc_id);
                    b_name.add(name);
                    b_cell.add(cell);

                    if(coor==null){
                        Log.wtf("populatebarangay()","coordinate is null");
                        b_coordinates.add(null);
                    }else{
                        if(coor.trim().equalsIgnoreCase("") || coor.trim().equalsIgnoreCase("null")){
                            Log.wtf("populatebarangay()","coordinate is NOT NULL, "+coor);
                            b_coordinates.add(null);
                        }
                        else{
                            try{
                                String latlng[] = coor.trim().split(",");
                                double lat = Double.parseDouble(latlng[0].trim());
                                double longti = Double.parseDouble(latlng[1].trim());
                                LatLng latLng = new LatLng(lat,longti);
                                Log.wtf("populateBarangayarrayList()","Latlng: "+latLng);
                                b_coordinates.add(latLng);
                            }catch (Exception e){
                                Log.wtf("populatebarangaylist (convertion of string to latlng)","Exception: "+e.getMessage());
                                b_coordinates.add(null);
                            }
                        }
                    }
                }while (c.moveToNext());
            }
        }
    }
    protected void findNearestBarangay(){
        BackgroundWorker backgroundWorker = new BackgroundWorker();
        backgroundWorker.execute(coordinates);
    }
    class BackgroundWorker extends AsyncTask<String,Void,Integer> {
        LatLng reporter_location;

        @Override
        protected Integer doInBackground(String... params) {
            try{
                Log.wtf("doinBackground","CALLED");
                String[] latlong =  params[0].split(",");
                Log.wtf("latlong","value: "+params[0]);

                double latitude = Double.parseDouble(latlong[0]);
                double longitude = Double.parseDouble(latlong[1]);
                reporter_location = new LatLng(latitude,longitude);
                int index = findNearestPoint();
                return index;
            }catch (Exception e){
                Log.wtf("doInbackground",e.getMessage());
                return -1;
            }
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.wtf("onPreExecute","called");
        }

        @Override
        protected void onPostExecute(final Integer index) {
            super.onPostExecute(index);
            if(index!=-1){
                Log.wtf("onPostExecute","worker is done, index: "+index);
                runOnUiThread(new Runnable() {
                    public void run() {
                        showLoadingLayout(false,false,null);
                        barangay_local_id = index;
                        number = b_cell.get(index);
                        if(number!=null){
                            if(number.trim().equalsIgnoreCase("null") || number.trim().equalsIgnoreCase("")){
                                txtNumber.setText("NO NUMBER");
                                number=null;
                            }else{
                                txtNumber.setText(b_cell.get(index));
                            }
                        }else{
                            txtNumber.setText("NO NUMBER");
                            number=null;
                        }
                        txtBarangayName.setText(b_name.get(index));
                    }
                });

                //hide the loading layout
                //show the barangay name in textview and its number

            }
        }

        private int findNearestPoint(){
            try {
                Log.wtf("FindNearestPoint()","CALLED");
                float nearest=0;
                float temp1=0;
                int index=0;
                LatLng reporter = reporter_location;

                //find the first index of not null
                for (int y = 0; y<b_coordinates.size();y++){
                    if(b_coordinates.get(y)!=null){
                        index = y;
                        break;
                    }
                }

                Log.wtf("findNearest()","Index nearest is "+index);
                nearest  =   getDistance(reporter, b_coordinates.get(index));

                for (int x=index;x<b_coordinates.size();x++){
                    if(b_coordinates.get(x)!=null){
                        temp1 = getDistance(reporter, b_coordinates.get(x));
                        Log.wtf("findnearestPointLoop["+x+"]","temp = "+temp1+"\nnearest = "+nearest);
                        if(temp1<=nearest){
                            //temp is lower
                            nearest = temp1;
                            index=x;
                        }
                    }
                }
                Log.wtf("(findNearestPoint)","Nearest point is index: "+index);
                return index;

            }catch (Exception e){
                Log.wtf("Exception (findNearestPoint)",e.getMessage());
                return -1;
            }
        }

        private float getDistance(LatLng position, LatLng assumed){
            float results[] = new float[1];
            Location.distanceBetween(
                    position.latitude,
                    position.longitude,
                    assumed.latitude,
                    assumed.longitude,
                    results);
            Log.wtf("showDistance","Result is "+results[0]+" meter");
            float value = results[0];
            return value;
        }


    }
}
