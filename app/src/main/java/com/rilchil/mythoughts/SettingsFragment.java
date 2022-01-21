package com.rilchil.mythoughts;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    static final String NOTIFICATION_CHANNEL = "MyThoughtsNotifications";
    private int hour;
    private int min;
    private SettingsListener settingsListener;
    private static final String SHARED_NOTI_ENABLED_INITIAL_VALUE = "NOTI_ENABLED_INITIAL_VALUE";
    static final String SHARED_DARK_MODE_ENABLED = "SHARED_DARK_MODE_ENABLED";
    private static final String DRIVE_BUILDER_APP_NAME = "MyThoughts";
    private static final int SIGN_IN_REQUEST_CODE = 1;
    private DriveServiceHelper driveServiceHelper;
    private GoogleSignInClient mGoogleSignInClient;
    //ContextCompat.getColor(getActivity(),R.color.darkThemeGrey)

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        settingsListener = (SettingsListener) context;
    }


    @Override
    public void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
        if(account!=null){
            ArrayList<String> scopes = new ArrayList();
            scopes.add(DriveScopes.DRIVE_FILE);
            scopes.add(DriveScopes.DRIVE_APPDATA);

            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(getActivity(),
                    scopes);
            credential.setSelectedAccount(account.getAccount());
            Drive googleDriveService =
                    new Drive.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            new GsonFactory(),
                            credential)
                            .setApplicationName(DRIVE_BUILDER_APP_NAME)
                            .build();
            SettingsFragment.this.driveServiceHelper = new DriveServiceHelper(googleDriveService);

            String email = account.getEmail();
            String summary = "Tap to sign out";
            CustomPreference loginPref = findPreference("pref_login");
            loginPref.setTitle(email);
            loginPref.setSummary(summary);
            loginPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    GoogleSignInOptions googleSignInOptions = new
                            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .requestProfile()
                            .requestScopes(new Scope(DriveScopes.DRIVE_FILE), new Scope(DriveScopes.DRIVE_APPDATA))
                            .build();
                    GoogleSignIn.getClient(getActivity(), googleSignInOptions).signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getActivity(),"Signed out",Toast.LENGTH_SHORT).show();
                        }
                    });
                    loginPref.setTitle("Google Account Login");
                    loginPref.setSummary("Tap to sign in");
                    CustomPreference backupPref = findPreference("pref_backup");
                    backupPref.setSummary("");
                    loginPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            googleSignIn();
                            return true;
                        }
                    });
                    return true;
                }
            });
            driveServiceHelper.getMostRecentBackupDetails().addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String s) {
                    CustomPreference backupPref = findPreference("pref_backup");
                    if(s != null) {
                        String message = "Last backup: " + s;
                        backupPref.setSummary(message);
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }

    }

    private void googleSignIn(){
        //if(!isSignedIn()) {
            GoogleSignInOptions googleSignInOptions = new
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestProfile()
                    .requestScopes(new Scope(DriveScopes.DRIVE_FILE), new Scope(DriveScopes.DRIVE_APPDATA))
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), googleSignInOptions);
            Intent intent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(intent, SIGN_IN_REQUEST_CODE);
        //}
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SIGN_IN_REQUEST_CODE){
            handleSignInResult(data);
        }
    }


    private void handleSignInResult(Intent data){
        GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                        String email = googleSignInAccount.getEmail();
                        String msg = email + " signed in";
                        Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
                        String summary = "Tap to sign out";
                        CustomPreference loginPref = findPreference("pref_login");
                        loginPref.setTitle(email);
                        loginPref.setSummary(summary);
                        loginPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(Preference preference) {
                                GoogleSignInOptions googleSignInOptions = new
                                        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestEmail()
                                        .requestProfile()
                                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE), new Scope(DriveScopes.DRIVE_APPDATA))
                                        .build();
                                mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(getActivity(),"Signed out",Toast.LENGTH_SHORT).show();
                                    }
                                });
                                loginPref.setTitle("Google Account Login");
                                loginPref.setSummary("Tap to sign in");
                                CustomPreference backupPref = findPreference("pref_backup");
                                backupPref.setSummary("");
                                loginPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                                    @Override
                                    public boolean onPreferenceClick(Preference preference) {
                                        googleSignIn();
                                        return true;
                                    }
                                });
                                return true;
                            }
                        });
                        ArrayList<String> scopes = new ArrayList();
                        scopes.add(DriveScopes.DRIVE_FILE);
                        scopes.add(DriveScopes.DRIVE_APPDATA);


                        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(getActivity(),
                                scopes);
                        credential.setSelectedAccount(googleSignInAccount.getAccount());
                        Drive googleDriveService =
                                new Drive.Builder(
                                        AndroidHttp.newCompatibleTransport(),
                                        new GsonFactory(),
                                        credential)
                                        .setApplicationName(DRIVE_BUILDER_APP_NAME)
                                        .build();

                        SettingsFragment.this.driveServiceHelper = new DriveServiceHelper(googleDriveService);

                        driveServiceHelper.getMostRecentBackupDetails().addOnSuccessListener(new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String s) {
                                CustomPreference backupPref = findPreference("pref_backup");
                                if(s != null) {
                                    String message = "Last backup: " + s;
                                    backupPref.setSummary(message);
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(),"Sign in failed",Toast.LENGTH_SHORT).show();

                    }
                });
    }


    private void onBackupPressed(){
        if(GoogleSignIn.getLastSignedInAccount(getActivity()) == null || driveServiceHelper == null){
            Toast.makeText(getActivity(), "Must sign in first", Toast.LENGTH_SHORT).show();
            return;
        }
        ProgressDialog progressDialog = ProgressDialog.show(getActivity(),"Backing up data","",true);
        String backupPath = createBackupCSVFile();
        driveServiceHelper.createDriveCSVFile(backupPath)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        progressDialog.dismiss();
                        if(getActivity()!=null) {
                            Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String lastBackupDateTime = simpleDateFormat.format(calendar.getTime());
                            CustomPreference backupPref = findPreference("pref_backup");
                            backupPref.setSummary("Last backup: " + lastBackupDateTime);
                            // Toast.makeText(getActivity(),s, Toast.LENGTH_LONG).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                if (getActivity()!=null) Toast.makeText(getActivity(),"Upload failed", Toast.LENGTH_SHORT).show();

            }
        });


    }

    private String createBackupCSVFile(){
        ThoughtDatabaseHelper databaseHelper = new ThoughtDatabaseHelper(getActivity());
        File interStorDir = getActivity().getFilesDir();
        File csvDest = new File(interStorDir,"thoughts.csv");
        try
        {
            if (!csvDest.exists()) {
                try {
                    csvDest.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //overwrite do not append
            FileWriter fileWriter = new FileWriter(csvDest,false);
            CSVWriter csvWriter = new CSVWriter(fileWriter);
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            //first line will be column names
            /*
            fileWriter.append(ThoughtTable.COLUMN_ID).append(",");
            fileWriter.append(ThoughtTable.COLUMN_TITLE).append(",");
            fileWriter.append(ThoughtTable.COLUMN_THOUGHT_TEXT).append(",");
            fileWriter.append(ThoughtTable.COLUMN_DATE).append(",");
            fileWriter.append(ThoughtTable.COLUMN_TIME).append("\n");

             */
            csvWriter.writeNext(ThoughtTable.COLUMNS);

            Cursor cursor = db.query(ThoughtTable.TABLE_NAME,
                    ThoughtTable.COLUMNS,
                    null, null, null, null,
                    "DATE(" + ThoughtTable.COLUMN_DATE + ") DESC," +
                            "TIME(" + ThoughtTable.COLUMN_TIME + ") DESC");
            while(cursor.moveToNext()){
                String id = cursor.getString(cursor.getColumnIndex(ThoughtTable.COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndex(ThoughtTable.COLUMN_TITLE));
                //title = "\"" + title + "\"";
                String thought = cursor.getString(cursor.getColumnIndex(ThoughtTable.COLUMN_THOUGHT_TEXT));
                //thought = thought.replaceAll("(\r|\n)", " ");
                //thought = "\"" + thought + "\"";
                String date = cursor.getString(cursor.getColumnIndex(ThoughtTable.COLUMN_DATE));
                String time = cursor.getString(cursor.getColumnIndex(ThoughtTable.COLUMN_TIME));
                String[] row = {id,title,thought,date,time};
                csvWriter.writeNext(row);
                /*
                fileWriter.append(id).append(",");
                fileWriter.append(title).append(",");
                fileWriter.append(thought).append(",");
                fileWriter.append(date).append(",");
                fileWriter.append(time).append("\n");
                fileWriter.flush();

                 */


            }
            csvWriter.flush();
            csvWriter.close();
            cursor.close();

        }
        catch(Exception sqlEx)
        {

        }
        return csvDest.getAbsolutePath();


    }



    private void onRestorePressed(){
        if(GoogleSignIn.getLastSignedInAccount(getActivity()) == null || driveServiceHelper == null){
            Toast.makeText(getActivity(), "Must sign in first", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(getActivity())
                .setTitle("Restore data")
                .setMessage("This will replace your current entries with those in the " +
                        "most recently backed up file. Any data that is not currently backed up will be lost.")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        ThoughtDatabaseHelper databaseHelper = new ThoughtDatabaseHelper(getActivity());
                        SQLiteDatabase database = databaseHelper.getWritableDatabase();
                        driveServiceHelper.setDatabase(database);
                        ProgressDialog progressDialog = ProgressDialog.show(getActivity(),"Restoring data","",true);

                        File interStorDir = getActivity().getFilesDir();
                        File csvDest = new File(interStorDir,"thoughts.csv");
                        driveServiceHelper.downloadCSVFile(csvDest.getAbsolutePath())
                                .addOnSuccessListener(new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(String s) {
                                        if(s!=null) {
                                            if(getActivity()!=null)
                                                Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();

                                        } else {
                                            if(getActivity()!=null) Toast.makeText(getActivity(), "No backup found", Toast.LENGTH_SHORT).show();
                                        }
                                        progressDialog.dismiss();


                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();

                                if ((getActivity()!=null))Toast.makeText(getActivity(),e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel",null)
                .show();

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences,rootKey);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        String settingsName = getString(R.string.settings);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(settingsName);


        final CustomSwitchPreferenceCompat darkSwitch = findPreference("switch_dark_preference");
        boolean initialDarkEnabled = sharedPreferences.getBoolean(SHARED_DARK_MODE_ENABLED,false);
        initialiseTextColors(initialDarkEnabled);

        CustomPreference loginPref = findPreference("pref_login");
        loginPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                googleSignIn();
                return true;
            }
        });


        CustomPreference backupPref = findPreference("pref_backup");
        backupPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                onBackupPressed();
                return true;
            }
        });


        CustomPreference restorePref = findPreference("pref_restore");
        restorePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                onRestorePressed();
                return true;
            }
        });


        //Toast.makeText(getActivity(),Boolean.toString(initialDarkEnabled),Toast.LENGTH_SHORT).show();
        darkSwitch.setDefaultValue(initialDarkEnabled);
        darkSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean active = (Boolean) newValue;
                //update settings view
                initialiseTextColors(active);
                if(active){
                    /*
                    if(getView()!= null) {
                        getView().setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.darkThemeGrey));
                        setTextDarkTheme();
                    }

                     */
                    setTextDarkTheme();
                    editor.putBoolean(SHARED_DARK_MODE_ENABLED,true);
                    editor.apply();
                    settingsListener.enableDarkMode();

                } else {
                    /*
                    if(getView()!= null) {
                        getView().setBackgroundColor(Color.TRANSPARENT);
                        setTextLightTheme();
                    }

                     */
                    setTextLightTheme();
                    editor.putBoolean(SHARED_DARK_MODE_ENABLED,false);
                    editor.apply();
                    settingsListener.disableDarkMode();
                }
                return true;
            }
        });


        CustomDropDownPreference styleDropDown = findPreference("pref_text_style");
        String initialStyleValue;
        String sharedTextStyle = sharedPreferences.getString(HomeFragment.SHARED_TEXT_TYPEFACE,HomeFragment.TEXT_NORMAL);
        if(sharedTextStyle.equals(HomeFragment.TEXT_NORMAL)){
            initialStyleValue = "Normal";
        } else {
            initialStyleValue = "Bold";
        }
        styleDropDown.setValue(initialStyleValue);
        styleDropDown.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(newValue.toString().equals("Bold")) {
                    settingsListener.setTextBold();
                } else if(newValue.toString().equals("Normal")){
                    settingsListener.setTextNormal();
                }
                return true;
            }
        });

        boolean initialNotiEnabled = sharedPreferences.getBoolean(SHARED_NOTI_ENABLED_INITIAL_VALUE, false);
        CustomSwitchPreferenceCompat switchPreference = findPreference("switch_notification_pref");
        switchPreference.setDefaultValue(initialNotiEnabled);
        switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean newVal = (Boolean) newValue;
                if(newVal) {
                    settingsListener.enableAlarm();
                } else {
                    settingsListener.cancelAlarm();
                }
                editor.putBoolean(SHARED_NOTI_ENABLED_INITIAL_VALUE,newVal);
                editor.apply();
                return true;
            }
        });
        final CustomPreference notiTimePref = findPreference("noti_time_pref");
        hour = sharedPreferences.getInt(MainActivity.SHARED_ALARM_HOUR,20);
        min = sharedPreferences.getInt(MainActivity.SHARED_ALARM_MINUTE,0);

        String defaultSummary  = String.format(Locale.getDefault(), "%02d:%02d", hour, min);
        notiTimePref.setSummary(defaultSummary);
        notiTimePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        hour = hourOfDay;
                        min = minute;
                        notiTimePref.setSummary(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                        settingsListener.setAlarmHourAndMinute(hour,min);

                    }
                };
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),timeSetListener,
                        hour, min,true);
                timePickerDialog.setTitle("Select Time");
                timePickerDialog.updateTime(hour,min);
                timePickerDialog.show();
                return true;
            }
        });

    }




    private void initialiseTextColors(boolean darkModeEnabled){


        CustomPreferenceCategory backupCat = findPreference("backup_pref_cat");
        backupCat.setDarkModeEnable(darkModeEnabled);
        CustomPreference loginPref = findPreference("pref_login");
        loginPref.setDarkModeEnable(darkModeEnabled);
        CustomPreference backupPref = findPreference("pref_backup");
        backupPref.setDarkModeEnable(darkModeEnabled);
        CustomPreference restorePref = findPreference("pref_restore");
        restorePref.setDarkModeEnable(darkModeEnabled);



        CustomPreferenceCategory appearanceCat = findPreference("appearance_pref_cat");
        appearanceCat.setDarkModeEnable(darkModeEnabled);
        CustomSwitchPreferenceCompat darkPref = findPreference("switch_dark_preference");
        darkPref.setDarkModeEnable(darkModeEnabled);
        CustomDropDownPreference stylePref = findPreference("pref_text_style");
        stylePref.setDarkModeEnable(darkModeEnabled);

        CustomPreferenceCategory reminderCat = findPreference("reminder_pref_cat");
        reminderCat.setDarkModeEnable(darkModeEnabled);
        CustomSwitchPreferenceCompat notiPref = findPreference("switch_notification_pref");
        notiPref.setDarkModeEnable(darkModeEnabled);
        CustomPreference notiTimePref = findPreference("noti_time_pref");
        notiTimePref.setDarkModeEnable(darkModeEnabled);

    }

    private void setTextDarkTheme(){

        CustomPreferenceCategory backupCat = findPreference("backup_pref_cat");
        backupCat.setTextDarkTheme();
        CustomPreference loginPref = findPreference("pref_login");
        loginPref.setTextWhite();
        CustomPreference backupPref = findPreference("pref_backup");
        backupPref.setTextWhite();
        CustomPreference restorePref = findPreference("pref_restore");
        restorePref.setTextWhite();



        CustomPreferenceCategory appearanceCat = findPreference("appearance_pref_cat");
        appearanceCat.setTextDarkTheme();
        CustomSwitchPreferenceCompat darkPref = findPreference("switch_dark_preference");
        darkPref.setTextWhite();
        CustomDropDownPreference stylePref = findPreference("pref_text_style");
        stylePref.setTextWhite();


        CustomPreferenceCategory reminderCat = findPreference("reminder_pref_cat");
        reminderCat.setTextDarkTheme();
        CustomSwitchPreferenceCompat notiPref = findPreference("switch_notification_pref");
        notiPref.setTextWhite();
        CustomPreference notiTimePref = findPreference("noti_time_pref");
        notiTimePref.setTextWhite();





    }

    private void setTextLightTheme(){
        CustomPreferenceCategory backupCat = findPreference("backup_pref_cat");
        backupCat.setTextLightTheme();
        CustomPreference loginPref = findPreference("pref_login");
        loginPref.setTextBlack();
        CustomPreference backupPref = findPreference("pref_backup");
        backupPref.setTextBlack();
        CustomPreference restorePref = findPreference("pref_restore");
        restorePref.setTextBlack();

        CustomPreferenceCategory appearanceCat = findPreference("appearance_pref_cat");
        appearanceCat.setTextLightTheme();
        CustomSwitchPreferenceCompat darkPref = findPreference("switch_dark_preference");
        darkPref.setTextBlack();
        CustomDropDownPreference stylePref = findPreference("pref_text_style");
        stylePref.setTextBlack();

        CustomPreferenceCategory reminderCat = findPreference("reminder_pref_cat");
        reminderCat.setTextLightTheme();
        CustomSwitchPreferenceCompat notiPref = findPreference("switch_notification_pref");
        notiPref.setTextBlack();
        CustomPreference notiTimePref = findPreference("noti_time_pref");
        notiTimePref.setTextBlack();


    }



}
