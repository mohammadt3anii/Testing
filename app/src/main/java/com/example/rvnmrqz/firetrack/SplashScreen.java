package com.example.rvnmrqz.firetrack;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class SplashScreen extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        getSupportActionBar().hide();

        //Toast.makeText(this, "Connection: "+ServerInfoClass.HOST_ADDRESS, Toast.LENGTH_SHORT).show();

        sharedPreferences = getSharedPreferences(MySharedPref.SHAREDPREF_NAME,MODE_PRIVATE);
        final String logged = sharedPreferences.getString(MySharedPref.LOGGED,"");


        int secondsDelayed = 1;
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if(logged==null){
                    openActivity(Activity_Login.class);
                }else{
                    switch (logged){
                        case "user":
                            //open user activity
                            openActivity(MainActivity_user.class);
                            break;
                        case "truck":
                            //open truck activity
                            break;
                        case "":
                            //open login activity
                            openActivity(Activity_Login.class);
                         //   Toast.makeText(SplashScreen.this, "change override in splashscreen login", Toast.LENGTH_SHORT).show();
                           // openActivity(MainActivity_user.class);
                        default:
                            //no match
                            break;
                    }
                }
            }
        }, secondsDelayed * 1000);
    }

    private void openActivity(Class activity){
        startActivity(new Intent(SplashScreen.this,activity));
        finish();
    }

}
