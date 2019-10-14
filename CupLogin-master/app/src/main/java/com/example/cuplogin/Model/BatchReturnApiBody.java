package com.example.cuplogin.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BatchReturnApiBody {

    @SerializedName("data")
    private List<ReturnApiBody> returnApiBodyList;

    public BatchReturnApiBody(List<ReturnApiBody> returnApiBodyList) {
        this.returnApiBodyList = returnApiBodyList;
    }

    public List<ReturnApiBody> getReturnApiBodyList() {
        return returnApiBodyList;
    }

    public void setReturnApiBodyList(List<ReturnApiBody> returnApiBodyList) {
        this.returnApiBodyList = returnApiBodyList;
    }


}
