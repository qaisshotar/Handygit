package com.example.handygit;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.handygit.Common.Common;
import com.example.handygit.Model.SelectPlaceEvent;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RequestWorkerActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;


    private Marker originMarker;

    private ValueAnimator animator;
    public static final int DESIRED_NUM_OF_SPINS =5;
    public static final int DESIRED_SECONDS_PER_ONE_FULL_360_SPIN=40;


    private SelectPlaceEvent selectPlaceEvent;
    private Circle lastUserCircle;
    private long duration = 1000;
    private ValueAnimator lastPluseAnimator;


    @BindView(R.id.finding_your_worker_layout)
    CardView finding_your_worker_layout;


    @BindView(R.id.confirm_handy)
    CardView confirm_handy;

    @BindView(R.id.confirm_layout)
    CardView confirm_layout;
    @BindView(R.id.confirm_button)
    Button confirm_button;

    @BindView(R.id.spinner_service)
    Spinner spinner_service;

    @BindView(R.id.fill_maps)
    View fill_maps;


    @OnClick(R.id.confirm_button)
    void onConfirmService(){

        if(selectPlaceEvent == null) return;


        mMap.clear();

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(selectPlaceEvent.getOrigin())
                .tilt(45f)
                .zoom(16f)
                .build();

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        addMarkerWithPulseAnimation();

    }

    private void addMarkerWithPulseAnimation() {

        finding_your_worker_layout.setVisibility(View.VISIBLE);
        fill_maps.setVisibility(View.VISIBLE);


        originMarker=mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker())
                .position(selectPlaceEvent.getOrigin()));

        addPulsatingEffect(selectPlaceEvent.getOrigin());

    }


    private void addPulsatingEffect(LatLng origin) {
        if(lastPluseAnimator !=null) lastPluseAnimator.cancel();
        if (lastUserCircle != null)lastUserCircle.setCenter(origin);

        lastPluseAnimator = Common.valueAnimate(duration, animation -> {
            if (lastUserCircle != null) lastUserCircle.setRadius((Float)animation.getAnimatedValue());
            else {
                lastUserCircle = mMap.addCircle(new CircleOptions()
                        .center(origin)
                        .radius((Float)animation.getAnimatedValue())
                        .strokeColor(Color.WHITE)
                        .fillColor(Color.parseColor("#33333333"))
                );

            }
        });
        startMapCameraSpinningAnimation(mMap.getCameraPosition().target);
    }

    private void startMapCameraSpinningAnimation(LatLng target) {
        if (animator != null) animator.cancel();
        animator= ValueAnimator.ofFloat(0,DESIRED_NUM_OF_SPINS*360);
        animator.setDuration(DESIRED_SECONDS_PER_ONE_FULL_360_SPIN*DESIRED_NUM_OF_SPINS*1000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setStartDelay(100);
        animator.addUpdateListener(ValueAnimator -> {
            Float newBearingValue = (Float) ValueAnimator.getAnimatedValue();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                    .target(target)
                    .zoom(16f)
                    .tilt(45f)
                    .bearing(newBearingValue)
                    .build()));
        });
        animator.start();
    }

    @Override
    protected void onDestroy() {
        if(animator != null)animator.end();
        super.onDestroy();
    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_worker);


        init();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void init() {
        ButterKnife.bind(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}