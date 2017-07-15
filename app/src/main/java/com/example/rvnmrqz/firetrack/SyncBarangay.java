package com.example.rvnmrqz.firetrack;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
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
    Context context;
    String server_url=ServerInfoClass.HOST_ADDRESS+"/get_data.php";
    int finalMode;

    public SyncBarangay(Context c,int MODE) {
        Log.wtf("SyncBarangay","Constructor is called");
        this.context = c;
        dbHelper = new DBHelper(context);
        finalMode = MODE;
        sync(MODE);



    }

    public void sync(final int mode){
        Log.wtf("Sync","Inside the Sync Method");
        requestQueue = Volley.newRequestQueue(context);
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
                        Log.wtf("onErrorResponse_sync","Error Message: "+error.getMessage());
                        if(error == null){
                            Log.wtf("onErrorResponse","Response is null, trying again");
                            sync(finalMode);
                        }
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                String qry = "SELECT * from tbl_barangay;";
                params.put("qry",qry);
                return params;
            }
        };
        requestQueue.add(request);
    }


    private void insert(String response){
        try{
            String b_id,b_name,b_cell,b_tel;
            JSONObject object = new JSONObject(response);
            JSONArray Jarray  = object.getJSONArray("mydata");

            for (int i = 0; i < Jarray.length(); i++)
            {
                JSONObject Jasonobject = Jarray.getJSONObject(i);
                b_id = Jasonobject.getString(dbHelper.BARANGAY_ID);
                b_name = Jasonobject.getString(dbHelper.BARANGAY_NAME);
                b_cell = Jasonobject.getString(dbHelper.BARANGAY_CEL);
                b_tel = Jasonobject.getString(dbHelper.BARANGAY_TEL);

                dbHelper.insertBarangay(b_id,b_name,b_cell,b_tel);
            }

            Log.wtf("onResponse","Barangay is inserted");

        }catch (Exception ee){
            Toast.makeText(context,ee.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

}
