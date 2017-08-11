package com.example.rvnmrqz.firetrack;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.nio.DoubleBuffer;


public class Fragment_truck_map extends Fragment implements OnMapReadyCallback{

    static GoogleMap mGooglemap;
    MapView mMapView;
    View myview;
    static Marker destination_marker;
    static LatLng destinationLatlng;
    Animation anim_down, anim_up;

    static LinearLayout confirmationLayout;
    Button btnAccept,btnDecline, btnCancel;

    public Fragment_truck_map() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myview = inflater.inflate(R.layout.fragment_truck_map, container, false);
        return myview;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        confirmationLayout = (LinearLayout) getActivity().findViewById(R.id.truck_map_confirmationLayout);
        btnAccept = (Button) getActivity().findViewById(R.id.confirmation_acceptButton);
        btnDecline = (Button) getActivity().findViewById(R.id.confirmation_declineButton);
        btnCancel = (Button) getActivity().findViewById(R.id.confirmation_cancelButton);
        mMapView = (MapView) myview.findViewById(R.id.map);
        if(mMapView !=null){
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }


        confirmationButtonListener();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mGooglemap = googleMap;
        mGooglemap.getUiSettings().setMapToolbarEnabled(false);
        mGooglemap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        resetMapView();
    }


    //PREVIEW AND CONFIRMATION
    public static void showPreviewOnMap(boolean show){
        if(show){
           confirmationLayout.setVisibility(View.VISIBLE);
        }else{
            confirmationLayout.setVisibility(View.GONE);
        }
    }
    protected void confirmationButtonListener(){
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.wtf("confirmationListener","Button Accept is clicked");
                Toast.makeText(getContext(),"Accept is clicked",Toast.LENGTH_SHORT).show();
                hideConfirmationLayout();
            }
        });
        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.wtf("confirmationListener","Button Decline is clicked");
                Toast.makeText(getContext(),"Decline is clicked",Toast.LENGTH_SHORT).show();
                hideConfirmationLayout();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.wtf("confirmationListener","Button Cancel is clicked");
                resetMapView();
                hideConfirmationLayout();
            }
        });

    }
    protected void hideConfirmationLayout(){
        anim_down = AnimationUtils.loadAnimation(getContext(),R.anim.slide_down);
        confirmationLayout.startAnimation(anim_down);
        anim_down.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                confirmationLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    protected static void addDestinationmarker(LatLng coor,String title, String snippetmsg){
        destinationLatlng = coor;
        animateSingleCameraView(false,destinationLatlng);
        destination_marker =  mGooglemap.addMarker(new MarkerOptions().position(coor).title(title).snippet(snippetmsg));
        destination_marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.fire_marker));
    }

    protected static void animateSingleCameraView(boolean tilted,LatLng location){
        CameraPosition position;
        if(tilted){
            position = new CameraPosition.Builder()
                    .target(location)
                    .tilt(65.5f).zoom(18f).build();
        }else{
             position = new CameraPosition.Builder()
                    .target(location)
                    .zoom(18f).build();
        }
        mGooglemap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
    }

    protected void resetMapView(){
        mGooglemap.clear();
        LatLng valenzuela_center = new LatLng(14.699006, 120.983371);
        mGooglemap.animateCamera(CameraUpdateFactory.newLatLngZoom(valenzuela_center, 13.5f));
    }


}
