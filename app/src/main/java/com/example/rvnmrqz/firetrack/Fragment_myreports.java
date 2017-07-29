package com.example.rvnmrqz.firetrack;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        layout_error_message  = (LinearLayout) getActivity().findViewById(R.id.myreports_messageLayout);
        layout_progress = (LinearLayout) getActivity().findViewById(R.id.myreports_progressLayout);
        layout_list = (LinearLayout) getActivity().findViewById(R.id.myreports_listviewLayout);
        txterrorMsg = (TextView) getActivity().findViewById(R.id.myreports_messageLayout_txtview);
        txtprogressMsg = (TextView) getActivity().findViewById(R.id.myreports_progressLayout_txtView);
        btnRefresh = (Button) getActivity().findViewById(R.id.myreports_messageLayout_button);
        dbHelper = new DBHelper(getActivity());

        if(isNetworkAvailable()){
            //load listview

        }else{
            //show snackbar
            showSnackbar("You're offline");
        }
    }

    protected void loadReports(){
        String url =  ServerInfoClass.HOST_ADDRESS+"/get_data.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                //di pa tapos query neto
                String query = "SELECT * FROM "+dbHelper.TABLE_REPORTS+" WHERE "+dbHelper.COL_REPORTER_id+" = "+account_id+";";
                params.put("qry",query);

                return super.getParams();
            }
        };

    }

    //Layout transitions
    public void showProgressLayout(String loadingmsg){
        layout_progress.setVisibility(View.VISIBLE);
        layout_error_message.setVisibility(View.GONE);
        layout_list.setVisibility(View.GONE);
    }
    public void showErrorMessage(String errorMsg,boolean showButton, String buttonText){
        layout_progress.setVisibility(View.GONE);
        layout_list.setVisibility(View.GONE);
        layout_error_message.setVisibility(View.VISIBLE);
    }
    public void showListview(){

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
