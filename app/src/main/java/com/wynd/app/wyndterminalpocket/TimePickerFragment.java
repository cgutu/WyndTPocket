package com.wynd.app.wyndterminalpocket;

/**
 * Created by cgutu on 11/02/16.
 */
import java.util.Calendar;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TimePickerFragment extends DialogFragment  {

    TimePickerDialog.OnTimeSetListener onTimeSet;
    private boolean isModal = false;

    public static TimePickerFragment newInstance()
    {
        TimePickerFragment frag = new TimePickerFragment();
        frag.isModal = true; // WHEN FRAGMENT IS CALLED AS A DIALOG SET FLAG
        return frag;
    }

    public TimePickerFragment(){}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of DatePickerDialog and return it
        return new TimePickerDialog(getActivity(), onTimeSet, hour, minute, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(isModal) // AVOID REQUEST FEATURE CRASH
        {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
        else {
            View rootView = inflater.inflate(R.layout.content_historique, container, false);
            return rootView;
        }
    }

    public void setCallBack(TimePickerDialog.OnTimeSetListener onTime) {
        onTimeSet = onTime;
    }

}