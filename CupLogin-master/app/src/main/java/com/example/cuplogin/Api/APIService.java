package com.example.cuplogin.Api;

import com.example.cuplogin.Model.BatchSalesApiBody;
import com.example.cuplogin.Model.SaleApiBody;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface APIService {

    @POST("sale/cache")
    Call<List<SaleApiBody>> sendBatchSaleRecords(@Body List<SaleApiBody> batchSaleApiBody );

}
