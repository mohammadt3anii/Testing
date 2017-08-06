package com.example.rvnmrqz.firetrack;

import android.animation.Animator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapFragment extends Fragment implements OnMapReadyCallback{

    private LatLng camera;
    String title="",snippetmsg="";
   // private LatLng camera = new LatLng(12.8797, 121.7740);
    LinearLayout progresslayout, maplayout;
    GoogleMap mGooglemap;
    MapView mMapView;
    View myview;
    Marker mark;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String cor = getArguments().getString("coordinates");
        title = getArguments().getString("title");
        snippetmsg = getArguments().getString("snippet");
        if(cor!=null){
            try{
                String[] latLng = cor.split(",");
                camera = new LatLng( Double.parseDouble(latLng[0]), Double.parseDouble(latLng[1]));
            }catch (Exception e){
                camera = new LatLng(12.8797, 121.7740);
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myview = inflater.inflate(R.layout.fragment_map, container, false);
        return myview;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Map View");
        progresslayout = (LinearLayout) getActivity().findViewById(R.id.map_progresslayout);
        maplayout = (LinearLayout) getActivity().findViewById(R.id.map_maplayout);

        mMapView = (MapView) myview.findViewById(R.id.map);
        if(mMapView !=null){
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mGooglemap = googleMap;
        mGooglemap.getUiSettings().setMapToolbarEnabled(false);
        mGooglemap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //philippine view
        LatLng philView = new LatLng(12.8797, 121.7740);
        mGooglemap.animateCamera(CameraUpdateFactory.newLatLngZoom(philView, 6));
        mGooglemap.animateCamera(CameraUpdateFactory.newLatLngZoom(camera,24));
        mGooglemap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Toast.makeText(getContext(), "Changed", Toast.LENGTH_SHORT).show();
            }
        });

     //   addmarker(camera,title,snippetmsg);
    }

    protected void addmarker(LatLng coor,String title, String snippetmsg){
        mark =  mGooglemap.addMarker(new MarkerOptions().position(coor).title(title).snippet(snippetmsg));
        mark.showInfoWindow();
    }

}
