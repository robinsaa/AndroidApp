package com.example.cuplogin;

import android.os.Bundle;

import com.example.cuplogin.Database.AppDatabase;
import com.example.cuplogin.Database.Sale;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ShowDatabaseActivity extends AppCompatActivity {

    TextView dbContent;
    List<Sale> mSales = new ArrayList<>();
    AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_database);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dbContent = findViewById(R.id.dbContent);

        mDb= Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "salesDb").allowMainThreadQueries().fallbackToDestructiveMigration().build();

        showDatabaseData();
    }


    private void showDatabaseData()
    {
        mSales= mDb.saleDao().getAll();
        if (mSales.size() != 0) {

            String text = "";
            for (Sale sale : mSales ){
                text += sale.getCafeId() +  ", " + sale.getCupId() +", "+ sale.getTimestamp()+"\n";
            }
            dbContent.setText(text);

        }
    }


}
