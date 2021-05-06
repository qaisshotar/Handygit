package com.example.handygit.ui.home;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.webkit.WebStorage;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.handygit.Callback.IFirebaseFaildListener;
import com.example.handygit.Callback.IFirebaseUserinfoListener;
import com.example.handygit.Common.Common;
import com.example.handygit.Model.GeoQueryModel;
import com.example.handygit.Model.WorkerGeoModel;
import com.example.handygit.Model.WorkerInfoMode;
import com.example.handygit.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.data.DataBufferObserver;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;


import java.io.IOException;
import java.util.List;
import java.util.Locale;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class HomeFragment extends Fragment implements OnMapReadyCallback, LocationListener, IFirebaseFaildListener, IFirebaseUserinfoListener {




    private AutoCompleteTextView autoCompleteTextView;


    private GoogleMap mMap;
    private HomeViewModel homeViewModel;
    private LocationRequest locationRequest;
    //location
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private SupportMapFragment mapFragment;

    //load worker
    private double distance = 1.0; // default in km
    private static final double LIMIT_RANGE = 10.0; //km
    private Location previousLocation, currentLocation; // use to calculate distance
    private boolean firstTime = true;

    //listener
    IFirebaseUserinfoListener iFirebaseUserinfoListener;
    IFirebaseFaildListener iFirebaseFaildListener;
    private String cityName;


    @Override
    public void onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onDestroy();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);


        init();
        initViews(root);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return root;
    }

    private void initViews(View root) {
        ButterKnife.bind(this,root);




    }

    private void init() {





        iFirebaseFaildListener = this;
        iFirebaseUserinfoListener = this;

        locationRequest = LocationRequest.create();
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(10);
        locationRequest.setFastestInterval(10);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null) {
                    LatLng newposition = new LatLng(locationResult.getLastLocation().getLatitude(),
                            locationResult.getLastLocation().getLatitude());
                    Location location = new Location(locationResult.getLastLocation().getProvider());
                    location.setLatitude(locationResult.getLastLocation().getLatitude());
                    location.setLongitude(locationResult.getLastLocation().getLongitude());
                    onLocationChanged(location);
                    Toast.makeText(getContext(), "Location " + newposition, Toast.LENGTH_SHORT).show();

                } else {
                    Snackbar.make(mapFragment.getView(), "Location Null!", Snackbar.LENGTH_LONG).show();

                }
                if (firstTime) {
                    previousLocation = currentLocation = locationResult.getLastLocation();
                    firstTime = false;
                } else {
                    previousLocation = currentLocation;
                    currentLocation = locationResult.getLastLocation();
                }
                if (previousLocation.distanceTo(currentLocation) / 1000 <= LIMIT_RANGE)//not over range
                    loadAvaliableWorkers();
                else {
                    //Do nothing
                }

            }


        };

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        loadAvaliableWorkers();

    }

    private void loadAvaliableWorkers() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(getView(), getString(R.string.permission_require), Snackbar.LENGTH_SHORT).show();
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnFailureListener(e -> Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show())
                .addOnSuccessListener(location -> {
                    //load all workers in city
                    Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                    List<Address> addressList;
                    try {
                        addressList=geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                        cityName =addressList.get(0).getLocality();

                        //Query
                        DatabaseReference worker_location_ref= FirebaseDatabase.getInstance()
                                .getReference(Common.WORKERS_LOCATION_REFERENCE)
                                .child(cityName);
                        GeoFire gf = new GeoFire(worker_location_ref);
                        GeoQuery geoQuery =gf.queryAtLocation(new GeoLocation(location.getLatitude(),
                                location.getLongitude()),distance);
                        geoQuery.removeAllListeners();
                        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                            @Override
                            public void onKeyEntered(String key, GeoLocation location) {
                                Common.workersFound.add(new WorkerGeoModel(key,location));
                            }

                            @Override
                            public void onKeyExited(String key) {

                            }

                            @Override
                            public void onKeyMoved(String key, GeoLocation location) {

                            }

                            @Override
                            public void onGeoQueryReady() {
                                if (distance <= LIMIT_RANGE)
                                {
                                    distance++;
                                    loadAvaliableWorkers(); // continue search in new distance
                                }
                                else
                                {
                                    distance =1.0; //rest it
                                    addWorkerMarker();
                                }

                            }

                            @Override
                            public void onGeoQueryError(DatabaseError error) {
                                Snackbar.make(getView(),error.getMessage(),Snackbar.LENGTH_SHORT).show();

                            }
                        });

                        worker_location_ref.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                //Have new driver
                                GeoQueryModel geoQueryModel = dataSnapshot.getValue(GeoQueryModel.class);
                                GeoLocation geoLocation = new GeoLocation(geoQueryModel.getL().get(0),
                                        geoQueryModel.getL().get(1));
                                WorkerGeoModel workerGeoModel = new WorkerGeoModel(dataSnapshot.getKey(),
                                        geoLocation);
                                Location newWorkerLocation = new Location("");
                                newWorkerLocation.setLatitude(geoLocation.latitude);
                                newWorkerLocation.setLongitude(geoLocation.longitude);
                                float newDistance = location.distanceTo(newWorkerLocation)/1000; // in km
                                if(newDistance <= LIMIT_RANGE)
                                    findWorkerKey(workerGeoModel);// worker in range
                            }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }catch (IOException e){
                        e.printStackTrace();
                        Snackbar.make(getView(),e.getMessage(),Snackbar.LENGTH_SHORT).show();
                    }

        });
    }

    private void addWorkerMarker() {
        if(Common.workersFound.size()>0)
        {
            Observable.fromIterable(Common.workersFound)
                   .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(workerGeoModel -> {
                findWorkerKey(workerGeoModel);
        },throwable -> {
                Snackbar.make(getView(),throwable.getMessage(),Snackbar.LENGTH_SHORT).show();
        },()->{

        });

        }
        else {
            Snackbar.make(getView(),getString(R.string.workers_not_found),Snackbar.LENGTH_SHORT).show();
        }
    }

    private void findWorkerKey(WorkerGeoModel workerGeoModel) {
        FirebaseDatabase.getInstance()
                .getReference(Common.WORKER_INFO_REFERENCE)
                .child(workerGeoModel.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChildren())
                        {
                            workerGeoModel.setWorkerInfoMode(dataSnapshot.getValue(WorkerInfoMode.class));
                            iFirebaseUserinfoListener.onWorkerInfoLoadSuccess(workerGeoModel);
                        }
                        else
                        {
                            iFirebaseFaildListener.onFirebaseLoadFaild(getString(R.string.not_found_key)+workerGeoModel);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f));

        mMap.setOnMyLocationButtonClickListener(() -> {

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f));
            return true;
        });

        Toast.makeText(getContext(),"Location Changed",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        Dexter.withContext(getContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        } else {
                            mMap.setMyLocationEnabled(true);
                            mMap.getUiSettings().setMyLocationButtonEnabled(true);
                            View locationbutton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1"))
                                    .getParent()).findViewById(Integer.parseInt("2"));

                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationbutton.getLayoutParams();
                            ;
                            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                            params.setMargins(0, 0, 0, 250); // move view to see zoom control

                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(getContext(),"permission"+permissionDeniedResponse.getPermissionName()+""+"was denied",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();
        mMap.getUiSettings().setZoomControlsEnabled(true);

        try {

            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(),R.raw.user_maps_style));
            if(!success)
                Log.e("EDMT_ERROR","Style parsing error");

        }catch (Resources.NotFoundException e) {
            Log.e("EDMT_ERROR",e.getMessage());
        }


    }

    @Override
    public void onFirebaseLoadFaild(String message) {
        Snackbar.make(getView(),message,Snackbar.LENGTH_SHORT).show();
    }


    @Override
    public void onWorkerInfoLoadSuccess(WorkerGeoModel workerGeoModel) {
        if(!Common.markerList.containsKey(workerGeoModel.getKey()))
            Common.markerList.put(workerGeoModel.getKey(),
                    mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(workerGeoModel.getGeoLocation().latitude,
                            workerGeoModel.getGeoLocation().longitude))
                    .flat(true)
                    .title(Common.buildName(workerGeoModel.getWorkerInfoMode().getFirstName(),
                            workerGeoModel.getWorkerInfoMode().getLastName()))
                    .snippet(workerGeoModel.getWorkerInfoMode().getPhoneNumber())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_baseline_handyman_24))));
        if(!TextUtils.isEmpty(cityName))
        {
            DatabaseReference workerLocation=FirebaseDatabase.getInstance()
                    .getReference(Common.WORKERS_LOCATION_REFERENCE)
                    .child(cityName)
                    .child(workerGeoModel.getKey());
            workerLocation.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.hasChildren())
                    {
                        if (Common.markerList.get(workerGeoModel.getKey())!=null)
                            Common.markerList.get(workerGeoModel.getKey()).remove();
                        Common.markerList.remove(workerGeoModel.getKey());
                        workerLocation.removeEventListener(this);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Snackbar.make(getView(),databaseError.getMessage(),Snackbar.LENGTH_SHORT).show();
                }
            });
        }



    }
}
