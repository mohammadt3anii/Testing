package com.example.rvnmrqz.firetrack;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


public class Fragment_myreports extends Fragment {

    LinearLayout layout_progress,layout_error_message,layout_list;


    public Fragment_myreports() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_myreports, container, false);
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(isNetworkAvailable()){

        }else{
            //show snackbar
            showSnackbar("You're offline");
        }
    }

    public void showProgressLayout(String loadingmsg){
        layout_progress.setVisibility(View.VISIBLE);
        layout_error_message.setVisibility(View.GONE);
        layout_list.setVisibility(View.GONE);
    }
    public void showErrorMessage(String errorMsg,boolean showButton, String buttonText){

    }
    public void showListview(){

    }

    protected void showSnackbar(String snackbarMsg){
        Snackbar.make(getActivity().findViewById(android.R.id.content), snackbarMsg, Snackbar.LENGTH_LONG)
                .setAction("Go online", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.setClassName("com.android.phone", "com.android.phone.NetworkSetting");
                        startActivity(intent);
                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light ))
                .show();
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }




}
