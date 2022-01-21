package com.rilchil.mythoughts;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ThemePreviewRecyclerAdapter extends RecyclerView.Adapter<ThemePreviewRecyclerAdapter.ViewHolder> {

    private static final String TAG = "ThemePreviewRecyclerAdapter";

    private ArrayList<Integer> previewImageDrawables;
    private ThemeListener themeListener;
    private SparseArray<ViewHolder> posHolderMap;

    public int getImageDrawableRscAtPos(int pos){
        return  previewImageDrawables.get(pos);
    }

    public ThemePreviewRecyclerAdapter(ThemeListener listener, ArrayList<Integer> previewImageDrawables) {
        this.previewImageDrawables = previewImageDrawables;
        themeListener = listener;
        posHolderMap = new SparseArray<>();
    }

    public void setPreviewImageDrawables(ArrayList<Integer> previewImageDrawables) {
        this.previewImageDrawables = previewImageDrawables;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }



    @NonNull
    @Override
    public ThemePreviewRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.theme_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView imageView = holder.itemView.findViewById(R.id.theme_imageview);
        final int drawableId = previewImageDrawables.get(position);
        imageView.setImageResource(drawableId);
        posHolderMap.put(position,holder);
        /*
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawableId == R.drawable.preview_black_blue){
                    themeListener.setBlackBlueTheme();
                } else if(drawableId == R.drawable.preview_normal){
                    themeListener.setNormalTheme();
                } else if(drawableId == R.drawable.preview_stars_fin){
                    themeListener.setStarTheme();
                }
            }
        });

         */

    }

    @Override
    public int getItemCount() {
        return previewImageDrawables.size();
    }


}
