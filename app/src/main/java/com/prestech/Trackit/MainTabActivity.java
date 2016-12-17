
package com.prestech.Trackit;

        import android.app.AlertDialog;
        import android.app.Dialog;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.graphics.Color;
        import android.support.annotation.NonNull;
        import android.support.design.widget.TabLayout;
        import android.support.v7.app.AppCompatActivity;
        import android.support.v7.widget.LinearLayoutManager;
        import android.support.v7.widget.RecyclerView;
        import android.support.v7.widget.Toolbar;

        import android.support.v4.app.Fragment;
        import android.support.v4.app.FragmentManager;
        import android.support.v4.app.FragmentPagerAdapter;
        import android.support.v4.view.ViewPager;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.ViewGroup;

        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.FirebaseUser;
        import com.google.firebase.database.ChildEventListener;
        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.database.Query;
        import com.google.firebase.database.ValueEventListener;

        import java.util.ArrayList;
        import java.util.Collection;
        import java.util.HashMap;


        import TrackitDataObjects.Driver;
        import TrackitDataObjects.Trip;




/************************************************************************************
 * **********************************************************************************
 * The MainTabActivity is the central hub for the functions of the application.
 * After authentication the Main Screen provides the user with several tabs that
 * allow the user to alter their account, view previous trips, start new trip,
 * and logout.  All these features are tabs within the Main Screen. The Main
 * Screen will handle the transitions of the user from each of the tabs.
 */
public class MainTabActivity extends AppCompatActivity
{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;


    //database references
    private static FirebaseUser firebaseUser;
    private static FirebaseAuth firebaseAuth;
    private static DatabaseReference dbReferenceToDriver;


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    //reference to the current logged in driver
    private static  Driver  loggedInDriver;

    //reference to the incoming intents
    private static Intent mIntent;

    /******************************************************************************************
     * This is an Activty call back method when the activity and its views are being created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //retrieve the intent sent to this activity
        mIntent = getIntent();

        //initialize firebaseAuth and firebaseUser
        firebaseAuth =FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();



        //getRerence to user's information from the database
        dbReferenceToDriver = FirebaseDatabase.getInstance().getReference("drivers/" + firebaseUser.getUid());

        //register the database reference with an even listener
        dbReferenceToDriver.addListenerForSingleValueEvent(dataValueEventListener);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        //initialize the menu tool bar
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


    }//onCreate() Ens



    /************************************************
     * This is the main activity's onDestroy method
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }//onDestroy() Ends

    /***********************************************************************
     * This is a menu call back method when the menu option is being created
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_tab, menu);
        return true;
    }//onCreateOptionMenu() Ends





    /*******************************************************************
     *
     * implement dataValueEventListener to retrieve login user's information
     */
    ValueEventListener dataValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            //create a Driver Object
            loggedInDriver = new Driver();

            //make sure loggedInDriver object is not null
            if(loggedInDriver != null && dataSnapshot != null){

                Log.i("LOGIN USER", "LOGIN INITIALIZED");
                //set the Driver attributes
                loggedInDriver.setFirstName(dataSnapshot.child("firstName").getValue().toString());
                loggedInDriver.setLastName(dataSnapshot.child("lastName").getValue().toString());
                loggedInDriver.setEmail(dataSnapshot.child("email").getValue().toString());
                loggedInDriver.setPhoneNumber(dataSnapshot.child("phoneNumber").getValue().toString());

            }//if ends

