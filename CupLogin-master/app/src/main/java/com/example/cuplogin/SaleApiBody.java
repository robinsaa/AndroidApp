package com.example.cuplogin;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SaleApiBody {


    @SerializedName("cup_id")
    @Expose
    private String cupId;

    @SerializedName("cafe_id")
    @Expose
    private String cafeId;

    public SaleApiBody(String cupId, String cafeId) {
        this.cafeId = cafeId;
        this.cupId = cupId;
    }

    public String getCupId() {
        return cupId;
    }

    public void setCupId(String cupId) {
        this.cupId = cupId;
    }

    public String getCafeId() {
        return cafeId;
    }

    public void setCafeId(String cafeId) {
        this.cafeId = cafeId;
    }

}
