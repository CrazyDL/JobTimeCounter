package com.example.dlevshtanov.jobtimecounter.presentation;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.example.dlevshtanov.jobtimecounter.domain.WorkoutTimeInteractor;
import com.example.dlevshtanov.jobtimecounter.models.Workday;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

@InjectViewState
public class MainPresenter extends MvpPresenter<MainView> {
    private static int MILLISECS_IN_HOUR = 60 * 60 * 1000;
    private static int MILLISECS_IN_MIN = 60 * 1000;

    private boolean mIsWorkStarted;
    private long mWorkoutTimeForWeek;
    private CompositeDisposable mDisposable;

    @Inject
    WorkoutTimeInteractor mWorkoutTimeInteractor;

    public void init() {
        mDisposable = new CompositeDisposable();
    }

    public void updateData() {
        getViewState().disableButton();
        getWorkState();
        getCurrentWorkDays();
    }

    public void onButtonClick() {
        getViewState().disableButton();
        if (mIsWorkStarted) {
            mIsWorkStarted = false;
            updateTime();
        } else {
            mIsWorkStarted = true;
            saveStartedTime();
        }
        saveWorkState();
        updateButtonStatus();
    }

    public void getCurrentWorkDays() {
        mDisposable.add(mWorkoutTimeInteractor.getCurrentWorkDays()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(workdays -> {
                    calculateWorkoutTimeForWeek(workdays);
                    getViewState().refreshWorkoutDays(workdays);
                    if (mIsWorkStarted) {
                        updateTime();
                    }
                }));
    }

    public void updateDay(Workday workday){
        mWorkoutTimeInteractor.updateDay(workday);
    }

    public void addTimeToWeekWorkout(long time){
        mWorkoutTimeForWeek += time;
        showWorkoutTime(mWorkoutTimeForWeek);
    }

    private void getWorkState() {
        mDisposable.add(mWorkoutTimeInteractor.isWorkStarted()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isWorkStarted -> {
                    mIsWorkStarted = isWorkStarted;
                    updateButtonStatus();
                }));
    }

    private void updateButtonStatus() {
        if (mWorkoutTimeInteractor.isDayOff(Calendar.getInstance())) {
            getViewState().showIsDayOffButton();
        } else if (mIsWorkStarted) {
            getViewState().showStopButton();
        } else {
            getViewState().showStartButton();
        }
    }

    private void updateTime() {
        mDisposable.add(mWorkoutTimeInteractor.getAndResetStartedTime()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(startedTime -> {
                    Calendar calendar = Calendar.getInstance();
                    Workday currentDay = mWorkoutTimeInteractor.getWorkdayFromCalendar(calendar);
                    calendar.setTime(startedTime);
                    Workday startedDay = mWorkoutTimeInteractor.getWorkdayFromCalendar(calendar);
                    if (startedDay.getDate().equals(currentDay.getDate()) && !mWorkoutTimeInteractor.isDayOff(currentDay)) {
                        Date currentTime = new Date();
                        long workoutTime = currentTime.getTime() - startedTime.getTime();
                        mWorkoutTimeForWeek += workoutTime;
                        getViewState().addTimeToCurrentDay(workoutTime);
                        showWorkoutTime(mWorkoutTimeForWeek);
                        mWorkoutTimeInteractor.addTimeToDay(currentDay, workoutTime);
                    } else {
                        mIsWorkStarted = false;
                        saveWorkState();
                        updateButtonStatus();
                    }
                }));
    }

    private void saveStartedTime() {
        mDisposable.add(mWorkoutTimeInteractor.setStartedTime(new Date())
                .subscribeOn(Schedulers.io())
                .subscribe());
    }

    private void saveWorkState() {
        mDisposable.add(mWorkoutTimeInteractor.setIsWorkStarted(mIsWorkStarted)
                .subscribeOn(Schedulers.io())
                .subscribe());
    }

    private void calculateWorkoutTimeForWeek(List<Workday> workdays) {
        mDisposable.add(Single.fromCallable(() -> {
            long workoutTime = 0;
            for (Workday workday : workdays) {
                workoutTime += workday.getWorkedOutTime();
            }
            mWorkoutTimeForWeek = workoutTime;
            return workoutTime;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showWorkoutTime));

    }

    private void showWorkoutTime(long millisecs) {
        getViewState().showWorkoutTime(millisecs / MILLISECS_IN_HOUR, (millisecs % MILLISECS_IN_HOUR) / MILLISECS_IN_MIN);
    }
}
