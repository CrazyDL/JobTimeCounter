package com.example.dlevshtanov.jobtimecounter.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;

import static dagger.internal.Preconditions.checkNotNull;

public class MyPreferences {
    public static final String APP_PREFERENCES = "APP_PREFERENCES";
    public static final String PREFERENCE_IS_WORK_STARTED = "PREFERENCE_IS_WORK_STARTED";
    public static final String PREFERENCE_STARTED_TIME = "PREFERENCE_STARTED_TIME";

    private SharedPreferences mSharedPreferences;

    @Inject
    public MyPreferences(Context context) {
        checkNotNull(context);
        mSharedPreferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    public Completable setIsWorkStarted(boolean isWorkStarted) {
        return Completable.fromAction(() -> {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(PREFERENCE_IS_WORK_STARTED, isWorkStarted);
            editor.apply();
        });
    }

    public Single<Boolean> isWorkStarted() {
        return Single.just(mSharedPreferences.getBoolean(PREFERENCE_IS_WORK_STARTED, false));
    }

    public Completable setStartedTime(Date date) {
        return Completable.fromAction(() -> {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putLong(PREFERENCE_STARTED_TIME, date.getTime());
            editor.apply();
        });
    }

    public Single<Date> getAndResetStartedTime() {
        return Single.fromCallable(() -> {
            long startedTime = mSharedPreferences.getLong(PREFERENCE_STARTED_TIME, -1);
            Date date = startedTime == -1 ? new Date() : new Date(startedTime);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putLong(PREFERENCE_STARTED_TIME, Calendar.getInstance().getTimeInMillis());
            editor.apply();
            return date;
        });
    }
}
