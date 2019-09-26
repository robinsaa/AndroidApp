package com.example.cuplogin;

import android.os.AsyncTask;
import android.os.Bundle;

import com.example.cuplogin.Database.AppDatabase;
import com.example.cuplogin.Database.Sale;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ShowDatabaseActivity extends AppCompatActivity {

    TextView dbContent;
    Button sendBtn;
    static List<Sale> mSales = new ArrayList<>();
    AppDatabase mDb;

    private static APIService mAPIService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_database);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dbContent = findViewById(R.id.dbContent);
        sendBtn = findViewById(R.id.sendSale);

        mDb = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "salesDb").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        mAPIService = ApiUtils.getAPIService();
        showDatabaseData();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendSaleRecord().execute();
            }
        });
    }


    private void showDatabaseData() {
        mSales = mDb.saleDao().getAll();
        if (mSales.size() != 0) {

            String text = "";
            for (Sale sale : mSales) {
                text += sale.getCafeId() + ", " + sale.getCupId() + ", " + sale.getTimestamp() + "\n";
            }
            dbContent.setText(text);

        }
    }


    private static class SendSaleRecord extends AsyncTask<String, Void, Void> {


        @Override
        protected Void doInBackground(String... strings) {

            Integer cafeId = Integer.parseInt(mSales.get(0).cafeId);
            Integer cupId = Integer.parseInt(mSales.get(0).cupId);
            SaleApiBody saleApiBody = new SaleApiBody(cupId,cafeId);

            mAPIService.postSaleRecord(saleApiBody).enqueue(new Callback<SaleApiBody>() {
                @Override
                public void onResponse(Call<SaleApiBody> call, Response<SaleApiBody> response) {
                    if(response.isSuccessful()) {
                        showResponse(response.body().toString());
                        Log.i("RESPONSE", "post submitted to API." + response.body().toString());
                        return;
                    }
                }

                @Override
                public void onFailure(Call<SaleApiBody> call, Throwable t) {
                    Log.e("RESPONSE", "Unable to submit post to API.");
                }

            });

            return null;
        }

        public void showResponse(String response) {

            Log.d("RESPONSE",response);
        }
    }
}

