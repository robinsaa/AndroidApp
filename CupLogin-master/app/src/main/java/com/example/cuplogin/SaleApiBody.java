package com.example.cuplogin;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SaleApiBody {


    @SerializedName("cup_id")
    @Expose
    private Integer cupId;

    @SerializedName("cafe_id")
    @Expose
    private Integer cafeId;

    public SaleApiBody(Integer cupId, Integer cafeId) {
        this.cafeId = cafeId;
        this.cupId = cupId;
    }

    public Integer getCupId() {
        return cupId;
    }

    public void setCupId(Integer cupId) {
        this.cupId = cupId;
    }

    public Integer getCafeId() {
        return cafeId;
    }

    public void setCafeId(Integer cafeId) {
        this.cafeId = cafeId;
    }

}
