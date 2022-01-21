package com.rilchil.mythoughts;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;

public class CustomPreferenceCategory extends PreferenceCategory {

    private TextView titleView;
    private boolean darkModeEnable = false;

    public void setDarkModeEnable(boolean darkModeEnable) {
        this.darkModeEnable = darkModeEnable;
    }

    public CustomPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomPreferenceCategory(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        titleView = (TextView) holder.findViewById(android.R.id.title);
        if(darkModeEnable) {
            titleView.setTextColor(ContextCompat.getColor(getContext(), R.color.prefCategoryDarkTheme));
        } else {
            titleView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));

        }
    }

    public void setTextDarkTheme(){
        if(titleView!=null) {
            titleView.setTextColor(ContextCompat.getColor(getContext(), R.color.prefCategoryDarkTheme));
        }

    }

    public void setTextLightTheme(){
        if(titleView!=null) {
            titleView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        }

    }
}
