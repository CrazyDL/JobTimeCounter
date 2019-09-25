package com.example.dlevshtanov.jobtimecounter.data;

import com.example.dlevshtanov.jobtimecounter.data.bd.WorkdaysDao;
import com.example.dlevshtanov.jobtimecounter.models.Workday;
import com.example.dlevshtanov.jobtimecounter.models.WorkdayDate;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

import static dagger.internal.Preconditions.checkNotNull;


public class WorkdaysRepository {

    private WorkdaysDao mWorkdaysDao;
    private MyPreferences mMyPreferences;

    @Inject
    public WorkdaysRepository(WorkdaysDao workdaysDao, MyPreferences myPreferences) {
        checkNotNull(workdaysDao);
        checkNotNull(myPreferences);
        mWorkdaysDao = workdaysDao;
        mMyPreferences = myPreferences;
    }

    public Single<List<Workday>> getCurrentWorkDays(@NonNull WorkdayDate date) {
        return mWorkdaysDao.getAllAfterDay(date);
    }

    public Maybe<Workday> getWorkdayByDate(@NonNull WorkdayDate date) {
        return mWorkdaysDao.getDayByDate(date);
    }

    public Single<List<Workday>> getAllWorkDays() {
        return mWorkdaysDao.getAll();
    }

    public void updateDay(Workday workday) {
        mWorkdaysDao.update(workday);
    }

    public void insertDay(Workday workday) {
        mWorkdaysDao.insert(workday);
    }

    public void insertDays(List<Workday> workdays) {
        mWorkdaysDao.insertAll(workdays);
    }

    public Completable setIsWorkStarted(boolean isWorkStarted) {
        return mMyPreferences.setIsWorkStarted(isWorkStarted);
    }

    public Single<Boolean> isWorkStarted() {
        return mMyPreferences.isWorkStarted();
    }

    public Completable setStartedTime(Date date) {
        return mMyPreferences.setStartedTime(date);
    }

    public Single<Date> getAndResetStartedTime() {
        return mMyPreferences.getAndResetStartedTime();
    }
}
