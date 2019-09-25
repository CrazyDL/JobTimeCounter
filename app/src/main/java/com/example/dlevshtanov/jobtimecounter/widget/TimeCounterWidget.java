package com.example.dlevshtanov.jobtimecounter.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.example.dlevshtanov.jobtimecounter.R;
import com.example.dlevshtanov.jobtimecounter.di.DaggerMainComponent;
import com.example.dlevshtanov.jobtimecounter.di.MainModule;
import com.example.dlevshtanov.jobtimecounter.domain.WorkoutTimeInteractor;
import com.example.dlevshtanov.jobtimecounter.models.Workday;
import com.example.dlevshtanov.jobtimecounter.presentation.MainActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class TimeCounterWidget extends AppWidgetProvider {
    public final static String ACTION_BUTTON_CLICK = "ACTION_BUTTON_CLICK";
    public final static String ACTION_BUTTON_IN_ACTIVITY_CLICKED = "ACTION_BUTTON_IN_ACTIVITY_CLICKED";

    private static int MILLISECS_IN_HOUR = 60 * 60 * 1000;
    private static int MILLISECS_IN_MIN = 60 * 1000;

    static private WorkoutTimeInteractor mTimeInteractor;
    static private CompositeDisposable mCompositeDisposable;
    static private boolean mIsWorkStarted;
    static private boolean mIsButtonClicked;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(),
                600000, getAlarmPendingIntent(context));
    }

    @Override
    public void onDisabled(Context context) {
        mCompositeDisposable.dispose();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getAlarmPendingIntent(context));
    }

    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction() != null) {
            int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
            Bundle extras = intent.getExtras();
            if (extras != null) {
                mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            }
            if (extras == null || mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                return;
            }
            if (intent.getAction().equalsIgnoreCase(ACTION_BUTTON_CLICK)) {
                if (!mTimeInteractor.isDayOff(Calendar.getInstance())) {
                    mIsButtonClicked = true;
                    if (mIsWorkStarted) {
                        mIsWorkStarted = false;
                    } else {
                        mIsWorkStarted = true;
                        mCompositeDisposable.add(mTimeInteractor.setStartedTime(new Date())
                                .subscribeOn(Schedulers.io())
                                .subscribe());
                    }
                    int id = mAppWidgetId;
                    mCompositeDisposable.add(mTimeInteractor.setIsWorkStarted(mIsWorkStarted)
                            .subscribeOn(Schedulers.io())
                            .subscribe(() -> updateAppWidget(context, AppWidgetManager.getInstance(context), id)));
                }
            } else if (intent.getAction().equalsIgnoreCase(ACTION_BUTTON_IN_ACTIVITY_CLICKED)) {
                mIsButtonClicked = true;
                updateAppWidget(context, AppWidgetManager.getInstance(context), mAppWidgetId);
            }
        }
    }

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        mCompositeDisposable = new CompositeDisposable();
        mTimeInteractor = DaggerMainComponent.builder()
                .mainModule(new MainModule(context))
                .build().getWorkoutTimeInteractor();
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.time_counter_widget);
        getWorkStatus(context, views, appWidgetManager, appWidgetId);
        initListeners(context, views, appWidgetId);
    }

    private void getWorkStatus(Context context, RemoteViews views, AppWidgetManager appWidgetManager, int appWidgetId) {
        mCompositeDisposable.add(mTimeInteractor.isWorkStarted()
                .subscribeOn(Schedulers.io())
                .subscribe(isWorkStarted -> {
                    mIsWorkStarted = isWorkStarted;
                    setButtonStatus(context, views);
                    getLastTime(context, views, appWidgetManager, appWidgetId);
                }));
    }

    private void setButtonStatus(Context context, RemoteViews views) {
        if (mTimeInteractor.isDayOff(Calendar.getInstance())) {
            views.setTextViewText(R.id.stop_or_start_button, context.getString(R.string.day_off));
            views.setBoolean(R.id.stop_or_start_button, "setEnabled", false);
        } else if (mIsWorkStarted) {
            views.setTextViewText(R.id.stop_or_start_button, context.getString(R.string.stop));
            views.setBoolean(R.id.stop_or_start_button, "setEnabled", true);
        } else {
            views.setTextViewText(R.id.stop_or_start_button, context.getString(R.string.start));
            views.setBoolean(R.id.stop_or_start_button, "setEnabled", true);
        }
    }

    private void getLastTime(Context context, RemoteViews views, AppWidgetManager appWidgetManager, int appWidgetId) {
        mCompositeDisposable.add(mTimeInteractor.getAndResetStartedTime()
                .subscribeOn(Schedulers.io())
                .subscribe(time -> {
                    updateLastTime(context, views, time);
                    getWorkedOutTime(context, views, time, appWidgetManager, appWidgetId);
                }));
    }

    private void updateLastTime(Context context, RemoteViews views, Date time) {
        Calendar startedTime = Calendar.getInstance();
        startedTime.setTime(time);
        Calendar currentTime = Calendar.getInstance();
        if (startedTime.get(Calendar.DAY_OF_YEAR) == currentTime.get(Calendar.DAY_OF_YEAR) || !mTimeInteractor.isDayOff(currentTime)) {

            if (mIsButtonClicked) {
                DateFormat mDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                views.setTextViewText(R.id.time_text_view, mDateFormat.format(new Date()));
            }
            if (mIsWorkStarted) {
                views.setTextViewText(R.id.time_start_in_text_view, context.getString(R.string.time_start_in));
            } else {
                views.setTextViewText(R.id.time_start_in_text_view, context.getString(R.string.time_stop_in));
            }
        } else {
            if (mIsWorkStarted) {
                mIsWorkStarted = false;
                mTimeInteractor.setIsWorkStarted(false);
                setButtonStatus(context, views);
            }
        }
    }

    private void getWorkedOutTime(Context context, RemoteViews views, Date startedTime, AppWidgetManager appWidgetManager, int appWidgetId) {
        mCompositeDisposable.add(mTimeInteractor.getCurrentWorkday()
                .subscribeOn(Schedulers.io())
                .doOnSuccess(currentWorkday -> updateWorkedOutTime(context, views, startedTime, currentWorkday, appWidgetManager, appWidgetId))
                .doOnComplete(() -> updateWorkedOutTime(context, views, startedTime, mTimeInteractor.getWorkdayFromCalendar(Calendar.getInstance()), appWidgetManager, appWidgetId))
                .subscribe());
    }

    private void updateWorkedOutTime(Context context, RemoteViews views, Date startedTime, Workday currentWorkday, AppWidgetManager appWidgetManager, int appWidgetId) {
        if (mIsWorkStarted) {
            Long diff = Calendar.getInstance().getTimeInMillis() - startedTime.getTime();
            currentWorkday.setWorkedOutTime(currentWorkday.getWorkedOutTime() + diff);
            mTimeInteractor.updateDay(currentWorkday);
        }
        views.setTextViewText(R.id.workout_text_view, context.getString(
                R.string.workout_widget,
                currentWorkday.getWorkedOutTime() / MILLISECS_IN_HOUR,
                (currentWorkday.getWorkedOutTime() % MILLISECS_IN_HOUR) / MILLISECS_IN_MIN));
        mIsButtonClicked = false;
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private void initListeners(Context context, RemoteViews views, int widgetID) {
        Intent activityIntent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, widgetID, activityIntent, 0);
        views.setOnClickPendingIntent(R.id.root_layout, pIntent);

        Intent updateIntent = new Intent(context, TimeCounterWidget.class);
        updateIntent.setAction(ACTION_BUTTON_CLICK);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, widgetID, updateIntent, 0);
        views.setOnClickPendingIntent(R.id.stop_or_start_button, pendingIntent);
    }

    private PendingIntent getAlarmPendingIntent(Context context) {
        Intent intent = new Intent(context, TimeCounterWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, TimeCounterWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}

