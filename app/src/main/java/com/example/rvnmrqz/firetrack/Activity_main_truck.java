package com.example.rvnmrqz.firetrack;


import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Activity_main_truck extends AppCompatActivity {

    String account_id=null;
    DBHelper dbHelper;
    public static AHBottomNavigation bottomNavigation;
    AHBottomNavigationItem item1,item2,item3;

    //tab 1
    FrameLayout tab1;
    ImageButton btnFullscreen;
    boolean fullscreen=false;
    FrameLayout frameContainer;


    //tab 2
    RelativeLayout tab2;
    LinearLayout tab2_listLayout, tab2_loadingLayout, tab2_errormsgLayout;
    ListView tab2_listview;
    TextView tab2_loadingTxt, tab2_errorTxt;
    Button tab2_errorButton;
    ProgressBar tab2_loadingProgressbar;

    ArrayList<String> report_firenotif_ids_list= new ArrayList<String>();
    ArrayList<String> report_images_list= new ArrayList<String>();
    ArrayList<String> report_coordinates_list = new ArrayList<String>();
    ArrayList<String> report_datetime_list = new ArrayList<String>();
    ArrayList<String> report_firestatus_list = new ArrayList<String>();
    ArrayList<String> report_alarmlevel_list = new ArrayList<String>();
    ArrayList<String> report_additionalInfo_list= new ArrayList<String>();

    //tab 3
    RelativeLayout tab3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitiy_main_truck);

        dbHelper = new DBHelper(this);
        //get the current user's account_id
        Cursor c = dbHelper.getSqliteData("SELECT "+dbHelper.COL_ACC_ID+" FROM "+dbHelper.TABLE_USER +" WHERE "+dbHelper.COL_USER_LOC_ID+"=1");
        if(c!=null){
            if(c.getCount()>0){
                c.moveToFirst();
                account_id = c.getString(c.getColumnIndex(dbHelper.COL_ACC_ID));
            }
        }

        frameContainer = (FrameLayout) findViewById(R.id.truck_containter);
        tab1 = (FrameLayout) findViewById(R.id.truck_tab1);
        tab2 = (RelativeLayout) findViewById(R.id.truck_tab2);
        tab3 = (RelativeLayout) findViewById(R.id.truck_tab3);
        btnFullscreen = (ImageButton) findViewById(R.id.truck_imgbtnFullScreen);
        initializeBottomNav();
        btnFullScreenListener();
        displayFragmentMap();

        //tab2
        tab2_listLayout = (LinearLayout) findViewById(R.id.tab2_listviewlayout);
        tab2_listview = (ListView) findViewById(R.id.tab2_listview_reports);

        tab2_errormsgLayout = (LinearLayout) findViewById(R.id.tab2_errormessagelayout);
        tab2_errorTxt = (TextView) findViewById(R.id.tab2_errorTextView);
        tab2_errorButton = (Button) findViewById(R.id.tab2_errorButton);
        tab2_errorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadReportNotifications();
            }
        });

        tab2_loadingLayout = (LinearLayout) findViewById(R.id.tab2_loadinglayout);
        tab2_loadingProgressbar = (ProgressBar) findViewById(R.id.tab2_loading_progressbar);
        tab2_loadingTxt = (TextView) findViewById(R.id.tab2_loading_textview);

        loadReportNotifications();
    }

    protected void initializeBottomNav(){
        bottomNavigation = (AHBottomNavigation) findViewById(R.id.truck_bottomnavigation);

        // Create items
        item1 = new AHBottomNavigationItem("Map", R.drawable.ic_map_black, R.color.colorBottomNavigationPrimary);
        item2 = new AHBottomNavigationItem("Reports", R.drawable.fire_bw,R.color.colorBottomNavigationPrimary);
        item3 = new AHBottomNavigationItem("Account",R.drawable.user,R.color.colorBottomNavigationPrimary);

        // Add items
        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);

        // Set background color
        bottomNavigation.setDefaultBackgroundColor(Color.parseColor("#FEFEFE"));

        // Change colors
        bottomNavigation.setAccentColor(Color.parseColor("#F63D2B"));
        bottomNavigation.setInactiveColor(Color.parseColor("#747474"));


        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                switch (position){
                    case 0:
                        showTab1();
                        break;
                    case 1:
                        showTab2();
                        break;
                    case 2:
                        showTab3();
                        break;
                }
                return true;
            }
        });
    }

    //Tab transisitions
    protected void showTab1(){
        tab1.setVisibility(View.VISIBLE);
        tab2.setVisibility(View.GONE);
        tab3.setVisibility(View.GONE);
    }
    protected void showTab2(){
        tab1.setVisibility(View.GONE);
        tab2.setVisibility(View.VISIBLE);
        tab3.setVisibility(View.GONE);
    }
    protected void showTab3(){
        tab1.setVisibility(View.GONE);
        tab2.setVisibility(View.GONE);
        tab3.setVisibility(View.VISIBLE);
    }


    //TAB1
    protected void btnFullScreenListener(){
        btnFullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!fullscreen){
                    fullScreenMap();
                }else{
                    //exit from fullscreen
                   exitFullScreenMap();
                }
            }
        });
    }
    protected void fullScreenMap(){
        fullscreen=true;
        //make it fullscreen
        btnFullscreen.setImageResource(R.drawable.ic_fullscreen_exit_black);
        bottomNavigation.setVisibility(View.GONE);
        getSupportActionBar().hide();
    }
    protected void exitFullScreenMap(){
        btnFullscreen.setImageResource(R.drawable.ic_fulllscreen_black);
        fullscreen=false;
        getSupportActionBar().show();
        bottomNavigation.setVisibility(View.VISIBLE);
        bottomNavigation.restoreBottomNavigation();
    }
    protected void displayFragmentMap(){
      FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.truck_fragment_container,new Fragment_truck_map()).commit();
    }
    //************************************************************

    //TAB2
    protected void loadReportNotifications(){
        report_firenotif_ids_list.clear();
        report_images_list.clear();
        report_datetime_list.clear();
        report_coordinates_list.clear();
        report_firestatus_list.clear();
        report_alarmlevel_list.clear();
        report_additionalInfo_list.clear();

        showTab2LoadingLayout(true);

        final String query = "SELECT f.firenotif_id, coordinates, additional_info, fire_status, picture, alarm_level, report_datetime " +
                " FROM " +
                " tbl_reports r " +
                " INNER JOIN " +
                " tbl_firenotifs f " +
                " ON r.report_id = f.report_id " +
                " LEFT JOIN " +
                " tbl_firenotif_response fr " +
                " ON f.firenotif_id = fr.firenotif_id " +
                " WHERE response_id is null " +
                " AND r.fire_status='on going' " +
                " AND r.report_status = 'approved'"+
                " AND f.firenotif_receiver="+account_id+";";
        String url = ServerInfoClass.HOST_ADDRESS+"/get_data.php";

        RequestQueue requestQueue = new Volley().newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.wtf("loadReportNotifications()","Response Received: "+response);
                try{
                    JSONObject object = new JSONObject(response);
                    JSONArray Jarray  = object.getJSONArray("mydata");
                    if(Jarray.length()>0){

                        //do extraction

                       /* ArrayList<String> report_firenotif_ids_list= new ArrayList<String>();
                        ArrayList<String> report_images_list= new ArrayList<String>();
                        ArrayList<String> report_coordinates_list = new ArrayList<String>();
                        ArrayList<String> report_datetime_list = new ArrayList<String>();
                        ArrayList<String> report_firestatus_list = new ArrayList<String>();
                        ArrayList<String> report_alarmlevel_list = new ArrayList<String>();
                        ArrayList<String> report_additionalInfo_list= new ArrayList<String>();
                        */
                        String notif_id,encoded_image,coordinates,datetime,firestatus,alarmlevel,additionalInfo;

                        for (int i = 0; i < Jarray.length(); i++) {
                            JSONObject Jasonobject = Jarray.getJSONObject(i);
                            notif_id = Jasonobject.getString(dbHelper.COL_FIRENOTIF_ID);
                            encoded_image = Jasonobject.getString(dbHelper.COL_REPORT_PICUTRE);
                            coordinates = Jasonobject.getString(dbHelper.COL_REPORT_COORDINATES);
                            datetime = Jasonobject.getString(dbHelper.COL_REPORT_DATETIME);
                            firestatus = Jasonobject.getString(dbHelper.COL_REPORT_FIRE_STATUS);
                            alarmlevel = Jasonobject.getString(dbHelper.COL_ALARM_LEVEL);
                            if(alarmlevel == null){
                                alarmlevel = "Unknown";
                            }else{
                                if(alarmlevel.trim().equalsIgnoreCase("null") || alarmlevel.trim().equalsIgnoreCase("")){
                                    alarmlevel = "Unknown";
                                }
                            }
                            additionalInfo = Jasonobject.getString(dbHelper.COL_REPORT_ADDITIONAL_INFO);
                            if(additionalInfo == null){
                                additionalInfo = "None";
                            }else{
                                if(additionalInfo.trim().equalsIgnoreCase("null") || additionalInfo.trim().equalsIgnoreCase("")){
                                    additionalInfo = "None";
                                }
                            }
                            report_firenotif_ids_list.add(notif_id);
                            report_images_list.add(encoded_image);
                            report_coordinates_list.add(coordinates);
                            report_datetime_list.add(datetime);
                            report_firestatus_list.add(firestatus);
                            report_alarmlevel_list.add(alarmlevel);
                            report_additionalInfo_list.add(additionalInfo);
                        }

                        //initialize the adapter
                        showTab2Listview(true);
                        tab2_listview.setAdapter(null);
                        ReportAdapter adapter = new ReportAdapter(getApplicationContext(),
                                report_firenotif_ids_list,
                                report_images_list,
                                report_coordinates_list,
                                report_datetime_list,
                                report_firestatus_list,
                                report_alarmlevel_list,
                                report_additionalInfo_list);
                        tab2_listview.setAdapter(adapter);

                    }else{
                        //show no reports yet UI
                        showTab2MessageLayout(true,"No Fire Reports Yet",true,"Refresh");
                    }
                }catch (Exception e){
                    Log.wtf("LoadReportNotifications Exception","Error : "+e.getMessage());
                    showTab2MessageLayout(true,e.getMessage(),true,"Retry");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                String message = "Error Received";
                Log.wtf("LoadFeed: onErrorResponse","Volley Error \n"+volleyError.getMessage());
                if (volleyError instanceof NetworkError) {
                    message = "Network Error Encountered";
                    Log.wtf("loadFeed (Volley Error)","NetworkError");
                    //showSnackbar("You're not connected to internet");
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
                    message = "Connection Timeout";
                    Log.wtf("loadFeed (Volley Error)","TimeoutError");
                }
                Log.wtf("Volley Error Message","Error: "+volleyError.getMessage());
                showTab2MessageLayout(true,message,true,"Retry");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("qry",query);
                Log.wtf("Map<String><String>","Query: "+query);
                return params;
            }
        };
        requestQueue.add(stringRequest);

    }
    protected void showTab2Listview(boolean show){
        if(show){
            tab2_loadingLayout.setVisibility(View.GONE);
            tab2_errormsgLayout.setVisibility(View.GONE);
            tab2_listLayout.setVisibility(View.VISIBLE);
        }else{
            tab2_listLayout.setVisibility(View.GONE);
        }

    }
    protected void showTab2LoadingLayout(boolean show){
         if(show){
             tab2_listLayout.setVisibility(View.GONE);
             tab2_errormsgLayout.setVisibility(View.GONE);
             tab2_loadingLayout.setVisibility(View.VISIBLE);
         }else{
             tab2_loadingLayout.setVisibility(View.GONE);
         }
    }
    protected void showTab2MessageLayout(boolean show,String msg, boolean showButton, String buttonText){
        if(show) {
            tab2_listLayout.setVisibility(View.GONE);
            tab2_loadingLayout.setVisibility(View.GONE);
            tab2_errormsgLayout.setVisibility(View.VISIBLE);
            tab2_errorTxt.setText(msg);
            if(showButton){
                tab2_errorButton.setVisibility(View.VISIBLE);
                tab2_errorButton.setText(buttonText);
            }else{
                tab2_errorButton.setVisibility(View.GONE);
            }
        }else{
            tab2_errormsgLayout.setVisibility(View.GONE);
        }
    }
    public static void setReportNotificationBadge(int notifcount){
        bottomNavigation.setNotification((notifcount+""),1);
    }
    // REPORT LISTVIEW ADAPTER
    class ReportAdapter extends ArrayAdapter {
        ArrayList<String> report_firenotif_ids_list= new ArrayList<String>();
        ArrayList<String> report_images_list= new ArrayList<String>();
        ArrayList<String> report_coordinates_list = new ArrayList<String>();
        ArrayList<String> report_datetime_list = new ArrayList<String>();
        ArrayList<String> report_firestatus_list = new ArrayList<String>();
        ArrayList<String> report_alarmlevel_list = new ArrayList<String>();
        ArrayList<String> report_additionalInfo_list= new ArrayList<String>();

        public ReportAdapter(Context context,
                             ArrayList<String> report_firenotif_ids_list,
                             ArrayList<String> report_images_list,
                             ArrayList<String> report_coordinates_list,
                             ArrayList<String> report_datetime_list,
                             ArrayList<String> report_firestatus_list,
                             ArrayList<String> report_alarmlevel_list,
                             ArrayList<String> report_additionalInfo_list) {

            //Overriding Default Constructor off ArratAdapter
            super(context, R.layout.template_post,R.id.post_id,report_firenotif_ids_list);
            this.report_firenotif_ids_list = report_firenotif_ids_list;
            this.report_images_list = report_images_list;
            this.report_coordinates_list = report_coordinates_list;
            this.report_datetime_list = report_datetime_list;
            this.report_firestatus_list = report_firestatus_list;
            this.report_alarmlevel_list = report_alarmlevel_list;
            this.report_additionalInfo_list = report_additionalInfo_list;
        }
        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //Inflating the layout
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.template_fire_report,parent,false);

            //Get the reference to the view objects
            TextView id  = (TextView) row.findViewById(R.id.report_template_firenotif_id);
            ImageView imageView = (ImageView) row.findViewById(R.id.report_template_image);
            TextView datetime = (TextView) row.findViewById(R.id.report_template_datetime);
            TextView coordinates = (TextView) row.findViewById(R.id.report_template_coordinates);
            ExpandableTextView additionalInfo = (ExpandableTextView) row.findViewById(R.id.report_template_moreDetails);
            ImageButton btnShowInMap = (ImageButton) row.findViewById(R.id.report_template_showInMapButton);

            btnShowInMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(Activity_main_truck.this, "Show in map button is clicked", Toast.LENGTH_SHORT).show();
                }
            });
            Button btnAccept = (Button) row.findViewById(R.id.report_template_acceptButton);
            btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(Activity_main_truck.this, "Button Accept is clicked", Toast.LENGTH_SHORT).show();
                }
            });
            Button btnDecline = (Button) row.findViewById(R.id.report_template_declineButton);
            btnDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(Activity_main_truck.this, "Button Decline is clicked", Toast.LENGTH_SHORT).show();
                }
            });

            //Providing the element of an array by specifying its position
            id.setText(report_firenotif_ids_list.get(position));
            datetime.setText(report_datetime_list.get(position));
            coordinates.setText(report_coordinates_list.get(position));

            String fire_stat = report_firestatus_list.get(position);
            String alarmlvl = report_alarmlevel_list.get(position);
            String additionalinfo = report_additionalInfo_list.get(position);

            String moredetails = "Fire Status: "+fire_stat+"\nAlarm Level: "+alarmlvl+"\nAdditional Info: "+additionalinfo;

            additionalInfo.setText(moredetails);

            String encoded_post_picture = report_images_list.get(position);
            if(encoded_post_picture!=null && encoded_post_picture.length()>10){
                try{
                    byte[] decodedString = Base64.decode(encoded_post_picture, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    imageView.setImageBitmap(decodedByte);
                }catch (Exception ee){
                    Toast.makeText(getContext(), "Failed to set Image", Toast.LENGTH_SHORT).show();
                }
            }else{
                //get the image from drawables
                imageView.setBackgroundResource(R.drawable.no_image_found);
            }

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.wtf("adapter","Image is clicked");
                    Toast.makeText(Activity_main_truck.this, "image is clicked", Toast.LENGTH_SHORT).show();
                   // initialLayout.setVisibility(View.GONE);
                    //frame1.setVisibility(View.VISIBLE);
                    //frame2.setVisibility(View.VISIBLE);
                    //addToBackStack(new Fragment_PostZoom(),"post_zoom");
                    //BitmapDrawable drawable = (BitmapDrawable) picture.getDrawable();
                    //postImageClicked = drawable.getBitmap();
                }
            });



            return row;
        }
    }
    //************************************************************


    @Override
    public void onBackPressed() {
        if(fullscreen){
            exitFullScreenMap();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.truck_main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menu_logout){
            logout();
        }
        return true;
    }

    protected void logout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logging out");
        builder.setMessage("Continue to log-out?");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences sharedPreferences = getSharedPreferences(MySharedPref.SHAREDPREF_NAME,MODE_PRIVATE);
                sharedPreferences.edit().clear().commit();
                stopService(new Intent(Activity_main_truck.this,Service_Notification.class));
                dbHelper.removeAllData();
                startActivity(new Intent(Activity_main_truck.this,SplashScreen.class));
                finish();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing
            }
        });
        builder.show();
    }

}
