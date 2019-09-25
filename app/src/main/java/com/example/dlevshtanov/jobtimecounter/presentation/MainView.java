package com.example.dlevshtanov.jobtimecounter.presentation;

import com.arellomobile.mvp.MvpView;
import com.example.dlevshtanov.jobtimecounter.models.Workday;

import java.util.List;

public interface MainView extends MvpView {

    void disableButton();

    void showStartButton();

    void showStopButton();

    void showIsDayOffButton();

    void showWorkoutTime(long hours, long mins);

    void refreshWorkoutDays(List<Workday> workdays);

    void addTimeToCurrentDay(long workoutTime);
}
