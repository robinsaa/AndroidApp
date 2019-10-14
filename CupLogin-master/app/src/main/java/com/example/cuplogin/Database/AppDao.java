package com.example.cuplogin.Database;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface AppDao {

    @Query("SELECT * FROM sale")
    List<Sale> getAllSales();

    @Query("SELECT * FROM  return_record")
    List<Return_Record> getAllReturns();

    @Query("SELECT * FROM sale WHERE sid IN (:userIds)")
    List<Sale> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM sale WHERE sid LIKE :sidValue LIMIT 1")
    Sale findSaleById(int sidValue);

    @Query("SELECT * FROM return_record WHERE rid LIKE :ridValue LIMIT 1")
    Return_Record findReturnById(int ridValue);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertToSale(Sale sale);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertToReturn(Return_Record return_record);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllSales(List<Sale> sales);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllReturns(List<Return_Record> return_records);

    @Delete
    void deleteSale(Sale sale);

    @Delete
    void deleteReturn(Return_Record return_record);

}
