package com.example.cuplogin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cuplogin.Database.AppDatabase;
import com.example.cuplogin.Database.Sale;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView userStatusTV,dbContent;
    Button logoutBtn,barcodeScanBtn,showDbButton;
    List<Sale> mSales = new ArrayList<>();
    DatabaseReference firebaseRef;
    String username = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logoutBtn = (Button) findViewById(R.id.logOutBtn);
        barcodeScanBtn = (Button) findViewById(R.id.scanBarcodeBtn);
        userStatusTV = (TextView) findViewById(R.id.userStatus);
        dbContent = (TextView) findViewById(R.id.dbContent);
        showDbButton = findViewById(R.id.viewDbBtn);

        final AppDatabase mDb = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "salesDb").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        
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



        showDbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSales= mDb.saleDao().getAll();
                if (mSales.size() != 0) {

                    String text = "";
                    for (Sale sale : mSales ){
                        text += sale.getCafeId() +  ", " + sale.getCupId() +", "+ sale.getTimestamp()+"\n";
                    }
                    dbContent.setText(text);

                }
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent I = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(I);
            }
        });

        barcodeScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent I = new Intent(MainActivity.this, BarcodeScanActivity.class);
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
