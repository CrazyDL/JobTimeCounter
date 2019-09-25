package com.example.dlevshtanov.jobtimecounter.data.bd;

import com.example.dlevshtanov.jobtimecounter.models.WorkdayDate;

import androidx.room.TypeConverter;

public class WorkdayDateConverter {
    private static int MULTIPLIER_FOR_YEAR = 10000;
    private static int MULTIPLIER_FOR_MONTH = 100;

    @TypeConverter
    public static WorkdayDate fromTimestamp(Integer value) {
        if (value == null) {
            return null;
        }
        int year = value / MULTIPLIER_FOR_YEAR;
        int month = (value % MULTIPLIER_FOR_YEAR) / MULTIPLIER_FOR_MONTH;
        int day = value % MULTIPLIER_FOR_MONTH;
        return new WorkdayDate(day, month, year);
    }

    @TypeConverter
    public static Integer workdayDateToTimestamp(WorkdayDate date) {
        return date == null ? null : date.getYear() * MULTIPLIER_FOR_YEAR + date.getMonth() * MULTIPLIER_FOR_MONTH + date.getDate();
    }
}