package com.example.angel.carnavigation.Activities;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.angel.carnavigation.BuildConfig;
import com.example.angel.carnavigation.Communicators.BluetoothCommunicator;
import com.example.angel.carnavigation.GlobalVars.GlobalVars;
import com.example.angel.carnavigation.LocaleManager.LocaleHelper;
import com.example.angel.carnavigation.Model.LocationCar;
import com.example.angel.carnavigation.R;
import com.example.angel.carnavigation.SharedPreference.PreferenceManager;
import com.example.angel.carnavigation.Fragments.CameraFragment;
import com.example.angel.carnavigation.Fragments.MapFragment;
import com.example.angel.carnavigation.Fragments.ProfileFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LocationActivity extends AppCompatActivity {

    private static final String TAG = "MapActivity";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    private Boolean mLocationPermissionsGranted = false;
    private Boolean mCameraPermissionsGranted = false;

    private GlobalVars gVars;

    @BindView(R.id.navigationView)
    BottomNavigationView bottomNavigationView;

    @BindView(R.id.frame_container)
    FrameLayout fragmentContainer;

    private String mDeviceAddress;
    protected BluetoothCommunicator mBluetoothConnection;
    private BluetoothAdapter mBluetoothAdapter = null;

    public boolean bluetoothConnection = false;



    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    Mat imageMat=new Mat(1980 , 720, CvType.CV_8UC4);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_map);

        FirebaseMessaging.getInstance().subscribeToTopic("all");
        gVars = new GlobalVars().getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);

        ButterKnife.bind(this);
        //notificationDb = new NotificationDbHelper(this);

        bottomNavigationView.setOnNavigationItemSelectedListener(BottomNavListener);
        bottomNavigationView.setSelectedItemId(R.id.nav_bottom_map);

        OpenCVLoader.initDebug();

        /*TcpCommunicator writer = TcpCommunicator.getInstance();
        TcpCommunicator.addListener(this);
        writer.init(5100);*/

        if (findViewById(R.id.frame_container) != null){

            if (savedInstanceState !=  null) {
                //onItemSelected(bottomNavigationView.getSelectedItemId());
                return;
            }

            MapFragment mapFragment = new MapFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.frame_container, mapFragment).commit();
        }

        requestCameraPermission();

        initBluetooth();

       /* Handler mHandler = new Handler() ;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mBluetoothConnection.mConnected){
                    manageConnection();
                }
            }
        }, 10000);*/
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            changeLanguage();
        }

        return super.onOptionsItemSelected(item);
    }

    String MAP_FRAG_TAG = "mapFragTag";
    String CAM_FRAG_TAG = "camFragTag";
    String PROF_FRAG_TAG = "profFragTag";


    private Boolean onItemSelected(int id){
        switch (id) {
            case R.id.nav_bottom_map:
                if (getSupportFragmentManager().findFragmentByTag(MAP_FRAG_TAG) != null){
                    getSupportFragmentManager().beginTransaction().show(getSupportFragmentManager().findFragmentByTag(MAP_FRAG_TAG)).commit();
                } else{
                    getSupportFragmentManager().beginTransaction().add(R.id.frame_container, new MapFragment(), MAP_FRAG_TAG).commit();
                }
                if (getSupportFragmentManager().findFragmentByTag(CAM_FRAG_TAG) != null){
                    getSupportFragmentManager().beginTransaction().detach(getSupportFragmentManager().findFragmentByTag(CAM_FRAG_TAG)).commit();
                }
                if (getSupportFragmentManager().findFragmentByTag(PROF_FRAG_TAG) != null){
                    getSupportFragmentManager().beginTransaction().hide(getSupportFragmentManager().findFragmentByTag(PROF_FRAG_TAG)).commit();
                }
                return true;
            case R.id.nav_bottom_profile:
                if (getSupportFragmentManager().findFragmentByTag(PROF_FRAG_TAG) != null){
                    getSupportFragmentManager().beginTransaction().show(getSupportFragmentManager().findFragmentByTag(PROF_FRAG_TAG)).commit();
                } else{
                    getSupportFragmentManager().beginTransaction().add(R.id.frame_container, new ProfileFragment(), PROF_FRAG_TAG).commit();
                }
                if (getSupportFragmentManager().findFragmentByTag(MAP_FRAG_TAG) != null){
                    getSupportFragmentManager().beginTransaction().hide(getSupportFragmentManager().findFragmentByTag(MAP_FRAG_TAG)).commit();
                }
                if (getSupportFragmentManager().findFragmentByTag(CAM_FRAG_TAG) != null){
                    getSupportFragmentManager().beginTransaction().detach(getSupportFragmentManager().findFragmentByTag(CAM_FRAG_TAG)).commit();
                }
                return true;
            case R.id.nav_bottom_camera:
                if (getSupportFragmentManager().findFragmentByTag(CAM_FRAG_TAG) != null){
                    getSupportFragmentManager().beginTransaction().attach(getSupportFragmentManager().findFragmentByTag(CAM_FRAG_TAG)).commit();
                } else{
                    getSupportFragmentManager().beginTransaction().add(R.id.frame_container, new CameraFragment(), CAM_FRAG_TAG).commit();
                }
                if (getSupportFragmentManager().findFragmentByTag(MAP_FRAG_TAG) != null){
                    getSupportFragmentManager().beginTransaction().hide(getSupportFragmentManager().findFragmentByTag(MAP_FRAG_TAG)).commit();
                }
                if (getSupportFragmentManager().findFragmentByTag(PROF_FRAG_TAG) != null){
                    getSupportFragmentManager().beginTransaction().hide(getSupportFragmentManager().findFragmentByTag(PROF_FRAG_TAG)).commit();
                }
                return true;
        }
        return false;
    }

    BottomNavigationView.OnNavigationItemSelectedListener BottomNavListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            return onItemSelected(item.getItemId());
        }
    };



    public void signOut(){
        // Firebase sign out
        gVars.getmAuth().signOut();

        // Google sign out
        gVars.getSignInClient().signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //notificationDb.deleteAll();
                        //mMap.clear();
                        Intent intent = new Intent(LocationActivity.this, SignActivity.class);
                        startActivity(intent);
                    }
                });
    }

    public void changeLanguage(){
        final String languages[] = new String[] {"es", "en"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.Seleccionar_idioma);
        builder.setItems(languages, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Context context = LocaleHelper.setLocale(LocationActivity.this, languages[which]);
                recreate();
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    //initMap();
                }
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    public void requestCameraPermission() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)

                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mCameraPermissionsGranted = true;
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
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void initBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            finish();
        }
        else {
            if (mBluetoothAdapter.isEnabled()) {
                autoConnect();
            }
            else {
                //Ask to the user turn the bluetooth on
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon,1);
            }
        }
    }

    private void autoConnect(){
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!bluetoothConnection){
                    mDeviceAddress = "B8:27:EB:39:3F:EC";
                    //mDeviceAddress = "B8:27:EB:B9:42:FB";

                    // Create a connection to this device
                    mBluetoothConnection = new BluetoothCommunicator(LocationActivity.this, mDeviceAddress);
                    mBluetoothConnection.executeOnExecutor(mBluetoothConnection.THREAD_POOL_EXECUTOR);
                }
            }
        }, 0, 5000);
    }

    public void manageConnection(){
        LocationCar l = new LocationCar(PreferenceManager.getInstance().getUserId(),gVars.getSpeed(),gVars.getLatitude(), gVars.getLongitude(),gVars.getLatitudeOld(), gVars.getLongitudeOld());
        //LocationCar l = new LocationCar(PreferenceManager.getInstance().getUserId(),"50","43.436832", "-8.073287","43.437832", "-8.074287");
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(new Gson().toJson(l));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mBluetoothConnection.write(jsonObject.toString() + "$");

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                while (mBluetoothConnection.available() > 0) {
                    String message = mBluetoothConnection.read();

                    if (message.length() > 0) {
                        crearNotificacion(message);

                    }
                    mBluetoothConnection.disconnect();
                }
            }
        }, 3000);   //3

        // seconds
    }

    private NotificationManager mManager;

    private void crearNotificacion(String message){

        JSONObject obj = null;
        try {
            obj = new JSONObject(message);
            String direccion = obj.getString("direccion");
            String behind = obj.getString("behind");
            String distance = obj.getString("distance");
            float distance_f = Float.parseFloat(distance);
            String speed = obj.getString("speed");

            switch (direccion) {
                case "0":
                    if (behind.equals("True")) {
                        showAlertDialogWithAutoDismiss("POSIBLE COLISION TRASERA", "Un coche a " + String.format("%.2f", distance_f * 1000) + " metros se aproxima a " + speed + " k/h por detras");
                    }
                    break;
                case "1":
                    showAlertDialogWithAutoDismiss("POSIBLE COLISION DELATERA", "Un coche a " + String.format("%.2f", distance_f * 1000) + " metros se aproxima a " + speed + " k/h por delante");
                    break;
                case "2":
                    showAlertDialogWithAutoDismiss("APROXIMACION A INTERSECCION", "Un coche a " + String.format("%.2f", distance_f * 1000) + " metros se aproxima a " + speed + " k/h en la interseccion");
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void mostrarNotificacion(String titulo, String body){

            Intent intent = new Intent(this, LocationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent =  PendingIntent.getActivity(this,0,intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri soundUri  = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,"default")
                .setSmallIcon(R.mipmap.ic_stat)
                .setContentTitle(titulo)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(titulo)
                        .bigText(body))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        getManager().notify(0,notificationBuilder.build());



        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("d/MM/yyyy 'at' h:mm a");
        String date = format.format(calendar.getTime());
    }

    private NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public void showAlertDialogWithAutoDismiss(String titulo, String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LocationActivity.this, R.style.AlertDialogStyle);
        builder.setTitle(titulo)
                .setMessage(body)
                .setIcon(R.mipmap.ic_warning_black_24dp)
                .setCancelable(false).setCancelable(false);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (alertDialog.isShowing()){
                    alertDialog.dismiss();
                }
            }
        }, 1000); //change 5000 with a specific time you want
    }
}
