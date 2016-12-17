package com.prestech.Trackit;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Calendar;

import TrackitDataObjects.Car;
import TrackitDataObjects.Trip;

import static com.google.maps.android.SphericalUtil.computeLength;

/*******************************************************************************************
 * *****************************************************************************************
 *The MapActivity  is designed to display to the user the trip that is currently being
 * recorded or a previously recorded trip.  It will display a Google Map with the users current
 * location in the center, the total number of minutes, and the total distance of the trip.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    final static String TRIP_INFO_INTENT = "TRIP_INFO_INTENT";
    final static String DATE = "DATE";
    final static  String TIME_TAKEN = "TIME TAKEN";
    final static String STARTING_POINT = "START_POINT";
    final static  String END_POINT = "END_POINT";
    final static  String MILES_TRAVELLED = "MILES_TRAVELLED";


    final static double METER_TO_MILE_FACTIOR = 0.000621371;
    final static int LOCATION_SETTINGS_REQUEST = 3;

    //reference to the map object
    private GoogleMap mMap;


    /***References to GoogleApi's***/
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private Location mLastKnownLocation;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;


    /**References to activity's views*/
    private TextView cordTextView;
    private TextView timeTextView;
    private TextView carInfoTextView;
    private TextView milesTextView;

    /**Reference to the "End Tracking" buiton*/
    private Button endTripBtn;

    /*Polyline setup references */
    private PolylineOptions polylineOptions;
    private Polyline polyline;

    /*Reference to Firebase database*/
    private DatabaseReference dbRefeerence;

    //will reference the intent received from the TripInfoActivity
    private Intent mInent;

    //will reference the bundle stored in the intent from TripInfoActivity
    private Bundle mBundle;

    //stores the number of miles traveled during the trip
    private static volatile double milesTravelled;

    //store the total time of the trip
    private static volatile String timeTravelled;

    //stores Trip's id
    private String tripId;

    /*Reference the bundle that will be sent */
    private Bundle tripSummaryBundle;


    //references background thread for CounterTimer and start it
    private Thread tripTimerThread;

    //reference background thread for calculating miles travelled
    private Thread milesCalculatorThread;

    //reference array list for Latitude and Logitude point
    private  ArrayList<LatLng> latLngsPath;

    //this variable control the thread's execution
    private volatile boolean  stopTracking;

    //this represents the car's odomter
    private static double carOdometer;

    private  String carDbKey;





    /*************************************************************
     * Activity's onCreate() call back method
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        carDbKey = "";

        //set the GUI content of this activity
        setContentView(R.layout.activity_maps);


        stopTracking = false;

        //get the activity's intent
        mInent = getIntent();
        mBundle = mInent.getBundleExtra(TripInfoActivity.BUNDLE_DATA);

        tripSummaryBundle = new Bundle();


        //Get reference to the path of the new trip in the database
        dbRefeerence = createNewTrip();


        //initialize odometer
        carOdometer = mBundle.getDouble(TripInfoActivity.ODOMETER);

        //set the initial number of miles travelled
        milesTravelled = 0;

        //set the initial time of travel
        timeTravelled = "00:00:00";

        //Create an ArrayList to store the LatLng Location
        latLngsPath = new ArrayList<>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /**The next three line of code initializes the text views from the XML files*/
        cordTextView = (TextView)findViewById(R.id.cord_display);
        carInfoTextView = (TextView)findViewById(R.id.car_info_textview);
        timeTextView = (TextView)findViewById(R.id.time_textview);
        milesTextView = (TextView)findViewById(R.id.miles_textview);

        /**initializes the "end trip" button and register its onClickListener*/
        endTripBtn = (Button)findViewById(R.id.stop_track_btn);

        endTripBtn.setOnClickListener(onClickListener);



        //make sure this view exist before setting its text
        if(carInfoTextView != null)
        {
            carInfoTextView.setText(mBundle.get(TripInfoActivity.CAR_TYPE).toString().toUpperCase());
        }//if ends

        //make sure this view exists before setting its text
        if(timeTextView != null)
        {
            timeTextView.setText("Time: "+ timeTravelled);
        }//if Ends

        //set up the google Api
         setUpGoogleApi();

        //set up location setting requests
        setUpLocationSettingsRequest();

        //validate location settings
        validateLocationSettings();

        //initialize the tipTrimerThread
        tripTimerThread = new Thread(new TripTimer());

        milesCalculatorThread = new Thread(new MilesCalculator());


    }//onCreate() Ends




    /*************************************************************
     *This is the map's call back method. It is call when map is ready
     * An object of the map created is passed to this method
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        //initialize googleMap
        mMap = googleMap;

        //initialize polylineOption
        polylineOptions = new PolylineOptions();

        //start TripTimer thread
        tripTimerThread.start();

        milesCalculatorThread.start();


        boolean mlocationPermissionGranted = ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        //check if Location permission has been granted
        if (mlocationPermissionGranted == false) {

            return;

        }//ends


        //shows the user's current location on the map
        mMap.setMyLocationEnabled(true);


        //get the user's last location
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);


        if(mLastLocation != null)
        {

            updateCameraPosition(mLastLocation);
        }//if ends

    }//onMapReady() Ends


    /*************************************************************
     * This is the activity's start callback method.
     * It is called when the this activity is started
     */
    @Override
    protected void onStart()
    {
        super.onStart();

        //connect to the google API
        mGoogleApiClient.connect();
    }//onStart() Ends


    /***************************************************************
     * This is the activity's stop call back method
     * It is called when this activity is stopped
     */
    @Override
    protected void onStop()
    {
        super.onStop();

        //disconnect from the google API
        mGoogleApiClient.disconnect();

        if(stopTracking == false){
            //update the database
            updateDatabase();
        }


           //stop all background thread
           stopTracking = true;

    }//onStop() Ends



    /*****************************************************************************
     * This is A google API Call back method; called when app connects to the API
     */
    @Override
    public void onConnected(Bundle bundle)
    {

        boolean mlocationPermissionGranted = ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        //check if Location permission has been granted
        if (mlocationPermissionGranted == false) {

            return;

        }//ends


        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


    }//onConnected() Ends


    /*****************************************************************************
     * This is a google API callback method; called when is Connection Suspended
     */
    @Override
    public void onConnectionSuspended(int i)
    {

    }//onConnectionSuspended Ends


    /******************************************************************************
     * This is a Google API callback method; called when connections fails
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {

    }//onConnectionFailed() Ends


    /*********************************************************************************
     *This is a google call back method; called when location changes
     */
    @Override
    public void onLocationChanged(Location location)
    {
        if(mCurrentLocation!=null)
        {
            mLastLocation = this.mCurrentLocation;
        }

        /**Update location as long as user does not stop
         * the tracking activity
         */
        if(stopTracking == false)
        {

            //set the current location
            this.mCurrentLocation = location;


            //move camera to the new location
            updateCameraPosition(this.mCurrentLocation);

            //draw the path
            tracePath(this.mCurrentLocation);

        }//if Ends



    }//onLocationChanged() Ends


    /*************************************************************************
     *
     * ***********************************************************************
     *This internal class defines the onClick listener interface fro the views
     */
      View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()){

                //if the "End Tracking" Buttong is clicked
                case R.id.stop_track_btn:

                    //call the "endTrack()"
                    endTracking();

                    break;

            }//switch Ends

        }
    };//OnClickListener Ends


    /*******************************************************************
     * *****************************************************************
     * This method is called when the "End Tracking " Button is clicked
     */
     public  void endTracking()
     {
         //update the database with current
         // data it was stopped while running
         if(stopTracking == false)
         {

             endTripBtn.setText("Show Trip Summary");

         }//if Ends



         //collect the starting and ending points
         if(latLngsPath.size() > 0)
         {
             tripSummaryBundle.putString(STARTING_POINT," ["+latLngsPath.get(0)+"] ");

             tripSummaryBundle.putString(END_POINT," ["+latLngsPath.get(latLngsPath.size()-1)+"] ");

         }//if ends

         //put the time travelled in the bundle
         tripSummaryBundle.putString(TIME_TAKEN, timeTravelled);

         //put the total miles into the bundle
         tripSummaryBundle.putString(MILES_TRAVELLED, ""+milesTravelled);


         //define an intent and send the time and miles the "trip summary" activity
         Intent tripSummaryIntent = new Intent(this, TripSummary.class);

         tripSummaryIntent.putExtra(TRIP_INFO_INTENT, tripSummaryBundle);

         tripSummaryIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

         startActivity(tripSummaryIntent);

     }//endTracking() Ends




    /**********************************************************************************
     *validateLocationSettings() checks if the Location settings are setup as requested
     * by the locationRequest object.
     * If not, it asked permission from the user to set it up
     */
    private void validateLocationSettings()
    {
        //location setting rquest object
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        //process location settings
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());


        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                final LocationSettingsStates locationSettingsStates = locationSettingsResult
                        .getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MapsActivity.this,
                                    MapsActivity.LOCATION_SETTINGS_REQUEST);

                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });

    }//validateLocationSettings() Ends


    /*************************************************************************
     * This method draws the polyline on the map; tracing the user's path
     */
    private void tracePath(Location mCurrentLocation)
    {

        //add the current lat and lng values to the polylineOption object
        this.polylineOptions.add(new LatLng
                (mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));

        //draw the path with the polylin object
        this.polyline = mMap.addPolyline(this.polylineOptions);

    }//tracePath() Ends


    /***************************************************************************
     * This method updates the cameer positioning of the map
     */
    private void updateCameraPosition(Location location)
    {
        if(mMap != null)
        {
            LatLng initialLatLng = new LatLng
                    (location.getLatitude(), location.getLongitude());

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(initialLatLng, 16.10f);
            this.mMap.moveCamera(cameraUpdate);

            //shows coordinates
            cordTextView.setText
                    ("Lat: [" + location.getLatitude()+"]  | Lng: ["+ location.getLongitude()+"]");

        }//if ends
    }//updateCameraPosition Ends


    /*********************************************************
     * This method sets up the google Api for the application
     */
    private void setUpGoogleApi()
    {
        //setup google Api
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }//setUpGoogleApi() Ends

    /************************************************************
     * This method sets up Location setting requests necessary for
     * the apps functionality
     */
    private void setUpLocationSettingsRequest()
    {
        //setup location Service
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }//setUpLocationSettingsApi() Ends


    /**********************************************************************
     * This method creates a new trip data entity and add it to the database
     *
     */
     private DatabaseReference createNewTrip()
     {
          Calendar calendar = Calendar.getInstance();

         //get driver's id from the database
         String driverId = ""+FirebaseAuth.getInstance().getCurrentUser().getUid();

         //store the car' info number
         String car = ""+mBundle.getString(TripInfoActivity.CAR_TYPE);


         //store the start time of the trip
         String startDateAndTime = calendar.getTime().toString();

         if(startDateAndTime != null)
         {
             tripSummaryBundle.putString(DATE, startDateAndTime);

         }


         //create a Trip object with the initial database values
         Trip trip = new Trip(driverId, car , startDateAndTime);

         //retrieve a unique id from the database to represent the trip's value
         tripId = FirebaseDatabase.getInstance().getReference("trips").push().getKey();

         //reference user's trips and adds this trip's id to the user's path
         DatabaseReference dbReferenceToUserTrip = FirebaseDatabase.getInstance()
                 .getReference("drivers/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/trips/"+tripId);

         //set the trips value to true; to indicate that it is owned by this user
         dbReferenceToUserTrip.setValue(true);

         //get a reference to the "Trips" path in the database
         DatabaseReference dbReferenceToTrips = FirebaseDatabase.getInstance().getReference("trips/"+tripId);

         //add the trip to the data base with the initial values
         dbReferenceToTrips.setValue(trip);

         //return a reference to the new Trip's location in the database
         return dbReferenceToTrips;

     }//createNewTrip() Ends


    /**********************************************************
     *This method will right data about the trip to the database
     */
    private void updateDatabase()
    {
        //update the value of the car odometer
        carOdometer = carOdometer + milesTravelled;

        String carVin = mBundle.getString(TripInfoActivity.CAR_VIN);
        //references the car,s odometer
        DatabaseReference dbCarRefernce = FirebaseDatabase.getInstance()
                .getReference("cars/"+mBundle.getString(carVin));

       // dbCarRefernce.addListenerForSingleValueEvent(dbCarValueListener);

        //dbCarRefernce = FirebaseDatabase.getInstance()
                //.getReference("cars/"+carDbKey);

        //update the odometer in the database
        dbCarRefernce.child("odometer").setValue(carOdometer);

        dbRefeerence.child("startPoint").setValue(tripSummaryBundle.getString(STARTING_POINT));
        dbRefeerence.child("endPoint").setValue(tripSummaryBundle.getString(END_POINT));
        dbRefeerence.child("milesTravelled").setValue(milesTravelled);

    }//updateDatabase() Ends



    /***************************************************************************
     * This is the activity Handler Object: it is used to establish communicatin
     * between the Activty main thread and other threads running in the background
     *
     * The handler is called be the TripTimer Runnable class (and subsequently by a thread)
     */
    private Handler mapHandler =  new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);


            //set the new time on the timerTextView
            timeTextView.setText(timeTravelled);

            //display miles only when it is greater than 0.09
            if(milesTravelled > 0.0)
            {
                milesTextView.setText("Miles ["+milesTravelled+"]");
            }//if ends

        }
    };//mapHandler Ends



    /*********************PRIVATE INNER Class***************************
     * This class implements the Runnable object. And it is responsible for
     * calculating the total time the trip covers
     */
    private  class TripTimer implements Runnable {

        //store the start(when the thread starts) time in milliseconds
        double startTime = System.currentTimeMillis();

        //will store the current time
        double currentTime;

        //store the difference between the startTime and the currentTime
        int timeVariance;

        int seconds = 0; //holds the number seconds
        int minutes = 0; //holds the number of minutes
        int hours = 0; // holds the number of hours
        int countOfThousandthMilliSecond = 1;//incremented each time timeVariance is at a new thousand



        /****************************************************************
         * Runnable's overiden method
         */
        @Override
        public void run()
        {

            while(stopTracking==false)
            {
                currentTime = System.currentTimeMillis();

                timeVariance = (int)(currentTime-startTime);

                //increment seconds at every 1000 milli second
                if(timeVariance > (countOfThousandthMilliSecond*1000))
                {
                    //increment the number seconds
                    ++seconds;

                    //check if 60 seconds has reached
                    if(seconds == 60)
                    {
                        //increment minutes
                        minutes++;

                        //reset seconds
                        seconds = 0;

                        //check if 60 minutes has reached
                        if (minutes == 60)
                        {
                            //increment hours
                            ++hours;

                            //reset minutes
                            minutes = 0;

                        }//if Ends

                    }//if Ends

                    timeTravelled = "Time: [" + hours+"h :"+minutes+"m :"+seconds+"s]";
                    //increment countOfThousandthMilliSecond
                    ++countOfThousandthMilliSecond;

                    mapHandler.sendMessage(mapHandler.obtainMessage());

                }//if Ends

                //sleep for 0.5 seconds
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }//try-catch Ends

            }//while() Ends
        }//run() Ends

    }//CountDownTimer Class Ends


    /**********************PRIVATE INNER CLASS***********************************
     *This class represents the runnable object that is used by a background thread
     * to compute the number of miles travelled
     *
     */
    private class MilesCalculator implements Runnable {
        //reference to the LatLng Point
        LatLng latLng;


        @Override
        public void run() {

            while(stopTracking == false)
            {
                //sleep for 2 seconds
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    //do nothing
                }


                //make sure the last location is not null
                if(mCurrentLocation != null){
                    //create a LatLng Object with a new LatLng Location
                    latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

                }else{

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        //do nothing
                    }//try Ends

                }//else Ends

                //check if the new LatLng object actually contains values
                if(latLng != null)
                {
                    //add the latlng to the list of path
                    latLngsPath.add(latLng);
                }

                /*******************************************************************
                 * For distance to be calculated we need atleast two latlng points on
                 * the map
                 *
                 * Make sure there are atleast two set of points in the list
                 */
                if(latLngsPath.size() > 1)
                {

                    //compute the miles travelled and convert it to two decimal places
                    milesTravelled = (int)((computeLength(latLngsPath)*METER_TO_MILE_FACTIOR)*10);
                    milesTravelled = (milesTravelled)/10d;


                }//if Ends


            }//while Ends

        }//run() Ends
    }//MilesCalculator Ends



}//MapsActivity Ends
