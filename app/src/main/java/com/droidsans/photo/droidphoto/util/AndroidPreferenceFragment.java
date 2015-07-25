package com.droidsans.photo.droidphoto.util;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.preference.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by droidsans on 7/25/2015.
 */
public class AndroidPreferenceFragment extends android.preference.PreferenceFragment {
    private static final float HC_HORIZONTAL_PADDING = 16;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        ListView listView = new ListView(getActivity());
        listView.setId(android.R.id.list);
        ListView.LayoutParams params = new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        listView.setLayoutParams(params);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//        if(false) {
            final int horizontalPadding = (int) (HC_HORIZONTAL_PADDING * getResources().getDisplayMetrics().density);
            listView.setPadding(horizontalPadding, 0, horizontalPadding, 0);
        } else {
            listView.setPadding(0, 0, 0, 0);
        }
        return listView;
    }
}
