package com.example.dlevshtanov.jobtimecounter.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Workday(
        @PrimaryKey var date: WorkdayDate,
        var dayOfWeek: Int,
        var workedOutTime: Long
)