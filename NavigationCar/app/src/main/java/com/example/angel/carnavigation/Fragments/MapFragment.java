package com.example.angel.carnavigation.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.angel.carnavigation.BuildConfig;
import com.example.angel.carnavigation.Communicators.TcpCommunicator;
import com.example.angel.carnavigation.GlobalVars.GlobalVars;
import com.example.angel.carnavigation.Listeners.ListenerRequest;
import com.example.angel.carnavigation.Listeners.OnTCPMessageRecievedListener;
import com.example.angel.carnavigation.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class MapFragment extends Fragment implements OnMapReadyCallback, ListenerRequest, OnTCPMessageRecievedListener {

    private static final String TAG = "MapActivity";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 20f;

    private Boolean mLocationPermissionsGranted = false;

    private GoogleMap mMap;
    private LatLng destination;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 2000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 2000;
    private static final int REQUEST_CHECK_SETTINGS = 100;

    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    public static Double latitude = 43.493729, longitude = -8.224421, latitude_old = 0.0, longitude_old = 0.0;
    public static float speed;
    //private Location mOldLocation = new Location("dummyprovider");

    LocationManager locationManager;

    private GlobalVars gVars;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        gVars = new GlobalVars().getInstance();

        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
        locationManager = (LocationManager)getContext().getSystemService(getContext().LOCATION_SERVICE);
        //init();

        gVars.setLatitude(Double.toString(latitude));
        gVars.setLongitude(Double.toString(longitude));
        gVars.setLatitudeOld(Double.toString(latitude_old));
        gVars.setLongitudeOld(Double.toString(longitude_old));

        TcpCommunicator writer = TcpCommunicator.getInstance();
        TcpCommunicator.addListener(this);
        writer.init(5200);

        return rootView;
    }


    @Override
    public void requestCompleted() {

    }

    @Override
    public void requestError(int statusCode) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        requestPermission();

        if(mLocationPermissionsGranted && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
            init();
            updateMapView();

/*            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }*/
            mMap.setMyLocationEnabled(true);
            mMap.setLocationSource(new MockLocationSource());
            mMap.setTrafficEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            if(!gVars.getLatitude_dst().equals(" ")){
                LatLng point = new LatLng(Double.valueOf(gVars.getLatitude_dst()), Double.valueOf(gVars.getLongitude_dst()));
                mMap.addMarker(new MarkerOptions().position(point));
            }

        }else{
            new AlertDialog.Builder(getContext())
                    .setMessage("Please turn on Location to continue")
                    .setPositiveButton("Open Location Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    init();

                                    updateMapView();
                                    mMap.setLocationSource(new MockLocationSource());
                                    mMap.setMyLocationEnabled(true);
                                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                                    if(!gVars.getLatitude_dst().equals(" ")){
                                        LatLng point = new LatLng(Double.valueOf(gVars.getLatitude_dst()), Double.valueOf(gVars.getLongitude_dst()));
                                        mMap.addMarker(new MarkerOptions().position(point));
                                    }
                                }
                            }, 5000);
                        }
                    }).
                    setNegativeButton("Cancel",null)
                    .show();
        }

