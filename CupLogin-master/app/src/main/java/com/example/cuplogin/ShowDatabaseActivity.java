package com.example.cuplogin;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.example.cuplogin.Api.APIService;
import com.example.cuplogin.Api.ApiUtils;
import com.example.cuplogin.Database.AppDatabase;
import com.example.cuplogin.Database.Sale;
import com.example.cuplogin.Model.BatchSalesApiBody;
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

    static AppDatabase mDb;

    private static APIService mAPIService;

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
        mAPIService = ApiUtils.getAPIService();
        showDatabaseData();

    }


    private void showDatabaseData() {
        mSales = mDb.saleDao().getAll();
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
                Log.i("RESPONSE", Integer.valueOf(conn.getResponseCode()).toString());
                deleteSaleRecordById(mSalesId);
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
            int[] mSalesId = new int[10];

            for (int i = 0; i < 10; i++) {
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
    ////                            Sale saleRecord = mDb.saleDao().findById(mSalesId[i]);
    ////                            mDb.saleDao().delete(saleRecord);
    ////                        }
    ////                        Log.d("Delete:", String.valueOf(mDb.saleDao().getAll().size()));
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
            for(int i=0;i<10;i++) {
                Sale saleRecord = mDb.saleDao().findById(mSalesId[i]);
                mDb.saleDao().delete(saleRecord);
            }
            Log.d("Delete:", String.valueOf(mDb.saleDao().getAll().size()));
            mSales.clear();
            finish();
            startActivity(getIntent());
        }
    }
}


