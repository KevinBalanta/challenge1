package com.example.challenge1.activities;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.challenge1.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener , LocationListener {

    private GoogleMap mMap;
    private boolean permission = false;
    private List<Address> adress;
    private Geocoder geoCoder;

    private LocationManager locationManager;
    private Location currentLocation;
    private Marker myMarker;
    private List<Marker> markers;

    private TextView infoTv;
    private Button okBtn;
    private EditText markerNameEt;
    private double latU;
    private double longiU;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 11);
        infoTv = findViewById(R.id.information_tv);
        markerNameEt = findViewById(R.id.marker_et);
        okBtn = findViewById(R.id.ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // se agrega el marker en mapa y array y se nombra, con su snippet ubication
                if(!markerNameEt.getText().toString().isEmpty() && markerNameEt.getText().toString() != null ){
                    Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latU, longiU)).title(markerNameEt.getText().toString()).snippet(calculateDistance(latU, longiU) +"\n"+ getMarkerAdress(latU, longiU)));
                    markers.add(marker);
                    disableMarkerEdit();
                    calculateDistances();
                }else{
                    showToast();

                }


            }
        });


        if(savedInstanceState == null){
            markers = new ArrayList<>();
        }else{
            paintMarkers();
        }







    }

    private void paintMarkers() {
        for(Marker marker : markers){
            mMap.addMarker(new MarkerOptions().position(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude)).title(marker.getTitle().toString()).snippet(marker.getSnippet()));
        }
        mMap.addMarker(new MarkerOptions().position(new LatLng(myMarker.getPosition().latitude, myMarker.getPosition().longitude)).title(myMarker.getTitle().toString()).snippet(myMarker.getSnippet()));
    }

    private void showToast() {
        Toast.makeText(this, "Please enter the marker´s name", Toast.LENGTH_LONG).show();
    }


    public void permissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 11);

            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        permission = true;
    }
/*
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        System.out.println("entró");
        if(requestCode == 11){
            System.out.println(grantResults[1]);
            if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("entró 2");
                permission = true;
            }
        }
    }*/

    //si está a menos de 10 mts, poner bienvenido a xxxx
    private void calculateDistances(){

        Marker mar = null;
        double distance = 0;
        for (Marker mark : markers) {
            if(mar == null){
                mar = mark;
                distance = calculate(mark.getPosition().latitude, mark.getPosition().longitude);
            }else{
                double a = calculate(mark.getPosition().latitude, mark.getPosition().longitude);
                if(a < distance){
                    mar = mark;
                    distance = a;
                }
            }
           mark.setSnippet( calculateDistance(mark.getPosition().latitude, mark.getPosition().longitude));
        }
        String inf = "";
        if(distance > 15){
            inf = "Usted está cerca de "+ getMarkerAdress(mar.getPosition().latitude, mar.getPosition().longitude);
        }else{
            inf = "LLEGASTE A : "+ getMarkerAdress(mar.getPosition().latitude, mar.getPosition().longitude);
        }


        infoTv.setText(inf);


    }

    private double calculate(double lat, double longit){
        double distance = Math.sqrt(Math.pow(myMarker.getPosition().latitude - lat, 2) +Math.pow(myMarker.getPosition().longitude - longit, 2) );

        return distance  ;
    }

    private String calculateDistance(double lat, double longit){
        double distance = Math.sqrt(Math.pow(myMarker.getPosition().latitude - lat, 2) +Math.pow(myMarker.getPosition().longitude - longit, 2) );

        return "Distance to the User:" + distance  ;
    }


    private void checkIfGpsIsEnabled() {
        try {
            int gpsSignal = Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.LOCATION_MODE);
            if (gpsSignal == 0) {
                //NO HAY SEÑAL DE GPS
                showInfoAlert();

            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();

        }
    }

    private void showInfoAlert() {
        new AlertDialog.Builder(this).setTitle("GPS Signal").setMessage("Would you like to enable the GPS?").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }).setNegativeButton("Cancel", null).show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkIfGpsIsEnabled();
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

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        geoCoder = new Geocoder(this, Locale.getDefault());
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

       /* if (!permission && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 11);

            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.

        }
       */

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney").snippet("INFO"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private String getMarkerAdress(double latitude, double longitude){
        double lat = latitude;
        double longi = longitude;

        try {
            adress = geoCoder.getFromLocation(lat, longi, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String adr = adress.get(0).getAddressLine(0);
        String city = adress.get(0).getLocality();

        return "Address: "+adr+"\n"+ " City: "+ city;
    }

    private void enableMarkerEdit(){
        markerNameEt.setVisibility(View.VISIBLE);
        okBtn.setVisibility(View.VISIBLE);

    }

    private void disableMarkerEdit() {
        markerNameEt.setText("");
        markerNameEt.setVisibility(View.GONE);

        okBtn.setVisibility(View.GONE);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        latU = latLng.latitude;
        longiU = latLng.longitude;

        if(permission){
            // se habilitan el edit text y el button del nombrado del marker
            // si se da ok se crea el marker y se agrega a la lista de markers
            enableMarkerEdit();
        }else{
            permissions();
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        if(permission) {
            if (myMarker == null) {

                myMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("USER").snippet(getMarkerAdress(location.getLatitude(), location.getLongitude())));
                zoomToLocationMarker(myMarker);
            } else {
                myMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                myMarker.setSnippet(getMarkerAdress(location.getLatitude(), location.getLongitude()));
                calculateDistances();
            }
        }else{
            permissions();
        }
    }
    private void zoomToLocationMarker(Marker location){
        CameraPosition camera = new CameraPosition.Builder().target(new LatLng(location.getPosition().latitude, location.getPosition().longitude)).zoom(15).tilt(30).build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camera));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
