package com.rilchil.mythoughts;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements ThemeListener, SettingsListener, HomeListener,
        FragmentManager.OnBackStackChangedListener {

    private static final String BUNDLE_FIRST_CREATION = "firstCreated";
    private FragmentManager fragmentManager;
    private static final String TAG_HOME_FRAGMENT = "home";
    private static final String TAG_THEME_FRAGMENT = "theme";
    private static final String TAG_SETTINGS_FRAGMENT = "settings";
    private static final String BUNDLE_ACTIVE_FRAGMENT = "activeFragment";
    private SharedPreferences sharedPreferences;
    private int alarmHour;
    private int alarmMinute;
    static final String SHARED_ALARM_HOUR = "HOUR";
    static final String SHARED_ALARM_MINUTE = "MINUTE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar_new_main);
        setSupportActionBar(toolbar);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(SettingsFragment.NOTIFICATION_CHANNEL,
                    "Reminder", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if(notificationManager!=null) {
                notificationManager.createNotificationChannel(channel);
            }
        }


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //check sharedPreferences
        alarmHour = sharedPreferences.getInt(SHARED_ALARM_HOUR, 20);
        alarmMinute = sharedPreferences.getInt(SHARED_ALARM_MINUTE, 0);

        //boolean firstCreated = true;

        HomeFragment homeFragment = new HomeFragment();
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.main_frame_layout,homeFragment,TAG_HOME_FRAGMENT);
        //transaction.addToBackStack(TAG_HOME_FRAGMENT);
        transaction.commit();
        String currentTheme = sharedPreferences.getString(HomeFragment.SHARED_THEME_NAME,HomeFragment.DEFAULT_THEME_NAME);

        if(currentTheme.equals(HomeFragment.DEFAULT_THEME_NAME)){
            Window window = getWindow();
            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
             // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // finally change the color
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
            toolbar.setBackgroundColor(getColor(R.color.colorPrimary));
        } else if(currentTheme.equals(HomeFragment.BLACK_BLUE_THEME_NAME)){
            Window window = getWindow();
            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // finally change the color
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
            toolbar.setBackgroundColor(getColor(R.color.colorPrimary));
        } else if(currentTheme.equals(HomeFragment.BLACK_STAR_THEME_NAME)){
            Window window = getWindow();
            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // finally change the color
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.black));
            toolbar.setBackgroundColor(getColor(R.color.black));

        }


        //Listen for changes in the back stack
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        //Handle when activity is recreated like on orientation Change
        shouldDisplayHomeUp();


        boolean darkModeEnabled = sharedPreferences.getBoolean(SettingsFragment.SHARED_DARK_MODE_ENABLED,false);
        if(darkModeEnabled){
            enableDarkMode();
        } else {
            disableDarkMode();
        }


    }



    @Override
    public void onBackStackChanged() {
        shouldDisplayHomeUp();
    }

    public void shouldDisplayHomeUp(){
        //Enable Up button only  if there are entries in the back stack
        boolean canGoBack = getSupportFragmentManager().getBackStackEntryCount()>0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(canGoBack);
    }

    @Override
    public boolean onSupportNavigateUp() {
        //This method is called when the up button is pressed. Just the pop back stack.
        getSupportFragmentManager().popBackStack();
        String appName = getString(R.string.app_name);
        getSupportActionBar().setTitle(appName);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        String appName = getString(R.string.app_name);
        getSupportActionBar().setTitle(appName);
        getSupportFragmentManager().popBackStack();

    }


    @Override
    public void setAlarmHourAndMinute(int hour, int minute) {
        this.alarmHour = hour;
        this.alarmMinute = minute;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SHARED_ALARM_HOUR,hour);
        editor.putInt(SHARED_ALARM_MINUTE,minute);
        editor.apply();

    }

    @Override
    public void cancelAlarm() {
        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if(alarmManager!=null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    @Override
    public void enableAlarm() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,alarmHour);
        calendar.set(Calendar.MINUTE, alarmMinute);
        calendar.set(Calendar.SECOND, 0);
        if (calendar.getTime().compareTo(new Date()) < 0)
            calendar.add(Calendar.DAY_OF_MONTH, 1);

        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }



    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BUNDLE_FIRST_CREATION,false);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }





    @Override
    public void setBlackBlueTheme() {
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(TAG_HOME_FRAGMENT);
        if(homeFragment != null){
            homeFragment.setBlackBlueTheme();
        }
        Window window = getWindow();
// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
        Toolbar toolbar = findViewById(R.id.toolbar_new_main);
        toolbar.setBackgroundColor(getColor(R.color.colorPrimary));
        Toast.makeText(this, "New Theme Selected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setNormalTheme() {
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(TAG_HOME_FRAGMENT);
        if(homeFragment != null){
            homeFragment.setNormalTheme();
        }
        Window window = getWindow();
// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
        Toolbar toolbar = findViewById(R.id.toolbar_new_main);
        toolbar.setBackgroundColor(getColor(R.color.colorPrimary));
        Toast.makeText(this, "New Theme Selected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setStarTheme() {
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(TAG_HOME_FRAGMENT);
        if(homeFragment != null) {
            homeFragment.setStarTheme();
        }
        Window window = getWindow();
// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.black));
        Toolbar toolbar = findViewById(R.id.toolbar_new_main);
        toolbar.setBackgroundColor(getColor(R.color.black));
        Toast.makeText(this, "New Theme Selected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setTextBold() {
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(TAG_HOME_FRAGMENT);
        if(homeFragment!=null){
            homeFragment.setTextBold();
        }
        Toast.makeText(this,"Bold Selected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setTextNormal() {
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(TAG_HOME_FRAGMENT);
        if(homeFragment!=null){
            homeFragment.setTextNormal();
        }
        Toast.makeText(this,"Normal Selected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void enableDarkMode() {
        FrameLayout frameLayout = findViewById(R.id.main_frame_layout);
        frameLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.darkThemeGrey));
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(TAG_HOME_FRAGMENT);
        if(homeFragment!=null){
            homeFragment.enableDarkMode();
        }
        ThemeFragment themeFragment = (ThemeFragment) getSupportFragmentManager().findFragmentByTag(TAG_THEME_FRAGMENT);
        if(themeFragment!=null){
            themeFragment.enableDarkMode();
        }
    }


    @Override
    public void disableDarkMode() {
        FrameLayout frameLayout = findViewById(R.id.main_frame_layout);
        frameLayout.setBackgroundColor(Color.TRANSPARENT);
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(TAG_HOME_FRAGMENT);
        if(homeFragment!=null){
            homeFragment.disableDarkMode();
        }
        ThemeFragment themeFragment = (ThemeFragment) getSupportFragmentManager().findFragmentByTag(TAG_THEME_FRAGMENT);
        if(themeFragment!=null){
            themeFragment.disableDarkMode();
        }
    }

    @Override
    public void updateHomeRecycler() {
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(TAG_HOME_FRAGMENT);
        if(homeFragment!=null){
            homeFragment.refreshRecyclerView();
        }
    }

    @Override
    public void onSettingsItemClicked() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        SettingsFragment settingsFragment = new SettingsFragment();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
        fragmentTransaction.addToBackStack(TAG_SETTINGS_FRAGMENT);
        fragmentTransaction.replace(R.id.main_frame_layout,settingsFragment,TAG_SETTINGS_FRAGMENT);
        fragmentTransaction.commit();

    }

    @Override
    public void onThemeItemClicked() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        ThemeFragment themeFragment = new ThemeFragment();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
        fragmentTransaction.replace(R.id.main_frame_layout,themeFragment,TAG_THEME_FRAGMENT);
        fragmentTransaction.addToBackStack(TAG_THEME_FRAGMENT);
        fragmentTransaction.commit();
    }
}