/*        if (mLocationPermissionsGranted) {
            updateMapView();

            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            if(!gVars.getLatitude_dst().equals(" ")){
                LatLng point = new LatLng(Double.valueOf(gVars.getLatitude_dst()), Double.valueOf(gVars.getLongitude_dst()));
                mMap.addMarker(new MarkerOptions().position(point));
            }

        }*/
    }

    private void updateMapView() {
        Log.d(TAG, "updateMapView: updating map to current location");
        try {
            if (mLocationPermissionsGranted) {

                final Task location = mFusedLocationClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            mCurrentLocation = (Location) task.getResult();

                            if (latitude != null && longitude != null) {
                                moveCamera(new LatLng(latitude, longitude),
                                        DEFAULT_ZOOM);
                                /*gVars.setLatitude(Double.toString(latitude));
                                gVars.setLongitude(Double.toString(longitude));
                                gVars.setLatitudeOld(Double.toString(latitude_old));
                                gVars.setLongitudeOld(Double.toString(longitude_old));*/

                                //updateLocationUI();

                                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                                    @Override
                                    public void onMapClick(LatLng point) {
                                        destination = point;
                                        LatLng origin = new LatLng(Double.valueOf(gVars.getLatitude()), Double.valueOf(gVars.getLongitude()));
                                        gVars.setLatitude_dst(Double.toString(point.latitude));
                                        gVars.setLongitude_dst(Double.toString(point.longitude));
                                        mMap.clear();
                                        mMap.addMarker(new MarkerOptions().position(point));
                                        //drawRoute(origin);


                                    }
                                });
                            } else {
                                //updateMapView();
                            }
                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            //Toast.makeText(LocationActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "updateMapView: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(latLng,zoom);
        mMap.animateCamera(cu);

    }


    private void init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        mSettingsClient = LocationServices.getSettingsClient(getContext());

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (mCurrentLocation != null){
                    //mOldLocation = mCurrentLocation;
                    mCurrentLocation = locationResult.getLastLocation();
                    updateLocationUI();
                }
            }
        };

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();

        startLocationUpdates();
        //requestPermission();
    }

    /**
     * Actualizacion de la interfaz de usuario
     * envío de datos al servidor
     */
    private void updateLocationUI() {
        //if (mCurrentLocation != null && (mCurrentLocation.getLatitude() != mOldLocation.getLatitude() || mCurrentLocation.getLongitude() != mOldLocation.getLongitude())) {
        if (mCurrentLocation != null && (latitude != latitude_old || longitude != longitude_old)) {
            speed = mCurrentLocation.getSpeed();
            String real_speed = String.format("%.0f", speed * 3.6);
            //String speed = String.format("%.0f", 10 * 3.6);

            if(getActivity().findViewById(R.id.button2) != null){
                Button speed_real = getActivity().findViewById(R.id.button2);
                speed_real.setText(real_speed);
            }

            if(getActivity().findViewById(R.id.button1) != null){
                Button speed_limit = getActivity().findViewById(R.id.button1);
                //speed_limit.setText(real_speed);
                Button speed_real = getActivity().findViewById(R.id.button2);
                CharSequence limit = speed_limit.getText();
                if (limit != "" && real_speed != "") {
                    if (Integer.valueOf(real_speed) > Integer.valueOf(limit.toString())) {
                        speed_real.setTextColor(getResources().getColor(R.color.red));
                    } else {
                        speed_real.setTextColor(getResources().getColor(R.color.colorAccent));
                    }
                }
            }

            String latitude_str = String.valueOf(latitude);
            String longitude_str = String.valueOf(longitude);

            gVars.setSpeed(real_speed);
            if(Integer.parseInt(gVars.getSpeed()) > 0){
                //ControllerVolley.updateLocation(this,gVars.getSpeed(),latitude_str,longitude_str);
            }
            LatLng origin = new LatLng(latitude,longitude);
            if(destination != null) {
                //drawRoute(origin);
            }
            updateMapView();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * Verificacion de la configuración de la ubicación
     */
    private void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateLocationUI();
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                        }

                        updateLocationUI();
                    }
                });
    }

    public void stopLocationUpdates() {
        if(mLocationCallback != null) {
            mFusedLocationClient
                    .removeLocationUpdates(mLocationCallback)
                    .addOnCompleteListener((Executor) this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });
        }
    }

    public void requestPermission() {
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mLocationPermissionsGranted = true;
                        //startLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
                                                                   PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        //startLocationUpdates();
        if (mLocationPermissionsGranted) {
            //updateLocationUI();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //stopLocationUpdates();
    }

    private Handler handler = new Handler();

    @Override
    public void onTCPMessageRecieved(String message) {
// TODO Auto-generated method stub
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    Log.d("TCP", message);
                    String[] location = message.split("   ");
                    NumberFormat formatter = new DecimalFormat("#0.000000");
                    if (location.length > 1) {
                        if(!String.format(Locale.ENGLISH, "%.5f", latitude).equals(String.format(Locale.ENGLISH, "%.5f", Double.parseDouble(location[1]))) && !String.format(Locale.ENGLISH, "%.5f", longitude).equals(String.format(Locale.ENGLISH, "%.5f", Double.parseDouble(location[2])))) {
                            latitude_old = latitude;
                            longitude_old = longitude;
                            latitude = Double.parseDouble(location[1]);
                            longitude = Double.parseDouble(location[2]);
                            gVars.setLatitude(String.format(Locale.ENGLISH, "%.6f", latitude));
                            gVars.setLongitude(String.format(Locale.ENGLISH, "%.6f", longitude));
                            gVars.setLatitudeOld(String.format(Locale.ENGLISH, "%.6f", latitude_old));
                            gVars.setLongitudeOld(String.format(Locale.ENGLISH, "%.6f", longitude_old));
                            updateMapView();
                        }
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        },3000);
    }


    public static class MockLocationSource implements LocationSource {

        private static final float ACCURACY = 1; // Meters
        private static final int MAX_SPEED = 10; // m/s
        private final LatLng CENTER = new LatLng(43.490729,-8.224421);
        //private static final double DELTA_LAT = 0.0005;
        //private static final double DELTA_LON = 0.0005;

        private final long UPDATE_PERIOD = TimeUnit.SECONDS.toMillis(2);

        private final Handler handler = new Handler();
        private LatLng lastCoordinate = CENTER;
        private OnLocationChangedListener listener;

        private void scheduleNewFix() {
            handler.postDelayed(updateLocationRunnable, UPDATE_PERIOD);
        }

        private final Runnable updateLocationRunnable = new Runnable() {

            @Override
            public void run() {
                Location randomLocation = generateRandomLocation();
                listener.onLocationChanged(randomLocation);
                scheduleNewFix();
            }
        };

        public Location generateRandomLocation() {

            Location location = new Location(getClass().getSimpleName());
            location.setTime(System.currentTimeMillis());
            location.setAccuracy(ACCURACY);
            //location.setBearing(randomizer.nextInt(360));
            location.setSpeed(MapFragment.speed);
            location.setLatitude(MapFragment.latitude);
            location.setLongitude(MapFragment.longitude);

            lastCoordinate = new LatLng(location.getLatitude(), location.getLongitude());

            return location;
        }

        @Override
        public void activate(OnLocationChangedListener locationChangedListener) {
            listener = locationChangedListener;
            scheduleNewFix();
        }

        @Override
        public void deactivate() {
            handler.removeCallbacks(updateLocationRunnable);
        }

    }
}
