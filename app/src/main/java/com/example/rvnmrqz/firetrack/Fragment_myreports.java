package com.example.rvnmrqz.firetrack;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;


public class Fragment_myreports extends Fragment {

    DBHelper dbHelper;
    LinearLayout layout_progress,layout_error_message,layout_list;
    TextView txtprogressMsg, txterrorMsg;
    Button btnRefresh;
    int account_id;

    public Fragment_myreports() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_myreports, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //initialize here
        layout_error_message  = (LinearLayout) getActivity().findViewById(R.id.myreports_messageLayout);
        layout_progress = (LinearLayout) getActivity().findViewById(R.id.myreports_progressLayout);
        layout_list = (LinearLayout) getActivity().findViewById(R.id.myreports_listviewLayout);
        txterrorMsg = (TextView) getActivity().findViewById(R.id.myreports_messageLayout_txtview);
        txtprogressMsg = (TextView) getActivity().findViewById(R.id.myreports_progressLayout_txtView);
        btnRefresh = (Button) getActivity().findViewById(R.id.myreports_messageLayout_button);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadReports();
            }
        });
        dbHelper = new DBHelper(getActivity());

        if(isNetworkAvailable()){
            //load listview
            loadReports();
        }else{
            //show snackbar
            showSnackbar("You're offline");
        }
    }

    protected void loadReports(){
        showProgressLayout("Loading, Please wait...");
        String url =  ServerInfoClass.HOST_ADDRESS+"/get_data.php";
        RequestQueue requestQue = Volley.newRequestQueue(getActivity());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.wtf("loadRerports","response has bee received \nResponse:"+response);
                        Toast.makeText(getActivity(), "Response has been received", Toast.LENGTH_SHORT).show();
                        showListview();
                        try {
                            JSONObject object = new JSONObject(response);
                            JSONArray Jarray  = object.getJSONArray("mydata");
                            if(Jarray.length()>0){
                                //extract the JSON
                                Log.wtf("loadReports (onResponse)","EXTRACT JSON");
                            }else{
                                Log.wtf("loadReports (onResponse)", "NO REPORTS YET");
                                showErrorMessage("No reports yet",true,"Refresh");
                            }
                        }catch (Exception e){
                            showErrorMessage("An error occured while refreshing data",true,"Retry");
                            Log.wtf("loadReports (onResponse Exception)","Exception Encountered in onResponse: "+e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.wtf("loadReports (onErrorResponse)",volleyError.getMessage());
                        String message = null;
                        Log.wtf("LoadFeed: onErrorResponse","Volley Error \n"+volleyError.getMessage());
                        if (volleyError instanceof NetworkError) {
                            message = "No internet connection";
                            Log.wtf("loadFeed (Volley Error)","NetworkError");
                            showSnackbar("You're not connected to internet");
                        } else if (volleyError instanceof ServerError) {
                            message = "Please check your internet connection";
                            Log.wtf("loadFeed (Volley Error)","ServerError");
                        } else if (volleyError instanceof AuthFailureError) {
                            message = "Please check your internet connection";
                            Log.wtf("loadFeed (Volley Error)","AuthFailureError");
                        } else if (volleyError instanceof ParseError) {
                            message = "An error encountered, Please try again";
                            Log.wtf("loadFeed (Volley Error)","ParseError");
                        } else if (volleyError instanceof NoConnectionError) {
                            message = "No internet connection";
                            Log.wtf("loadFeed (Volley Error)","NoConnectionError");
                        } else if (volleyError instanceof TimeoutError) {
                            message = "Connection TimeOut!\nPlease check your internet connection.";
                            Log.wtf("loadFeed (Volley Error)","TimeoutError");
                        }
                        showErrorMessage(message,true,"Refresh");
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();

                String query = "SELECT * FROM "+dbHelper.TABLE_REPORTS+" WHERE "+dbHelper.COL_REPORTER_id+" = "+account_id+";";
                params.put("qry",query);

                return params;
            }
        };
        int socketTimeout = ServerInfoClass.TIME_OUT; // 30 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        stringRequest.setShouldCache(false);
        requestQue.add(stringRequest);

    }

    //Layout transitions
    public void showProgressLayout(String loadingmsg){
        layout_progress.setVisibility(View.VISIBLE);
        layout_error_message.setVisibility(View.GONE);
        layout_list.setVisibility(View.GONE);
        txtprogressMsg.setText(loadingmsg);
    }
    public void showErrorMessage(String errorMsg,boolean showButton, String buttonText){
        layout_progress.setVisibility(View.GONE);
        layout_list.setVisibility(View.GONE);
        layout_error_message.setVisibility(View.VISIBLE);
        txterrorMsg.setText(errorMsg);
        if(showButton){
            btnRefresh.setVisibility(View.VISIBLE);
            btnRefresh.setText(buttonText);
        }else{
            btnRefresh.setVisibility(View.GONE);
        }
    }
    public void showListview(){
        layout_progress.setVisibility(View.GONE);
        layout_error_message.setVisibility(View.GONE);
        layout_list.setVisibility(View.VISIBLE);
    }


    protected void showSnackbar(String snackbarMsg){
        Snackbar.make(getActivity().findViewById(android.R.id.content), snackbarMsg, Snackbar.LENGTH_LONG)
                .setAction("Go online", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light ))
                .show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }




}
