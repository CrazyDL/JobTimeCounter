package com.example.dlevshtanov.jobtimecounter.presentation;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.arellomobile.mvp.MvpActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.example.dlevshtanov.jobtimecounter.R;
import com.example.dlevshtanov.jobtimecounter.di.DaggerMainComponent;
import com.example.dlevshtanov.jobtimecounter.di.MainModule;
import com.example.dlevshtanov.jobtimecounter.models.Workday;
import com.example.dlevshtanov.jobtimecounter.widget.TimeCounterWidget;

import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends MvpActivity implements MainView, OnDayItemChangedCallback {
    private static final int REPEAT_TIME_MILLISECS = 90000;
    private Button mStartOrFinishButton;
    private TextView mWorkoutForWeekTextView;
    private WorkdaysAdapter mWorkdaysAdapter;
    private Handler mHandler;
    private Runnable mUpdateDataRunnable;

    @InjectPresenter
    MainPresenter mMainPresenter;

    @ProvidePresenter
    MainPresenter provideMainPresenter() {
        MainPresenter mainPresenter = new MainPresenter();
        DaggerMainComponent.builder()
                .mainModule(new MainModule(getApplicationContext()))
                .build().inject(mainPresenter);
        return mainPresenter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        initViews();
        initListeners();
        mMainPresenter.init();
        mHandler = new Handler();
        mUpdateDataRunnable = () -> {
            mMainPresenter.updateData();
            mHandler.postDelayed(mUpdateDataRunnable, REPEAT_TIME_MILLISECS);
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUpdateDataRunnable.run();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mUpdateDataRunnable);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    @Override
    public void disableButton() {
        mStartOrFinishButton.setEnabled(false);
    }

    @Override
    public void showStartButton() {
        mStartOrFinishButton.setEnabled(true);
        mStartOrFinishButton.setText(R.string.start_countdown);
        mWorkdaysAdapter.setIsWorkStarted(false);
    }

    @Override
    public void showStopButton() {
        mStartOrFinishButton.setEnabled(true);
        mStartOrFinishButton.setText(R.string.stop_countdown);
        mWorkdaysAdapter.setIsWorkStarted(true);
    }

    @Override
    public void showIsDayOffButton() {
        mStartOrFinishButton.setEnabled(false);
        mStartOrFinishButton.setText(R.string.today_day_off);
    }

    @Override
    public void showWorkoutTime(long hours, long mins) {
        mWorkoutForWeekTextView.setText(getResources().getString(R.string.workout_for_week, hours, mins));
    }

    @Override
    public void refreshWorkoutDays(List<Workday> workdays) {
        mWorkdaysAdapter.updateWorkdays(workdays);
    }

    @Override
    public void addTimeToCurrentDay(long workoutTime) {
        mWorkdaysAdapter.addTimeToLastDay(workoutTime);
    }

    @Override
    public void updateDay(Workday workday) {
        mMainPresenter.updateDay(workday);
    }

    @Override
    public void addTimeToWeekWorkout(long time) {
        mMainPresenter.addTimeToWeekWorkout(time);
        new Handler().postDelayed(this::updateWidget, 2000);
    }

    private void initViews() {
        mStartOrFinishButton = findViewById(R.id.start_or_finish_counting_button);
        RecyclerView mDaysRecyclerView = findViewById(R.id.days_recycler_view);
        mWorkoutForWeekTextView = findViewById(R.id.worked_out_for_week_text_view);
        mDaysRecyclerView.setHasFixedSize(true);
        mDaysRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mWorkdaysAdapter = new WorkdaysAdapter(getResources().getStringArray(R.array.days_of_week), this);
        mDaysRecyclerView.setAdapter(mWorkdaysAdapter);
    }

    private void initListeners() {
        mStartOrFinishButton.setOnClickListener((v) -> {
            mMainPresenter.onButtonClick();
            new Handler().postDelayed(this::sendClickToWidget, 2000);
        });
    }

    private void sendClickToWidget(){
        Intent intent = new Intent(this, TimeCounterWidget.class);
        intent.setAction(TimeCounterWidget.ACTION_BUTTON_IN_ACTIVITY_CLICKED);
        int[] ids = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(new ComponentName(getApplication(), TimeCounterWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, ids[0]);
        sendBroadcast(intent);
    }

    private void updateWidget(){
        Intent intent = new Intent(this, TimeCounterWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(new ComponentName(getApplication(), TimeCounterWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }
}