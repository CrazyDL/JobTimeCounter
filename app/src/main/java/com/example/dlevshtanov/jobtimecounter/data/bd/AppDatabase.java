package com.example.dlevshtanov.jobtimecounter.data.bd;

import com.example.dlevshtanov.jobtimecounter.models.Workday;
import com.example.dlevshtanov.jobtimecounter.models.WorkdayDate;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Workday.class}, version = AppDatabase.DATABASE_VERSION)
@TypeConverters({WorkdayDateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public static final String DATABASE_NAME = "workdays.db";
    static final int DATABASE_VERSION = 1;

    public abstract WorkdaysDao workdaysDao();
}
