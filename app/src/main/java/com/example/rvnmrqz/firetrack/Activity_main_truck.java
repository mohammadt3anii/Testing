package com.example.rvnmrqz.firetrack;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;

public class Activity_main_truck extends AppCompatActivity {

    public static AHBottomNavigation bottomNavigation;
    AHBottomNavigationItem item1,item2,item3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitiy_main_truck);

        initializeBottomNav();
    }


    protected void initializeBottomNav(){
        bottomNavigation = (AHBottomNavigation) findViewById(R.id.truck_bottomnavigation);

        // Create items
        item1 = new AHBottomNavigationItem("Reports", R.drawable.fire_bw,R.color.colorBottomNavigationPrimary);
        item2 = new AHBottomNavigationItem("Map", R.drawable.feed, R.color.colorBottomNavigationPrimary);

        // Add items
        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);


        // Set background color
        bottomNavigation.setDefaultBackgroundColor(Color.parseColor("#FEFEFE"));

        // Change colors
        bottomNavigation.setAccentColor(Color.parseColor("#F63D2B"));
        bottomNavigation.setInactiveColor(Color.parseColor("#747474"));

    }
}
