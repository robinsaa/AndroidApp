package com.example.cuplogin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.example.cuplogin.Api.APIService;
import com.example.cuplogin.Api.ApiUtils;
import com.example.cuplogin.Database.AppDatabase;
import com.example.cuplogin.Database.Return_Record;
import com.example.cuplogin.Database.Sale;
import com.example.cuplogin.Model.BatchReturnApiBody;
import com.example.cuplogin.Model.BatchSalesApiBody;
import com.example.cuplogin.Model.ReturnApiBody;
import com.example.cuplogin.Model.SaleApiBody;
import com.google.gson.Gson;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowDatabaseActivity extends AppCompatActivity {

    TextView dbContent,errorMessage;
    static Context mContext;
    static List<Sale> mSales = new ArrayList<>();
    static List<Return_Record> mReturns = new ArrayList<>();

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
        SharedPreferences mCafePref = getApplicationContext().getSharedPreferences("BorrowCupPref",Context.MODE_PRIVATE);
        CAFE_ID = mCafePref.getString("cafe_id",null);
        USER_TYPE = mCafePref.getString("user_type",null);

        showDatabaseData();

    }


    private void showDatabaseData() {

        if(USER_TYPE.equals("cafe")){
            mSales = mDb.appDao().getAllSales();
            Toast.makeText(mContext,"Showing " + USER_TYPE, Toast.LENGTH_SHORT).show();

            Log.d("Database", "Cafe ID : " + CAFE_ID );

            if (mSales.size() != 0) {
                errorMessage.setVisibility(View.GONE);
                String text = "";
                for (Sale sale : mSales) {
                    text += sale.getCafeId() + ", " + sale.getCupId() + ", " + sale.getTimestamp() + "\n";
                }
                dbContent.setText(text);


                if (mSales.size() >= 10) {
                    Log.d("Database", "Database Triggered to post to api");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                        new SendBatchSalesRecords().execute();
                    }

                }

                Toast.makeText(mContext,"Showing " + mSales.size() + " Sale Records",Toast.LENGTH_SHORT).show();
            }
            else{
                errorMessage.setVisibility(View.VISIBLE);
            }

        }
        else if(USER_TYPE.equals("dishwasher")){
            mReturns = mDb.appDao().getAllReturns();
            Toast.makeText(mContext,"Showing " + USER_TYPE,Toast.LENGTH_SHORT).show();

            Log.d("Database", "Dishwasher ID : " + CAFE_ID );

            if (mReturns.size() != 0) {
                errorMessage.setVisibility(View.GONE);
                String text = "";
                for (Return_Record returnRec : mReturns) {
                    text += returnRec.getCupId() + ", " + returnRec.getBinId() + ", " + returnRec.getDishwasherId() +", "+ returnRec.getScannedAt() + "\n";
                }
                dbContent.setText(text);


                if (mReturns.size() >= 10) {
                    Log.d("Database", "Database Triggered to post to api");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                        new SendBatchReturnRecords().execute();
                    }

                }

                Toast.makeText(mContext,"Showing " + mReturns.size() + " Return Records",Toast.LENGTH_SHORT).show();
            }
            else{
                errorMessage.setVisibility(View.VISIBLE);
            }

        }

    }



    private class SendBatchSalesRecords extends AsyncTask<String, Void, Void> {

        //Handle Http POST Request for a specific url
        public void httpConnectionPostRequest(String apiUrl, String strJsonData, int[] mSalesId) {
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
                Log.d("Data", strJsonData);
                PrintWriter out = new PrintWriter(conn.getOutputStream());
                out.print(strJsonData);
                out.close();
                String responseCode = Integer.valueOf(conn.getResponseCode()).toString();

                Log.i("RESPONSE", responseCode);
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
            int[] mSalesId = new int[mSales.size()];

            for (int i = 0; i < mSales.size(); i++) {
                SaleApiBody sale = new SaleApiBody(mSales.get(i).getCupId(),
                        mSales.get(i).getCafeId(), mSales.get(i).getTimestamp());
                saleApiBody.add(sale);
                mSalesId[i] = mSales.get(i).getSid();
            }
            BatchSalesApiBody batchSalesApiBody = new BatchSalesApiBody(saleApiBody);
            Gson gson = new Gson();
            String json = gson.toJson(batchSalesApiBody.getSaleApiBodyList());
            String fullRestApiUrl = "***REMOVED***";
            Log.d("Batch IDS", json);
            Log.d("Batch IDS", String.valueOf(mSalesId.length));

            httpConnectionPostRequest(fullRestApiUrl, json ,mSalesId);

    //-- TODO Part to Figure out as the Retrofit doesnt seem to work when array is to be passed in POST--//

    //            mAPIService.sendBatchSaleRecords(batchSalesApiBody.getSaleApiBodyList()).enqueue(new Callback<List<SaleApiBody>>() {
    //                @Override
    //                public void onResponse(Call<List<SaleApiBody>> call, Response<List<SaleApiBody>> response) {
    //                    if(response.isSuccessful()) {
    //                        Log.d("Delete:","In delete Sale Record");
    ////                        for(int i=0;i<2;i++) {
    ////                            Sale saleRecord = mDb.appDao()().findById(mSalesId[i]);
    ////                            mDb.appDao()().delete(saleRecord);
    ////                        }
    ////                        Log.d("Delete:", String.valueOf(mDb.appDao()().getAll().size()));
    ////                        mSales.clear();
    //
    //                        Log.d("RESPONSE",response.body().toString());
    ////                        Log.d("RESPONSE", saleApiBody.toString());
    //                        Log.i("RESPONSE", "post submitted to API." + response.body().toString());
    //
    //                        return;
    //                    }
    //
    //                }
    //
    //                @Override
    //                public void onFailure(Call<List<SaleApiBody>> call, Throwable t) {
    //                    t.printStackTrace();
    //                }
    //
    //
    //            });

            return null;
        }


        public void deleteSaleRecordById(int[] mSalesId) {
            Log.d("Delete:","In delete Sale Record");
            for(int i=0;i<mSalesId.length;i++) {
                Sale saleRecord = mDb.appDao().findSaleById(mSalesId[i]);
                mDb.appDao().deleteSale(saleRecord);
            }
            Log.d("Delete:", String.valueOf(mDb.appDao().getAllSales().size()));
            mSales.clear();
            finish();
            startActivity(getIntent());
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
                Log.d("Data", strJsonData);
                PrintWriter out = new PrintWriter(conn.getOutputStream());
                out.print(strJsonData);
                out.close();
                String responseCode = Integer.valueOf(conn.getResponseCode()).toString();
                Log.i("RESPONSE",responseCode );
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
            String fullRestApiUrl = "***REMOVED***";
            Log.d("URL", json);
            Log.d("Batch IDS", String.valueOf(mReturnsId.length));

            httpConnectionPostRequest(fullRestApiUrl, json ,mReturnsId);

            //-- TODO Part to Figure out as the Retrofit doesnt seem to work when array is to be passed in POST--//

            //            mAPIService.sendBatchSaleRecords(batchSalesApiBody.getSaleApiBodyList()).enqueue(new Callback<List<SaleApiBody>>() {
            //                @Override
            //                public void onResponse(Call<List<SaleApiBody>> call, Response<List<SaleApiBody>> response) {
            //                    if(response.isSuccessful()) {
            //                        Log.d("Delete:","In delete Sale Record");
            ////                        for(int i=0;i<2;i++) {
            ////                            Sale saleRecord = mDb.appDao()().findById(mSalesId[i]);
            ////                            mDb.appDao()().delete(saleRecord);
            ////                        }
            ////                        Log.d("Delete:", String.valueOf(mDb.appDao()().getAll().size()));
            ////                        mSales.clear();
            //
            //                        Log.d("RESPONSE",response.body().toString());
            ////                        Log.d("RESPONSE", saleApiBody.toString());
            //                        Log.i("RESPONSE", "post submitted to API." + response.body().toString());
            //
            //                        return;
            //                    }
            //
            //                }
            //
            //                @Override
            //                public void onFailure(Call<List<SaleApiBody>> call, Throwable t) {
            //                    t.printStackTrace();
            //                }
            //
            //
            //            });

            return null;
        }


        public void deleteReturnRecordById(int[] mReturnsId) {
            Log.d("Delete:","In delete Return Record");
            for(int i=0;i<mReturnsId.length;i++) {
                Return_Record returnRecord = mDb.appDao().findReturnById(mReturnsId[i]);
                mDb.appDao().deleteReturn(returnRecord);
            }
            Log.d("Deleted:", String.valueOf(mDb.appDao().getAllReturns().size()));
            mReturns.clear();
            finish();
            startActivity(getIntent());
        }


    }
}


