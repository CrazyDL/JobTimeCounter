package com.example.dlevshtanov.jobtimecounter.presentation;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.dlevshtanov.jobtimecounter.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class WorkdayViewHolder extends RecyclerView.ViewHolder {
    TextView mDayOfTheWeekTextView;
    TextView mDateTextView;
    EditText mWorkoutHoursEditText;
    EditText mWorkoutMinutesEditText;
    WorkdaysAdapter.TimeFocusChangeListener mHoursListener;
    WorkdaysAdapter.TimeFocusChangeListener mMinsListener;

    public WorkdayViewHolder(@NonNull View itemView,
                             WorkdaysAdapter.TimeFocusChangeListener hoursListener,
                             WorkdaysAdapter.TimeFocusChangeListener minsListener) {
        super(itemView);
        mHoursListener = hoursListener;
        mMinsListener = minsListener;
        mDayOfTheWeekTextView = itemView.findViewById(R.id.day_of_the_week);
        mDateTextView = itemView.findViewById(R.id.date);
        mWorkoutHoursEditText = itemView.findViewById(R.id.worked_out_hours);
        mWorkoutMinutesEditText = itemView.findViewById(R.id.worked_out_mins);
        mWorkoutHoursEditText.setOnFocusChangeListener(hoursListener);
        mWorkoutMinutesEditText.setOnFocusChangeListener(minsListener);
    }

    public void setListenersPositions(int position){
        mHoursListener.setPosition(position);
        mMinsListener.setPosition(position);
    }

    public void setEditTextsStatus(boolean isEnabled){
        mWorkoutHoursEditText.setEnabled(isEnabled);
        mWorkoutMinutesEditText.setEnabled(isEnabled);
    }
}
