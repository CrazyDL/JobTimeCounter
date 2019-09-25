package com.example.dlevshtanov.jobtimecounter.presentation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.dlevshtanov.jobtimecounter.R;
import com.example.dlevshtanov.jobtimecounter.models.Workday;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class WorkdaysAdapter extends RecyclerView.Adapter<WorkdayViewHolder> {
    private static int MILLISECS_IN_HOUR = 60 * 60 * 1000;
    private static int MILLISECS_IN_MIN = 60 * 1000;

    private String[] mDaysOfWeek;
    private List<Workday> mWorkdays;
    private OnDayItemChangedCallback mCallback;
    private boolean mIsWorkStarted = false;
    private DateFormat mDateFormat;

    public WorkdaysAdapter(@NonNull String[] daysOfWeek, OnDayItemChangedCallback callback) {
        mCallback = callback;
        mDaysOfWeek = daysOfWeek;
        mWorkdays = new ArrayList<>();
        mDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public WorkdayViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.day_element_card_view, viewGroup, false);
        return new WorkdayViewHolder(v,
                new TimeFocusChangeListener(TimeFocusChangeListener.HOURS),
                new TimeFocusChangeListener(TimeFocusChangeListener.MINS));
    }

    @Override
    public void onBindViewHolder(@NonNull WorkdayViewHolder workdayViewHolder, int i) {
        Workday workday = mWorkdays.get(i);
        workdayViewHolder.setListenersPositions(i);
        workdayViewHolder.mDateTextView.setText(workday.getDate().toString());
        workdayViewHolder.mDayOfTheWeekTextView.setText(mDaysOfWeek[workday.getDayOfWeek()]);
        workdayViewHolder.mWorkoutHoursEditText.setText(String.valueOf(workday.getWorkedOutTime() / MILLISECS_IN_HOUR));
        workdayViewHolder.mWorkoutMinutesEditText.setText(String.valueOf((workday.getWorkedOutTime() % MILLISECS_IN_HOUR) / MILLISECS_IN_MIN));
        if (workday.getDate().toString().equals(mDateFormat.format(new Date()))) {
            workdayViewHolder.setEditTextsStatus(!mIsWorkStarted);
        } else {
            workdayViewHolder.setEditTextsStatus(true);
        }
    }

    @Override
    public int getItemCount() {
        return mWorkdays.size();
    }

    public void updateWorkdays(List<Workday> workdays) {
        mWorkdays.clear();
        mWorkdays.addAll(workdays);
        notifyDataSetChanged();
    }

    public void addTimeToLastDay(long workoutTime) {
        if (mWorkdays != null && !mWorkdays.isEmpty()) {
            int lastPos = mWorkdays.size() - 1;
            Workday lastWorkDay = mWorkdays.get(lastPos);
            lastWorkDay.setWorkedOutTime(lastWorkDay.getWorkedOutTime() + workoutTime);
            notifyItemChanged(lastPos);
        }
    }

    public void setIsWorkStarted(boolean isWorkStarted) {
        mIsWorkStarted = isWorkStarted;
        if (!mWorkdays.isEmpty()) {
            notifyItemChanged(getItemCount() - 1);
        }
    }

    class TimeFocusChangeListener implements View.OnFocusChangeListener {
        public final static int HOURS = 0;
        public final static int MINS = 1;

        private int mPosition;
        private int mTimeUnits;

        public TimeFocusChangeListener(int timeUnits) {
            mTimeUnits = timeUnits;
        }

        public void setPosition(int position) {
            mPosition = position;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (v instanceof EditText && !hasFocus) {
                EditText editText = (EditText) v;
                String text = editText.getText().toString();
                Long time = text.isEmpty() ? 0L : Long.valueOf(text);
                Workday workday = mWorkdays.get(mPosition);
                Long newWorkoutTime = mTimeUnits == HOURS
                        ? time * MILLISECS_IN_HOUR + workday.getWorkedOutTime() % MILLISECS_IN_HOUR
                        : time * MILLISECS_IN_MIN + (workday.getWorkedOutTime() / MILLISECS_IN_HOUR) * MILLISECS_IN_HOUR;
                Long diff = newWorkoutTime - workday.getWorkedOutTime();
                if (diff != 0) {
                    workday.setWorkedOutTime(newWorkoutTime);
                    mCallback.updateDay(workday);
                    mCallback.addTimeToWeekWorkout(diff);
                }
                editText.setText(String.valueOf(time));
            }

        }
    }
}
