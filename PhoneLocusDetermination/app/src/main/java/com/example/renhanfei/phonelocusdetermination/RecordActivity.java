package com.example.renhanfei.phonelocusdetermination;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.vision.text.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordActivity extends Activity implements
        SensorEventListener,
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = RecordActivity.class.getSimpleName();
    private SensorManager mSensorManager;
    private Sensor mSensor;

    LocusDB db;

    private double mX, mY, aX, aY, preAX = 0, preAY = 0, preVx = 0, preVy = 0;
    private long startTime = 0;
    private int recordInterval = 200;
    private int lastUpdateTime = 0;
    private boolean isRecording = false;
    private long recordID = 0;
    private String Lat = "", Lng = "";

    // Got from the first task
    private double centerLat = 1.29866;
    private double centerLng = 103.7784;

    // for kalman filter
    /***
     * Give up meanwhile filtering because of Laggggggggg
     */
//    KalmanFilter kalmanFilter = new KalmanFilter();
//    KalmanStatus prePosition = new KalmanStatus();
//    KalmanStatus curPosition = new KalmanStatus();

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    String geoAddress = "I am Address.";

    Calendar mCalendar;
    private double vy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);// TYPE_GRAVITY
        if (null == mSensorManager) {
            Log.d(TAG, "device not support SensorManager");
        }
        mSensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_NORMAL);// SENSOR_DELAY_GAME

        db = new LocusDB(this);

        // map
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(200);
        mLocationRequest.setFastestInterval(200);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    // gps
    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public void onPause() {
        LocationServices.FusedLocationApi.removeLocationUpdates
                (mGoogleApiClient, this);
        super.onPause();
    }

    public void onResume() {
        if (mGoogleApiClient.isConnected()) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                PendingResult<Status> pendingResult =
                        LocationServices.FusedLocationApi.requestLocationUpdates(
                                mGoogleApiClient, mLocationRequest, this);
            }
        }
        super.onResume();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            PendingResult<Status> pendingResult =
                    LocationServices.FusedLocationApi.requestLocationUpdates(
                            mGoogleApiClient, mLocationRequest, this);
        }
    }

    Location mCurrentLocation;

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {

        mCurrentLocation = location;
        Geocoder geocoder;
        List<Address> addresses;

        if (mCurrentLocation != null) {

            Lat = String.valueOf(mCurrentLocation.getLatitude());
            Lng = String.valueOf(mCurrentLocation.getLongitude());

            // get the location with accuracy and also provider, using
            mCurrentLocation.getAccuracy();
            mCurrentLocation.getProvider();

            geocoder = new Geocoder(this, Locale.getDefault());
            try {
                addresses = geocoder.getFromLocation(Double.valueOf(Lat), Double.valueOf(Lng), 1);
                int numAddresses = addresses.size();
                for (int a = 0; a < numAddresses; a++) {
                    int maxAddressLineIndex = addresses.get(a).getMaxAddressLineIndex();
                    for (int b = 0; b < maxAddressLineIndex; b++) {
                        String address = addresses.get(a).getAddressLine(b);
                        String city = addresses.get(a).getLocality();
                        String state = addresses.get(a).getAdminArea();
                        String country = addresses.get(a).getCountryName();
                        String postalCode = addresses.get(a).getPostalCode();
                        String knownName = addresses.get(a).getFeatureName();
                        // Only if available else return NULL
                        // if (knownName != null) geoInfoCtx += knownName + " ";
                        geoAddress = "";
                        if (address != null) geoAddress += address + " ";
                        if (city != null) geoAddress += city + " ";
                        if (state != null) geoAddress += state + " ";
                        if (country != null) geoAddress += country + " ";
                        if (postalCode != null) geoAddress += postalCode;
                    }
                }

            } catch (IOException e) {
                Log.e("Getting Address: ", "Error : ", e);
            }

            // geo info
            String geoData = Lat + "  " + Lng + "\n\n" + geoAddress;
            TextView geoDataView = (TextView) findViewById(R.id.gps_data);
            geoDataView.setText(geoData);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == null) {
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            double x = event.values[0];
            double y = event.values[1];

            aX = mX - x;
            aY = mY - y;

            TextView accView = (TextView) findViewById(R.id.acc_data);
            String accInfo = "X: " + String.format("%.2f", aX) + "\n\n" +
                    "Y: " + String.format("%.2f", aY);
            accView.setText(accInfo);

            mX = x;
            mY = y;

            // if start recording, refresh time info
            if (isRecording) {
                mCalendar = Calendar.getInstance();
                long nowTime = mCalendar.getTimeInMillis();

                long recordStamp = nowTime - startTime;
                double recordTime = recordStamp / 100l / 10.0;

                String debugInfo = "Lat: " + Lat + "  Lng: " + Lng + "\n" + geoAddress;

                // record time
                TextView recordTimeView = (TextView) findViewById(R.id.record_time);
                recordTimeView.setText(String.valueOf(recordTime) + " s");


                // insert item
                if (recordStamp > lastUpdateTime) {

                    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd HH:mm:ss");
                    Date curTime = new Date(System.currentTimeMillis()); // get current time
                    String timeStamp = formatter.format(curTime);

                    double[] coordinates = {Double.valueOf(Lat), Double.valueOf(Lng)};
                    double[] position = convertCoordinates(coordinates);

                    double positionX = position[0];
                    double positionY = position[1];
                    double filteredX = positionX;
                    double filteredY = positionY;

                    double Vx = preVx + (aX + preAX) * recordInterval / 1000 / 2;
                    double vy = preVy + (aY + preAY) * recordInterval / 1000 / 2;

                    // insert into database
                    long id = addItem(timeStamp, String.valueOf(recordTime), Lat, Lng,
                            String.valueOf(aX), String.valueOf(aY), String.valueOf(positionX),
                            String.valueOf(positionY), String.valueOf(filteredX),
                            String.valueOf(filteredY), recordID,
                            String.valueOf(Vx), String.valueOf(vy));

                    if (id < 0)
                        Toast.makeText(this, "Recording fail.", Toast.LENGTH_SHORT);
                    else
                        Toast.makeText(this, "Recording items...", Toast.LENGTH_SHORT);

                    // update status
//                    cloneStatus(curPosition, prePosition);
                    preAX = aX;
                    preAY = aY;
                    preVx = Vx;
                    preVy = vy;

                    // update time
                    lastUpdateTime += recordInterval;

                }
            }

        }

    }

    public void cloneStatus(KalmanStatus source, KalmanStatus target) {
//        public double[] advanceEstimation;
//        public double[] estimation;
//        public double[][] advanceError; // P-
//        public double[][] error; // P
//        public double[] measurement;
//        public double[][] kalmanGain;
//        public double time; // from start unit:s

        target.estimation = source.estimation.clone();

        for (int i = 0; i < 4; i++) {
            target.error[i] = source.error[i].clone();
        }
    }

    // button functions
    public void onClick_startRecord(View v) {

        // set notice info
        TextView recordConditionView = (TextView) findViewById(R.id.record_status);
        String startInfo = "Recording ...\n";
        recordConditionView.setText(startInfo);

        // set start time
        mCalendar = Calendar.getInstance();
        startTime = mCalendar.getTimeInMillis();

        // set record flag
        isRecording = true;

        // insert record to db
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd HH:mm:ss");
        Date curTime = new Date(System.currentTimeMillis()); // get current time
        String time_stamp = formatter.format(curTime);

        recordID = addRecord(time_stamp, geoAddress);

        // make STOP clickable
        Button stopButton = (Button) findViewById(R.id.stop_btn);
        stopButton.setClickable(true);
        // make START unclickable
        Button startButton = (Button) findViewById(R.id.start_btn);
        startButton.setClickable(false);

    }

    public void onClick_stopRecord(View v) {

        // set notice info
        TextView recordConditionView = (TextView) findViewById(R.id.record_status);
        String stopInfo = "Stopped\n";
        recordConditionView.setText(stopInfo);

        // set record flag
        isRecording = false;

        // update database
        mCalendar = Calendar.getInstance();
        long endTime = mCalendar.getTimeInMillis();
        double recordTime = (endTime - startTime) / 100l / 10.0; // in second

        //updateRecord(recordID, String.valueOf(recordTime));
        updateRecord(recordID, String.valueOf(recordTime));
        Toast.makeText(this, "Stop recording.", Toast.LENGTH_SHORT).show();

        // make back
        recordID = 0;
        startTime = 0;
        Button stopButton = (Button) findViewById(R.id.stop_btn);
        stopButton.setClickable(false);
        Button startButton = (Button) findViewById(R.id.start_btn);
        startButton.setClickable(true);

    }

    public void onClick_showRecords(View v) {
        Intent myIntent = new Intent(this, HistoryActivity.class);
        startActivity(myIntent);
    }


    /*
    Convert latlng to xy
     */
    public double[] convertCoordinates(double[] geoCoordinates) {

        double kilometerPerLat = 111;
        double kilometerPerLng = 111 * Math.cos(centerLat * Math.PI / 180);

        double[] rectCoordinates = new double[2]; // 0.5m/unit
        double latDiff, lngDiff;

        latDiff = (centerLat - geoCoordinates[0]) * kilometerPerLat;
        lngDiff = (geoCoordinates[1] - centerLng) * kilometerPerLng;

        rectCoordinates[0] = latDiff * 1000;
        rectCoordinates[1] = lngDiff * 1000;

        return rectCoordinates;
    }


    /*
    Database operation
     */
    public long addRecord(String time_stamp, String record_address) {

        db.open();

        long id = db.insertRecord(time_stamp, record_address);

        if (id > 0) {
            Toast.makeText(this, "Start recording.", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(this, "Recording failed.", Toast.LENGTH_SHORT).show();

        db.close();


        return id;

    }

    public void updateRecord(long id, String record_time) {

        db.open();

        db.updateRecord(id, record_time);

        db.close();
    }

    public long addItem(String time_stamp, String record_time, String record_lat,
                        String record_lng, String record_x, String record_y, String position_x,
                        String position_y, String filtered_x, String filtered_y,
                        long record_id, String vx, String vy) {
        db.open();

        long id = db.insertItem(time_stamp, record_time, record_lat, record_lng,
                record_x, record_y, position_x, position_y,
                filtered_x, filtered_y, record_id, vx, vy);

        db.close();
        return id;
    }

}
