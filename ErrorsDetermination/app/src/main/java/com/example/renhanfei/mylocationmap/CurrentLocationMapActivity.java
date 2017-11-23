package com.example.renhanfei.mylocationmap;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;

public class CurrentLocationMapActivity extends FragmentActivity implements
        OnMapReadyCallback,
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;

    LocationRecordsDB db;

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Marker mCurrentMarker;

    String geoAddress;
    String lat, lng;

    private String position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_location_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        position = "east";
        geoAddress = "";
        lat = "31.3";
        lng = "120.6";
        mCurrentMarker = null;

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        db = new LocationRecordsDB(this);
    }

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

        String mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        String geoInfoCtx = "Geographical Infomation";
        String geoCoordinates;

        if (mCurrentLocation != null) {

            lat = String.valueOf(mCurrentLocation.getLatitude());
            lng = String.valueOf(mCurrentLocation.getLongitude());

            // get the location with accuracy and also provider, using
            mCurrentLocation.getAccuracy();
            mCurrentLocation.getProvider();

            geoCoordinates = "Latitude: " + lat + " Logitude: " + lng;

            geocoder = new Geocoder(this, Locale.getDefault());
            try {
                addresses = geocoder.getFromLocation(Double.valueOf(lat), Double.valueOf(lng), 1);
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

                geoInfoCtx = geoCoordinates + "\n" + geoAddress;

            } catch (IOException e) {
                Log.e("Getting Address: ", "Error : ", e);
            }
        }


        TextView geoInfo = (TextView) findViewById(R.id.geoInfo);
        geoInfo.setText(geoInfoCtx);

        //Place current location marker
        if (mCurrentMarker != null) {
            mCurrentMarker.remove();
        }

        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mCurrentMarker = mMap.addMarker(new MarkerOptions().
                position(currentLocation).title("Marker in CurrentLocation"));

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    // Records

    public void eastPressed(View view) {
        position = "east";
        refreshPosition();
    }

    public void southPressed(View view) {
        position = "south";
        refreshPosition();
    }

    public void westPressed(View view) {
        position = "west";
        refreshPosition();
    }

    public void northPressed(View view) {
        position = "north";
        refreshPosition();
    }

    public void refreshPosition() {
        char[] recordPosition = position.toCharArray();
        recordPosition[0] -= 32;
        TextView positionInfo = (TextView) findViewById(R.id.positionText);
        positionInfo.setText(String.valueOf(recordPosition));
    }

    public void onClick_Record(View view) {

        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd HH:mm:ss");

        Date curTime = new Date(System.currentTimeMillis()); // get current time
        String record_time = formatter.format(curTime);

        addRecord(record_time, position, lat, lng, geoAddress);

    }

    public void onClick_ShowRecords(View view) {
        Intent myIntent = new Intent(this, RecordsActivity.class);
        startActivity(myIntent);
    }


    // Database Operation

    public void addRecord(String record_time, String record_position, String record_latitude,
                          String record_longitude, String record_address) {


        db.open();

        long id = db.insertLocation(record_time, record_position, record_latitude,
                record_longitude, record_address);


        if (id > 0) {
            Toast.makeText(this, "Add successful.", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(this, "Add failed.", Toast.LENGTH_SHORT).show();

        db.close();


    }


}