package com.rilchil.mythoughts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddThoughtActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_thought);

        Toolbar toolbar = findViewById(R.id.toolbar_add_thought);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean darkModeEnabled = sharedPreferences.getBoolean(SettingsFragment.SHARED_DARK_MODE_ENABLED, false);
        LinearLayout linearLayout = findViewById(R.id.add_linear_layout);
        EditText titleText = findViewById(R.id.add_edittext_title);
        EditText thoughtText = findViewById(R.id.add_edittext_thought_text);
        if(darkModeEnabled){
            linearLayout.setBackgroundColor(ContextCompat.getColor(this,R.color.darkThemeGrey));
            titleText.setTextColor(ContextCompat.getColor(this,R.color.white));
            titleText.setHintTextColor(ContextCompat.getColor(this,R.color.addThoughtDarkThemeHint));
            thoughtText.setTextColor(ContextCompat.getColor(this,R.color.white));
            thoughtText.setHintTextColor(ContextCompat.getColor(this,R.color.addThoughtDarkThemeHint));
        } else {
            linearLayout.setBackgroundColor(Color.TRANSPARENT);
            titleText.setTextColor(ContextCompat.getColor(this,R.color.black));
            titleText.setHintTextColor(ContextCompat.getColor(this,R.color.addThoughtLightThemeHint));
            thoughtText.setTextColor(ContextCompat.getColor(this,R.color.black));
            thoughtText.setHintTextColor(ContextCompat.getColor(this,R.color.addThoughtLightThemeHint));
        }

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


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_add_thought, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(checkIfEntryIsEmpty()){
            super.onBackPressed();
            return;
        }
        createAddConfirmationDialog();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.done_item:
                if(checkIfEntryIsEmpty()) {
                    finish();
                } else {
                    //close keyboard before saving to database so the toast can pop up in correct place
                    closeKeyboard();
                    new AddThoughtTask().execute();
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void closeKeyboard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if(getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private boolean checkIfEntryIsEmpty(){
        EditText titleEdit = findViewById(R.id.add_edittext_title);
        String title = titleEdit.getText().toString().trim();

        EditText thoughtEdit = findViewById(R.id.add_edittext_thought_text);
        String thought = thoughtEdit.getText().toString().trim();

        if(title.length() == 0 && thought.length() == 0){
            return true;
        }
        return false;
    }

    private void createAddConfirmationDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(R.string.dialog_add_message)
                .setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //if the user has typed something save the entry to database
                        closeKeyboard();
                        new AddThoughtTask().execute();
                        //close activity
                        AddThoughtActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton(R.string.dialog_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //close activity without saving
                        AddThoughtActivity.super.onBackPressed();

                    }
                });
        dialogBuilder.show();
    }


    private class AddThoughtTask extends AsyncTask<Void, Void, Boolean>{

        private ContentValues contentValues;
        private SQLiteDatabase database;

        @Override
        protected void onPreExecute() {

            contentValues = new ContentValues();

            //make the title the first line of the thought if no title is provided

            EditText titleText = findViewById(R.id.add_edittext_title);
            String title = titleText.getText().toString().trim();

            EditText thoughtText = findViewById(R.id.add_edittext_thought_text);
            String thought = thoughtText.getText().toString().trim();
            contentValues.put(ThoughtTable.COLUMN_THOUGHT_TEXT, thought);

            //make the title the first line of the thought if no title is provided
            if (title.length() == 0 && thought.length() > 0){
                int firstLineStartPos = thoughtText.getLayout().getLineStart(0);
                int firstLineEndPos = thoughtText.getLayout().getLineEnd(0);
                title = thought.substring(firstLineStartPos, firstLineEndPos);
            }
            contentValues.put(ThoughtTable.COLUMN_TITLE, title);


            Calendar calendar = Calendar.getInstance();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
            Date dateObject = calendar.getTime();
            String dateString = sdf.format(dateObject);
            contentValues.put(ThoughtTable.COLUMN_DATE, dateString);

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.UK);
            String time = timeFormat.format(dateObject);
            contentValues.put(ThoughtTable.COLUMN_TIME, time);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            ThoughtDatabaseHelper helper = new ThoughtDatabaseHelper(AddThoughtActivity.this);
            try{
                database = helper.getWritableDatabase();
                database.insert(ThoughtTable.TABLE_NAME, null, contentValues);
                database.close();
                return true;
            } catch(SQLiteException e){
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(success) {
                Toast toast = Toast.makeText(AddThoughtActivity.this, "Saved", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(AddThoughtActivity.this,
                        "Save Unsuccessful", Toast.LENGTH_SHORT);
                toast.show();
            }
        }

    }

}
