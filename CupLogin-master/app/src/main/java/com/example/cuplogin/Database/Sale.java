package com.example.cuplogin.Database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Sale {

    @PrimaryKey
    public int sid;

    @ColumnInfo(name = "cafe_id")
    public Integer cafeId;

    @ColumnInfo(name = "cup_id")
    public Integer cupId;

    @ColumnInfo(name = "timestamp")
    public String timestamp;



    public int getSid() {
        return sid;
    }

    public Integer getCafeId() {
        return cafeId;
    }

    public Integer getCupId() {
        return cupId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Sale(int sid, Integer cafeId, Integer cupId, String timestamp) {
        this.sid = sid;
        this.cafeId = cafeId;
        this.cupId = cupId;
        this.timestamp = timestamp;
    }
}
