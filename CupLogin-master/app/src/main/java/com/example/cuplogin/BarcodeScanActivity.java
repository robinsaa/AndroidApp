package com.example.cuplogin;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.room.Room;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cuplogin.Database.AppDatabase;
import com.example.cuplogin.Database.Return_Record;
import com.example.cuplogin.Database.Sale;
import com.example.cuplogin.Model.BatchReturnApiBody;
import com.example.cuplogin.Model.BatchSalesApiBody;
import com.example.cuplogin.Model.ReturnApiBody;
import com.example.cuplogin.Model.SaleApiBody;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BarcodeScanActivity extends AppCompatActivity {

    TextView barcodeValueTV;
    CameraSource cameraSource;
    SurfaceView surfaceView;
    BarcodeDetector barcodeDetector;
    final int REQUEST_CAMERA_PERMISSION_ID = 1001;
    boolean MOVED_TO_DATABASE = false;
    private AppDatabase mDb;
    String CAFE_ID,USER_TYPE;

    static List<Sale> mSales = new ArrayList<>();
    static List<Return_Record> mReturns = new ArrayList<>();

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case REQUEST_CAMERA_PERMISSION_ID:{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                        }
                    try {
                        cameraSource.start(surfaceView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scan);

        barcodeValueTV = findViewById(R.id.barcodeValueTV);
        surfaceView = findViewById(R.id.surfaceView);

        SharedPreferences mCafePref = getApplicationContext().getSharedPreferences("BorrowCupPref",Context.MODE_PRIVATE);
        CAFE_ID = mCafePref.getString("cafe_id",null);
        USER_TYPE = mCafePref.getString("user_type",null);

        mDb = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "salesDb").allowMainThreadQueries()
                .fallbackToDestructiveMigration().build();

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE).build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(24)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(640, 480).build();



        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(BarcodeScanActivity.this,new String[]{Manifest.permission.CAMERA},REQUEST_CAMERA_PERMISSION_ID);
                        return;
                    }
                }
                try {
                    cameraSource.start(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        final long[] lastTimestamp = {0};
        final Set<String> idSet = new HashSet<String>();


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();
                final int DELAY = 1000;

                if (System.currentTimeMillis() - lastTimestamp[0] < DELAY) {
                    return;
                }

                if(qrCodes.size() != 0 && !(idSet.contains(qrCodes.valueAt(0).displayValue)))
                {
                    idSet.add(qrCodes.valueAt(0).displayValue);
                    lastTimestamp[0] = System.currentTimeMillis();
                    Log.d("SCAN ID's", String.valueOf(idSet));

                    final String qrCodeValue = qrCodes.valueAt(0).displayValue;
                    runOnUiThread(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void run() {
                            Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(1000);
                            saveToDB(qrCodeValue);
                        }
                    });
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveToDB(String barcodeValue) {

        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX");
        final String dateInTimeZone  = dateFormat.format(new Date());

        if(USER_TYPE.equals("cafe")){
            int size = getDatabaseCount();
            if(size >= 10)
            {
                mSales = mDb.appDao().getAllSales();
                Log.d("Database", "Database Triggered to post to sale table of API");
                new SendBatchSalesRecords().execute();
            }
            mDb.appDao().insertToSale(new Sale(size+1, CAFE_ID, barcodeValue, dateInTimeZone));
            MOVED_TO_DATABASE = true;
            customToast("Recorded Sale Cup Details!");
        }
        else if(USER_TYPE.equals("dishwasher")){
            int size = getDatabaseCount();
            if (size >= 10) {
                mReturns = mDb.appDao().getAllReturns();
                Log.d("Database", "Database Triggered to post to return table of API");
                new SendBatchReturnRecords().execute();
            }
            mDb.appDao().insertToReturn(new Return_Record(size+1, barcodeValue,null, CAFE_ID, dateInTimeZone));
            MOVED_TO_DATABASE = true;
            customToast("Recorded Return Cup Details!");

        }

    }

    public void customToast(String message) {

        Toast toast = Toast.makeText(BarcodeScanActivity.this, message, Toast.LENGTH_SHORT);
        View view = toast.getView();

        //To change the Background of Toast
        view.setBackgroundColor(getResources().getColor(R.color.colorPantone));
        TextView text = (TextView) view.findViewById(android.R.id.message);

        //Shadow of the Of the Text Color
        text.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
        text.setTextColor(Color.WHITE);
        text.setTextSize(Integer.valueOf(getResources().getString(R.string.text_size)));
        toast.show();
    }

    private int getDatabaseCount() {
        if(USER_TYPE.equals("cafe")){
            List<Sale> mSales = mDb.appDao().getAllSales();
            if(mSales.size() > 0)
            {
                return mSales.size();
            }
            else {
                return 0;
            }

        }
        else if(USER_TYPE.equals("dishwasher")){
            List<Return_Record> mReturns = mDb.appDao().getAllReturns();
            if(mReturns.size() > 0)
            {
                return mReturns.size();
            }
            else {
                return 0;
            }
        }

        return 0;
    }

    private class SendBatchSalesRecords extends AsyncTask<String, Void, String> {

        int[] mSalesId;
        //Handle Http POST Request for a specific url
        public String httpConnectionPostRequest(String apiUrl, String strJsonData, int[] mSalesId) {
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
                Log.d("Data", strJsonData);
                PrintWriter out = new PrintWriter(conn.getOutputStream());
                out.print(strJsonData);
                out.close();
                responseCode = Integer.valueOf(conn.getResponseCode()).toString();
                Log.i("RESPONSE", responseCode);


            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return responseCode;
        }

        @Override
        protected String doInBackground(String... strings) {

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
//            String fullRestApiUrl = "***REMOVED***";

//            Testing URL
            String fullRestApiUrl = "***REMOVED***";

            Log.d("Batch IDS", json);
            Log.d("Batch IDS", String.valueOf(mSalesId.length));


            return httpConnectionPostRequest(fullRestApiUrl, json ,mSalesId);
        }

        protected void onPostExecute(String result) {
            Log.d("Batch IDS", result);

            if(result.equals("201") || result.equals("200")){
                deleteSaleRecordById(mSalesId);
            }
        }


        public void deleteSaleRecordById(int[] mSalesId) {
            Log.d("Delete:","In delete Sale Record");
            for(int i=0;i<mSalesId.length;i++) {
                Sale saleRecord = mDb.appDao().findSaleById(mSalesId[i]);
                mDb.appDao().deleteSale(saleRecord);
            }
            Log.d("After Delete Size:", String.valueOf(mDb.appDao().getAllSales().size()));
            mSales.clear();
//            finish();
//            startActivity(getIntent());

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
//            Production URL
//            String fullRestApiUrl = "***REMOVED***";

//            Testing URL
            String fullRestApiUrl = "***REMOVED***";

            Log.d("URL", json);
            Log.d("Batch IDS", String.valueOf(mReturnsId.length));

            httpConnectionPostRequest(fullRestApiUrl, json ,mReturnsId);

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

