package com.example.cuplogin.Database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity
public class Return_Record {



    @PrimaryKey
    public int rid;

    @ColumnInfo(name = "cup_id")
    private String cupId;

    @ColumnInfo(name = "bin_id")
    private String binId;

    @ColumnInfo(name = "dishwasher_id")
    private String dishwasherId;

    @ColumnInfo(name = "scanned_at")
    private String scannedAt;

    public Return_Record(int rid,String cupId, String binId, String dishwasherId, String scannedAt) {
        this.rid = rid;
        this.cupId = cupId;
        this.binId = binId;
        this.dishwasherId = dishwasherId;
        this.scannedAt = scannedAt;
    }

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
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
