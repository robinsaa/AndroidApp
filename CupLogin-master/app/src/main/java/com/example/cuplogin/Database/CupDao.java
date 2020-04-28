package com.example.cuplogin.Database;

import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CupDao {

    @Query("SELECT * FROM cup")
    List<Cup> getAllCups();

    @Query("SELECT * FROM cup WHERE cup_id LIKE :cup_id_value LIMIT 1")
    Cup findCupById(String cup_id_value);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCup(Cup cup);

    @Update
    void update(Cup... cup);

    @Delete
    void deleteCup(Cup cup);

    @Query("DELETE FROM cup")
    void deleteAll();







}
