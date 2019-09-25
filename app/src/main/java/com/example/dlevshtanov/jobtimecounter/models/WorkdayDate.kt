package com.example.dlevshtanov.jobtimecounter.models

data class WorkdayDate(
        var date: Int,
        var month: Int,
        var year: Int
) {
    override fun toString(): String {
        return String.format("%02d.%02d.%d", date, month, year);
    }
}