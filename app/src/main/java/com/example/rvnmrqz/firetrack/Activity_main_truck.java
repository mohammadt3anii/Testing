package com.example.rvnmrqz.firetrack;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;

import java.util.zip.Inflater;

public class Activity_main_truck extends AppCompatActivity {

    public static AHBottomNavigation bottomNavigation;
    AHBottomNavigationItem item1,item2;
    FrameLayout frameContainer;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitiy_main_truck);
        dbHelper = new DBHelper(this);
        frameContainer = (FrameLayout) findViewById(R.id.truck_containter);
        initializeBottomNav();
    }


    protected void initializeBottomNav(){
        bottomNavigation = (AHBottomNavigation) findViewById(R.id.truck_bottomnavigation);

        // Create items
        item1 = new AHBottomNavigationItem("Map", R.drawable.ic_map_black, R.color.colorBottomNavigationPrimary);
        item2 = new AHBottomNavigationItem("Reports", R.drawable.fire_bw,R.color.colorBottomNavigationPrimary);

        // Add items
        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);


        // Set background color
        bottomNavigation.setDefaultBackgroundColor(Color.parseColor("#FEFEFE"));

        // Change colors
        bottomNavigation.setAccentColor(Color.parseColor("#F63D2B"));
        bottomNavigation.setInactiveColor(Color.parseColor("#747474"));

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
