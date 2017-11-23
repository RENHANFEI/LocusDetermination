package com.example.renhanfei.mylocationmap;

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
import android.os.Trace;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;

public class TraceRecordActivity extends Activity implements
        SensorEventListener,
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = TraceRecordActivity.class.getSimpleName();
    private SensorManager mSensorManager;
    private Sensor mSensor;

    AccelerometerDB db;

    private double mX, mY, aX, aY;
    private long startTime = 0;
    private int recordInterval = 500;
    private int lastUpdateTime = 0;
    private boolean isRecording = false;
    private long recordID = 0;
    private String goOrientation = "yplus";
    private String Lat = "", Lng = "";

    private double[] acc = new double[999];
    private int accCursor = 0;
    final private double standardMovement = 20;
    double otherError = 0;

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    String geoAddress = "I am Address.";

    Calendar mCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trace_record);

        // sensor
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);// TYPE_GRAVITY
        if (null == mSensorManager) {
            Log.d(TAG, "device not support SensorManager");
        }
        mSensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_NORMAL);// SENSOR_DELAY_GAME

        db = new AccelerometerDB(this);

        // map
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
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

            TextView accView = (TextView) findViewById(R.id.accView);
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

                TextView recordTimeView = (TextView) findViewById(R.id.recordTimeView);
                recordTimeView.setText(String.valueOf(recordTime) + " s");

                // insert item
                if (recordStamp > lastUpdateTime) {

                    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd HH:mm:ss");
                    Date curTime = new Date(System.currentTimeMillis()); // get current time
                    String time_stamp = formatter.format(curTime);

                    long id = insertItem(time_stamp, String.valueOf(recordTime), Lat, Lng,
                            String.valueOf(aX), String.valueOf(aY), recordID);

                    if (id < 0)
                        Toast.makeText(this, "Recording fail.", Toast.LENGTH_SHORT);
                    else
                        Toast.makeText(this, "Recording items...", Toast.LENGTH_SHORT);

                    lastUpdateTime += recordInterval;

                    // update acc
                    if (goOrientation.equalsIgnoreCase("xplus")) {
                        acc[accCursor++] = aX;
                        otherError += aY;
                    } else if (goOrientation.equalsIgnoreCase("xminus")) {
                        acc[accCursor++] = -aX;
                        otherError += aY;
                    } else if (goOrientation.equalsIgnoreCase("yplus")) {
                        acc[accCursor++] = aY;
                        otherError += aX;
                    } else { // y minus
                        acc[accCursor++] = -aY;
                        otherError += aX;
                    }

                }
            }

        }

    }

    public void onClick_showRecords(View v) {
        Intent myIntent = new Intent(this, AccelerometerRecordsActivity.class);
        startActivity(myIntent);
    }

    public void onClick_startRecord(View v) {

        // set notice info
        TextView recordConditionView = (TextView) findViewById(R.id.recordConditionView);
        String startInfo = "Recording ... go " + goOrientation + " ...";
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

        recordID = addRecord(time_stamp, geoAddress, goOrientation);

        // make STOP clickable
        Button stopButton = (Button) findViewById(R.id.stop_btn);
        stopButton.setClickable(true);

        // make START unclickable
        Button startButton = (Button) findViewById(R.id.start_btn);
        startButton.setClickable(false);
    }

    public void onClick_stopRecord(View v) {

        // set notice info
        TextView recordConditionView = (TextView) findViewById(R.id.recordConditionView);
        String stopInfo = "Stopped";
        recordConditionView.setText(stopInfo);

        // set record flag
        isRecording = false;

        // update database
        // get time
        mCalendar = Calendar.getInstance();
        long endTime = mCalendar.getTimeInMillis();
        double recordTime = (endTime - startTime) / 100l / 10.0; // in second

        // calculate error
        double driftError = measuredAcc() - trueAcc(recordTime);
        otherError /= accCursor;

        // updateRecord(recordID, String.valueOf(recordTime));
        updateRecord(recordID, String.valueOf(recordTime), String.valueOf(driftError),
                String.valueOf(otherError));

        // make back
        recordID = 0;
        startTime = 0;
        Arrays.fill(acc, 0);
        accCursor = 0;
        otherError = 0;
        Button stopButton = (Button) findViewById(R.id.stop_btn);
        stopButton.setClickable(false);
        Button startButton = (Button) findViewById(R.id.start_btn);
        startButton.setClickable(true);

    }

    private double measuredAcc() {

        double mean = 0;

        for (int i = 0; i < accCursor; i++) {
            mean += acc[i];
        }

        mean /= accCursor;

        return mean;
    }

    private double trueAcc(double time) {
        return 2 * standardMovement / time / time;
    }

    public void xPlusPressed(View v) {
        goOrientation = "xplus";
    }

    public void xMinusPressed(View v) {
        goOrientation = "xminus";
    }

    public void yPlusPressed(View v) {
        goOrientation = "yplus";
    }

    public void yMinusPressed(View v) {
        goOrientation = "yminus";
    }

    // database operation

    public long addRecord(String time_stamp, String record_address, String record_orientation) {

        db.open();

        long id = db.insertRecord(time_stamp, record_address, record_orientation);

        if (id > 0) {
            Toast.makeText(this, "Start recording.", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(this, "Recording failed.", Toast.LENGTH_SHORT).show();

        db.close();


        return id;

    }

    public void updateRecord(long id, String record_time, String record_error, String other_error) {

        db.open();

        db.updateRecord(id, record_time, record_error, other_error);

        db.close();
    }

    public long insertItem(String time_stamp, String record_time, String record_lat,
                           String record_lng, String record_x, String record_y, long record_id) {
        db.open();

        long id = db.insertItem(time_stamp, record_time, record_lat, record_lng,
                record_x, record_y, record_id);

        db.close();
        return id;
    }


}
