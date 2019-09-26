package com.example.cuplogin;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface APIService {

    @POST("/sale")
    @FormUrlEncoded
    Call<SaleApiBody> postSaleRecord(@Body SaleApiBody saleApiBody );


}