            //resume the home page layout
            PlaceholderFragment.homeTab.onResume();

        }//onDataChange()

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };//dataValueEventListener Ends




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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }
        else  if (id == R.id.action_logout)
        {
            FirebaseAuth.getInstance().signOut();
            Intent loginActivityIntent = new Intent(MainTabActivity.this, LoginActivity.class);
            loginActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginActivityIntent);
        }

        return super.onOptionsItemSelected(item);
    }//onOptionsItemSelected() Ends


    /**********************************************************************************************************
     * Implement the PasswordDialogFragment interfaces and override the method. This allows the MainTbActivity
     * to communicate with the PasswordDialogFragment.
     */
    public static PasswordDialogFragment.MyInterface passwordDialogInterface = new PasswordDialogFragment.MyInterface(){

        //override the interface's updatePassword() method
        @Override
        public String updatePassword(String oldPassword, String newPassword, String confirmedNewPassword) {

            //return WRONG_OLD_PASSWORD if password entered is not correct
            if(!(oldPassword.equals(mIntent.getStringExtra(LoginActivity.SHADOW)))) {
                return PasswordDialogFragment.WRONG_OLD_PASS;
            }

            //return the NEW_PASS_MISMATCH message if the new password
            //(and its re-entrant confirmation) does no match
            else if(!(newPassword.equals(confirmedNewPassword))){

                return PasswordDialogFragment.NEW_PASS_MISMATCH;

            }

            //Return SAME_OLD_PASS if the new password matches
            //the oldpassword
            else if(newPassword.equals(oldPassword))
            {
                return PasswordDialogFragment.SAME_OLD_PASS;
            }
            //update the password if no fault is found
            else {
                //change the password
                firebaseUser.updatePassword(newPassword);

                return PasswordDialogFragment.SUCCESS;
            }

        }//updatePassword Ends
    };



    /*=============================SUB-CLASS OF MainTabActivity==============================
     * This class is responsible for creating the fragments for each tabs
     *
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements View.OnClickListener
    {

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String FRAGMENT_RESOURCE_ID = "section_number";




        /**This are the fragment objects; Home, PastTrip, and My Account Tabs, living with the MainActivity Fragment***/
        public static   final PlaceholderFragment homeTab = PlaceholderFragment.newInstance(R.layout.activity_home_page);
        public static  final PlaceholderFragment pastTripTab = PlaceholderFragment.newInstance(R.layout.activity_past_trip);
        public static  final PlaceholderFragment myAccountTab = PlaceholderFragment.newInstance(R.layout.activity_my_account);


        /***** Reference objects used in the "PAST TRIP" tap***/

        //reference to recycle view
        private RecyclerView mRecyclerView;
        //recycleView's adapter
        private RecyclerView.Adapter mAdapter;
        //layoutManager for recyclview
        private LinearLayoutManager llm;

        //holds PAST TRIP data to be display in the recycle view
        private HashMap<String,String> mRecycleViewResources;

        /******reference to HOME PAGE tab*****/

        //reference to "New Trip" Button
        private Button newTripBtn;

        private static  TextView welcomeTextView;


        private  View  rootView;

        /*****Reference view to "MY ACCOUNT" tab******/

        private EditText firstNameEditView;
        private EditText lastNameEditView;
        private EditText emailEditView;
        private EditText passwordEditView;
        private EditText phoneNumberEditView;
        private TextView changePasswdTextView;

        private DatabaseReference dbTripReference;


        /**************************************************
         * This is a non-argument Constructor
         */
        public PlaceholderFragment()
        {

        }//constructor ends


        /****************************************************************
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber)
        {
            //create and instance of the fragement
            PlaceholderFragment fragment = new PlaceholderFragment();

            //create a bundle for storing intra-fragment shared data
            Bundle args = new Bundle();

            //enter the fragements resource id into the bundle
            //(this will be collected from the onCreateView()
            args.putInt(FRAGMENT_RESOURCE_ID, sectionNumber);

            //place the data into fragment's arguments
            fragment.setArguments(args);

            //return the created fragments
            return fragment;
        }//newInstance(int) Ends



        /************************************************************************************
         * This is a Fragment call back method when views are created
         * @param inflater
         * @param container
         * @param savedInstanceState
         * @return
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {


            //get the fragment's id
            int layoutId = getArguments().getInt(FRAGMENT_RESOURCE_ID);


            //inflate the view with the LayoutId. And store it as the rootview
            rootView = inflater.inflate(layoutId, container, false);


            /**Initialize the variables needed by the HOME tab*/
            if(layoutId == R.layout.activity_home_page)
            {
                Log.i("WELCOME_TAB", ""+layoutId);

                /*******************initialize "HOME" TAB views**************************/
                newTripBtn =  (Button)rootView.findViewById(R.id.home_new_trip_btn);

                welcomeTextView = (TextView)rootView.findViewById(R.id.welcome_textview);

                //if button is not null
                if(newTripBtn != null)
                {
                    //set its onClickListener
                    newTripBtn.setOnClickListener(this);
                }//if ends

            }//if Ends



            /*******************Setup "PAST TRIP" TAB views******************************/

            if(layoutId == R.layout.activity_past_trip){

                mRecycleViewResources = new HashMap<>();


                Log.i("PAST_TRIP_TAB", ""+layoutId);


                dbTripReference = FirebaseDatabase.getInstance().getReference("trips");

                //query database
                Query query = dbTripReference.orderByChild("driverID").equalTo(firebaseUser.getUid());

                query.addChildEventListener(childEventListener);

                //initialize the RecycleView
                mRecyclerView = (RecyclerView)rootView.findViewById(R.id.past_trip_recycler);

                setupRecycleViewEnvironment();

            }//if Ends




            /**********************Setup "MY ACCOUNT" TAB views********************/

            if(layoutId == R.layout.activity_my_account){

                Log.i("MY_ACCOUNT_TAB", ""+layoutId);

                firstNameEditView = (EditText) rootView.findViewById(R.id.first_name_field);
                lastNameEditView = (EditText)rootView.findViewById(R.id.last_name_field);
                emailEditView = (EditText)rootView.findViewById(R.id.email_field);
                phoneNumberEditView = (EditText) rootView.findViewById(R.id.phone_field);
                passwordEditView = (EditText)rootView.findViewById(R.id.passsd_field);

                changePasswdTextView= (TextView)rootView.findViewById(R.id.change_passwd_tv);

                //set changePasswdTextView's onClick listener
                if(changePasswdTextView != null)
                {
                    changePasswdTextView.setOnClickListener(this);
                }


                //Prints out user's infor in the "MY ACCOUNT" app
                populateUserAccountInfo(loggedInDriver);

            }//if Ends


            //return the rootView
            return rootView;


        }//onCreateViews() Ends



        /*******************************************
         * Fragments resume callback method
         */
        @Override
        public void onResume() {
            super.onResume();


            /**check if the Home Tab is resuming**/
            if(getArguments().getInt(FRAGMENT_RESOURCE_ID) == R.layout.activity_home_page)
            {

                //initialize the welcomeTextView if it is null
                if(welcomeTextView == null)
                {
                    welcomeTextView = (TextView)rootView.findViewById(R.id.welcome_textview);

                }//if Ends


                //include the user's name in the welcome message and display it on the home tab
                if(loggedInDriver!=null && welcomeTextView != null){
                    welcomeTextView.setText("Welcome \n To \n Trackit \n"+ loggedInDriver.getLastName());
                }//if Ends

            }//if Ends

            //populate the Driver's accout information
            populateUserAccountInfo(loggedInDriver);

        }//onResume() Ends



        /***************************************
         * Fragment's start call back method
         */
        @Override
        public void onStart() {
            super.onStart();

        }//onStart() Ends



        /****************************************
         * Fragments destroy callback method
         */
        @Override
        public void onDestroy() {
            super.onDestroy();

            //mLoginIntent = null;
        }//onDestroy() Ends


        /***********************************************************************************
         *This method is the implementation of the OnClickListener; it register the views to
         * a clickable event.
         */
        @Override
        public void onClick(View view)
        {
            //use to the swich-case to define the onClickListener
            //base on which registered view is clicked
            switch (view.getId())
            {
                case R.id.home_new_trip_btn:
                    startActivity(new Intent(getContext(),TripInfoActivity.class));
                    break;

                case R.id.change_passwd_tv:
                    // custom dialog
                    PasswordDialogFragment passwordDialogFragment = new PasswordDialogFragment();
                    passwordDialogFragment.show(getActivity().getSupportFragmentManager(), "change_password");

                    break;

            }//switch ends
        }//onClick ends




        /************************************************************
         *setupRecycleViewEnvironment() initializes the  list (recycle list)
         * view layout environment for the "PAST TRIP" tab
         */
        private void setupRecycleViewEnvironment()
        {
            //check if the RecycleView object is available.
            if(mRecyclerView != null && mRecycleViewResources != null)
            {

                //create a layoutManager
                llm = new LinearLayoutManager(getContext());

                //create an adapter object
                mAdapter = new MyRecyclerAdapter( mRecycleViewResources.values());

                //set mRecyclerView  properties
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setLayoutManager(llm);
                mRecyclerView.setAdapter(mAdapter);


            }//if ends

        }//setupRecycleViewEnvironment() Ends



        /******************************************************************
         *populateUserAccountInfo() This method is used to populate the
         * "MY-ACCOUNT" Tab with the logged in driver's information
         */
        private  void populateUserAccountInfo(Driver loggedInDriver)
        {

            /**
             * Make sure the driver object and its attributes are not
             * NULL before attempting to update it
             */
            if(loggedInDriver != null)
            {

                if(firstNameEditView != null )
                {
                    firstNameEditView.setEnabled(false);
                    firstNameEditView.setText(loggedInDriver.getFirstName());
                }

                if(lastNameEditView != null )
                {
                    lastNameEditView.setEnabled(false);
                    lastNameEditView.setText(loggedInDriver.getLastName());
                }

                if(passwordEditView != null )
                {
                    passwordEditView.setEnabled(false);

                }
                if(emailEditView != null )
                {
                    emailEditView.setEnabled(false);
                    emailEditView.setText(loggedInDriver.getEmail());
                }

                if(phoneNumberEditView != null )
                {
                    phoneNumberEditView.setEnabled(false);
                    phoneNumberEditView.setText(loggedInDriver.getPhoneNumber());

                }
            }//populateUserAccountInfo()


        }//populateUserAccountInfo




        /*****************************************************************************
         * This Initerface listens for changes in the Trips section of the database
         * It implements various method for taking different types of action based on the
         * changes. If a trip is: removed, added, moved, changed or cancelled.
         *
         */
        ChildEventListener childEventListener = new ChildEventListener() {

            //this method is called when and item is added to the database
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                //make sure the mRecycleViewResources List is not null
                if(mRecycleViewResources != null)
                {
                    //use the while loop to iterate through the trips look for the one belonging
                    //to a specific user
                    Trip trip = dataSnapshot.getValue(Trip.class);

                    //add the trips to mRecycleViewResources
                    mRecycleViewResources.put(trip.getStartDateAndTime(), trip.toString());

                    setupRecycleViewEnvironment();

                }

            }//onChildAdded Ends



            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                if(mRecycleViewResources != null)
                {
                    Trip trip = dataSnapshot.getValue(Trip.class);


                    mRecycleViewResources.put(trip.getStartDateAndTime(), trip.toString());

                    Log.i("WHAT IS THE SIZE : ","DATA CHANGED");


                    setupRecycleViewEnvironment();
                }//if Ends

            }//onChildChanged() Ends

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                if(mRecycleViewResources != null)
                {
                    Trip trip = dataSnapshot.getValue(Trip.class);

                    if(mRecycleViewResources.containsKey(trip.getStartDateAndTime()))
                    {
                        mRecycleViewResources.remove(trip.getStartDateAndTime());


                    }//else Ends

                    setupRecycleViewEnvironment();
                }//if Ends
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }//PlaceholderFragment Class Ends


    /*=========================SUB CLASS OF MainTabActivity=================================
     * This  class is a subclass of the FragmentPagerAdapter Class. It creates the Tabs, and
     * attaches the layouts of the tabs/pagers to their position
     *
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {


        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below)

            if(position==0)
            {

                return PlaceholderFragment.homeTab;
            }
            else if (position==1)
            {

                return PlaceholderFragment.pastTripTab;
            }
            else
                return PlaceholderFragment.myAccountTab;

        }//getItem() Ends


        /*****************************************************************************
         *This is a FragmentPagerAdapter method which return the number TABS to created
         * For this project we are creating 3 tabs
         * @return
         */
        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }//getCount() Ends



        /***********************************************************
         *This method specify the HEADINGS for each of the 3 tabs
         * @param position
         * @return
         */
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "HOME";
                case 1:
                    return "PAST TRIPS";
                case 2:
                    return "MY ACCOUNT";
            }
            return null;
        }//getPageTitle() Ends

    }//SectionsPagerAdapter Class Ends


    /****************************************************************************************
     * This class defines the dialog box displayed when the user clicks the "Change Password"
     * Text view in the "MY ACCOUNT" TAB.
     */
    public static class PasswordDialogFragment extends android.support.v4.app.DialogFragment {

        /******************************************************************************
         * This constants represents messages that can be displayed when the user attempts
         * to change password
         */
        public  static  final  String SUCCESS = "Password has been change successfully";
        public  static  final  String WRONG_OLD_PASS = "Incorrect Password Entered";
        public  static  final  String NEW_PASS_MISMATCH = "New Password does not match";
        public  static  final  String SAME_OLD_PASS = "New Password should not match the old password";


        /*These are the view that would exist withing the dialog box*/
        EditText oldPasswdTextView,newPasswordTextView,confirmPasswordTextView;

        TextView messageTextView;


        /**************************************************************************************
         * This method is called when the dialog box is created
         *
         * Here I define the code needed to be initialized when the dialog box is created
         * @param savedInstanceState
         * @return
         */
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {


            //get the dialog's layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();


            //inflate the dialogs layout view
            View dialogRootView = inflater.inflate(R.layout.new_password, null);


            /*Initialize Edit text views of the dialog box */
             oldPasswdTextView = (EditText) dialogRootView.findViewById(R.id.old_password_view);
             newPasswordTextView = (EditText) dialogRootView.findViewById(R.id.new_password_view);
             confirmPasswordTextView  = (EditText) dialogRootView.findViewById(R.id.confirm_password_view);
             messageTextView = (TextView) dialogRootView.findViewById(R.id.message_textView);


            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            //set up the dialog's properties
            builder.setView(dialogRootView)
                    .setTitle("Change Password")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {

                        }//onClick Ends
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });

            // Create the AlertDialog object and return it
            return builder.create();
        }//onCreateDialog Ends

        /************************************************************
         * This is PasswordDialogFragment onStart call back method
         * It is called when the fragment is started .
         *
         * Here I define the various codes that should be initialized when activity
         * calls the onStart method
         *
         */
        @Override
        public void onStart() {
            super.onStart();

            //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
            final AlertDialog passwordDialog = (AlertDialog)getDialog();

            //check if the fragments password dialog box (defined in the onCreate()) has been initialize
            if(passwordDialog != null)
            {
                //Get and instance of the dialog's button
                Button positiveButton = (Button) passwordDialog.getButton(Dialog.BUTTON_POSITIVE);

                //set and its onClick listener for comfirming change password
                positiveButton.setOnClickListener(new View.OnClickListener()
                {

                    @Override
                    public void onClick(View v)
                    {
                        //get the passwords (old, new, and confirmation password) Entereed by the user
                        //and store them in their strings
                        String oldPassword = oldPasswdTextView.getText().toString().trim();
                        String newPassword = newPasswordTextView.getText().toString().trim();
                        String confirmPassword = confirmPasswordTextView.getText().toString().trim();

                        //pass the password in to the  updatePassword() method and store the string (message) it returns in the "message" variable
                        String message = passwordDialogInterface.updatePassword(oldPassword, newPassword, confirmPassword);


                        messageTextView.setText(message);


                        //use the switch-case logic to highlight (with red) the appropriate
                        //EditText view that contains an invalid input
                        switch (message){

                            case NEW_PASS_MISMATCH:

                                newPasswordTextView.setBackgroundColor(Color.RED);
                                confirmPasswordTextView.setBackgroundColor(Color.RED);
                                oldPasswdTextView.setBackgroundColor(Color.WHITE);
                                break;

                            case WRONG_OLD_PASS:

                                newPasswordTextView.setBackgroundColor(Color.WHITE);
                                confirmPasswordTextView.setBackgroundColor(Color.WHITE);
                                oldPasswdTextView.setBackgroundColor(Color.RED);

                                break;

                            case SUCCESS:
                                //wait for a few seconds so
                                // that user can see the message
                                passwordDialog.dismiss();
                                //display a toast message
                                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

                        }//switch-case ends
                    }//onClick() Ends

                });//new OnClickListener Ends
            }//if Ends
        }//onStart Ends

        /*****************************************************************
         * This is an interface used for communicatiing with this dialog
         */
        public interface MyInterface {

            public String updatePassword
                    (String oldPassword, String newPassword, String confirmedNewPassword);
        }//DialogInterface Class Ends


    }//PasswordDialogFragment Ends

}//MainTabActivity CLASS EDNS






