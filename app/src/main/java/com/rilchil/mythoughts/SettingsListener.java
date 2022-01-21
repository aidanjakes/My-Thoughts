package com.rilchil.mythoughts;

public interface SettingsListener {

    void setTextBold();
    void setTextNormal();
    void setAlarmHourAndMinute(int hour, int minute);
    void cancelAlarm();
    void enableAlarm();
    void enableDarkMode();
    void disableDarkMode();
    void updateHomeRecycler();
}
