package com.example.renhanfei.mylocationmap;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class SpecifiedLocationMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specified_location_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        LatLng NUS = new LatLng(1.2956, 103.776);
        LatLng OrchardRd = new LatLng(1.3051, 103.831);

        Marker markerNUS = mMap.addMarker(new
                MarkerOptions().position(NUS).title("Marker in NUS").snippet("This is NUS"));


        Marker markerOrchard = mMap.addMarker(new MarkerOptions().position(OrchardRd)
                .title("Marker in Orchard Road").snippet("This is Orchard Road")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_pin)));

        // move camera to NUS with a zoom of 20
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(NUS, 20));

        // zoom in, animating the camera
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
    }
}
