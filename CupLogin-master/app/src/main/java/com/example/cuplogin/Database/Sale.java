package com.example.cuplogin.Database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Sale {

    @PrimaryKey
    public int sid;

    @ColumnInfo(name = "cafe_id")
    public String cafeId;

    @ColumnInfo(name = "cup_id")
    public String cupId;

    @ColumnInfo(name = "timestamp")
    public String timestamp;



    public int getSid() {
        return sid;
    }

    public String getCafeId() {
        return cafeId;
    }

    public String getCupId() {
        return cupId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Sale(int sid, String cafeId, String cupId, String timestamp) {
        this.sid = sid;
        this.cafeId = cafeId;
        this.cupId = cupId;
        this.timestamp = timestamp;
    }
}
