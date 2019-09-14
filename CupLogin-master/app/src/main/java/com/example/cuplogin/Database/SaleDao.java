package com.example.cuplogin.Database;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface SaleDao {

    @Query("SELECT * FROM sale")
    List<Sale> getAll();

    @Query("SELECT * FROM sale WHERE sid IN (:userIds)")
    List<Sale> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM sale WHERE sid LIKE :sidValue LIMIT 1")
    Sale findById(int sidValue);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Sale sale);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Sale> sales);

    @Delete
    void delete(Sale sale);
}
