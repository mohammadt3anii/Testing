package com.example.rvnmrqz.firetrack;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


public class Fragment_PostZoom extends Fragment {


    TextView txtClose;
    TouchImageView touchImageView;

    public Fragment_PostZoom() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fragment__post_zoom, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtClose = (TextView) getActivity().findViewById(R.id.zoom_txtClose);
        touchImageView = (TouchImageView) getActivity().findViewById(R.id.zoom_touchimageview);

        if(MainActivity_user.postImageClicked!=null){
            touchImageView.setImageBitmap(MainActivity_user.postImageClicked);
        }else{
            Toast.makeText(getContext(), "Cannot zoom the photo", Toast.LENGTH_SHORT).show();
        }

        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Close Zoom Layout", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().popBackStack();
                MainActivity_user.bottomNavigation.setCurrentItem(1);

            }
        });

    }
}
