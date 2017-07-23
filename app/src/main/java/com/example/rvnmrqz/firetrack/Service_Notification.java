package com.example.rvnmrqz.firetrack;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
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
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by arvin on 7/10/2017.
 */

public class Service_Notification extends Service {

    DBHelper dbhelper;
    static int maxNotifId;
    static String userid,user_barangay_id;
    static Handler handler;
    static Timer timer;
    static TimerTask timerTask;
    int tick=0;
    int seconds;
    int maxCount=5;
    boolean continueCount=true;

    NotificationManager nm;
    NotificationCompat.Builder b;
    RequestQueue requestQueue;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //start timer ticks
        Log.wtf("NotificationService","Serivice Started");
        try{
            dbhelper = new DBHelper(this);
            Cursor c = dbhelper.getSqliteData("SELECT * FROM "+dbhelper.TABLE_USER+" WHERE "+dbhelper.COL_USER_LOC_ID+" = 1");
            if(c!=null){
                c.moveToFirst();
                userid = c.getString(c.getColumnIndex(dbhelper.COL_ACC_ID));
                user_barangay_id = c.getString(c.getColumnIndex(dbhelper.COL_BARANGAY_ID));
            }
        }
        catch (Exception e){
            Log.wtf("SERVICE_ONCREATE", "Exception "+e.getMessage());
        }
        maxNotifId = getLastNotificationId();
        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        timer.cancel();
        timer.purge();
        timerTask.cancel();
        timer=null;
        timerTask=null;
        handler=null;
        Log.wtf("service_doWork", "request service is stopped");
    }

    public void startTimer(){

        Log.wtf("service_startTimer", "Timer started");
        initializeTimer();
        timer.scheduleAtFixedRate(timerTask, seconds, seconds);
    }

    private void initializeTimer(){
        seconds=1000;
        //*********************
        //just to clear the objects
        timer=null;
        timerTask=null;
        handler=null;
        //********************
        handler = new Handler();
        timer = new Timer(false);
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(continueCount){
                            tick++;
                            if(tick==maxCount){
                                doWork();
                            }
                        }
                        Log.wtf("service_timer", "Timer Tick: "+tick);
                    }
                });
            }
        };
        Log.wtf("service_initializeTimer", "Timer initialized");
    }

    private void stopCounting(){
        tick=0;
        continueCount=false;
        Log.wtf("service_stopCounting", "Timer stppped");
    }

    private void restartCounting(){
        tick=0;
        continueCount=true;
        Log.wtf("service_restartCounting", "Timer restarted");
    }

    private void doWork(){
        Log.wtf("service_doWork", "Taskworker is called");
        //start the taskworker
        String url = ServerInfoClass.HOST_ADDRESS+"/get_data.php";

        final String query ="SELECT * FROM "+dbhelper.TABLE_UPDATES+" WHERE receiver IN('ALL'|'u-"+userid+"'|'b-"+user_barangay_id+"') AND "+dbhelper.COL_UPDATE_ID+">"+maxNotifId+";";

        requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                       insert(response);
                       restartCounting();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        
                        Log.wtf("NotificationService","An error occured in requestQue \nError\n"+error.getMessage()+"\nCause: "+error.getCause());
                        restartCounting();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("qry",query);
                stopCounting();
                return params;
            }
        };
        int socketTimeout = ServerInfoClass.TIME_OUT;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        stringRequest.setShouldCache(false);
        requestQueue.add(stringRequest);
    }

    private void insert(String response){
        try{

            String update_id,category,title,content,sender_id,datetime,opened="no";
            JSONObject object = new JSONObject(response);
            JSONArray Jarray  = object.getJSONArray("mydata");
            boolean notificationReceived=false;
            boolean queryReceived = false;
            for (int i = 0; i < Jarray.length(); i++)
            {
                JSONObject Jasonobject = Jarray.getJSONObject(i);
                update_id = Jasonobject.getString(dbhelper.COL_UPDATE_ID);
                category = Jasonobject.getString(dbhelper.COL_CATEGORY);
                title = Jasonobject.getString(dbhelper.COL_TITLE);
                content = Jasonobject.getString(dbhelper.COL_CONTENT);
                sender_id = Jasonobject.getString(dbhelper.COL_SENDER_ID);
                datetime = Jasonobject.getString(dbhelper.COL_DATETIME);

                //insert in sqlite
                dbhelper.insertUpdate(update_id,category,title,content,sender_id,datetime,opened);

                if(category.equalsIgnoreCase("notif")){
                    //notification received
                    notificationReceived=true;
                }else{
                    //this is a SQL update,execute
                    queryReceived=true;
                    dbhelper.executeThisQuery(content);
                }
            }
            if(notificationReceived || queryReceived){
                //there is a new update received
                int tmp = getLastNotificationId();
                if(tmp>maxNotifId){
                    //there is a notification
                    maxNotifId=tmp;
                    //show some notification in the drawer
                    Log.wtf("maxnotifId", "New Value is "+maxNotifId);
                    Log.wtf("onResponse","Notif is inserted");
                }
                if(notificationReceived){
                    //a notification is received
                    showNotification();
                }
                else{
                    //else it is a query
                    Log.wtf("SyncNotifications","There is a new QUERY Received");
                }
            }

        }catch (Exception ee){
            Toast.makeText(Service_Notification.this,ee.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }



    private int getLastNotificationId(){
        int lastId;
        Cursor c = dbhelper.getSqliteData("SELECT MAX("+dbhelper.COL_UPDATE_ID+") max_id FROM "+dbhelper.TABLE_UPDATES+";");
        if(c!=null){
            Log.wtf("getLastNotificationId","c is not null");
            c.moveToFirst();
            String temp = c.getString(c.getColumnIndex("max_id"));
            if(temp!=null){
                lastId = Integer.parseInt(temp);
                return lastId;
            }else{
                return 0;
            }
        }else{
            Log.wtf("getLastNotificationId","c is null");
            return 0;
        }
    }

    protected void showNotification(){
        final Intent mainIntent = new Intent(this,MainActivity_user.class);
        mainIntent.putExtra("notif","notify");
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                (mainIntent), PendingIntent.FLAG_UPDATE_CURRENT);

        b = new NotificationCompat.Builder(this);
        b.setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setOngoing(false)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.fire)
                .setTicker("New Notification Received")
                .setContentTitle("FireTRACK")
                .setContentText("New Notification")
                .setContentIntent(pendingIntent);
        nm = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE);
        nm.notify(100, b.build());
    }
}
