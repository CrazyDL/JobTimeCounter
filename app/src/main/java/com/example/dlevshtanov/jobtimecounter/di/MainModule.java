package com.example.dlevshtanov.jobtimecounter.di;

import android.content.Context;

import com.example.dlevshtanov.jobtimecounter.data.MyPreferences;
import com.example.dlevshtanov.jobtimecounter.data.WorkdaysRepository;
import com.example.dlevshtanov.jobtimecounter.data.bd.AppDatabase;
import com.example.dlevshtanov.jobtimecounter.domain.WorkoutTimeInteractor;

import javax.inject.Singleton;

import androidx.room.Room;
import dagger.Module;
import dagger.Provides;

@Module
public class MainModule {

    private final Context mContext;

    public MainModule (Context context) {
        mContext = context;
    }

    @Provides
    @Singleton
    public Context context() {
        return mContext;
    }

    @Provides
    @Singleton
    WorkoutTimeInteractor provideWorkoutTimeInteractor(WorkdaysRepository workdaysRepository) {
        return new WorkoutTimeInteractor(workdaysRepository);
    }

    @Provides
    @Singleton
    WorkdaysRepository provideWorkdaysRepository(AppDatabase appDatabase, MyPreferences myPreferences){
        return new WorkdaysRepository(appDatabase.workdaysDao(), myPreferences);
    }

    @Provides
    @Singleton
    AppDatabase provideAppDatabase(){
        return Room.databaseBuilder(mContext, AppDatabase.class, AppDatabase.DATABASE_NAME)
                .build();
    }
}
