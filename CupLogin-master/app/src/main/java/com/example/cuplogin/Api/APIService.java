package com.example.cuplogin.Api;

import com.example.cuplogin.SaleApiBody;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface APIService {

    @POST("sale")
    Call<SaleApiBody> sendSaleRecord(@Body SaleApiBody saleApiBody );

    @POST("sale")
    Call<SaleApiBody> sendBatchSaleRecords(@Body List<SaleApiBody> saleApiBody );

}
