package com.example.cuplogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cuplogin.Database.AppDatabase;
import com.example.cuplogin.Database.Sale;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //Variables
    TextView userStatusTV;
    Button barcodeScanBtn;
    DatabaseReference firebaseRef;
    String fullName = "User";
    String cafeId = "0";

    String UID = null;
    Toolbar toolbar;
    FirebaseAuth firebaseAuth;
    DatabaseReference mUserRef;
    SharedPreferences mUserPref;
    SharedPreferences.Editor mPrefEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUserPref = getApplicationContext().getSharedPreferences("BorrowCupPref", 0);
        mPrefEditor = mUserPref.edit();


        //Setup UI Elements
        barcodeScanBtn = (Button) findViewById(R.id.scanBarcodeBtn);
        userStatusTV = (TextView) findViewById(R.id.userStatus);
        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        //Handle authentication
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseRef = FirebaseDatabase.getInstance().getReference();

        if (firebaseAuth.getCurrentUser() != null) {
            UID = firebaseAuth.getCurrentUser().getUid();

            mUserRef = firebaseRef.child(UID);
            mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.exists())
                    {
                        if(dataSnapshot.hasChild("cafe-id"))
                        {
                            String cafeId = dataSnapshot.child("cafe-id").getValue(String.class);
                            String userType = dataSnapshot.child("user-role").getValue(String.class);

                            mPrefEditor.putString("cafe_id", cafeId);
                            mPrefEditor.putString("user_type", userType);
                            mPrefEditor.apply();

                            fullName = dataSnapshot.child("full_name").getValue(String.class);
                            if (fullName != null) {
                                userStatusTV.setText("Hi, " + fullName);

                            } else {
                                // No user is signed in
                                userStatusTV.setText("Error Loading Profile");
                            }

                        }

                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }



        barcodeScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent I = new Intent(MainActivity.this, BarcodeScanActivity.class);
                startActivity(I);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.logout:
                logOut();
                return true;
            case R.id.showdb:
                Intent I = new Intent(MainActivity.this, ShowDatabaseActivity.class);
                startActivity(I);
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("fullName", fullName);
    }

    private void logOut(){
        FirebaseAuth.getInstance().signOut();
        finish();
        Intent I = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(I);

    }




}
