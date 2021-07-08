package com.example.cuplogin;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import com.example.cuplogin.Database.AppDatabase;
import com.example.cuplogin.Database.Return_Record;
import com.example.cuplogin.Database.Sale;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

public class ShowDatabaseActivity extends AppCompatActivity {

    TextView dbContent, errorMessage;
    static Context mContext;
    static List<Sale> mSales = new ArrayList<>();
    static List<Return_Record> mReturns = new ArrayList<>();
    private static final String TAG = "ExampleJobService";

    static AppDatabase mDb;
    String CAFE_ID = null;
    String USER_TYPE = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_database);
        dbContent = findViewById(R.id.dbContent);
        errorMessage = findViewById(R.id.errorMessage);
        mContext = getApplicationContext();

        mDb = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "salesDb")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        SharedPreferences mCafePref = getApplicationContext().getSharedPreferences("BorrowCupPref", Context.MODE_PRIVATE);
        CAFE_ID = mCafePref.getString("cafe_id", null);
        USER_TYPE = mCafePref.getString("user_type", null);

        showDatabaseData();

    }


    private void showDatabaseData() {

        if (USER_TYPE.equals("cafe")) {
            mSales = mDb.appDao().getAllSales();
            Toast.makeText(mContext, "Showing " + USER_TYPE, Toast.LENGTH_SHORT).show();

            Log.d("Database", "Cafe ID : " + CAFE_ID);

            if (mSales.size() != 0) {
                errorMessage.setVisibility(View.GONE);
                String text = "";
                for (Sale sale : mSales) {
                    text += sale.getCafeId() + ", " + sale.getCupId() + ", " + sale.getTimestamp() + "\n";
                }
                dbContent.setText(text);


                Toast.makeText(mContext, "Showing " + mSales.size() + " Sale Records", Toast.LENGTH_SHORT).show();
            } else {
                errorMessage.setVisibility(View.VISIBLE);
            }

        } else if (USER_TYPE.equals("dishwasher")) {
            mReturns = mDb.appDao().getAllReturns();
            Toast.makeText(mContext, "Showing " + USER_TYPE, Toast.LENGTH_SHORT).show();

            Log.d("Database", "Dishwasher ID : " + CAFE_ID);

            if (mReturns.size() != 0) {
                errorMessage.setVisibility(View.GONE);
                String text = "";
                for (Return_Record returnRec : mReturns) {
                    text += returnRec.getCupId() + ", " + returnRec.getBinId() + ", " + returnRec.getDishwasherId() + ", " + returnRec.getScannedAt() + "\n";
                }
                dbContent.setText(text);


                Toast.makeText(mContext, "Showing " + mReturns.size() + " Return Records", Toast.LENGTH_SHORT).show();
            } else {
                errorMessage.setVisibility(View.VISIBLE);
            }

        }

    }


//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    public void scheduleJob(View view) {
//        ComponentName componentName = new ComponentName(this, BackgroundService.class);
//        JobInfo info = new JobInfo.Builder(123, componentName)
//                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
//                .setPersisted(true)
//                .setPeriodic(15 * 60 *1000)
//                .build();
//
//        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
//        int resultCode = jobScheduler.schedule(info);
//        if(resultCode == JobScheduler.RESULT_SUCCESS){
//            Log.d(TAG, "Job Scheduled(Activity)");
//        }
//        else{
//            Log.d(TAG, "Job Scheduling failed");
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    public void cancelJob(View view) {
//        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
//        jobScheduler.cancel(123);
//        Log.d(TAG, "Job Cancelled");
//
//    }
}