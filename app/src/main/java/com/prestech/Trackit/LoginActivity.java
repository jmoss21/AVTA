package com.prestech.Trackit;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import TrackitDataObjects.Driver;

/**
 *The LoginActivity is designed to authenticate the user of application to the company's backend database.
 * It provides a mechanism for the driver to input credentials and returns a boolean representing the result
 * of the authentication attempt. The Login class will receive the username and password of the Driver and pass
 * credentials along to the AvtaDataModel package by instantiating a Driver Object.  The Driver Object will create
 * a DatabaseDriver class that connects to the backend database and authenticates the user through the method
 * validateCredentials.  The Login interface will transfer the user to the Home Screen once valid credentials
 * have been confirmed.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    //reference to activity's view objects
    private Button loginBtn;
    private TextView infoTextView;
    private EditText emailField;
    private EditText passwordField;

    private String email ;
    private String password;



    //Firebase database objects
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //database references
    private static DatabaseReference mDatabase;

    //
    private static Driver loggedInDriver;


    //reference to intent that will hold user's information
    public final static  String SHADOW = "SHADOW";


    /*********************************************************************
     *This is the activity's life cycle create call back method
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activtity_login);


        //initialize the views
        loginBtn = (Button)findViewById(R.id.login_btn);
        emailField = (EditText)findViewById(R.id.login_email_field);
        passwordField = (EditText)findViewById(R.id.login_passwd_field);
        infoTextView = (TextView) findViewById(R.id.login_info_display);


        //register onclick listeners
        loginBtn.setOnClickListener(this);

        //initialize Firebase object
        mAuth = FirebaseAuth.getInstance();



        //register the activity with a firebase authentication listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                //create a database user object
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if the user object contains user information
                if(user != null)
                {

                }//if ends
                else
                {
                }//else ends

            }//onAuthStateChanged() Ends
        };//mAuthListener Ends

        //location permission (move to previous class)

        SystemPermission.requestLocationPermission(LoginActivity.this);

    }//create() Ends



    /***********************************************************************
     * This is a menu call back method when the menu option is being created
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }//onOptionsItemSelected() Ends

    /************************************************************************
     *This is the activity lifecycle start callback method
     */
    @Override
    public void onStart() {
        super.onStart();

        //register the authListener when activity starts
        mAuth.addAuthStateListener(mAuthListener);

    }//onStart() Ends

    /************************************************************************
     *This is the activity lifecycle stop callback method
     */
    @Override
    public void onStop() {
        super.onStop();

        //remove the authentication listener when activity is stopped
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }//if ends

    }//onStop() Ends




    /***************************************************************************
     * This is method registers the login button with the onClick Event
     */
    @Override
    public void onClick(View view)
    {

        switch (view.getId())
        {
            case R.id.login_btn:
                email = emailField.getText().toString().trim();
                password = passwordField.getText().toString().trim();

                //call the sign_in method and pass the password and email to it
                if(!email.isEmpty() && !password.isEmpty())
                {
                    infoTextView.setText("");

                    signIn(email, password);

                }else{
                    infoTextView.setText("Email/Username and Password must be entered");
                }

                break;

        }
    }//onClick() Ends


    /*******************************************************************************
     * this method signs the user into the application. Recieve the user's email and
     * password as parameters.
     * @param email
     * @param password
     */
    private  void signIn(final String email, final String password)
    {
        //Uses the FirebaseAuth sign in method to log the user in
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        //displays this text if login is no successful
                        if (!task.isSuccessful()) {

                            infoTextView.setText("invalid credentials");
                        }
                        else
                        {
                            //create and intent for MainTabActvity
                            Intent mainActivityIntent = new Intent(LoginActivity.this, MainTabActivity.class);

                            //passes the password to the intent
                            mainActivityIntent.putExtra(SHADOW, password);

                            //makes sure that any old MainTabActivity is clear
                            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                            //start mainActivityIntent
                            startActivity(mainActivityIntent);
                        }//if-else ends
                    }//onComplete() Ends

                });//OnCompleteListener Ends
    }//signIn(String email, String password) Ends


}//LoginActivty Class Ends



