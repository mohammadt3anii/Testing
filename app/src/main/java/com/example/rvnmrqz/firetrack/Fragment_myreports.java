package com.example.rvnmrqz.firetrack;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Fragment_myreports extends Fragment {

    DBHelper dbHelper;
    LinearLayout layout_progress,layout_error_message,layout_list;
    TextView txtprogressMsg, txterrorMsg;
    SwipeRefreshLayout listviewRefreshLayout;
    ListView reportListview;
    Button btnRefresh;
    int account_id;
    ArrayList<String> id;
    ArrayList<String> datetime;
    ArrayList<String>  status;
    ArrayList<String>  coordinates;

    public Fragment_myreports() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_myreports, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //initialize here

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("My Reports");
        layout_error_message  = (LinearLayout) getActivity().findViewById(R.id.myreports_messageLayout);
        layout_progress = (LinearLayout) getActivity().findViewById(R.id.myreports_progressLayout);
        layout_list = (LinearLayout) getActivity().findViewById(R.id.myreports_listviewLayout);
        txterrorMsg = (TextView) getActivity().findViewById(R.id.myreports_messageLayout_txtview);
        txtprogressMsg = (TextView) getActivity().findViewById(R.id.myreports_progressLayout_txtView);
        reportListview = (ListView) getActivity().findViewById(R.id.myreports_listview);
        btnRefresh = (Button) getActivity().findViewById(R.id.myreports_messageLayout_button);
        listviewRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.myreports_list_swipeRefreshlayout);
        listviewRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadReports();
            }
        });
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadReports();
            }
        });
        dbHelper = new DBHelper(getActivity());

        id = new ArrayList<>();
        datetime = new ArrayList<>();
        status = new ArrayList<>();
        coordinates = new ArrayList<>();

        //get the user logged
        Cursor c = dbHelper.getSqliteData("SELECT "+dbHelper.COL_ACC_ID+" FROM "+dbHelper.TABLE_USER+" WHERE "+dbHelper.COL_USER_LOC_ID+"=1;");
        if(c!=null){
            if(c.getCount()>0){
                c.moveToFirst();
                account_id = c.getInt(c.getColumnIndex(dbHelper.COL_ACC_ID));
            }
        }

        if(isNetworkAvailable()){
            //load listview
            loadReports();
        }else{
            //show snackbar
            showErrorMessage("No Internet Connection",true,"Retry");
            showSnackbar("You're offline");
        }

    }

    protected void loadReports(){
        id.clear();
        datetime.clear();
        status.clear();
        coordinates.clear();
        listviewRefreshLayout.setRefreshing(false);
        showProgressLayout("Loading, Please wait...");
        String url =  ServerInfoClass.HOST_ADDRESS+"/get_data.php";
        RequestQueue requestQue = Volley.newRequestQueue(getActivity());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.wtf("loadRerports","response has bee received \nResponse:"+response);
                        showListview();
                        try {
                            JSONObject object = new JSONObject(response);
                            JSONArray Jarray  = object.getJSONArray("mydata");
                            if(Jarray.length()>0){
                                //extract the JSON
                                for(int x=0;x<Jarray.length();x++){
                                    JSONObject Jasonobject = Jarray.getJSONObject(x);
                                    id.add(Jasonobject.getString(dbHelper.COL_REPORT_ID));
                                    datetime.add(Jasonobject.getString(dbHelper.COL_REPORT_DATETIME));
                                    status.add(Jasonobject.getString(dbHelper.COL_REPORT_STATUS));
                                    coordinates.add(Jasonobject.getString(dbHelper.COL_REPORT_COORDINATES));
                                }
                               //pass to adapater
                                ReportAdapter reportAdapter = new ReportAdapter(getContext(),id,datetime,status,coordinates);
                                reportListview.setAdapter(reportAdapter);

                            }else{
                                Log.wtf("loadReports (onResponse)", "NO REPORTS YET");
                                showErrorMessage("No Reports Yet",true,"Refresh");
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
                            message = "Network Error Encountered";
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
                Log.wtf("loadReports(), params","Query: "+query );
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


    class ReportAdapter extends ArrayAdapter {
        ArrayList<String> report_id= new ArrayList<>();
        ArrayList<String> datetime = new ArrayList<>();
        ArrayList<String> status = new ArrayList<>();
        ArrayList<String> coordinates = new ArrayList<>();


        public ReportAdapter(Context context, ArrayList<String> report_id, ArrayList<String> datetime,   ArrayList<String> status,  ArrayList<String> coordinates) {
            //Overriding Default Constructor off ArratAdapter
            super(context, R.layout.template_post,R.id.post_id,report_id);
            this.report_id = report_id;
            this.datetime=datetime;
            this.status=status;
            this.coordinates=coordinates;

        }
        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //Inflating the layout
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.template_reports,parent,false);

            //Get the reference to the view objects
            TextView txtid  = (TextView) row.findViewById(R.id.report_id);
            final TextView txtdatetime = (TextView) row.findViewById(R.id.report_datetime);
            final TextView txtstatus = (TextView) row.findViewById(R.id.report_status);
            final TextView txtcoor = (TextView) row.findViewById(R.id.report_coordinates);
            Button btnShowMap = (Button) row.findViewById(R.id.report_showmap);
            btnShowMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    MapFragment mapfrag = new MapFragment();
                    Bundle args = new Bundle();
                    args.putString("coordinates",txtcoor.getText().toString());
                    args.putString("title",txtdatetime.getText().toString());
                    args.putString("snippet",txtstatus.getText().toString());
                    mapfrag.setArguments(args);

                    MainActivity_user.addToBackStack(mapfrag,"map");
                }
            });

            //Providing the element of an array by specifying its position
            txtid.setText(report_id.get(position));
            txtdatetime.setText(datetime.get(position));
            txtstatus.setText(status.get(position));
            txtcoor.setText(coordinates.get(position));
            return row;
        }
    }
}
