package com.rilchil.mythoughts;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private Cursor cursor;
    private SQLiteDatabase database;
    private boolean firstStart;
    private ThoughtRecyclerAdapter recyclerAdapter;
    private boolean shouldCloseSearchView;
    private SharedPreferences sharedPreferences;
    static final String SHARED_TEXT_TYPEFACE = "SHARED_TEXT_TYPEFACE";
    static final String TEXT_BOLD = "BOLD";
    static final String TEXT_NORMAL = "NORMAL";
    static final String SHARED_THEME_NAME = "SHARED_THEME_NAME";
    static final String DEFAULT_THEME_NAME = "NORMAL";
    static final String BLACK_STAR_THEME_NAME = "BLACK_STARS";
    static final String BLACK_BLUE_THEME_NAME = "BLACK_BLUE";


    private HomeListener homeListener;


    public HomeFragment() {
        // Required empty public constructor

    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        homeListener = (HomeListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        homeListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_home, container, false);

        // Fragment screen orientation normal both portait and landscape
        setHasOptionsMenu(true);
        getCursorForRecyclerView();
        setUpFAB(view);
        shouldCloseSearchView = false;
        firstStart = true;
        if(getActivity()!=null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        }
        int unselectedResource = R.color.white;
        int unselectedTextColor = R.color.defaultTextColor;
        boolean darkModeEnabled = sharedPreferences.getBoolean(SettingsFragment.SHARED_DARK_MODE_ENABLED, false);
        String currentTheme = sharedPreferences.getString(SHARED_THEME_NAME,DEFAULT_THEME_NAME);
        if(currentTheme.equals(DEFAULT_THEME_NAME) && !darkModeEnabled) {
            FloatingActionButton fab = view.findViewById(R.id.fab_home);
            fab.setBackgroundTintList(ColorStateList
                    .valueOf(ContextCompat.getColor(getActivity(), R.color.colorPrimary)));
            fab.setRippleColor(ColorStateList
                    .valueOf(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)));
        } else if(currentTheme.equals(DEFAULT_THEME_NAME)){
            unselectedResource = R.color.darkThemeThoughtBackground;
            unselectedTextColor = R.color.white;
            FloatingActionButton fab = view.findViewById(R.id.fab_home);
            fab.setBackgroundTintList(ColorStateList
                    .valueOf(ContextCompat.getColor(getActivity(), R.color.colorPrimary)));
            fab.setRippleColor(ColorStateList
                    .valueOf(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)));
        } else if(currentTheme.equals(BLACK_STAR_THEME_NAME)){
            unselectedResource = R.drawable.stars_fin;
            unselectedTextColor = R.color.white;
            FloatingActionButton fab = view.findViewById(R.id.fab_home);
            fab.setBackgroundTintList(ColorStateList
                    .valueOf(ContextCompat.getColor(getActivity(),R.color.fabStarColor)));
            fab.setRippleColor(ColorStateList
                    .valueOf(ContextCompat.getColor(getActivity(),R.color.fabStarSelectedColor)));
        } else if(currentTheme.equals(BLACK_BLUE_THEME_NAME)){
            unselectedResource = R.drawable.black_blue_bg;
            unselectedTextColor = R.color.white;
            FloatingActionButton fab = view.findViewById(R.id.fab_home);
            fab.setBackgroundTintList(ColorStateList
                    .valueOf(ContextCompat.getColor(getActivity(),R.color.colorPrimary)));
            fab.setRippleColor(ColorStateList
                    .valueOf(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark)));

        }
        int textTypeFace;
        String textTypeString = sharedPreferences.getString(SHARED_TEXT_TYPEFACE,TEXT_NORMAL);
        if(textTypeString.equals(TEXT_NORMAL)){
            textTypeFace = Typeface.NORMAL;
        } else {
            textTypeFace = Typeface.BOLD;
        }
        setRecyclerViewAdapter(view, unselectedResource, unselectedTextColor, textTypeFace);


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cursor.close();
        database.close();
    }

    private void getCursorForRecyclerView(){
        ThoughtDatabaseHelper helper = new ThoughtDatabaseHelper(getActivity());
        try{
            database = helper.getReadableDatabase();
            cursor = database.query(ThoughtTable.TABLE_NAME,
                    ThoughtTable.COLUMNS,
                    null, null, null, null,
                    "DATE(" + ThoughtTable.COLUMN_DATE + ") DESC," +
                            "TIME(" + ThoughtTable.COLUMN_TIME + ") DESC");

        } catch(SQLiteException e){
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(firstStart){
            firstStart = false;
        } else {
            refreshRecyclerView();
            closeSearchView();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        RecyclerView recyclerView = getView().findViewById(R.id.home_recycler_view);
        ThoughtRecyclerAdapter adapter = (ThoughtRecyclerAdapter) recyclerView.getAdapter();
        if(adapter != null){
            adapter.closeActionMode();
        }


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(getView() == null){return;}
    }

    private void setRecyclerViewAdapter(View view, int unselectedResource, int unselectedTextColor,
                                        int textTypeFace){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        RecyclerView recyclerView = view.findViewById(R.id.home_recycler_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        if(cursor != null){
            recyclerAdapter= new ThoughtRecyclerAdapter(cursor,database, unselectedResource,
                    unselectedTextColor, textTypeFace);
            recyclerView.setAdapter(recyclerAdapter);
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        if(shouldCloseSearchView){
            MenuItem searchItem = menu.findItem(R.id.home_menu_search);
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setIconified(false);
            searchItem.collapseActionView();
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.home_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.home_menu_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                recyclerAdapter.setFilterContext(getActivity());
                recyclerAdapter.getFilter().filter(newText);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.home_menu_new_first:
                displayNewestFirst();
                return true;
            case R.id.home_menu_old_first:
                displayOldestFirst();
                return true;
            case R.id.home_menu_settings:
                homeListener.onSettingsItemClicked();
                return true;
            case R.id.home_menu_theme:
                homeListener.onThemeItemClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUpFAB(View view){
        final FloatingActionButton floatingActionButton = view.findViewById(R.id.fab_home);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), AddThoughtActivity.class);
                startActivity(intent);
                if(recyclerAdapter != null){
                    recyclerAdapter.closeActionMode();
                }
            }
        });
    }

    public void refreshRecyclerView(){
        ThoughtDatabaseHelper helper = new ThoughtDatabaseHelper(getActivity());
        try{
            cursor.close();
            database.close();
            database = helper.getReadableDatabase();
            Cursor newCursor = database.query(ThoughtTable.TABLE_NAME,
                    ThoughtTable.COLUMNS,
                    null, null, null, null,
                    "DATE(" + ThoughtTable.COLUMN_DATE + ") DESC," +
                            "TIME(" + ThoughtTable.COLUMN_TIME + ") DESC");
            recyclerAdapter.changeCursor(newCursor, database);
            cursor = newCursor;
        } catch (SQLiteException e){

        }
    }

    private void displayOldestFirst(){
        ThoughtDatabaseHelper helper = new ThoughtDatabaseHelper(getActivity());
        try{
            cursor.close();
            database.close();
            database = helper.getReadableDatabase();
            Cursor newCursor = database.query(ThoughtTable.TABLE_NAME,
                    ThoughtTable.COLUMNS,
                    null, null, null, null,
                    "DATE(" + ThoughtTable.COLUMN_DATE + ") ASC," +
                            "TIME(" + ThoughtTable.COLUMN_TIME + ") ASC");
            recyclerAdapter.changeCursor(newCursor, database);
            cursor = newCursor;
        } catch (SQLiteException e){

        }
    }

    private void displayNewestFirst(){
        ThoughtDatabaseHelper helper = new ThoughtDatabaseHelper(getActivity());
        try{
            cursor.close();
            database.close();
            database = helper.getReadableDatabase();
            Cursor newCursor = database.query(ThoughtTable.TABLE_NAME,
                    ThoughtTable.COLUMNS,
                    null, null, null, null,
                    "DATE(" + ThoughtTable.COLUMN_DATE + ") DESC," +
                            "TIME(" + ThoughtTable.COLUMN_TIME + ") DESC");
            recyclerAdapter.changeCursor(newCursor, database);
            cursor = newCursor;
        } catch (SQLiteException e){

        }
    }

    public void scrollToTopOfRecyclerView(){
        if(recyclerAdapter != null){
            if(!recyclerAdapter.hasActiveActionMode() && getView() != null) {
                RecyclerView recyclerView = getView().findViewById(R.id.home_recycler_view);
                recyclerView.smoothScrollToPosition(0);
            }

        }
    }

    public void closeActionMode(){
        if(recyclerAdapter != null){
            recyclerAdapter.closeActionMode();
        }
    }

    public void closeSearchView(){
        shouldCloseSearchView = true;
        getActivity().invalidateOptionsMenu();
        shouldCloseSearchView = false;
    }


    public void enableDarkMode(){
        String themeName = sharedPreferences.getString(SHARED_THEME_NAME, DEFAULT_THEME_NAME);
        if(themeName.equals(DEFAULT_THEME_NAME)){
            recyclerAdapter.setUnselectedResource(R.color.darkThemeThoughtBackground);
            recyclerAdapter.setUnselectedTextColor(R.color.white);
            recyclerAdapter.notifyDataSetChanged();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.apply();
        }
    }

    public void disableDarkMode(){
        String themeName = sharedPreferences.getString(SHARED_THEME_NAME, DEFAULT_THEME_NAME);
        if(themeName.equals(DEFAULT_THEME_NAME)){
            recyclerAdapter.setUnselectedResource(R.color.white);
            recyclerAdapter.setUnselectedTextColor(R.color.defaultTextColor);
            recyclerAdapter.notifyDataSetChanged();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.apply();
        }
    }

    //atm changing theme only changes unselected resource and unselected text color
    public void setBlackBlueTheme(){
        recyclerAdapter.setUnselectedResource(R.drawable.black_blue_bg);
        recyclerAdapter.setUnselectedTextColor(R.color.white);
        recyclerAdapter.notifyDataSetChanged();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_THEME_NAME, BLACK_BLUE_THEME_NAME);
        editor.apply();
    }

    public void setNormalTheme(){
        boolean darkModeEnabled = sharedPreferences.getBoolean(SettingsFragment.SHARED_DARK_MODE_ENABLED, false);
        if(darkModeEnabled){
            recyclerAdapter.setUnselectedResource(R.color.darkThemeThoughtBackground);
            recyclerAdapter.setUnselectedTextColor(R.color.white);
            recyclerAdapter.notifyDataSetChanged();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(SHARED_THEME_NAME,DEFAULT_THEME_NAME);
            editor.apply();
        } else {
            recyclerAdapter.setUnselectedResource(R.color.white);
            recyclerAdapter.setUnselectedTextColor(R.color.defaultTextColor);
            recyclerAdapter.notifyDataSetChanged();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(SHARED_THEME_NAME,DEFAULT_THEME_NAME);
            editor.apply();
        }
    }

    public void setStarTheme(){
        recyclerAdapter.setUnselectedResource(R.drawable.stars_fin);
        recyclerAdapter.setUnselectedTextColor(R.color.white);
        recyclerAdapter.notifyDataSetChanged();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_THEME_NAME,BLACK_STAR_THEME_NAME);
        editor.apply();
    }

    public void setTextBold(){
        recyclerAdapter.setTextTypeFace(Typeface.BOLD);
        recyclerAdapter.notifyDataSetChanged();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_TEXT_TYPEFACE,TEXT_BOLD);
        editor.apply();
    }

    public void setTextNormal(){
        recyclerAdapter.setTextTypeFace(Typeface.NORMAL);
        recyclerAdapter.notifyDataSetChanged();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_TEXT_TYPEFACE,TEXT_NORMAL);
        editor.apply();
    }




}
