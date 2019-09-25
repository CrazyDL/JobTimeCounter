package com.example.dlevshtanov.jobtimecounter.domain;

import com.example.dlevshtanov.jobtimecounter.data.WorkdaysRepository;
import com.example.dlevshtanov.jobtimecounter.models.Workday;
import com.example.dlevshtanov.jobtimecounter.models.WorkdayDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static dagger.internal.Preconditions.checkNotNull;

public class WorkoutTimeInteractor {

    private WorkdaysRepository mWorkdaysRepository;

    @Inject
    public WorkoutTimeInteractor(WorkdaysRepository workdaysRepository) {
        checkNotNull(workdaysRepository);
        mWorkdaysRepository = workdaysRepository;
    }

    public Single<List<Workday>> getCurrentWorkDays() {
        return Single.fromCallable(() -> {
            Workday currentWorkday = getWorkdayFromCalendar(Calendar.getInstance());
            Calendar firstDayOfWeek = Calendar.getInstance();
            firstDayOfWeek.add(Calendar.DAY_OF_YEAR, -convertNumerationWeekToRus(firstDayOfWeek));
            List<Workday> workdays = mWorkdaysRepository.getCurrentWorkDays(getWorkdayFromCalendar(firstDayOfWeek).getDate()).blockingGet();
            if ((isDayOff(currentWorkday) && workdays.size() != 5) || workdays.size() != currentWorkday.getDayOfWeek() + 1) {
                List<Workday> newWorkdays = new ArrayList<>();
                for (int i = 0; i <= currentWorkday.getDayOfWeek() && i < 5; i++) {
                    Workday newWorkday = getWorkdayFromCalendar(firstDayOfWeek);
                    boolean isDayExist = false;
                    for (Workday workday : workdays) {
                        if (workday.getDate().equals(newWorkday.getDate())) {
                            isDayExist = true;
                        }
                    }
                    if (!isDayExist) {
                        workdays.add(i, newWorkday);
                        newWorkdays.add(newWorkday);
                    }
                    firstDayOfWeek.add(Calendar.DAY_OF_YEAR, 1);
                }
                insertDays(newWorkdays);
            }
            return workdays;
        });
    }

    public Maybe<Workday> getCurrentWorkday(){
        return mWorkdaysRepository.getWorkdayByDate(getWorkdayFromCalendar(Calendar.getInstance()).getDate());
    }

    public Single<Boolean> isWorkStarted() {
        return mWorkdaysRepository.isWorkStarted();
    }

    public Completable setIsWorkStarted(boolean isWorkStarted) {
        return mWorkdaysRepository.setIsWorkStarted(isWorkStarted);
    }

    public Completable setStartedTime(Date date) {
        return mWorkdaysRepository.setStartedTime(date);
    }

    public Single<Date> getAndResetStartedTime() {
        return mWorkdaysRepository.getAndResetStartedTime();
    }

    public void updateDay(Workday workday) {
        Completable.fromAction(() -> mWorkdaysRepository.updateDay(workday))
                .subscribeOn(Schedulers.io()).subscribe();
    }

    public void addTimeToDay(Workday workday, long workoutTime) {
        mWorkdaysRepository.getWorkdayByDate(workday.getDate())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(day -> {
                    day.setWorkedOutTime(day.getWorkedOutTime() + workoutTime);
                    updateDay(day);
                });
    }

    public void insertDay(Workday workday) {
        Completable.fromAction(() -> mWorkdaysRepository.insertDay(workday))
                .subscribeOn(Schedulers.io()).subscribe();
    }

    public void insertDays(List<Workday> workdays) {
        Completable.fromAction(() -> mWorkdaysRepository.insertDays(workdays))
                .subscribeOn(Schedulers.io()).subscribe();
    }

    public boolean isDayOff(Calendar day) {
        return day.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || day.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }

    public boolean isDayOff(Workday day) {
        return day.getDayOfWeek() == 5 || day.getDayOfWeek() == 6;
    }

    public Workday getWorkdayFromCalendar(Calendar calendar) {
        WorkdayDate workdayDate = new WorkdayDate(
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR)
        );
        return new Workday(workdayDate, convertNumerationWeekToRus(calendar), 0);
    }

    private int convertNumerationWeekToRus(Calendar calendar) {
        return (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7;
    }

}
