package com.example.rvnmrqz.firetrack;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.LOCATION_SERVICE;


/**
 * Created by Rvn Mrqz on 2/19/2017.
 */

public class Fragment_online_reporting extends Fragment {
    View myview;
    ImageView imgProof;

    int     CAMERA_PERMISSION =1,
            LOCATION_PERMISSION=2,
            TAKE_PICTURE_REQUEST=20,
            OPEN_GPS_SETTINGS_REQUEST=30,
            OPEN_PERMISSION_REQUEST=40;

    String encodedImage;
    private Uri imageUri;
    public static Uri finalImageUri=null;
    DBHelper dbhelper;
    ProgressDialog pd;
    TextView txtLocation,txtDigits,txtAdditionalInfo;
    Button btnSubmit;
    private LocationManager locationManager;
    private LocationListener locationListener;
    String coordinates =null;
    String user_account_id;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myview = inflater.inflate(R.layout.fragment_online_reporting, container, false);
        return myview;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Online Reporting");
        imgProof = (ImageView) getActivity().findViewById(R.id.img_proof);
        imgClickListener();
        txtLocation = (TextView) getActivity().findViewById(R.id.txtLocation);
        txtLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocationChoices();
            }
        });
        showLocationChoices();
        btnSubmit = (Button) getActivity().findViewById(R.id.btnSubmit);
        btnSubmitListener();
        txtAdditionalInfo = (EditText) getActivity().findViewById(R.id.txtAdditionalInfo);
        txtDigits = (TextView) getActivity().findViewById(R.id.txtDigits);
        txtAdditionalInfo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = txtAdditionalInfo.getText().length();
                txtDigits.setText(length + "/160");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        pd = new ProgressDialog(getActivity());

        dbhelper = new DBHelper(getActivity());
        Cursor c = dbhelper.getSqliteData("SELECT " + dbhelper.COL_ACC_ID + " FROM " + dbhelper.TABLE_USER + " WHERE " + dbhelper.COL_USER_LOC_ID + "=1");
        if (c != null) {
            c.moveToFirst();
            user_account_id = c.getString(c.getColumnIndex(dbhelper.COL_ACC_ID));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try{
            boolean allTrue=true;
          //  android.os.Process.killProcess(android.os.Process.myPid());
            if(requestCode == CAMERA_PERMISSION){
                for (int x=0;x<grantResults.length;x++){
                    if(grantResults[x] == PackageManager.PERMISSION_DENIED){
                        allTrue=false;
                        break;
                    }
                }
                if(allTrue){
                    Log.wtf("permissionResult","Camera permissions are granted");
                    Toast.makeText(getActivity(), "GRANTED", Toast.LENGTH_SHORT).show();
                    openCamera();
                }else{
                    Log.wtf("permissionResult","Camera permissions are not all granted");
                    Toast.makeText(getActivity(), "NOT GRANTED", Toast.LENGTH_SHORT).show();
                }
            }//end of permission check for camera
            else if(requestCode == LOCATION_PERMISSION){
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.wtf("permissionResult","Fine Location is granted");
                    getCurrentLocation();
                }else{
                    Log.wtf("permissionResult","Fine Location is NOT granted");
                    //NOT GRANTED
                    boolean showRationale = shouldShowRequestPermissionRationale(permissions[0]);
                    if(!showRationale){
                        //USER SELECTED DO NOT SHOW AGAIN
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Allow Permission");
                        builder.setMessage("You cannot use this function without permission. Please allow the permission.");
                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivityForResult(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)),OPEN_PERMISSION_REQUEST);
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
                    else{

                    }
                }
            }
        }catch (Exception e){
            Toast.makeText(getActivity(), "Exception Handled ", Toast.LENGTH_SHORT).show();
            Log.wtf("onPermissionResult:ERROR",e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==TAKE_PICTURE_REQUEST){
            if(resultCode == Activity.RESULT_OK){
                Uri selectedImage = imageUri;
                getActivity().getContentResolver().notifyChange(selectedImage,null);
                ContentResolver cr = getActivity().getContentResolver();
                Bitmap bitmap;
                try{
                    bitmap = MediaStore.Images.Media.getBitmap(cr,selectedImage);
                    Bitmap photo =  bitmap ;
                    photo = Bitmap.createScaledBitmap(photo, 240, 320, false);
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

                    File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"temp.jpg");
                    f.createNewFile();
                    FileOutputStream fo = new FileOutputStream(f);
                    fo.write(bytes.toByteArray());
                    fo.close();

                    // Toast.makeText(ra,f.toString(), Toast.LENGTH_SHORT).show();

                    imgProof.setImageBitmap(photo);
                    finalImageUri=Uri.fromFile(new File(f.toString())); //passes the lower reso file
                    //finalImageUri = selectedImage; //this is the original file, higher pixel

                    encodedImage = encodeToBase64(finalImageUri);
                }
                catch (Exception e){
                    Toast.makeText(getActivity(), "Error encountered", Toast.LENGTH_SHORT).show();
                    Log.wtf("Exception in result",e.getMessage());
                }
            }
        }
        else if(requestCode ==  OPEN_GPS_SETTINGS_REQUEST){
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

                        dbhelper  = new DBHelper(getActivity());

                        Cursor c = dbhelper.getSqliteData("SELECT "+dbhelper.COL_COORDINATES +" FROM "+dbhelper.TABLE_USER+" WHERE "+dbhelper.COL_USER_LOC_ID+" = 1;");
                        if(c!=null){
                            c.moveToFirst();
                            coordinates = c.getString(c.getColumnIndex(dbhelper.COL_COORDINATES));
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
        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.wtf("onDismiss","Dialog is dismissed");

            }
        });
        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.wtf("onCancel","dialog is canceled");

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

    //CAMERA
    protected void imgClickListener(){
        imgProof.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int check_cam  = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
                int check_read  = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
                int check_write  = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if(check_cam == PackageManager.PERMISSION_GRANTED && check_read == PackageManager.PERMISSION_GRANTED && check_write == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getActivity(), "Granted", Toast.LENGTH_SHORT).show();
                    openCamera();
                }else{
                    Toast.makeText(getActivity(), "Not Granted", Toast.LENGTH_SHORT).show();
                    requestPermissions(new String[] {Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION);
                }
            }
        });
    }
    protected void openCamera(){
        Log.wtf("openCamera","opencamera function is called");
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"temp.jpg");
        imageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent,TAKE_PICTURE_REQUEST);
    }
    private String encodeToBase64(Uri uri){

        try{
            File imageFile = new File(getRealPathFromURI(uri));
            //    Toast.makeText(ra, "Before: "+uri.toString()+"\nafter: "+imageFile.toString(), Toast.LENGTH_SHORT).show();
            //Bitmap bm = ((BitmapDrawable) iv.getDrawable()).getBitmap();
            Bitmap bm = BitmapFactory.decodeFile(imageFile.toString());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
            byte[] byteArrayImage = baos.toByteArray();

            String encodedImg = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
            return encodedImg;

        }
        catch (Exception ee){
            Toast.makeText(getActivity(), "Failed to encode to base64\n"+ee.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return null;
    }
    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getActivity().getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        // Toast.makeText(ra, result+"", Toast.LENGTH_SHORT).show();
        return result;
    }

    //SENDING
    protected void btnSubmitListener(){
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.wtf("btnSubmitListener","Clicked");
                if (coordinates != null) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Confirmation")
                            .setMessage("Send your report now?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    sendReport();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                    .show();
                }//END OF COORDINATES IS NOT NULL
                else {
                    txtLocation.requestFocus();
                    txtLocation.setError("No Location Given");
                }
            }
        });
    }

    private void sendReport() {
        showLoadingDialog();
        String server_url = ServerInfoClass.HOST_ADDRESS + "/do_query.php";
        RequestQueue requestQue = Volley.newRequestQueue(getActivity());
        StringRequest stringReq = new StringRequest(Request.Method.POST, server_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.wtf("Report Response", response);
                        closeLoadingDialog();
                        Log.wtf("sendReport()","Response: "+response);
                        if (response.trim().equals("Process Successful")) {
                            //close reporting
                            Toast.makeText(getActivity(), "Report Sent", Toast.LENGTH_SHORT).show();
                            Fragment_online_reporting.super.getActivity().onBackPressed();
                        } else {
                            Toast.makeText(getActivity(), "Report Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.wtf("sendReport()","Error "+error.getMessage());
                        if (error == null) {
                            Toast.makeText(getActivity(), "No Response from server", Toast.LENGTH_SHORT).show();
                        }
                        closeLoadingDialog();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String query = "INSERT INTO tbl_reports(reporter_id,report_status,report_datetime,additional_info,coordinates,picture)" +
                        "VALUES(" + user_account_id +", 'PENDING', NOW(), '" + txtAdditionalInfo.getText().toString().trim() + "','" + coordinates + "','" + encodedImage + "');";
                Map<String, String> params = new HashMap<String, String>();
                params.put("query", query);
                return params;
            }
        };
        int socketTimeout = ServerInfoClass.TIME_OUT; // 30 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringReq.setRetryPolicy(policy);
        stringReq.setShouldCache(false);
        requestQue.add(stringReq);
    }

    private int getNearestBarangay(){
        int Barangay_ID=0;
        //get the nearest barangay

        return Barangay_ID;
    }

    private void showLoadingDialog() {
           pd.setTitle("Submitting");
           pd.setMessage("Please wait...");
           pd.show();

    }

    private void closeLoadingDialog() {
                pd.hide();
                pd.dismiss();
            }



}


