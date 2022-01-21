package com.rilchil.mythoughts;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class CustomPreference extends Preference {

    private TextView titleView;
    private TextView summaryView;
    private boolean darkModeEnable = false;


    public CustomPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public CustomPreference(Context context) {
        super(context);

    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        titleView = (TextView) holder.findViewById(android.R.id.title);
        summaryView = (TextView) holder.findViewById(android.R.id.summary);
        if(darkModeEnable) {
            if(titleView!=null) titleView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            if(summaryView!=null)summaryView.setTextColor(ContextCompat.getColor(getContext(),R.color.summaryDarkThemeTextColor));
        } else {
            if(titleView!=null)titleView.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            if(summaryView!=null)summaryView.setTextColor(ContextCompat.getColor(getContext(), R.color.summaryLightThemeTextColor));

        }
    }

    public void setDarkModeEnable(boolean darkModeEnable) {
        this.darkModeEnable = darkModeEnable;
    }

    public void setTextWhite(){
        if(titleView!=null) {
            titleView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        }

        if(summaryView!=null) {
            summaryView.setTextColor(ContextCompat.getColor(getContext(),R.color.summaryDarkThemeTextColor));
        }


    }

    public void setTextBlack(){
        if(titleView!=null) {
            titleView.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        }
        if(summaryView!=null) {
            summaryView.setTextColor(ContextCompat.getColor(getContext(), R.color.summaryLightThemeTextColor));
        }
    }



}