/*==================================================================================================
 ***************************************************************************************************
 *=====================================INNER CLASS==================================================
 *MyRecyclerAdapter is used to create the Recycle view object needed by the "PAST TRIPS" Tabs to display
 * a the list of past trips
 */
class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.MyViewHolder>
{


    //this will hold the list of trips to be displayed
    private ArrayList<String> mRecycleViewResources;


    /***************************************************************
     *
     * @param mRecycleViewResources
     */
    public MyRecyclerAdapter(Collection<String> mRecycleViewResources)
    {
        this.mRecycleViewResources = new ArrayList<>(mRecycleViewResources);


    }

    /****************************************************************
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_main_tab, parent, false);

        MyViewHolder mViewHolder = new MyViewHolder(view);

        return mViewHolder;

    }//ViewHolder Ends

    /**
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position)
    {
        holder.dataTextView.setText(mRecycleViewResources.get(position));

        //you can register the onClick listener too
    }

    /**
     *
     * @param dataResource
     */
    public void addItem(String dataResource)
    {
        mRecycleViewResources.add(dataResource);

        notifyItemInserted(mRecycleViewResources.size()-1);
    }
    /**
     *
     * @return
     */
    @Override
    public int getItemCount()
    {
        return mRecycleViewResources.size();
    }//getItemCount() Ends

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    /**===================INNER Class To MyRecyclerAdapter=================
     *
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder
    {

        TextView dataTextView;

        /**
         *
         * @param itemView
         */
        public MyViewHolder(View itemView)
        {
            super(itemView);
            dataTextView = (TextView) itemView.findViewById(R.id.recycler_textview);

        }//ViewHolder constructor ends

    }//ViewHolder Ends


}//MyRecycler Adapter Ends

