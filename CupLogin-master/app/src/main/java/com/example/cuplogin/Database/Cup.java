package com.example.cuplogin.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Cup {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "cup_id")
    public String mCupId;

    @ColumnInfo(name = "time_stamp")
    String mTimestamp;


    public Cup( String cupId, String timestamp) {
        this.mCupId = cupId;
        this.mTimestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getmCupId() {
        return mCupId;
    }

    public void setmCupId(String mCupId) {
        this.mCupId = mCupId;
    }

    public String getmTimeStamp() {
        return mTimestamp;
    }

    public void setmTimeStamp(String mTimeStamp) {
        this.mTimestamp = mTimeStamp;
    }
}
