package com.example.rvnmrqz.firetrack;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Activity_Login extends AppCompatActivity {

    DBHelper dbhelper;
    EditText txtemail, txtpassword;
    Button btnSignin;
    TextView txtSignup;
    static TextView forgotPass;
    public static Activity fa;
    String email_address;
    ProgressDialog pd;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    StringRequest request;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__login);

         sharedPreferences = getSharedPreferences(MySharedPref.SHAREDPREF_NAME,MODE_PRIVATE);


        try{
            PackageManager packageManager = getApplicationContext().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getApplicationContext().getPackageName(),0);
            String version = packageInfo.versionName;
            TextView t1 = (TextView) findViewById(R.id.txt_appVersion);
            t1.setText("Version "+version);

            pd = new ProgressDialog(this);
            dbhelper = new DBHelper(this);
            Log.wtf("login","onCreate");
        }
        catch (Exception ee){
            ee.printStackTrace();
        }

        fa = this;
        txtemail = (EditText) findViewById(R.id.txtEmail);
        txtpassword = (EditText) findViewById(R.id.txtPassword);
        btnSignin = (Button) findViewById(R.id.btnSignin);

        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        txtpassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    login();
                }
                return false;
            }
        });
        txtSignup = (TextView) findViewById(R.id.txtSignup);
        txtSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkAvailable()==true){
                 //   startActivity(new Intent(Activity_Login.this,Activity_Registration.class));
                    //finish();
                    Toast.makeText(Activity_Login.this, "Create Registration Activity", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(Activity_Login.this, "Registration is not available offline", Toast.LENGTH_SHORT).show();
                }
            }
        });

        forgotPass = (TextView) findViewById(R.id.lblForgotPass);
        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = getLayoutInflater();
                View forgotPassDialog =  inflater.inflate(R.layout.dialog_forgot_password, null);
                final EditText email = (EditText) forgotPassDialog.findViewById(R.id.email_dialog);
                email.setText(email_address);
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(Activity_Login.this);
                builder.setTitle("Request Password Recovery");
                builder.setView(forgotPassDialog);
                builder.setPositiveButton("Request", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendPasswordRecovery(email_address);

                    }
                })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                builder.show();
            }
        });

        checkBarangayDB();
    }

    private void sendPasswordRecovery(final String email_address){
        String url = ServerInfoClass.HOST_ADDRESS+"/recover_password.php";
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(),response.trim(),Toast.LENGTH_SHORT).show();
                        Log.wtf("Response",response.trim());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Activity_Login.this, "Volley Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.wtf("errorResponse",error.getMessage());
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String > params = new HashMap<>();
                params.put("email",email_address);
                return params;

            }
        };
        requestQueue.add(request);
    }

    public static void enableForgotPass(){
        forgotPass.setVisibility(View.VISIBLE);
        forgotPass.setEnabled(true);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void btnLoginClick(View view){
        login();
    }

    private void login(){
        final String username,password;
        username = txtemail.getText().toString();
        password = txtpassword.getText().toString();
        email_address = username; //to pass the value for forgot password
        Log.wtf("login","login clicked");
        if(isNetworkAvailable()){
            if(validateInput()) {
                Log.wtf("login","worker");

                showLoadingDialog();

                String server_url = ServerInfoClass.HOST_ADDRESS+"/login.php";
                final RequestQueue requestQueue = Volley.newRequestQueue(this);
                 request = new StringRequest(Request.Method.POST, server_url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                   if(response!=null && !response.toString().trim().equals("wrong")){
                                    if(!response.toString().trim().contains("Warning")){
                                        try{
                                            long res;
                                            String acc_id,username,pass,acc_type,fname,mname="",lname,gender,birthday,barangay_id,contact_no,coordinates,picture,plateNo;
                                            JSONObject object = new JSONObject(response);
                                            JSONArray Jarray  = object.getJSONArray("mydata");
                                            JSONObject Jasonobject = Jarray.getJSONObject(0);

                                            int acc_type_id = Integer.parseInt(Jasonobject.getString("acc_type_id"));
                                            acc_id = Jasonobject.getString(dbhelper.COL_ACC_ID);
                                            username = Jasonobject.getString(dbhelper.COL_USERNAME);
                                            pass = Jasonobject.getString(dbhelper.COL_PASSWORD);
                                            acc_type = Jasonobject.getString(dbhelper.COL_ACC_TYPE_ID);

                                            switch (acc_type_id){
                                                case 1:
                                                    //super admin
                                                    break;
                                                case 2:
                                                    //admin
                                                    break;
                                                case 3:
                                                    //user
                                                    fname = Jasonobject.getString(dbhelper.COL_FNAME);

                                                    if(Jasonobject.getString(dbhelper.COL_MNAME)!=null){
                                                        if(Jasonobject.getString(dbhelper.COL_MNAME).trim().equals("null")){
                                                            mname="";
                                                        }
                                                    }else{
                                                        mname="";
                                                    }
                                                    lname = Jasonobject.getString(dbhelper.COL_LNAME);
                                                    gender = Jasonobject.getString(dbhelper.COL_GENDER);
                                                    birthday = Jasonobject.getString(dbhelper.COL_BIRTHDAY);
                                                    barangay_id = Jasonobject.getString(dbhelper.COL_BARANGAY_ID);
                                                    contact_no = Jasonobject.getString(dbhelper.COL_CONTACT_NO);
                                                    coordinates = Jasonobject.getString(dbhelper.COL_COORDINATES);
                                                    picture = Jasonobject.getString(dbhelper.COL_PICTURE);
                                                    res = dbhelper.insertLoggedUser(acc_id,username,pass,acc_type,fname,mname,lname,gender,birthday,barangay_id,contact_no,coordinates,picture);
                                                    if (res!=-1){
                                                        Log.wtf("login","insert successful");
                                                        setSharedPrefData(MySharedPref.LOGGED,"user");
                                                        startActivity(new Intent(getApplicationContext(),MainActivity_user.class));
                                                        finish();
                                                    }
                                                    break;
                                                case 4:
                                                    //truck
                                                    contact_no = Jasonobject.getString(dbhelper.COL_CONTACT_NO);
                                                    plateNo = Jasonobject.getString(dbhelper.COL_PLATE_NO);
                                                    res = dbhelper.insertLoggedTruck(acc_id, username, pass, acc_type, contact_no, plateNo);
                                                    if (res!=-1){
                                                        Log.wtf("login","insert successful");

                                                        setSharedPrefData(MySharedPref.LOGGED,"truck");
                                                        //open truck UI
                                                        // startActivity(new Intent(getApplicationContext(),MainActivity_user.class));
                                                        // finish();
                                                    }
                                                    break;
                                                default:
                                                    //not in the list
                                                    break;
                                            }
                                        }catch (Exception ee){
                                            Log.wtf("Response Error", ee.getMessage());
                                            Toast.makeText(Activity_Login.this, "Error encountered\n"+ee.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }//end of if does not contains Warning
                                    else{
                                        Toast.makeText(Activity_Login.this, "MySQL Connection Problem", Toast.LENGTH_SHORT).show();
                                    }
                                }else{
                                    //wrong pass
                                    enableForgotPass();
                                    Toast.makeText(Activity_Login.this, "Wrong username or password", Toast.LENGTH_SHORT).show();
                                }
                                closeLoadingDialog();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                closeLoadingDialog();
                                if(error instanceof TimeoutError || error instanceof NoConnectionError) {
                                    Toast.makeText(getApplicationContext(),"Check your internet connection",Toast.LENGTH_SHORT).show();
                                    // ...
                                }else{
                                    Toast.makeText(Activity_Login.this, "Error is :"+error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }){
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("username",username);
                                params.put("password",password);
                                return params;
                    }
                };
                int socketTimeout = 30000; // 30 seconds
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

                request.setRetryPolicy(policy);
                request.setShouldCache(false);
                requestQueue.add(request);
            }
        }
        else{
            Toast.makeText(this, "Not connected to internet", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void showLoadingDialog(){
        pd.setTitle("Logging");
        pd.setMessage("Please wait...");
        pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(request!=null){
                    request.cancel();
                    Toast.makeText(Activity_Login.this, "Request Canceled", Toast.LENGTH_SHORT).show();
                }
            }
        });
        pd.show();
    }
    private void closeLoadingDialog(){
        pd.hide();
        pd.dismiss();
    }

    protected boolean validateInput(){
        if(validateEmail()==true){
            if(validatePassword()==true){
                return true;
            }
        }
        return  false;
    }
    protected boolean validateEmail(){
        if(txtemail.getText().toString().trim().equalsIgnoreCase("")){
            txtemail.setError("Email is required");
            return false;
        }
        else{
            if(!txtemail.getText().toString().contains("@")){
                txtemail.setError("Invalid Email");
                return false;
            }
            else{
                if(txtemail.getText().length()<3){
                    txtemail.setError("Invalid Email");
                    return false;
                }
                txtemail.setError(null);
            }
        }
        return true;
    }
    protected  boolean validatePassword(){
        if(txtpassword.getText().toString().trim().equalsIgnoreCase("")){
            txtpassword.setError("Password is required");
            return false;
        }
        else{
            if(txtpassword.getText().toString().length()<8){
                txtpassword.setError("Invalid Password");
                return false;
            }
            else{
                txtpassword.setError(null);
            }
        }
        return  true;
    }

    protected void checkBarangayDB(){
        Cursor c = dbhelper.getSqliteData("SELECT * FROM "+dbhelper.TABLE_BARANGAY);
        c.moveToFirst();
        if(c!=null){
           if(c.getCount()==0){// populating barangay table
                new SyncBarangay(this,1);
           }
        }
    }


    //SHARED PREFERENCE
    protected void setSharedPrefData(String key, String value){
        try{
            editor = sharedPreferences.edit();
            editor.putString(key,value);
            editor.apply();
        }catch (Exception ee){
            Toast.makeText(Activity_Login.this, "Error in setSharedPrefData", Toast.LENGTH_SHORT).show();
            Log.wtf("setSharedPrefData: ERROR ",ee.getMessage());
        }
    }


}
