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
                            dbHelper.removeTableData(dbHelper.TABLE_UPDATES);
                            insert(response);
                        }
                        SharedPreferences sharedPreferences = context.getSharedPreferences(MySharedPref.SHAREDPREF_NAME,Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(MySharedPref.NOTIF,"no");
                        editor.commit();
                        Log.wtf("SyncNotifications: onResponse","Response="+response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.wtf("SyncNotification: onErrorResponse","Error Message: "+error.getMessage());
                        if(error == null){
                            Log.wtf("SyncNotification:onErrorResponse","Response is null, trying again");
                            sync(finalMode);
                        }
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                String qry = "SELECT * FROM "+dbHelper.TABLE_UPDATES+" WHERE "+dbHelper.COL_NOTIF_RECEIVER +" IN('ALL'|'u-"+user_id+"'|'b-"+user_barangay_id+"') ORDER BY "+dbHelper.COL_UPDATE_ID+" limit 50;";
                params.put("qry",qry);
                return params;
            }
        };
        int socketTimeout = ServerInfoClass.TIME_OUT;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void insert(String response){
        try{
            String update_id,category,title,content,sender_id,datetime,opened="yes";
            JSONObject object = new JSONObject(response);
            JSONArray Jarray  = object.getJSONArray("mydata");
            for (int i = 0; i < Jarray.length(); i++)
            {
                JSONObject Jasonobject = Jarray.getJSONObject(i);
                update_id = Jasonobject.getString(dbHelper.COL_UPDATE_ID);
                category = Jasonobject.getString(dbHelper.COL_CATEGORY);
                title = Jasonobject.getString(dbHelper.COL_TITLE);
                content = Jasonobject.getString(dbHelper.COL_CONTENT);
                sender_id = Jasonobject.getString(dbHelper.COL_SENDER_ID);
                datetime = Jasonobject.getString(dbHelper.COL_DATETIME);
                //insert in sqlite
                dbHelper.insertUpdate(update_id,category,title,content,sender_id,datetime,opened);
            }
            if(Jarray.length()>0){
                //there is a value retrieved
                Log.wtf("insert","Load main notifications UI");
                if(MainActivity_user.static_main_user !=null){
                    MainActivity_user.loadNotifications();
                }
            }
            Log.wtf("onResponse","Notif is inserted");
        }catch (Exception ee){
            Log.wtf("SyncNotifications_insert()","Exception: "+ee.getMessage());
            Toast.makeText(context, "A problem occurred while refreshing", Toast.LENGTH_SHORT).show();
        }
    }

}
