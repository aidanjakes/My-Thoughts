package com.rilchil.mythoughts;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ThoughtRecyclerAdapter extends RecyclerView.Adapter<ThoughtRecyclerAdapter.ViewHolder>
        implements Filterable {


    private Cursor cursor;
    private SQLiteDatabase database;
    private ActionMode actionMode;
    private boolean existingDeleteDialog;
    private Context filterContext;
    private ArrayList<Integer> selectedPositions;
    private ArrayList<ViewHolder> selectedHolders = new ArrayList<>();
    private int selectedResource;
    private int unselectedResource;
    private int selectedTextColor;
    private int unselectedTextColor;
    private int textTypeFace;


    public void setFilterContext(Context filterContext) {
        this.filterContext = filterContext;
    }

    public void setUnselectedResource(int unselectedResource) {
        this.unselectedResource = unselectedResource;
    }

    public void setUnselectedTextColor(int unselectedTextColor) {
        this.unselectedTextColor = unselectedTextColor;
    }

    public void setTextTypeFace(int textTypeFace) {
        this.textTypeFace = textTypeFace;
    }

    //default color will be BLUE
    public ThoughtRecyclerAdapter(Cursor cursor, SQLiteDatabase db,int unselectedResource,
                                  int unselectedTextColor, int textTypeFace){
        this.cursor = cursor;
        database = db;
        actionMode = null;
        existingDeleteDialog = false;
        selectedPositions = new ArrayList<>();
        this.unselectedResource = unselectedResource;
        this.unselectedTextColor = unselectedTextColor;
        selectedResource = R.color.darkGrey;
        selectedTextColor = R.color.white;
        this.textTypeFace = textTypeFace;
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        private CardView cardView;
        private ConstraintLayout constraintLayout;

        public ViewHolder(@NonNull CardView itemView) {
            super(itemView);
            cardView = itemView;
            constraintLayout = itemView.findViewById(R.id.cardview_constraint_layout);
        }
    }

    @NonNull
    @Override
    public ThoughtRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.thought_item, parent, false);
        return new ViewHolder(cardView);
    }


    private void selectItem(ViewHolder holder, int position){
        holder.constraintLayout.setBackgroundResource(selectedResource);
        TextView titleView = holder.cardView.findViewById(R.id.cardview_textview_title);
        TextView dateView = holder.cardView.findViewById(R.id.cardview_textview_date);
        TextView timeView = holder.cardView.findViewById(R.id.cardview_textview_time);
        titleView.setTextColor(ContextCompat.getColor(titleView.getContext(), selectedTextColor));
        dateView.setTextColor(ContextCompat.getColor(dateView.getContext(), selectedTextColor));
        timeView.setTextColor(ContextCompat.getColor(timeView.getContext(), selectedTextColor));
        selectedPositions.add(position);
        actionMode.invalidate();
        selectedHolders.add(holder);
    }

    private void deselectItem(ViewHolder holder, int position){
        holder.constraintLayout.setBackgroundResource(unselectedResource);
        TextView titleView = holder.cardView.findViewById(R.id.cardview_textview_title);
        TextView dateView = holder.cardView.findViewById(R.id.cardview_textview_date);
        TextView timeView = holder.cardView.findViewById(R.id.cardview_textview_time);
        titleView.setTextColor(ContextCompat.getColor(titleView.getContext(), unselectedTextColor));
        dateView.setTextColor(ContextCompat.getColor(dateView.getContext(), unselectedTextColor));
        timeView.setTextColor(ContextCompat.getColor(timeView.getContext(), unselectedTextColor));
        selectedPositions.remove(Integer.valueOf(position));
        actionMode.invalidate();
        selectedHolders.remove(holder);
    }


    @Override
    public void onBindViewHolder(@NonNull final ThoughtRecyclerAdapter.ViewHolder holder, final int position) {
        final CardView cardView = holder.cardView;
        if(!cursor.moveToPosition(position)){
            return;
        }


        final String title = cursor.getString(cursor.getColumnIndex(ThoughtTable.COLUMN_TITLE));
        final TextView titleView = cardView.findViewById(R.id.cardview_textview_title);
        titleView.setText(title);

        String date = cursor.getString(cursor.getColumnIndex(ThoughtTable.COLUMN_DATE));
        int year = Integer.parseInt(date.substring(0,4));
        int month = Integer.parseInt(date.substring(5,7)) - 1;
        int day = Integer.parseInt(date.substring(8,10));
        Calendar calendar = Calendar.getInstance();
        calendar.set(year,month,day);
        Calendar currentCal = Calendar.getInstance();
        int currentYear = currentCal.get(Calendar.YEAR);
        SimpleDateFormat simpleDateFormat;
        if(currentYear != year) {
            simpleDateFormat = new SimpleDateFormat("d MMM yyyy");
        } else {
            simpleDateFormat = new SimpleDateFormat("d MMM");
        }
        TextView dateView = cardView.findViewById(R.id.cardview_textview_date);
        Context dateFormatContext = holder.cardView.getContext();
        //date = convertYYYYMMDDFormat(date, dateFormatContext);
        date = simpleDateFormat.format(calendar.getTime());
        dateView.setText(date);

        String time = cursor.getString(cursor.getColumnIndex(ThoughtTable.COLUMN_TIME));
        //time is stored as YYYY-MM-DD
        TextView timeView = cardView.findViewById(R.id.cardview_textview_time);
        time = convertHHmmssToHHmm(time);
        timeView.setText(time);

        titleView.setTypeface(null, textTypeFace);
        timeView.setTypeface(null, textTypeFace);
        dateView.setTypeface(null, textTypeFace);

        if(selectedPositions.contains(position)){
            holder.constraintLayout.setBackgroundResource(selectedResource);
            titleView.setTextColor(ContextCompat.getColor(titleView.getContext(), selectedTextColor));
            dateView.setTextColor(ContextCompat.getColor(dateView.getContext(), selectedTextColor));
            timeView.setTextColor(ContextCompat.getColor(timeView.getContext(), selectedTextColor));
        } else {
            holder.constraintLayout.setBackgroundResource(unselectedResource);
            titleView.setTextColor(ContextCompat.getColor(titleView.getContext(), unselectedTextColor));
            dateView.setTextColor(ContextCompat.getColor(dateView.getContext(), unselectedTextColor));
            timeView.setTextColor(ContextCompat.getColor(timeView.getContext(), unselectedTextColor));

        }


        final int id = (int) (cursor.getLong(cursor.getColumnIndex(ThoughtTable.COLUMN_ID)));


        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionMode != null){
                    if(!selectedPositions.contains(position)){
                        /*
                        cardView.setBackgroundColor(ContextCompat.getColor(cardView.getContext(),R.color.darkGrey));
                        selectedPositions.add(position);
                        actionMode.invalidate();
                        selectedHolders.add(holder);

                         */
                        selectItem(holder, position);
                    } else {
                        /*
                        cardView.setBackgroundColor(ContextCompat.getColor(cardView.getContext(),R.color.white));
                        selectedPositions.remove(Integer.valueOf(position));
                        actionMode.invalidate();
                        selectedHolders.remove(holder);

                         */
                        deselectItem(holder, position);
                        if(selectedPositions.size() == 0){
                            closeActionMode();
                        }
                    }
                    return;
                }

                //will pass the activity the ID which can be used to update an entry
                Intent intent = new Intent(cardView.getContext(), DisplayThoughtActivity.class);
                //uses shared element transition using title
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
                        .makeSceneTransitionAnimation((Activity) (cardView.getContext()),titleView,
                                cardView.getContext().getString(R.string.title_transition));
                intent.putExtra(DisplayThoughtActivity.EXTRA_THOUGHT_ID, id);
                cardView.getContext().startActivity(intent, optionsCompat.toBundle());
            }
        });
        cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //return true if you dont want onclick to be called as well
                if(actionMode == null) {
                    actionMode = ((AppCompatActivity) (v.getContext())).startSupportActionMode(actionModeCallBack);
                }
                if(!selectedPositions.contains(position)){
                    /*
                    cardView.setBackgroundColor(ContextCompat.getColor(cardView.getContext(),R.color.darkGrey));
                    selectedPositions.add(position);
                    actionMode.invalidate();
                    selectedHolders.add(holder);

                     */
                    selectItem(holder,position);
                } else {
                    /*
                    cardView.setBackgroundColor(ContextCompat.getColor(cardView.getContext(),R.color.white));
                    selectedPositions.remove(Integer.valueOf(position));
                    actionMode.invalidate();
                    selectedHolders.remove(holder);

                     */
                    deselectItem(holder, position);
                    if(selectedPositions.size() == 0){
                        closeActionMode();
                    }
                }
                return true;
            }
        });

    }


    public void closeActionMode(){
        if(actionMode != null) {
            for (ViewHolder holder : selectedHolders) {
                holder.constraintLayout.setBackgroundResource(unselectedResource);
                TextView titleView = holder.cardView.findViewById(R.id.cardview_textview_title);
                TextView dateView = holder.cardView.findViewById(R.id.cardview_textview_date);
                TextView timeView = holder.cardView.findViewById(R.id.cardview_textview_time);
                titleView.setTextColor(ContextCompat.getColor(titleView.getContext(), unselectedTextColor));
                dateView.setTextColor(ContextCompat.getColor(dateView.getContext(), unselectedTextColor));
                timeView.setTextColor(ContextCompat.getColor(timeView.getContext(), unselectedTextColor));

            }
            actionMode.finish();
            selectedHolders.clear();
            selectedPositions.clear();
        }
    }


    private static String convertHHmmssToHHmm(String time){
        return time.substring(0,5);
    }

    private static String convertYYYYMMDDFormat(String dateString, Context context){
        /*dateString will be passed as YYYY-MM-DD
         * and will be converted to Mon DD YYYY
         * i.e 18 Aug 2019
         */
        String dayNumberString = dateString.substring(8,10);
        if(dayNumberString.startsWith("0")){
            //if day is 01 then remove the 0
            dayNumberString = dayNumberString.substring(1,2);
        }
        String monthNumberString = dateString.substring(5,7);
        int monthNumber;
        if(monthNumberString.startsWith("0")){
            //08
            monthNumber = Integer.parseInt(monthNumberString.substring(1,2));
        } else {
            monthNumber = Integer.parseInt(monthNumberString);
        }
        String yearNumberString = dateString.substring(0,4);
        String yearString = "";
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if(!yearNumberString.equals(String.valueOf(currentYear))){
            yearString += yearNumberString;
        }
        String[] abbreviatedMonths = context.getResources().getStringArray(R.array.months_abbreviated);
        String monthAbbreviated = abbreviatedMonths[monthNumber-1];
        String formattedDate = monthAbbreviated + " " + dayNumberString + " " +yearString;
        return formattedDate;
    }

    public long getItemId(int position){
        if(cursor.moveToPosition(position)){
            return cursor.getLong(cursor.getColumnIndex(ThoughtTable.COLUMN_ID));
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public void changeCursor(Cursor newCursor, SQLiteDatabase db){
        if(cursor != null){
            cursor.close();
        }
        if(database != null){
            database.close();
        }
        database = db;
        cursor = newCursor;
        //close action mode to maintain consistencies with selected view holders
        if(actionMode != null){
            actionMode.finish();
        }

        if(cursor != null){
            /*
            This is only if i want to maintain selections after going to share a thought
            for(ViewHolder holder: selectedHolders){
                holder.cardView.setBackgroundColor(Color.WHITE);
                UPDATE, ABOVE LINE SHOULD BE -->> setdeSelectedBackground(holder.cardView);


            }
            selectedHolders.clear();

             */
            notifyDataSetChanged();
        }
    }

    private String[] getSelectedIDsStringArray(){
        String[] selectedIDStrings = new String[selectedPositions.size()];
        for(int i = 0; i < selectedPositions.size(); i++){
            selectedIDStrings[i] = Integer.toString((int) getItemId(selectedPositions.get(i)));
        }
        return  selectedIDStrings;
    }

    private void createDeleteConfirmationDialog(){
        final Context context = selectedHolders.get(0).cardView.getContext();
        String selectedCount = Integer.toString(selectedPositions.size());
        String dialogDeleteMessage = "Are you sure you want to delete the " + selectedCount +
                " selected items?";
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setMessage(dialogDeleteMessage);
        alertBuilder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(selectedPositions.size() > 0){
                    new DeleteSelectedItemsTask(context).execute();
                    existingDeleteDialog = false;
                }
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                existingDeleteDialog = false;
            }
        });
        alertBuilder.show();
        existingDeleteDialog = true;
    }

    public void createDeleteConfirmationDialogFromFragment(Context context){
        String selectedCount = Integer.toString(selectedPositions.size());
        String dialogDeleteMessage = "Are you sure you want to delete the " + selectedCount +
                " selected items?";
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setMessage(dialogDeleteMessage);
        final Context givenContext = context;
        alertBuilder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(selectedPositions.size() > 0){
                    new DeleteSelectedItemsTask(givenContext).execute();
                    existingDeleteDialog = false;
                }
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                existingDeleteDialog = false;
            }
        });
        alertBuilder.show();
        existingDeleteDialog = true;
    }


    private ActionMode.Callback actionModeCallBack = new ActionMode.Callback() {


        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.contextual_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch(item.getItemId()){
                case R.id.contextual_delete:
                    createDeleteConfirmationDialog();
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;

            for (ViewHolder holder : selectedHolders) {
                holder.constraintLayout.setBackgroundResource(unselectedResource);
                TextView titleView = holder.cardView.findViewById(R.id.cardview_textview_title);
                TextView dateView = holder.cardView.findViewById(R.id.cardview_textview_date);
                TextView timeView = holder.cardView.findViewById(R.id.cardview_textview_time);
                titleView.setTextColor(ContextCompat.getColor(titleView.getContext(), unselectedTextColor));
                dateView.setTextColor(ContextCompat.getColor(dateView.getContext(), unselectedTextColor));
                timeView.setTextColor(ContextCompat.getColor(timeView.getContext(), unselectedTextColor));
            }


            selectedHolders.clear();
            selectedPositions.clear();
        }

    };

    public boolean hasActiveActionMode(){
        return actionMode != null;
    }

    public void createActionMode(View v){
        if(actionMode == null) {
            actionMode = ((AppCompatActivity) (v.getContext())).startSupportActionMode(actionModeCallBack);
        }
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        private SQLiteDatabase filterDB;
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if(constraint == null || constraint.length() == 0){
                //return full cursor
                ThoughtDatabaseHelper helper = new ThoughtDatabaseHelper(filterContext);
                try{
                    filterDB = helper.getReadableDatabase();
                    Cursor cursor = filterDB.query(ThoughtTable.TABLE_NAME,
                            ThoughtTable.COLUMNS,
                            null, null, null, null,
                            "DATE(" + ThoughtTable.COLUMN_DATE + ") DESC," +
                                    "TIME(" + ThoughtTable.COLUMN_TIME + ") DESC");
                    FilterResults results = new FilterResults();
                    results.values = cursor;
                    return results;
                } catch(SQLiteException e){
                }
            } else {
                String filteredPattern = constraint.toString().toLowerCase().trim();
                ThoughtDatabaseHelper helper = new ThoughtDatabaseHelper(filterContext);
                try{
                    filterDB = helper.getReadableDatabase();
                    Cursor cursor = filterDB.query(ThoughtTable.TABLE_NAME,
                            ThoughtTable.COLUMNS,
                            ThoughtTable.COLUMN_TITLE + " LIKE ?",
                            new String[] {"%" + filteredPattern + "%"},
                            null, null,
                            "DATE(" + ThoughtTable.COLUMN_DATE + ") DESC," +
                                    "TIME(" + ThoughtTable.COLUMN_TIME + ") DESC");
                    FilterResults results = new FilterResults();
                    results.values = cursor;
                    return results;
                } catch(SQLiteException e){
                }
            }
            return null;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if(results != null) {
                Cursor cursor = (Cursor) results.values;
                changeCursor(cursor, filterDB);
            }
        }
    };

    private class DeleteSelectedItemsTask extends AsyncTask<Void,Void,Boolean>{

        private String selectedIDStringCSV;
        private Context context;
        private Cursor newCursor;
        private SQLiteDatabase newDatabase;
        private String[] selectedIDsStringArray;

        public DeleteSelectedItemsTask(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            selectedIDsStringArray = getSelectedIDsStringArray();
            selectedIDStringCSV = TextUtils.join(",",selectedIDsStringArray);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            ThoughtDatabaseHelper helper = new ThoughtDatabaseHelper(context);
            try{
                SQLiteDatabase db = helper.getWritableDatabase();
                db.delete(ThoughtTable.TABLE_NAME,
                        ThoughtTable.COLUMN_ID + " IN (" + selectedIDStringCSV + ")",
                        null);
                db.close();
                ThoughtDatabaseHelper newHelper = new ThoughtDatabaseHelper(context);
                newDatabase = newHelper.getReadableDatabase();
                newCursor = newDatabase.query(ThoughtTable.TABLE_NAME,
                        ThoughtTable.COLUMNS,
                        null, null, null, null,
                        "DATE(" + ThoughtTable.COLUMN_DATE + ") DESC," +
                                "TIME(" + ThoughtTable.COLUMN_TIME + ") DESC");
                return  true;
            }catch(SQLiteException e){
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(success) {
                closeActionMode();
                ThoughtRecyclerAdapter.this.changeCursor(newCursor, newDatabase);
            } else {
                Toast toast = Toast.makeText(context, "Deletion Unsuccessful", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

}
