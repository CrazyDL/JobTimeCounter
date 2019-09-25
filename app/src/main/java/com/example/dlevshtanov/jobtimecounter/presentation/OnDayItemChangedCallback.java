package com.example.dlevshtanov.jobtimecounter.presentation;

import com.example.dlevshtanov.jobtimecounter.models.Workday;

public interface OnDayItemChangedCallback {

    void updateDay(Workday workday);

    void addTimeToWeekWorkout(long time);
}
