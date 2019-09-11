package com.example.cuplogin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    TextView userStatusTV;
    Button logoutBtn;
    DatabaseReference firebaseRef;
    String username = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logoutBtn = (Button) findViewById(R.id.logOutBtn);
        userStatusTV = (TextView) findViewById(R.id.userStatus);
        firebaseRef = FirebaseDatabase.getInstance().getReference();
        Intent receivedIntent = getIntent();
        if(receivedIntent != null)
        {
            username = receivedIntent.getStringExtra("username");
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // User is signed in
            String email = user.getUid();
            if(email != null)
            {
                userStatusTV.setText("Hi User, " + user.getEmail());
            }
        } else {
            // No user is signed in
            userStatusTV.setText("Error Loading Profile");
        }

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent I = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(I);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("userName", username);
    }
}
