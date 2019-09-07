package com.example.challenge1.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.challenge1.R;
import com.example.challenge1.fragments.MapFragment;
import com.example.challenge1.fragments.WelcomeFragment;

public class MainActivity extends AppCompatActivity {
    private Fragment currentFragment;
    private Fragment mapFragment = new MapFragment();
    private Fragment welcomeFragment = new WelcomeFragment();

    boolean permission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        permissions();

        if(savedInstanceState == null) {
            currentFragment = welcomeFragment;

            changeFragment(currentFragment);
        }




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
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }*/

    /*@Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_map:

                if(permission){
                    currentFragment = mapFragment;

                }else{
                    permissions();
                }



                break;
            case R.id.menu_welcome:


                currentFragment = welcomeFragment;

                break;
        }

        changeFragment(currentFragment);
        return super.onOptionsItemSelected(item);
    }*/


    public void changeMapFragment(){
        currentFragment = mapFragment;
        changeFragment(currentFragment);
    }


    public void changeFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }


}

