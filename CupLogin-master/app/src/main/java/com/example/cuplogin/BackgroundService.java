package com.example.cuplogin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.room.Room;

import com.example.cuplogin.Database.AppDatabase;
import com.example.cuplogin.Database.Return_Record;
import com.example.cuplogin.Database.Sale;
import com.example.cuplogin.Model.BatchReturnApiBody;
import com.example.cuplogin.Model.BatchSalesApiBody;
import com.example.cuplogin.Model.ReturnApiBody;
import com.example.cuplogin.Model.SaleApiBody;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.gson.Gson;

import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BackgroundService extends JobService {

    private static final String TAG = "JobService";
    private boolean jobCancelled = false;
    String CAFE_ID,USER_TYPE;

    private AppDatabase mDb;

    static List<Sale> mSales = new ArrayList<>();
    static List<Return_Record> mReturns = new ArrayList<>();

    private void doBackgroundWork(final JobParameters params){

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(USER_TYPE.equals("cafe")) {
                    Log.d(TAG, "On job start Sales Count " + mSales.size());
                    new SendBatchSalesRecords().execute();
                }
                else if(USER_TYPE.equals("dishwasher")) {
                    Log.d(TAG, "On job start Return Count " + mReturns.size());
                    new SendBatchReturnRecords().execute();
                }

                if(jobCancelled){
                    return;
                }
                Log.d(TAG,"Job Finished");
                jobFinished(params,false);
            }
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "Job cancelled before completion");

        if(USER_TYPE.equals("cafe")) {
            new SendBatchSalesRecords().cancel(true);
        }
        else if(USER_TYPE.equals("dishwasher")) {
            new SendBatchReturnRecords().cancel(true);
        }

        jobCancelled = true;
        return false;
    }

    @Override
    public boolean onStartJob(@NonNull JobParameters job) {
        Log.d(TAG, "Job Started!");

        mDb = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "salesDb").allowMainThreadQueries()
                .fallbackToDestructiveMigration().build();

        SharedPreferences mCafePref = getApplicationContext().getSharedPreferences("BorrowCupPref",Context.MODE_PRIVATE);
        CAFE_ID = mCafePref.getString("cafe_id",null);
        USER_TYPE = mCafePref.getString("user_type",null);
        mSales = mDb.appDao().getAllSales();
        mReturns = mDb.appDao().getAllReturns();


        doBackgroundWork(job);

        return true;
    }

    private class SendBatchSalesRecords extends AsyncTask<String, Void, Void> {

        int[] mSalesId;
        //Handle Http POST Request for a specific url
        public void httpConnectionPostRequest(String apiUrl, String strJsonData, int[] mSalesId) {
            //initialise
            URL url = null;
            String responseCode = null;
            HttpURLConnection conn = null;
            try {
                url = new URL(apiUrl);
                //open the connection
                conn = (HttpURLConnection) url.openConnection();
                //set the timeout
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                //set the connection method to POST
                conn.setRequestMethod("POST");
                //set the output to true
                conn.setDoOutput(true);
                //set length of the data you want to send
                conn.setFixedLengthStreamingMode(strJsonData.getBytes().length);
                //add HTTP headers
                conn.setRequestProperty("Content-Type", "application/json");
                //Send the POST out
                Log.d(TAG, strJsonData);
                PrintWriter out = new PrintWriter(conn.getOutputStream());
                out.print(strJsonData);
                out.close();
                responseCode = Integer.valueOf(conn.getResponseCode()).toString();
                Log.i(TAG, responseCode);
                if(responseCode.equals("201") || responseCode.equals("200")){
                    deleteSaleRecordById(mSalesId);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        @Override
        protected Void doInBackground(String... strings) {

            List<SaleApiBody> saleApiBody = new ArrayList<>();
            mSalesId = new int[mSales.size()];

            for (int i = 0; i < mSales.size(); i++) {
                SaleApiBody sale = new SaleApiBody(mSales.get(i).getCupId(),
                        mSales.get(i).getCafeId(), mSales.get(i).getTimestamp());
                saleApiBody.add(sale);
                mSalesId[i] = mSales.get(i).getSid();
            }
            BatchSalesApiBody batchSalesApiBody = new BatchSalesApiBody(saleApiBody);
            Gson gson = new Gson();
            String json = gson.toJson(batchSalesApiBody.getSaleApiBodyList());

//            Production URL
//            String fullRestApiUrl = BuildConfig.PROD_SALE_URL;

//            Testing URL
            String fullRestApiUrl = BuildConfig.TEST_SALE_URL;

            Log.d(TAG, json);
            Log.d(TAG, "Before sending to api sales array size" + String.valueOf(mSalesId.length));


            httpConnectionPostRequest(fullRestApiUrl, json ,mSalesId);
            return null;
        }


        public void deleteSaleRecordById(int[] mSalesId) {
            Log.d("Delete:","In delete Sale Record");
            for(int i=0;i<mSalesId.length;i++) {
                Sale saleRecord = mDb.appDao().findSaleById(mSalesId[i]);
                mDb.appDao().deleteSale(saleRecord);
            }
            Log.d("After Delete Size:", String.valueOf(mDb.appDao().getAllSales().size()));
            mSales.clear();

        }
    }



    private class SendBatchReturnRecords extends AsyncTask<String, Void, Void> {

        //Handle Http POST Request for a specific url
        public void httpConnectionPostRequest(String apiUrl, String strJsonData, int[] mReturnsId) {
            //initialise
            URL url = null;
            HttpURLConnection conn = null;
            try {
                url = new URL(apiUrl);
                //open the connection
                conn = (HttpURLConnection) url.openConnection();
                //set the timeout
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                //set the connection method to POST
                conn.setRequestMethod("POST");
                //set the output to true
                conn.setDoOutput(true);
                //set length of the data you want to send
                conn.setFixedLengthStreamingMode(strJsonData.getBytes().length);
                //add HTTP headers
                conn.setRequestProperty("Content-Type", "application/json");
                //Send the POST out
                Log.d(TAG, strJsonData);
                PrintWriter out = new PrintWriter(conn.getOutputStream());
                out.print(strJsonData);
                out.close();
                String responseCode = Integer.valueOf(conn.getResponseCode()).toString();
                Log.i(TAG,responseCode );
                if(responseCode.equals("201") || responseCode.equals("200")){
                    deleteReturnRecordById(mReturnsId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

        }

        @Override
        protected Void doInBackground(String... strings) {

            List<ReturnApiBody> returnApiBody = new ArrayList<>();
            int[] mReturnsId = new int[mReturns.size()];

            for (int i = 0; i < mReturns.size(); i++) {
                ReturnApiBody return_cup = new ReturnApiBody(mReturns.get(i).getCupId(),null,mReturns.get(i).getDishwasherId(),mReturns.get(i).getScannedAt());
                returnApiBody.add(return_cup);
                mReturnsId[i] = mReturns.get(i).getRid();
            }

            BatchReturnApiBody batchReturnApiBody = new BatchReturnApiBody(returnApiBody);
            Gson gson = new Gson();
            String json = gson.toJson(batchReturnApiBody.getReturnApiBodyList());

//            Production URL
//            String fullRestApiUrl = BuildConfig.PROD_RETURN_URL;

//            Testing URL
            String fullRestApiUrl = BuildConfig.TEST_RETURN_URL;

            Log.d(TAG, json);
            Log.d(TAG, String.valueOf(mReturnsId.length));

            httpConnectionPostRequest(fullRestApiUrl, json ,mReturnsId);

            return null;
        }


        public void deleteReturnRecordById(int[] mReturnsId) {
            Log.d(TAG,"In delete Return Record");
            Log.d(TAG,"No of cups to be deleted :" + mReturnsId.length);
            Log.d(TAG,"Cup ID's for deletion :" + Arrays.toString(mReturnsId));

            for(int i=0; i< mReturnsId.length; i++) {
                Return_Record returnRecord = mDb.appDao().findReturnById(mReturnsId[i]);
                mDb.appDao().deleteReturn(returnRecord);
            }

            Log.d(TAG, "After deletion (new call to DB) count : " + String.valueOf(mDb.appDao().getAllReturns().size()));
            mReturns.clear();
        }


    }

}
