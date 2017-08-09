package com.example.rvnmrqz.firetrack;


import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;

public class Activity_main_truck extends AppCompatActivity {

    FrameLayout tab1;
    RelativeLayout tab2,tab3;
    ImageButton btnFullscreen;
    boolean fullscreen=false;
    public static AHBottomNavigation bottomNavigation;
    AHBottomNavigationItem item1,item2,item3;
    FrameLayout frameContainer;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitiy_main_truck);

        dbHelper = new DBHelper(this);
        frameContainer = (FrameLayout) findViewById(R.id.truck_containter);
        tab1 = (FrameLayout) findViewById(R.id.truck_tab1);
        tab2 = (RelativeLayout) findViewById(R.id.truck_tab2);
        tab3 = (RelativeLayout) findViewById(R.id.truck_tab3);
        btnFullscreen = (ImageButton) findViewById(R.id.truck_imgbtnFullScreen);
        initializeBottomNav();
        btnFullScreenListener();
        displayFragmentMap();
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


    }
    public static void setReportNotificationBadge(int notifcount){
        bottomNavigation.setNotification((notifcount+""),1);
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
