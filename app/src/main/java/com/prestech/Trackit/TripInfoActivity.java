package com.prestech.Trackit;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import TrackitDataObjects.Car;

/**
 * This Activity will be responsible for collecting the vehicle's vin and odometer
 * before the the app can begin tracking vehicles movement.
 */
public class TripInfoActivity extends AppCompatActivity implements View.OnClickListener
{

    //intent constants
    public static final String CAR_VIN = "CAR_VIN";
    public static final String CAR_TYPE = "CAR_TYPE";
    public static final String ODOMETER = "ODOMETER";
    public static final String BUNDLE_DATA = "BUNDLE_DATA" ;




    //references to the layout views
    private Button startTripBtn;

    //reference to the view that displays cars odometer
    private TextView odometerTextView;

    //reference the bundle created by the activity(to send to the next activity)
    private static Bundle bundleData;

    //reference the spinner view object
    private static Spinner mMakeModeVinSpinner;

    private static HashMap<String, Car> companyCars;

    //reference to the lists used to store the car models and the vin numbers, respectively
    private static ArrayList<String> mCarMakeAndModel, mVinNumbers;



    //database reference
    private static DatabaseReference mDatabase;





    /****************************************************************
     * Activity's onCreate call back method
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_info);

        //reference the database PATH 'cars/vinNumber
        mDatabase = FirebaseDatabase.getInstance().getReference("cars");
        //set value change listener
        mDatabase.addListenerForSingleValueEvent(mDatabaseValueListener);


        //initialize button
        startTripBtn = (Button)findViewById(R.id.start_new_trip_btn);

        odometerTextView = (TextView)findViewById(R.id.odometer_textview);

        if(odometerTextView != null)
        {
            odometerTextView.setEnabled(false);
        }

        mMakeModeVinSpinner = (Spinner)findViewById(R.id.car_spinner);

        //makes sure spinner is not null
        if(mMakeModeVinSpinner != null)
        {
            //set onItemSelected listener
            mMakeModeVinSpinner.setOnItemSelectedListener(onItemSelectedListener);
        }//if Ends

        //initialize ArrayLists
        mVinNumbers = new ArrayList<>();
        mCarMakeAndModel = new ArrayList<>();

        //initialize cars HashMap
        companyCars = new HashMap<>();

        mCarMakeAndModel.add("SELECT MAKE:MODEL [VIN]");

        //make sure the button is not null
        if(startTripBtn != null)
        {
            //set its onClick Listener

            startTripBtn.setOnClickListener(this);
            startTripBtn.setEnabled(false);
        }//if ends


        //populates the spiner with vin numbers
        setSpinnerAdapter(mMakeModeVinSpinner, mCarMakeAndModel);

        bundleData = new Bundle();

    }//onCreate() Ends


    /***********************************************************************
     * This is a menu call back method when the menu option is being created
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trip_info, menu);
        return true;
    }//onCreateOptionMenu() Ends


    /**********************************************************
     * This is a menu call back method when an item is selected
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }//onOptionsItemSelected() Ends




    /**
     * Database value Listener
     */
    ValueEventListener mDatabaseValueListener = new ValueEventListener() {

        /**
         *
         * @param dataSnapshot
         */
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            Iterable<DataSnapshot> dataSnapshotIterable = dataSnapshot.getChildren();
            Iterator<DataSnapshot> dataSnapshotIterator = dataSnapshotIterable.iterator();

            //referecnce to a car object
            TrackitDataObjects.Car car;

            DataSnapshot dataSnapped;

            //iterate through the cars
            while (dataSnapshotIterator.hasNext())
            {
                dataSnapped = dataSnapshotIterator.next();

                //create a car object from database
                car = dataSnapped.getValue(TrackitDataObjects.Car.class);

                companyCars.put(car.getVinNumber(), car);

                bundleData.putString(car.getVinNumber(), dataSnapped.getKey());

                Log.i("CAR KEYS", dataSnapped.getKey());

                //add car's vin to array
                mCarMakeAndModel.add((car.getMake()+": "+car.getModel()).toUpperCase()
                        +" ["+car.getVinNumber()+"]");

                mVinNumbers.add(car.getVinNumber());
            }//while ends

        }//onDataChanges() Ends


        /***************************************************
         *
         * @param databaseError
         */
        @Override
        public void onCancelled(DatabaseError databaseError) {

        }//onCancelled() Ends
    };//mDatabaseValueListener  Ends




    /****************************************************************
     *
     */
    private void setSpinnerAdapter(Spinner spinner, ArrayList<String> spinnerList)
    {


        ArrayAdapter<String> spinnerAdapter;

        //check to make sure mVinSpinner is not null
        if(spinner != null)
        {
            spinner.setPrompt(spinnerList.get(0));


            spinnerAdapter = new ArrayAdapter
                    (TripInfoActivity.this,R.layout.support_simple_spinner_dropdown_item, spinnerList)
            {
                @Override
                public boolean isEnabled(int position) {

                    if(position==0)
                    {
                        return false;
                    }
                    else
                    {
                        return true;
                    }
                }//isEnableEnds


                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {

                    View view = super.getDropDownView(position, convertView, parent);
                    TextView textView = (TextView) view;

                    if(position == 0){
                        textView.setTextColor(Color.GRAY);
                    }
                    else {

                        textView.setTextColor(Color.BLACK);
                    }

                    if(position%2==0)
                    {
                        textView.setBackgroundColor(Color.TRANSPARENT);
                    }


                    return view;
                }//getDropDownView() Ends*/


            };

            spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

            spinner.setAdapter(spinnerAdapter);
        }//if ends


    }//populateSpinner()Ends


    /****************************************************************
     * Implementation of the OnClickListener
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId())
        {

            case R.id.start_new_trip_btn:
                openMapActivity();
                break;
        }//switch
    }//onClick

    /*******************************************************
     This method opens the map activity
     */
    private void openMapActivity()
    {
        Intent mapActivityIntent = new Intent(TripInfoActivity.this, MapsActivity.class);

        mapActivityIntent.putExtra(BUNDLE_DATA, bundleData);

        startActivity(mapActivityIntent);

    }//openMapActivity Ends


    /***********************************************************************************
     * Define the onSelectListener for the spinners
     */
    AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
        {

            Car selectedCar;

            if(position > 0)
            {

                //initialize selected car
                selectedCar = companyCars.get(mVinNumbers.get(position-1));

                //display odometer
                odometerTextView.setText(selectedCar.getOdometer()+"");


                //put the odometer in a bundle
                bundleData.putDouble(ODOMETER, selectedCar.getOdometer());

                bundleData.putString(CAR_VIN,selectedCar.getVinNumber());

                //put the car type in a bundle
                bundleData.putString(CAR_TYPE, "["+selectedCar.getMake()+" :" +
                        " "+selectedCar.getModel()+" : "+selectedCar.getYear()+" : "+selectedCar.getVinNumber()+"]");



                //activate button
                startTripBtn.setEnabled(true);

                //display a TOAST message
                Toast.makeText
                        (getApplicationContext(), "Selected : "+selectedCar.toString(), Toast.LENGTH_LONG)
                        .show();
            }//if Ends

        }//onItemSelected Ends

        @Override
        public void onNothingSelected(AdapterView<?> adapterView)
        {

        }//onNothingSelected Ends

    };//onItemSelectedListener Ends

}//TripInfoActivty Class  Ends

