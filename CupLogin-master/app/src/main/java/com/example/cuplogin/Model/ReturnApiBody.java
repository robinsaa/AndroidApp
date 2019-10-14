package com.example.cuplogin.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReturnApiBody {

    @SerializedName("cup_id")
    @Expose
    private String cupId;

    @SerializedName("bin_id")
    @Expose
    private String binId;

    @SerializedName("dishwasher_id")
    @Expose
    private String dishwasherId;

    @SerializedName("scanned_at")
    @Expose
    private String scannedAt;

    public ReturnApiBody(String cupId, String binId, String dishwasherId, String scannedAt) {
        this.cupId = cupId;
        this.binId = binId;
        this.dishwasherId = dishwasherId;
        this.scannedAt = scannedAt;
    }

    public String getCupId() {
        return cupId;
    }

    public void setCupId(String cupId) {
        this.cupId = cupId;
    }

    public String getBinId() {
        return binId;
    }

    public void setBinId(String binId) {
        this.binId = binId;
    }

    public String getDishwasherId() {
        return dishwasherId;
    }

    public void setDishwasherId(String dishwasherId) {
        this.dishwasherId = dishwasherId;
    }

    public String getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(String scannedAt) {
        this.scannedAt = scannedAt;
    }

}
