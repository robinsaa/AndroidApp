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
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cuplogin.Database.AppDatabase;
import com.example.cuplogin.Database.Return_Record;
import com.example.cuplogin.Database.Sale;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
                AppDatabase.class, "salesDb").allowMainThreadQueries().fallbackToDestructiveMigration().build();

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
                final int DELAY = 2000;

                if (System.currentTimeMillis() - lastTimestamp[0] < DELAY) {
                    return;
                }

                if(qrCodes.size() != 0 && !(idSet.contains(qrCodes.valueAt(0).displayValue)))
                {
                    idSet.add(qrCodes.valueAt(0).displayValue);
                    lastTimestamp[0] = System.currentTimeMillis();
                    Log.d("SCAN", String.valueOf(idSet));

                    runOnUiThread(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void run() {
                            Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(1000);
                            barcodeValueTV.setText(qrCodes.valueAt(0).displayValue);

                            saveToDB(barcodeValueTV.getText().toString());
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
            int size = getDatabseCount();
            mDb.appDao().insertToSale(new Sale(size+1, CAFE_ID, barcodeValue, dateInTimeZone));
            MOVED_TO_DATABASE = true;
            Toast.makeText(getApplicationContext(),"Recorded Sale Cup Details!",Toast.LENGTH_SHORT).show();
        }
        else if(USER_TYPE.equals("dishwasher")){
            int size = getDatabseCount();
            mDb.appDao().insertToReturn(new Return_Record(size+1, barcodeValue,null, CAFE_ID, dateInTimeZone));
            MOVED_TO_DATABASE = true;
            Toast.makeText(getApplicationContext(),"Recorded Return Cup Details!",Toast.LENGTH_SHORT).show();

        }

    }

    private int getDatabseCount() {
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
