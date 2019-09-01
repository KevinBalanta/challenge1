package com.example.challenge1.fragments;


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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.challenge1.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback , GoogleMap.OnMapLongClickListener , LocationListener {

    private View viewRoot;
    private MapView mapView;


    private GoogleMap mMap;

    private boolean enableToAdd = true;
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

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewRoot = inflater.inflate(R.layout.fragment_map, container, false);

        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 11);
        final ViewGroup vwg = viewRoot.findViewById(R.id.map_container);
        infoTv = viewRoot.findViewById(R.id.information_tv);
        markerNameEt = viewRoot.findViewById(R.id.marker_et);
        okBtn = viewRoot.findViewById(R.id.ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // se agrega el marker en mapa y array y se nombra, con su snippet ubication
                TransitionManager.beginDelayedTransition(vwg);
                Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latU, longiU)).title(markerNameEt.getText().toString()).snippet(calculateDistance(latU, longiU) +"\n"+ getMarkerAdress(latU, longiU)));
                markers.add(marker);
                calculateDistances();
                disableMarkerEdit();
            }
        });

        return viewRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = (MapView) viewRoot.findViewById(R.id.map);
        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
        this.checkIfGpsIsEnabled();




        markers = new ArrayList<>();

    }


    @Override
    public void onResume() {
        super.onResume();

        checkIfGpsIsEnabled();

    }





    //si está a menos de 10 mts, poner bienvenido a xxxx
    private void calculateDistances(){

        Marker mar = null;
        double distance = 0;
        for (Marker mark : markers) {
            if(mar == null){
                mar = mark;
                distance = calculate(mark.getPosition().latitude, mark.getPosition().longitude);
                distance = distance*111.12 *1000;
            }else{
                double a = calculate(mark.getPosition().latitude, mark.getPosition().longitude);
                a = a*111.12 *1000;
                if(a < distance){
                    mar = mark;
                    distance = a;
                }
            }
            mark.setSnippet( calculateDistance(mark.getPosition().latitude, mark.getPosition().longitude));
        }

            String inf = "";
            if (distance > 15) {
                inf = "Usted está cerca de " +mar.getTitle() +"\n"+getMarkerAdress(mar.getPosition().latitude, mar.getPosition().longitude);
            } else {
                inf = "LLEGASTE A : " +mar.getTitle();
            }



        infoTv.setText(inf);


    }

    private double calculate(double lat, double longit){
        double distance = Math.sqrt(Math.pow(myMarker.getPosition().latitude - lat, 2) +Math.pow(myMarker.getPosition().longitude - longit, 2) );

        return distance  ;
    }

    private String calculateDistance(double lat, double longit){
        double distance = Math.sqrt(Math.pow(myMarker.getPosition().latitude - lat, 2) +Math.pow(myMarker.getPosition().longitude - longit, 2) );
        distance = distance*111.12 *1000;
        return "Distance to the User:" + distance +" Mts" ;
    }

    private void checkIfGpsIsEnabled() {
        try {
            int gpsSignal = Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Secure.LOCATION_MODE);
            if (gpsSignal == 0) {
                //NO HAY SEÑAL DE GPS
                showInfoAlert();

            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();

        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        geoCoder = new Geocoder(getContext(), Locale.getDefault());
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

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

    private void showInfoAlert() {
        new AlertDialog.Builder(getContext()).setTitle("GPS Signal").setMessage("Would you like to enable the GPS?").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }).setNegativeButton("Cancel", null).show();
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
        enableToAdd = false;

    }

    private void disableMarkerEdit() {
        markerNameEt.setText("");
        markerNameEt.setVisibility(View.GONE);

        okBtn.setVisibility(View.GONE);
        enableToAdd = true;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        latU = latLng.latitude;
        longiU = latLng.longitude;


            // se habilitan el edit text y el button del nombrado del marker
            // si se da ok se crea el marker y se agrega a la lista de markers
            if(enableToAdd){
                enableMarkerEdit();
            }else{
                Toast.makeText(getContext(), "Please set the marker´s name", Toast.LENGTH_LONG);
            }

        }



    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;

            if (myMarker == null) {

                myMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("USER").snippet(getMarkerAdress(location.getLatitude(), location.getLongitude())));
                myMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_ubication));
                infoTv.setText("We have found you! :)");
            } else {
                myMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                myMarker.setSnippet(getMarkerAdress(location.getLatitude(), location.getLongitude()));
                if(!markers.isEmpty()) {
                    calculateDistances();
                }
            }
            zoomToLocationMarker(myMarker);

    }

    private void zoomToLocationMarker(Marker location){
        CameraPosition camera = new CameraPosition.Builder().target(new LatLng(location.getPosition().latitude, location.getPosition().longitude)).zoom(17).tilt(30).build();

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
