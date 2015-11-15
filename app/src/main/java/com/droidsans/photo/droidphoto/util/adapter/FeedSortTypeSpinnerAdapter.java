package com.droidsans.photo.droidphoto.util.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.droidsans.photo.droidphoto.FeedFragment;
import com.droidsans.photo.droidphoto.R;

import java.util.List;

/**
 * Created by Xenocide93 on 11/7/15.
 */
public class FeedSortTypeSpinnerAdapter extends ArrayAdapter<String> {
    private LayoutInflater inflater;
    private ImageView actionbarIconView;
    private int sortType;

    public FeedSortTypeSpinnerAdapter(Context context, int resource, List<String> objects, int sortType) {
        super(context, resource, objects);
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.sortType = sortType;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(actionbarIconView == null){
            actionbarIconView = (ImageView) inflater.inflate(R.layout.actionbar_button, null, false);
        }

        if(sortType == FeedFragment.MOST_RECENT_TAG){
            actionbarIconView.setImageResource(R.drawable.ic_sort_by_time_white_24px);
        } else if(sortType == FeedFragment.MOST_POPULAR_TAG ){
            actionbarIconView.setImageResource(R.drawable.ic_sort_by_like_white_24px);
        }

        return actionbarIconView;
    }

    @Override
    public View getDropDownView(final int position, View convertView, ViewGroup parent) {

        TextView dropdownSpinner = (TextView) inflater.inflate(
                R.layout.actionbar_spinner_dropdown_white_text,
                null,
                false);

        dropdownSpinner.setText(getItem(position));

        return dropdownSpinner;
    }

    public void setSortType (int sortType){
        this.sortType = sortType;
        notifyDataSetChanged();
    }

    public int getSortType(){
        return sortType;
    }


}
