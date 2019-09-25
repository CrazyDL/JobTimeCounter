package com.example.dlevshtanov.jobtimecounter.di;

import android.content.Context;

import com.example.dlevshtanov.jobtimecounter.data.WorkdaysRepository;
import com.example.dlevshtanov.jobtimecounter.data.bd.AppDatabase;
import com.example.dlevshtanov.jobtimecounter.domain.WorkoutTimeInteractor;
import com.example.dlevshtanov.jobtimecounter.presentation.MainActivity;
import com.example.dlevshtanov.jobtimecounter.presentation.MainPresenter;
import com.example.dlevshtanov.jobtimecounter.widget.TimeCounterWidget;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = MainModule.class)
public interface MainComponent {

    void inject(MainPresenter mainPresenter);

    void inject(TimeCounterWidget timeCounterWidget);

    WorkoutTimeInteractor getWorkoutTimeInteractor();

    AppDatabase getAppDatabase();

    WorkdaysRepository getWorkdaysRepository();

    Context getContext();
}
