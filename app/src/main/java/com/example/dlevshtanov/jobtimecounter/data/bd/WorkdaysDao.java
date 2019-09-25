package com.example.dlevshtanov.jobtimecounter.data.bd;

import com.example.dlevshtanov.jobtimecounter.models.Workday;
import com.example.dlevshtanov.jobtimecounter.models.WorkdayDate;

import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Maybe;
import io.reactivex.Single;

@Dao
public interface WorkdaysDao {
    @Query("SELECT * FROM workday ORDER BY date")
    Single<List<Workday>> getAll();

    @Query("SELECT * FROM workday WHERE date >= :date ORDER BY date")
    Single<List<Workday>> getAllAfterDay(@NonNull WorkdayDate date);

    @Query("SELECT * FROM workday WHERE date = :date")
    Maybe<Workday> getDayByDate(@NonNull WorkdayDate date);

    @Query("SELECT * FROM workday ORDER BY date desc LIMIT 1")
    Single<Workday> getLastDay();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Workday workday);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Workday> workdays);

    @Update
    void update(Workday workday);

    @Delete
    void delete(Workday workday);
}
