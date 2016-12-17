package com.prestech.Trackit;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/*******************************************************
 * This contain all method for requesting system permision to access
 * a hardware resource.
 */

public class SystemPermission {

    //This is a constant used as an ID to keep track of permission request
    public static final int MY_LOCATION_PERMISSION_REQUEST_CODE = 1;


    /*******************************************************************************
     *requstLocationPermission() request permission from the user to access location
     */
    public  static void requestLocationPermission(Context context) {

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ask for user's permission


                //asks for location permision
                ActivityCompat.requestPermissions((Activity) context, new String[]
                                {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_LOCATION_PERMISSION_REQUEST_CODE);

        }//if ends

    }//locationPermissionGranted() Ends;




}
