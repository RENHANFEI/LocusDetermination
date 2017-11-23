package com.example.renhanfei.mylocationmap;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapPlotActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationRecordsDB db;

    // records
    ArrayList<String[]> myRecords;
    private double[] originCoordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_plot);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        db = new LocationRecordsDB(this);
        myRecords = getAllRecords();
        originCoordinates = getOriginCoordinates(myRecords);
    }

    public void onClick_goBack(View v) {
        finish();
    }

    public void onClick_switchDiagram(View v) {
        Intent myIntent = new Intent(this, LocationPlotActivity.class);
        startActivity(myIntent);
    }

    public ArrayList<String[]> getAllRecords() {

        db.open();

        Cursor c = db.getAllRecords();

        ArrayList<String[]> records = new ArrayList<String[]>();

        if (c.moveToFirst()) {
            do {
                String[] record = {c.getString(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5), "\n"};
                // 0:id 1:Time 2:Position 3:latitude 4:longitude 5:address
                records.add(record);
            } while (c.moveToNext());
        }
        db.close();
        return records;
    }

    private double[] getOriginCoordinates(ArrayList<String[]> myRecords) {

        double lat = 0;
        double lng = 0;

        double[] center = new double[2];

        for (int i = 0; i < myRecords.size(); i++) {
            String[] myRecord = myRecords.get(i);
            lat += Double.valueOf(myRecord[3]);
            lng += Double.valueOf(myRecord[4]);
        }

        center[0] = lat / myRecords.size();
        center[1] = lng / myRecords.size();

        return center;

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        LatLng geoCoordinates;

        for (int i = 0; i < myRecords.size(); i++) {

            String[] myRecord = myRecords.get(i);
            geoCoordinates = new LatLng(Double.valueOf(myRecord[3]), Double.valueOf(myRecord[4]));

            if (myRecord[2].equalsIgnoreCase("east")) {
                mMap.addMarker(new MarkerOptions().position(geoCoordinates)
                        .title("East").snippet("East")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.east_pin)));
            } else if (myRecord[2].equalsIgnoreCase("south")) {
                mMap.addMarker(new MarkerOptions().position(geoCoordinates)
                        .title("South").snippet("South")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.south_pin)));
            } else if (myRecord[2].equalsIgnoreCase("west")) {
                mMap.addMarker(new MarkerOptions().position(geoCoordinates)
                        .title("West").snippet("West")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.west_pin)));
            } else { // north
                mMap.addMarker(new MarkerOptions().position(geoCoordinates)
                        .title("North").snippet("North")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.north_pin)));
            }
        }

        LatLng origin = new LatLng(originCoordinates[0], originCoordinates[1]);

        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 18));
    }
}
