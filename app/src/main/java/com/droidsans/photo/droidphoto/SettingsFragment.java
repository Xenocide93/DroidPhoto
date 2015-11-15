package com.droidsans.photo.droidphoto;


import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.droidsans.photo.droidphoto.util.AndroidPreferenceFragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends AndroidPreferenceFragment {


//    public SettingsFragment() {
//        // Required empty public constructor
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener((SettingsActivity)getActivity());
    }

    @Override
    public void onStart() {
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener((SettingsActivity)getActivity());
        super.onStart();
    }

    //    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
//        return rootView;
//    }


}
