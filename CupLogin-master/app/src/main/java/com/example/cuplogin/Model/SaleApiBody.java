package com.example.cuplogin.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SaleApiBody {


    @SerializedName("cup_id")
    @Expose
    private String cupId;

    @SerializedName("cafe_id")
    @Expose
    private String cafeId;

    @SerializedName("scanned_at")
    @Expose
    private String scannedAt;



    public SaleApiBody(String cupId, String cafeId, String scannedAt) {
        this.cafeId = cafeId;
        this.cupId = cupId;
        this.scannedAt =scannedAt;
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


    public String getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(String scannedAt) {
        this.scannedAt = scannedAt;
    }

}
