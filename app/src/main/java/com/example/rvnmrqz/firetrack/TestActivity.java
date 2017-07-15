package com.example.rvnmrqz.firetrack;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static java.lang.Math.acos;
import static java.lang.Math.toRadians;

public class TestActivity extends AppCompatActivity {

    TextView t1;
    Button b1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        t1 = (TextView) findViewById(R.id.txtResult);
        b1 = (Button) findViewById(R.id.btnShow);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double distance;
                String a = "14.718765, 120.930516";
                String b  = "14.718222, 120.930879";
                String apart[] = a.trim().split(",");
                String bpart[] = b.trim().split(",");
                //A 14.723008,120.928143
                //B 14.722717,120.928615

                Location locationA = new Location("");
                locationA.setLatitude(Double.parseDouble(apart[0]));
                locationA.setLongitude(Double.parseDouble(apart[1]));

                Location locationB = new Location("");
                locationB.setLatitude(Double.parseDouble(bpart[0]));
                locationB.setLongitude(Double.parseDouble(bpart[1]));

                distance = locationA.distanceTo(locationB)/1000;
                t1.setText(String.valueOf(distance));
            }
        });
    }




}
