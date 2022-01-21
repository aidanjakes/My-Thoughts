package com.rilchil.mythoughts;


import android.content.Context;
import android.content.SharedPreferences;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ThemeFragment extends Fragment {

    private ThemeListener themeListener;
    private int currentPagerPos;
    private ThemePreviewRecyclerAdapter recyclerAdapter;

    public ThemeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        themeListener = (ThemeListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_theme, container, false);

        currentPagerPos = 0;
        String themeName = getString(R.string.theme);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(themeName);
        final ViewPager2 viewPager2 = view.findViewById(R.id.theme_viewpager);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean darkModeEnabled = sharedPreferences.getBoolean(SettingsFragment.SHARED_DARK_MODE_ENABLED,
                false);
        ArrayList<Integer> previewImageDrawables = new ArrayList<>();
        if(darkModeEnabled){
            previewImageDrawables.add(R.drawable.preview_stars_fin_dark);
            previewImageDrawables.add(R.drawable.preview_normal_dark);
            previewImageDrawables.add(R.drawable.preview_blue_black_dark);
        } else {
            previewImageDrawables.add(R.drawable.preview_stars_fin_light);
            previewImageDrawables.add(R.drawable.preview_normal_light);
            previewImageDrawables.add(R.drawable.preview_blue_black_light);
        }
        recyclerAdapter = new ThemePreviewRecyclerAdapter(themeListener, previewImageDrawables);
        viewPager2.setAdapter(recyclerAdapter);
        viewPager2.setOffscreenPageLimit(3);
        //preview next pages idrk how this works just copied some medium article
        viewPager2.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float pageMarginPx = getResources().getDimension(R.dimen.pageMargin);
                float offsetPx = getResources().getDimension(R.dimen.offset);
                float offset = position * -(2 * offsetPx + pageMarginPx);
                if(ViewCompat.getLayoutDirection(viewPager2) == ViewCompat.LAYOUT_DIRECTION_RTL){
                    page.setTranslationX(-offset);
                } else {
                    page.setTranslationX(offset);
                }
                page.setScaleY(1- (0.085f * Math.abs(position)));
            }
        });
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPagerPos = position;
            }
        });
        Button selectButton = view.findViewById(R.id.select_theme_button);
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int drawableId = recyclerAdapter.getImageDrawableRscAtPos(currentPagerPos);
                if(drawableId == R.drawable.preview_blue_black_dark || drawableId == R.drawable.preview_blue_black_light){
                    themeListener.setBlackBlueTheme();
                } else if(drawableId == R.drawable.preview_normal_light || drawableId == R.drawable.preview_normal_dark){
                    themeListener.setNormalTheme();
                } else if(drawableId == R.drawable.preview_stars_fin_light || drawableId == R.drawable.preview_stars_fin_dark){
                    themeListener.setStarTheme();
                }
            }
        });



        /*
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL,false);
        RecyclerView recyclerView = view.findViewById(R.id.theme_viewpager);
        recyclerView.setLayoutManager(linearLayoutManager);
        ThemePreviewRecyclerAdapter recyclerAdapter = new ThemePreviewRecyclerAdapter(themeListener);
        recyclerView.setAdapter(recyclerAdapter);
        */

        return  view;

    }

    public void enableDarkMode(){
        ArrayList<Integer> previewImageDrawables = new ArrayList<>();
        previewImageDrawables.add(R.drawable.preview_stars_fin_dark);
        previewImageDrawables.add(R.drawable.preview_normal_dark);
        previewImageDrawables.add(R.drawable.preview_normal_dark);
        recyclerAdapter.setPreviewImageDrawables(previewImageDrawables);
    }

    public void disableDarkMode(){
        ArrayList<Integer> previewImageDrawables = new ArrayList<>();
        previewImageDrawables.add(R.drawable.preview_stars_fin_light);
        previewImageDrawables.add(R.drawable.preview_normal_light);
        previewImageDrawables.add(R.drawable.preview_normal_light);
        recyclerAdapter.setPreviewImageDrawables(previewImageDrawables);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /*
    public class HorizontalMarginItemDecoration extends RecyclerView.ItemDecoration{

        public int getHorizontalMarginInPx() {
            return (int) getResources().getDimension(R.dimen.viewpager_current_item_horizontal_margin);
        }


        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.left = getHorizontalMarginInPx();
            outRect.right = getHorizontalMarginInPx();
            super.getItemOffsets(outRect, view, parent, state);
        }
    }

     */







}
