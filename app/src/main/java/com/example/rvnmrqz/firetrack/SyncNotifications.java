package com.example.rvnmrqz.firetrack;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by arvin on 6/26/2017.
 */

public class SyncNotifications
{

    //this is only for one time use for downloading the notifications into the sqlite database

    public RequestQueue requestQueue;
    final DBHelper dbHelper;
    Context context;
    String server_url=ServerInfoClass.HOST_ADDRESS+"/get_data.php";
    int finalMode;
    String user_id=null;
    String user_barangay_id=null;

    public SyncNotifications(Context c, int MODE) {
        Log.wtf("SyncNotifications","Constructor is called");
        this.context = c;
        dbHelper = new DBHelper(context);
        Cursor result = dbHelper.getSqliteData("SELECT * FROM "+dbHelper.TABLE_USER+" WHERE "+dbHelper.COL_USER_LOC_ID+" = 1;");
        if(result!=null){
            result.moveToFirst();
            if(result.getCount()>0){
                Log.wtf("SyncNotifications","result count is greater than 0");
                user_id = result.getString(result.getColumnIndex(dbHelper.COL_ACC_ID));
                user_barangay_id = result.getString(result.getColumnIndex(dbHelper.COL_BARANGAY_ID));
                if(user_id !=null && user_barangay_id!=null){
                    Log.wtf("SyncNotifications","both values is not null");
                    finalMode = MODE;
                    sync(MODE);
                }else{
                    Toast.makeText(c, "User Values returns null", Toast.LENGTH_SHORT).show();
                    Log.wtf("SyncNotifications","Cursor returns an null value \nuser_id: "+user_id+"\nuserbarangay_id: "+user_barangay_id);
                }
            }else{
                Log.wtf("SyncNotifications","result count is less than 0");
            }
        }else{
            Log.wtf("SyncNotifications", "result is null");
            Toast.makeText(c, "Cursor return Null", Toast.LENGTH_SHORT).show();
        }

    }

    public void sync(final int mode){
        Log.wtf("Sync","Inside the Notification Sync Method");
        requestQueue = Volley.newRequestQueue(context);
        StringRequest request = new StringRequest(Request.Method.POST, server_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //get the response
                        //pass to containers
                        //insert the response in sqlite
                        if(mode == 1){
                            //insert only
                            insert(response);

                        }else if(mode == 2){
                            //remove and insert
                            dbHelper.removeTableData(dbHelper.TABLE_NOTIFICATION);
                            insert(response);
                        }
                        SharedPreferences sharedPreferences = context.getSharedPreferences(MySharedPref.SHAREDPREF_NAME,Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(MySharedPref.NOTIF,"no");
                        editor.commit();
                        Log.wtf("SyncNotifications: onResponse_sync","Response="+response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.wtf("SyncNotification","Error Message: "+error.getMessage());
                        if(error == null){
                            Log.wtf("SyncNotification:onErrorResponse","Response is null, trying again");
                            sync(finalMode);
                        }
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                String qry = "SELECT * FROM "+dbHelper.TABLE_NOTIFICATION+" WHERE " +
                        dbHelper.COL_NOTIF_USER_RECEIVER+" = "+user_id+" " +
                        "OR "+
                        dbHelper.COL_NOTIF_BARANGAY_RECEIVER +" = "+user_barangay_id+";";
                params.put("qry",qry);
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

    private void insert(String response){
        try{
            Log.wtf("SyncNotifications,sync", "insert: "+response);
            String id,sender,title,msg,datetime,personal,opened;
            JSONObject object = new JSONObject(response);
            JSONArray Jarray  = object.getJSONArray("mydata");

            for (int i = 0; i < Jarray.length(); i++)
            {
                JSONObject Jasonobject = Jarray.getJSONObject(i);

                 id = Jasonobject.getString(dbHelper.COL_NOTIF_ID);
                 sender = Jasonobject.getString(dbHelper.COL_NOTIF_SENDER);
                 title = Jasonobject.getString(dbHelper.COL_NOTIF_TITLE);
                 msg = Jasonobject.getString(dbHelper.COL_NOTIF_MESSAGE);
                 datetime = Jasonobject.getString(dbHelper.COL_NOTIF_DATETIME);
                 String receiver_user = Jasonobject.getString(dbHelper.COL_NOTIF_USER_RECEIVER);
                 personal="false";
                 if(receiver_user!=null) {
                    if (!receiver_user.trim().equals("") && !receiver_user.trim().equals("null")) {
                        //this is a personal message
                        personal = "true";
                    }
                 }

                //String personal = Jasonobject.getString(dbHelper.COL_NOTIF_PERSONAL);
                opened = "yes";
                dbHelper.insertNotification(id,sender,title,msg,datetime,personal,opened);

            }


            Log.wtf("onResponse","Notif is inserted");

        }catch (Exception ee){
            Toast.makeText(context,ee.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

}
