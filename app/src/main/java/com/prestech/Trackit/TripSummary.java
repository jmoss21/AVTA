package com.prestech.Trackit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class TripSummary extends AppCompatActivity implements View.OnClickListener {


    /**Reference to all the views (button and text view) from the xml layout file*/
    Button startNewTripBtn;
    Button goToHomePageBtn;

    /**Reference to all text view in the layout of this activity*/
    TextView timeTravelledTv;
    TextView endPointTv;
    TextView mileTravelledTextView;
    TextView dateTextView;
    TextView startPointTV;


    //reference to the intent received from the map actvity
    Intent intentFromMapActvity;

    //references the bundle received from the map activity (parcelled within the intent)
    Bundle bundleFromMapActivity;



    /*********************************************************************
     * This is the activity's onCreate call beack method.
     * Callled when this activty is created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_summary);



        /**the lines of code below initializes the view**/
        startNewTripBtn = (Button)findViewById(R.id.start_new_trip_btn);
        goToHomePageBtn = (Button)findViewById(R.id.take_me_home_btn);

        startNewTripBtn.setOnClickListener(this);
        goToHomePageBtn.setOnClickListener(this);

        /**Initializing the text view**/
        dateTextView = (TextView)findViewById(R.id.date_tv);
        mileTravelledTextView =(TextView)findViewById(R.id.miles_tv);
        endPointTv= (TextView)findViewById(R.id.end_point_tv);
        timeTravelledTv = (TextView)findViewById(R.id.time_tv);
        startPointTV = (TextView)findViewById(R.id.start_point_tv);

        /**retrieve the intent sent from the Map Activty*/
        intentFromMapActvity = getIntent();

        /*retrieve the bundle (of information) sent from the MapActivity*/
        bundleFromMapActivity = intentFromMapActvity.getBundleExtra(MapsActivity.TRIP_INFO_INTENT);


        //set the text for each of the textViews
        setText(dateTextView, MapsActivity.DATE);
        setText(mileTravelledTextView, MapsActivity.MILES_TRAVELLED);
        setText(endPointTv, MapsActivity.END_POINT);
        setText(startPointTV, MapsActivity.STARTING_POINT);
        setText(timeTravelledTv, MapsActivity.TIME_TAKEN);

    }//onCreate() Ends



    /*******************************************************************
     * This is the menu call back method; called when menu is created
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_trip_summary, menu);
        return  true;
    }//onCreateOptionsMenu() ends


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



    private void setText(TextView textView, String bundle_code)
    {
        //make sure textVIew is initialized
        if(textView != null)
        {
            //retrieve the string from the bundle
            String text = bundleFromMapActivity.getString(bundle_code);

            //set the the text
            textView.setText(textView.getText()+" : "+text);

        }//if ends

    }
    /************************************************************
     * This is an implementation of the OnClickListener interface
     * Implemented by the class
     * @param view
     */
    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.start_new_trip_btn:

                //open the TripInfo class for user to select a car
                Intent tripInfoIntent = new Intent(this, TripInfoActivity.class);
                tripInfoIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(tripInfoIntent);

                break;

            case R.id.take_me_home_btn:

                Intent mainActivityIntent = new Intent(this, MainTabActivity.class);

                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //open the user's home page
                startActivity(mainActivityIntent);
                break;
        }//switch() ends
    }//onClick() Ends

}//TripSummary Class Ends
