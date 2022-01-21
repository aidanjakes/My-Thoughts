package com.rilchil.mythoughts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.widget.TextView;
import android.widget.Toast;
import java.text.DateFormat;
import java.util.Calendar;

public class DisplayThoughtActivity extends AppCompatActivity {

    /*
    clickable, longClickable, focusable, focusableInTouchMode and cursorVisible all set to false
    until user presses edit button
     */

    public static final String EXTRA_THOUGHT_ID = "thoughtID";
    public static final String EXTRA_COLOR = "extracolor";
    private static final String BUNDLE_EDITABLE = "wasEditable";
    private int rowID;
    private Cursor cursor;
    private SQLiteDatabase database;
    private String originalTitle;
    private String originalThought;
    private boolean isEditable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_thought);

        isEditable = false;
        if(savedInstanceState != null){
            isEditable = savedInstanceState.getBoolean(BUNDLE_EDITABLE, false);
        }
        if(isEditable){
            EditText titleEdit = findViewById(R.id.display_title_edittext);
            EditText thoughtEdit = findViewById(R.id.display_thought_edittext);
            setEditable(titleEdit);
            setEditable(thoughtEdit);
        }
        Intent intent = getIntent();

        rowID = intent.getIntExtra(EXTRA_THOUGHT_ID, 0);

        setTextFields();

        Toolbar toolbar = findViewById(R.id.display_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean darkModeEnabled = sharedPreferences.getBoolean(SettingsFragment.SHARED_DARK_MODE_ENABLED, false);
        LinearLayout linearLayout = findViewById(R.id.display_linear_layout);
        EditText editTitle = findViewById(R.id.display_title_edittext);
        EditText editThought = findViewById(R.id.display_thought_edittext);
        TextView timeText = findViewById(R.id.display_date_and_time);
        if(darkModeEnabled){
            linearLayout.setBackgroundColor(ContextCompat.getColor(this,R.color.darkThemeGrey));
            editTitle.setTextColor(ContextCompat.getColor(this,R.color.white));
            editThought.setTextColor(ContextCompat.getColor(this,R.color.white));
            timeText.setTextColor(ContextCompat.getColor(this,R.color.displayTimeDarkTheme));
        } else {
            linearLayout.setBackgroundColor(Color.TRANSPARENT);
            editTitle.setTextColor(ContextCompat.getColor(this,R.color.black));
            editThought.setTextColor(ContextCompat.getColor(this,R.color.black));
            timeText.setTextColor(ContextCompat.getColor(this,R.color.displayTimeLightTheme));
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
        menuInflater.inflate(R.menu.menu_display_thought, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.display_item_share:
                ThoughtDatabaseHelper helper = new ThoughtDatabaseHelper(this);
                String date,time,thought;
                try{
                    SQLiteDatabase database = helper.getReadableDatabase();
                    Cursor cursor = database.query(ThoughtTable.TABLE_NAME,
                            new String[] {ThoughtTable.COLUMN_ID, ThoughtTable.COLUMN_DATE, ThoughtTable.COLUMN_TIME},
                            ThoughtTable.COLUMN_ID + " = ?",
                            new String[] {Integer.toString(rowID)},
                            null, null, null);
                    if(cursor.moveToFirst()){
                        date = cursor.getString(cursor.getColumnIndex(ThoughtTable.COLUMN_DATE));
                        int year = Integer.parseInt(date.substring(0,4));
                        int month = Integer.parseInt(date.substring(5,7)) - 1;
                        int day = Integer.parseInt(date.substring(8,10));
                        DateFormat dateFormat = DateFormat.getDateInstance();
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year,month,day);
                        String formattedDate = dateFormat.format(calendar.getTime());
                        time = cursor.getString(cursor.getColumnIndex(ThoughtTable.COLUMN_TIME));
                        time = time.substring(0,5);
                        EditText editText = findViewById(R.id.display_thought_edittext);
                        thought = editText.getText().toString();

                        EditText titleText = findViewById(R.id.display_title_edittext);
                        setNotEditable(editText);
                        setNotEditable(titleText);
                        String shareMessage = "On " + formattedDate + " at " + time + " I was thinking \"" + thought + "\"";
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                        startActivity(intent);
                    }
                } catch (SQLiteException e){
                    Toast toast = Toast.makeText(this, "Database Unavailable",Toast.LENGTH_SHORT);
                    toast.show();

                }
                return true;
            case R.id.display_item_edit:
                EditText titleEdit = findViewById(R.id.display_title_edittext);
                EditText thoughtEdit = findViewById(R.id.display_thought_edittext);
                setEditable(titleEdit);
                setEditable(thoughtEdit);
                isEditable = true;
                return true;
            case R.id.display_item_done:
                if (valuesHaveChanged()){
                    closeKeyboard();
                    new UpdateThoughtTask().execute();
                    //close activity
                    finish();
                } else {
                    finish();

                }
                return true;
            default:
                return false;
        }
    }

    private void setTextFields(){
        ThoughtDatabaseHelper helper = new ThoughtDatabaseHelper(this);
        try{
            database = helper.getReadableDatabase();
            cursor = database.query(ThoughtTable.TABLE_NAME,
                    new String[] {ThoughtTable.COLUMN_ID, ThoughtTable.COLUMN_TITLE,
                            ThoughtTable.COLUMN_THOUGHT_TEXT, ThoughtTable.COLUMN_DATE,
                            ThoughtTable.COLUMN_TIME},
                    ThoughtTable.COLUMN_ID + " = ?",
                    new String[] {Integer.toString(rowID)},
                    null, null, null);
            if(cursor.moveToFirst()){
                String title = cursor.getString(cursor.getColumnIndex(ThoughtTable.COLUMN_TITLE));
                originalTitle = title;
                TextView displayTitleView = findViewById(R.id.display_title_edittext);
                displayTitleView.setText(title);

                String thought = cursor.getString(cursor.getColumnIndex(ThoughtTable.COLUMN_THOUGHT_TEXT));
                originalThought = thought;
                TextView thoughtView = findViewById(R.id.display_thought_edittext);
                thoughtView.setText(thought);

                String date = cursor.getString(cursor.getColumnIndex(ThoughtTable.COLUMN_DATE));
                int year = Integer.parseInt(date.substring(0,4));
                int month = Integer.parseInt(date.substring(5,7)) - 1;
                int day = Integer.parseInt(date.substring(8,10));
                String time = cursor.getString(cursor.getColumnIndex(ThoughtTable.COLUMN_TIME));
                DateFormat dateFormat = DateFormat.getDateInstance();
                Calendar calendar = Calendar.getInstance();
                calendar.set(year,month,day);
                String formattedDate = dateFormat.format(calendar.getTime());
                String formattedTime = time.substring(0,5);
                String finalStr = formattedDate + " " + formattedTime;
                TextView textView = findViewById(R.id.display_date_and_time);
                textView.setText(finalStr);
            }
        } catch (SQLiteException e){
            Toast toast = Toast.makeText(this, "Database Unavailable",Toast.LENGTH_SHORT);
            toast.show();

        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BUNDLE_EDITABLE, isEditable);
    }

    @Override
    protected void onDestroy() {
        database.close();
        cursor.close();
        super.onDestroy();
    }

    private boolean valuesHaveChanged(){
        EditText editTitle = findViewById(R.id.display_title_edittext);
        String newTitle = editTitle.getText().toString().trim();

        EditText editThought = findViewById(R.id.display_thought_edittext);
        String newThought = editThought.getText().toString().trim();

        return !(newTitle.equals(originalTitle) && newThought.equals(originalThought));
    }

    @Override
    public void onBackPressed() {
        if(!valuesHaveChanged()){
            finish();
            return;
        }
        createEditConfirmationDialog();
    }

    private void closeKeyboard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if(getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }


    private void setEditable(EditText editText){
        editText.setClickable(true);
        editText.setLongClickable(true);
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.setCursorVisible(true);
        editText.requestFocus();
        editText.setSelection(editText.getText().toString().length());
    }

    private void setNotEditable(EditText editText){
        editText.setClickable(false);
        editText.setLongClickable(false);
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
        editText.setCursorVisible(false);
    }

    private void createEditConfirmationDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(R.string.dialog_edit_message)
                .setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //saveEditedEntry
                        closeKeyboard();
                        new UpdateThoughtTask().execute();
                        //close activity
                        finish();
                    }
                })
                .setNegativeButton(R.string.dialog_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //close activity without saving
                        finish();
                    }
                });
        dialogBuilder.show();

    }



    private class UpdateThoughtTask extends AsyncTask<Void,Void,Boolean>{

        private ContentValues contentValues;
        private SQLiteDatabase database;

        @Override
        protected void onPreExecute() {
            contentValues = new ContentValues();

            EditText editTitle = findViewById(R.id.display_title_edittext);
            String title = editTitle.getText().toString().trim();

            EditText editThought = findViewById(R.id.display_thought_edittext);
            String thought = editThought.getText().toString();
            contentValues.put(ThoughtTable.COLUMN_THOUGHT_TEXT, thought);



            if(title.length() == 0 && thought.length() == 0){
                title = "untitled";
            } if(title.length() == 0){
                int firstLineStartPos = editThought.getLayout().getLineStart(0);
                int firstLineEndPos = editThought.getLayout().getLineEnd(0);
                title = thought.substring(firstLineStartPos, firstLineEndPos);
            }
            contentValues.put(ThoughtTable.COLUMN_TITLE, title);

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            ThoughtDatabaseHelper helper = new ThoughtDatabaseHelper(DisplayThoughtActivity.this);
            try{
                database = helper.getWritableDatabase();
                database.update(ThoughtTable.TABLE_NAME, contentValues,
                        ThoughtTable.COLUMN_ID + " = ?",
                        new String[] {Integer.toString(rowID)});
                database.close();
                return true;
            } catch(SQLiteException e){
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(success) {
                Toast toast = Toast.makeText(DisplayThoughtActivity.this, "Saved", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(DisplayThoughtActivity.this,
                        "Save Unsuccessful", Toast.LENGTH_SHORT);
                toast.show();
            }

        }
    }

}
