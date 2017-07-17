package com.example.rvnmrqz.firetrack;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity_user extends AppCompatActivity {


    LinearLayout frame1;
    LinearLayout frame2,frame3;
    LinearLayout initialLayout,feed_postLayout,feed_messageLayout,feed_loadingLayout;
    TextView feed_messageTV;
    SwipeRefreshLayout feed_swipeRefreshLayout,notif_swipeRefreshLayout;
    ListView feed_listview;
    Button btnReport, btnMyReports, btnFeed_message;
    public static FragmentManager fragmentManager;
    static DBHelper dbHelper;
    public static boolean reminderIsShown = false;


    ArrayList<String> post_id;
    ArrayList<String> postername;
    ArrayList<String> postdatetime;
    ArrayList<String> postmessage;
    ArrayList<String> postpicture;
    FeedAdapter adapter;

     ListView notif_listview;
     ArrayList<String> notif_titles;
     ArrayList<String> notif_datetime;
     ArrayList<String> notif_messages;
     NotificationAdapter notif_adapter;

    int sql_limit=5;
    int sql_offset=0;

    View footerView;
    boolean isFooterLoading=false;
    boolean noMorePost=false;
    boolean notificationsLoaded=false;
    String server_url;
    RequestQueue requestQueue;

    static Activity user_act;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_report:
                    showFrame1();
                    return true;
                case R.id.navigation_dashboard:
                    showFrame2();
                    return true;
                case R.id.navigation_notifications:
                    showFrame3();
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        dbHelper = new DBHelper(this);

        frame1 = (LinearLayout) findViewById(R.id.report_framelayout);
        frame2 = (LinearLayout) findViewById(R.id.news_framelayout);
        frame3 = (LinearLayout) findViewById(R.id.notification_framelayout);
        initialLayout = (LinearLayout) findViewById(R.id.initial_layout);

        btnReport = (Button) findViewById(R.id.btnReportFire);
        btnReportListener();
        btnMyReports = (Button) findViewById(R.id.btnMyReports);
        btnMyReportListener();

        feed_loadingLayout = (LinearLayout) findViewById(R.id.feed_loadingLayout);
        feed_loadingLayout.setVisibility(View.GONE);
        feed_messageLayout = (LinearLayout) findViewById(R.id.feed_messageLayout);
        feed_messageLayout.setVisibility(View.GONE);
        feed_postLayout = (LinearLayout) findViewById(R.id.feed_postLayout);
        feed_swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.feed_swipe_refresh);
        feed_swipeRefreshLayoutListener();
        feed_messageTV = (TextView) findViewById(R.id.feed_messageTextview);
        feed_listview = (ListView) findViewById(R.id.listview_feed);
        btnFeed_message = (Button) findViewById(R.id.feed_messageButton);
        fragmentManager = getSupportFragmentManager();

        //arraylist of adapter feed
        post_id= new ArrayList<>();
        postername = new ArrayList<>();
        postdatetime = new ArrayList<>();
        postmessage = new ArrayList<>();
        postpicture = new ArrayList<>();

        //arraylist of adapter notifications
        notif_titles = new ArrayList<>();
        notif_datetime = new ArrayList<>();
        notif_messages = new ArrayList<>();
        notif_listview = (ListView) findViewById(R.id.notif_listview);

        notif_swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.notif_swipe_refresh_layout);
        notif_swipeRefreshLayoutListner();

        footerView =  ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.listview_loading_footer, null, false);
        SharedPreferences sharedPreferences = getSharedPreferences(MySharedPref.SHAREDPREF_NAME,MODE_PRIVATE);
        String syncNotif = sharedPreferences.getString(MySharedPref.NOTIF,"");
        if(syncNotif.length()==0){
            //to sync notif
            //sync is not yet done
            Log.wtf("Sync Notif","SyncNotif have 0 length");
            new SyncNotifications(MainActivity_user.this,1);
        }else{
            //already done syncing before
            Log.wtf("Sync Notif", "SyncNotif length is not 0, value is "+syncNotif);
            loadNotifications();
        }

        loadFeed();

        String extra = getIntent().getStringExtra("notif");
        if(extra!=null){
            showFrame3();
        }else{
            showFrame1();
        }

    }

    //FRAME 1****************************************************************
    protected void btnReportListener(){
        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //for back button in action bar
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                initialLayout.setVisibility(View.GONE);
                addToBackStack(new Fragment_report_options(),"report_options");

            }
        });
    }
    protected void btnMyReportListener(){
        btnMyReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity_user.this,"Open Fragment",Toast.LENGTH_SHORT).show();
            }
        });
    }
    //***********************************************************************

    //FRAME 2****************************************************************
    protected void feed_swipeRefreshLayoutListener(){
        feed_swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadFeed();
                feed_swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    protected void loadFeed(){
        Log.wtf("Loadfeed","Loadfeed called");
        showFeedLoading(true);
        server_url = ServerInfoClass.HOST_ADDRESS+"/get_data.php";
        requestQueue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, server_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response!=null){
                            try{
                                Log.wtf("onResponse","Response is not null");
                                JSONObject object = new JSONObject(response);
                                JSONArray Jarray  = object.getJSONArray("mydata");
                                Log.wtf("onResponse","Jarray has "+Jarray.length());

                                //clear the list in the UI
                                feed_listview.setAdapter(null);
                                //contactList.clear();
                                post_id.clear();
                                postername.clear();
                                postdatetime.clear();
                                postmessage.clear();
                                postpicture.clear();

                                if(Jarray.length()>0) {
                                    showFeedMessage(false,null);
                                    showFeedLoading(false);
                                    feed_postLayout.setVisibility(View.VISIBLE);
                                        for (int i = 0; i < Jarray.length(); i++) {
                                            JSONObject Jasonobject = Jarray.getJSONObject(i);
                                            String id = Jasonobject.getString("post_id");
                                          //  String encoded_poster_image = Jasonobject.getString("")
                                            String poster_name = Jasonobject.getString("barangay_name");
                                            String datetime = Jasonobject.getString("post_datetime");
                                            String message = Jasonobject.getString("message");
                                            String encoded_post_picture = Jasonobject.getString("picture");

                                            post_id.add(id);
                                            postername.add(poster_name);
                                            postdatetime.add(datetime);
                                            postmessage.add(message);
                                            postpicture.add(encoded_post_picture);
                                        }
                                        noMorePost=false;
                                        sql_offset = Jarray.length();
                                        setListViewAdapter();
                                }else{
                                    //no post
                                    showFeedMessage(true,"No Post Available");
                                    feed_postLayout.setVisibility(View.GONE);
                                    feed_messageLayout.setVisibility(View.VISIBLE);
                                }


                            }catch (Exception ee){
                                showFeedMessage(true,"Can't load feed");
                                Log.wtf("loadFeed_ERROR", ee.getMessage());
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.wtf("LoadFeed: onErrorResponse","Volley Error \n"+error.getMessage());
                        showFeedMessage(true,"Can't load feed");
                        if(error == null){
                            //retry
                            loadFeed();
                        }
                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                       Map<String,String> params = new HashMap<String, String>();
                        params.put("qry","SELECT b.barangay_name,f.* FROM tbl_feed f INNER JOIN tbl_monitoring m ON f.creator_id = m.acc_id INNER JOIN tbl_barangay b ON b.barangay_id=m.barangay_id ORDER BY post_id desc  LIMIT "+sql_limit+";");

                        return params;
                 }
        };
        requestQueue.add(request);
    }
    protected void setListViewAdapter(){
        adapter = new FeedAdapter(MainActivity_user.this,post_id,postername,postdatetime,postmessage,postpicture);
        feed_listview.setAdapter(adapter);
        listViewListners();
    }
    protected void listViewListners(){
        //LISTVIEW CLICK LISTENER
        feed_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

            }
        });

        feed_listview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(view.getLastVisiblePosition() == totalItemCount-1 && isFooterLoading==false && noMorePost==false){
                    isFooterLoading=true;
                    Log.wtf("scroll_Listner","LAST");
                    addFooter(true);
                    loadMore();
                }

            }
        });
    }
    protected void showFeedMessage(boolean showMessageLayout,String message){
        if(showMessageLayout){
            feed_loadingLayout.setVisibility(View.GONE);
            feed_postLayout.setVisibility(View.GONE);
            feed_messageLayout.setVisibility(View.VISIBLE);
            if(message!=null){
                feed_messageTV.setText(message);
            }
        }
    }
    protected void showFeedLoading(boolean show){
        if(show){
            feed_messageLayout.setVisibility(View.GONE);
            feed_postLayout.setVisibility(View.GONE);
            feed_loadingLayout.setVisibility(View.VISIBLE);
        }else{
            feed_loadingLayout.setVisibility(View.GONE);
        }
        btnFeed_messageClickListner();
    }
    protected void btnFeed_messageClickListner(){
        btnFeed_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFeed();
            }
        });
    }
    protected void addFooter(boolean yes){
        if(yes){
            //add footer
            feed_listview.addFooterView(footerView);
        }else{
            //remove footer
            feed_listview.removeFooterView(footerView);
        }
    }
    protected void loadMore(){
        Log.wtf("Loadmore","Loadmore is called");
        Log.wtf("Loadmore","Limit = "+sql_limit+" Offset = "+sql_offset);
        server_url = ServerInfoClass.HOST_ADDRESS+"/get_data.php";
        requestQueue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, server_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response!=null){
                            try{
                                JSONObject object = new JSONObject(response);
                                JSONArray Jarray  = object.getJSONArray("mydata");
                                //clear the list in the UI

                                if(Jarray.length()>0) {
                                    feed_postLayout.setVisibility(View.VISIBLE);
                                    for (int i = 0; i < Jarray.length(); i++) {
                                        JSONObject Jasonobject = Jarray.getJSONObject(i);
                                        String id = Jasonobject.getString("post_id");
                                        //  String encoded_poster_image = Jasonobject.getString("")
                                        String poster_name = Jasonobject.getString("barangay_name");
                                        String datetime = Jasonobject.getString("post_datetime");
                                        String message = Jasonobject.getString("message");
                                        String encoded_post_picture = Jasonobject.getString("picture");
                                        post_id.add(id);
                                        postername.add(poster_name);
                                        postdatetime.add(datetime);
                                        postmessage.add(message);
                                        postpicture.add(encoded_post_picture);
                                    }
                                    adapter.notifyDataSetChanged();
                                  //  adapter = new FeedAdapter(MainActivity_user.this,post_id,postername,postdatetime,postmessage,postpicture);
                                  //  feed_listview.setAdapter(adapter);
                                    Log.wtf("Loadmore","Loaded "+Jarray.length());
                                    sql_offset = sql_offset+Jarray.length();
                                }else{
                                   Log.wtf("Loadmore","No More post");
                                    noMorePost=true;
                                }
                                addFooter(false);
                                isFooterLoading=false;

                            }catch (Exception ee){
                                showFeedMessage(true,"Can't load feed");
                                Log.wtf("loadFeed_ERROR", ee.getMessage());
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.wtf("Loadmore_onError","Cause: "+error.getCause());
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                Log.wtf("asd","limit: "+sql_limit+" offset:"+sql_offset);
                params.put("qry","SELECT b.barangay_name,f.* FROM tbl_feed f INNER JOIN tbl_monitoring m ON f.creator_id = m.acc_id INNER JOIN tbl_barangay b ON b.barangay_id=m.barangay_id ORDER BY post_id desc  LIMIT "+2+" OFFSET "+sql_offset+";");
                return params;
            }
        };
        requestQueue.add(request);
    }
    // POST LISTVIEW ADAPTER
    class FeedAdapter extends ArrayAdapter {
        ArrayList<String> post_id= new ArrayList<String>();
        ArrayList<String> postername = new ArrayList<String>();
        ArrayList<String> postdatetime = new ArrayList<String>();
        ArrayList<String> postmessage = new ArrayList<String>();
        ArrayList<String> postpicture = new ArrayList<String>();


        public FeedAdapter(Context context, ArrayList<String> post_id, ArrayList<String> postername,   ArrayList<String> postdatetime,  ArrayList<String> postmessage,  ArrayList<String> postpicture) {
            //Overriding Default Constructor off ArratAdapter
            super(context, R.layout.template_post,R.id.post_id,post_id);
            this.post_id = post_id;
            this.postername=postername;
            this.postdatetime=postdatetime;
            this.postmessage=postmessage;
            this.postpicture=postpicture;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //Inflating the layout
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.template_post,parent,false);

            //Get the reference to the view objects
            TextView id  = (TextView) row.findViewById(R.id.post_id);
            //    holder.poster_image  = (ImageView) convertView.findViewById(R.id.poster_image);
            TextView name = (TextView) row.findViewById(R.id.poster_name);
            TextView datetime = (TextView) row.findViewById(R.id.post_datetime);
            TextView message = (TextView) row.findViewById(R.id.post_message);
            ImageView picture  = (ImageView) row.findViewById(R.id.post_picture);

            //Providing the element of an array by specifying its position
            id.setText(post_id.get(position));
            name.setText(postername.get(position));
            datetime.setText(postdatetime.get(position));
            message.setText(postmessage.get(position));

            String encoded_post_picture = postpicture.get(position);
            if(encoded_post_picture!=null && encoded_post_picture.length()>10){
                try{
                    byte[] decodedString = Base64.decode(encoded_post_picture, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    picture.setImageBitmap(decodedByte);
                }catch (Exception ee){
                    Toast.makeText(getContext(), "Failed to set Image", Toast.LENGTH_SHORT).show();
                }
            }

            return row;
        }
    }
    //***********************************************************************

    //FRAME 3****************************************************************
    public  void loadNotifications(){
        Log.wtf("loadNotifications","Start");
        feed_swipeRefreshLayout.setRefreshing(true);

        notif_adapter=null;
        notif_titles.clear();
        notif_datetime.clear();
        notif_messages.clear();

        dbHelper = new DBHelper(getApplicationContext());
        Cursor c = dbHelper.getSqliteData("SELECT * FROM "+dbHelper.TABLE_NOTIFICATION+";");
        c.moveToFirst();
        Log.wtf("loadNotifications","Cursor c: "+c.getCount());
        int counter=0;
        int cursorLength = c.getCount();
        while(counter<cursorLength){
            notif_titles.add(c.getString(c.getColumnIndex(dbHelper.COL_NOTIF_TITLE)));
            notif_datetime.add(c.getString(c.getColumnIndex(dbHelper.COL_NOTIF_DATETIME)));
            notif_messages.add(c.getString(c.getColumnIndex(dbHelper.COL_NOTIF_MESSAGE)));
            counter++;
            c.moveToNext();
        }
        if(cursorLength>0){
            notif_adapter = new NotificationAdapter(getApplicationContext(),notif_titles,notif_datetime,notif_messages);
            notif_listview.setAdapter(notif_adapter);
        }
        feed_swipeRefreshLayout.setRefreshing(false);
        notificationsLoaded=true;
    }
    class NotificationAdapter extends ArrayAdapter{
         ArrayList<String> notif_titles = new ArrayList<>();
         ArrayList<String> notif_datetime = new ArrayList<>();
         ArrayList<String> notif_messages = new ArrayList<>();
        public NotificationAdapter(@NonNull Context context, ArrayList<String> notif_titles, ArrayList<String> notif_datetime, ArrayList<String> notif_messages) {
            super(context, R.layout.template_notification, R.id.notif_title,notif_titles);

            this.notif_titles=notif_titles;
            this.notif_datetime=notif_datetime;
            this.notif_messages=notif_messages;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = getLayoutInflater();
            View row = layoutInflater.inflate(R.layout.template_notification,parent,false);

            TextView txtTitle = (TextView) row.findViewById(R.id.notif_title);
            TextView txtDateTime = (TextView) row.findViewById(R.id.notif_datetime);
            ExpandableTextView txtMessage = (ExpandableTextView) row.findViewById(R.id.notif_message);

            txtTitle.setText(notif_titles.get(position));
            txtMessage.setText(notif_messages.get(position));
            txtDateTime.setText(notif_datetime.get(position));

            return row;
        }
    }
    protected void notif_swipeRefreshLayoutListner(){
        notif_swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadNotifications();
                notif_swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    //***********************************************************************

    //FRAME TRANSITIONS
    protected void showFrame1(){
        clearBackstack();
        initialLayout.setVisibility(View.VISIBLE);
        frame1.setVisibility(View.VISIBLE);
        frame2.setVisibility(View.GONE);
        frame3.setVisibility(View.GONE);

    }
    protected void showFrame2(){
        clearBackstack();
        frame2.setVisibility(View.VISIBLE);
        frame1.setVisibility(View.GONE);
        frame3.setVisibility(View.GONE);

    }
    protected void showFrame3(){

        clearBackstack();
        if(!notificationsLoaded){
            loadNotifications();
        }
        frame3.setVisibility(View.VISIBLE);
        frame1.setVisibility(View.GONE);
        frame2.setVisibility(View.GONE);

    }


    //BACKSTACKS
    public static void addToBackStack(Fragment fragment, String name){
        try{
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.report_framelayout,fragment);
            fragmentTransaction.addToBackStack(name);
            fragmentTransaction.commit();
        }catch (Exception ee){
            Log.wtf("addToBackStack","ERROR: "+ee.getMessage());
            Toast.makeText(user_act,"An Error Occured Changing Stack", Toast.LENGTH_SHORT).show();
        }
    }
    protected void clearBackstack(){
        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
        //for back button in action bar
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onBackPressed() {
        goBack();
    }
    public void goBack(){
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if(backStackEntryCount>0) {
            super.onBackPressed();
            backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();

            if (frame1.getVisibility() == View.VISIBLE && backStackEntryCount == 0) {
                initialLayout.setVisibility(View.VISIBLE);
            }
            if (backStackEntryCount == 0) {
                getSupportActionBar().setDisplayShowHomeEnabled(false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }else{
            if(frame1.getVisibility()==View.GONE){
                BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
                View view = bottomNavigationView.findViewById(R.id.navigation_report);
                view.performClick();
            }else{
                super.onBackPressed();
            }
        }

    }

    //MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_user) {

              Toast.makeText(this, "Add Activity", Toast.LENGTH_SHORT).show();
        }
        else if(id == R.id.menu_logout){
            logout();
        }
        else if(id == R.id.menu_test){
            startActivity(new Intent(MainActivity_user.this,Activity_DatabaseManager.class));
        }
        else if(id == R.id.menu_startService){
                if(isMyServiceRunning(Service_Notification.class)){
                    stopService(new Intent(MainActivity_user.this, Service_Notification.class));
                    Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
                }else{
                    startService(new Intent(MainActivity_user.this,Service_Notification.class));
                    Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
                }
        }
        else if(id == android.R.id.home){
            goBack();
            return true;
        }

        return super.onOptionsItemSelected(item);
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

                dbHelper.removeAllData();
                startActivity(new Intent(MainActivity_user.this,SplashScreen.class));
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
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



        
}
