package com.example.cuplogin;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.room.Room;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
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

import static com.example.cuplogin.BarcodeScanActivity.mSales;

public class BarcodeScanActivity extends AppCompatActivity {

    TextView barcodeValueTV;
    CameraSource cameraSource;
    SurfaceView surfaceView;
    BarcodeDetector barcodeDetector;
    final int REQUEST_CAMERA_PERMISSION_ID = 1001;
    boolean MOVED_TO_DATABASE = false;
    private AppDatabase mDb;
    String CAFE_ID,USER_TYPE;
    private static final String TAG = "ExampleJobService";

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
            mDb.appDao().insertToSale(new Sale(size+1, CAFE_ID, barcodeValue, dateInTimeZone));
            MOVED_TO_DATABASE = true;
            customToast("Recorded Sale Cup Details!");
        }
        else if(USER_TYPE.equals("dishwasher")){
            int size = getDatabaseCount();
            mDb.appDao().insertToReturn(new Return_Record(size+1, barcodeValue,null, CAFE_ID, dateInTimeZone));
            MOVED_TO_DATABASE = true;
            customToast("Recorded Return Cup Details!");
        }

    }

//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    public void scheduleJob() {
//        ComponentName componentName = new ComponentName(this, BackgroundService.class);
//        JobInfo info = new JobInfo.Builder(123, componentName)
//                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
//                .setPersisted(true)
//                .setPeriodic(15 * 60 *1000)
//                .build();
//        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
//        int resultCode = jobScheduler.schedule(info);
//        if(resultCode == JobScheduler.RESULT_SUCCESS){
//            Log.d(TAG, "Job Scheduled(Activity)");
//        }
//        else{
//            Log.d(TAG, "Job Scheduling failed");
//        }
//    }

//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    public void cancelJob(View view) {
//        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
//        jobScheduler.cancel(123);
//        Log.d(TAG, "Job Cancelled");
//
//    }
//

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






















}

