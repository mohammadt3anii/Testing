package com.example.rvnmrqz.firetrack;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
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

public class SyncBarangay
{

    public RequestQueue requestQueue;
    DBHelper dbHelper;
    static Activity static_parent;
    String server_url=ServerInfoClass.HOST_ADDRESS+"/get_data.php";
    int finalMode;
    boolean showMessage;

    public SyncBarangay(Activity c,int MODE,boolean showMessage) {
        Log.wtf("SyncBarangay","Constructor is called");

        static_parent = c;
        dbHelper = new DBHelper(static_parent);
        finalMode = MODE;
        this.showMessage = showMessage;
        sync(MODE);

    }

    public void sync(final int mode){
        Log.wtf("Sync","Inside the Sync Method");
        requestQueue = Volley.newRequestQueue(static_parent);
        StringRequest request = new StringRequest(Request.Method.POST, server_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //get the response
                        //pass to containers
                        //delete sqlite barangay table rows
                        //insert the response in sqlite
                        if(mode == 1){
                            //insert only
                            insert(response);

                        }else if(mode == 2){
                            //remove and insert
                            dbHelper.removeTableData(dbHelper.TABLE_BARANGAY);
                            insert(response);
                        }
                        Log.wtf("Barangay_sync:onResponse_sync","Response="+response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.wtf("onErrorResponse (Sync Barangay)","Error Message: "+error.getMessage());
                       if (showMessage){
                           showMessage("Failed to capture Barangay details");
                       }
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                String qry = "SELECT * from tbl_barangay;";
                params.put("qry",qry);

                if(showMessage){
                    showMessage("Capturing Details");
                }
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
            String b_id,b_name,b_cell,b_tel,b_coordinates;
            JSONObject object = new JSONObject(response);
            JSONArray Jarray  = object.getJSONArray("mydata");

            for (int i = 0; i < Jarray.length(); i++)
            {
                JSONObject Jasonobject = Jarray.getJSONObject(i);
                b_id = Jasonobject.getString(dbHelper.BARANGAY_ID);
                b_name = Jasonobject.getString(dbHelper.BARANGAY_NAME);
                b_cell = Jasonobject.getString(dbHelper.BARANGAY_CEL);
                b_tel = Jasonobject.getString(dbHelper.BARANGAY_TEL);
                b_coordinates = Jasonobject.getString(dbHelper.BARANGAY_COORDINATES);
                dbHelper.insertBarangay(b_id,b_name,b_cell,b_tel,b_coordinates);
            }
            if (showMessage){
                showMessage("Barangay details saved");
                if(Fragment_sms_reporting.context!=null){
                  //  Fragment_sms_reporting.populateAutoCompleteBarangay();
                }
            }
            Log.wtf("onResponse","Barangay is inserted");

        }catch (Exception ee){
            Toast.makeText(static_parent,ee.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }


    private void showMessage(final String message){
        static_parent.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(static_parent.getBaseContext(),message, Toast.LENGTH_LONG).show();
            }
        });
    }

}
