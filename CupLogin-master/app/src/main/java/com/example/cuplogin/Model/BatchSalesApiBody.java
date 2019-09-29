package com.example.cuplogin.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BatchSalesApiBody {

    @SerializedName("data")
    private List<SaleApiBody> saleApiBodyList;

    public BatchSalesApiBody(List<SaleApiBody> saleApiBodyList) {
        this.saleApiBodyList = saleApiBodyList;
    }

    public List<SaleApiBody> getSaleApiBodyList() {
        return saleApiBodyList;
    }

    public void setSaleApiBodyList(List<SaleApiBody> saleApiBodyList) {
        this.saleApiBodyList = saleApiBodyList;
    }
}

