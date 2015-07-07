package com.droidsans.photo.droidphoto;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.droidsans.photo.droidphoto.util.FontTextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ImageViewerFragment extends Fragment {

    private RelativeLayout mainView;

    private ImageView picture;
    private Bitmap imageBitmap;
    private FontTextView deviceName, exposureTime, aperture, iso, location, user, caption;
    private LinearLayout locationLayout, captionLayout;
    private String photoURL;
    private final String baseURL = "data/photo/original/";

    private int percentage = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_image_viewer, container, false);
        mainView = (RelativeLayout) rootView.findViewById(R.id.fullpic_view);
        findAllById();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setData();
        super.onActivityCreated(savedInstanceState);

    }

    public boolean setData() {
        photoURL = getArguments().getString("photoURL");
        caption.setText(getArguments().getString("caption"));
        if(caption.getText().equals("")) captionLayout.setVisibility(LinearLayout.GONE);
        deviceName.setText(getArguments().getString("vendor") + " " + getArguments().getString("model"));
        if(!getArguments().getString("exposureTime").equals("")) {
            if(getArguments().getString("exposureTime").contains("1/")) {
                exposureTime.setText(getArguments().getString("exposureTime"));
            } else if(getArguments().getString("exposureTime").contains("/")) {
                String expTime = getArguments().getString("exposureTime");
                exposureTime.setText("1/" + (int) (Double.parseDouble(expTime.substring(expTime.indexOf("/") + 1, expTime.length())) / Double.parseDouble(expTime.substring(0, expTime.indexOf("/")))));
            } else {
                exposureTime.setText("1/" + (int) (1.0 / Double.parseDouble(getArguments().getString("exposureTime"))));
            }
            if(getArguments().getString("aperture").contains("f/")) {
                aperture.setText(getArguments().getString("aperture"));
            } else {
                aperture.setText("f/" + getArguments().getString("aperture"));
            }
            iso.setText(getArguments().getString("iso"));
        }
        location.setText(getArguments().getString("gpsLocation"));
        if(!getArguments().getString("gpsLocalized").equals(""))location.append(" (" + getArguments().getString("gpsLocalized") + ")");
        if(location.getText().equals("")) locationLayout.setVisibility(LinearLayout.GONE);
        user.setText(getArguments().getString("username"));
        return true;
    }

    public void findAllById() {
        caption = (FontTextView) mainView.findViewById(R.id.caption);
        captionLayout = (LinearLayout) mainView.findViewById(R.id.caption_layout);
        picture = (ImageView) mainView.findViewById(R.id.picture);
        deviceName = (FontTextView) mainView.findViewById(R.id.device_name);
        exposureTime = (FontTextView) mainView.findViewById(R.id.shutter_speed);
        aperture = (FontTextView) mainView.findViewById(R.id.aperture);
        iso = (FontTextView) mainView.findViewById(R.id.iso);
        location = (FontTextView) mainView.findViewById(R.id.location);
        locationLayout = (LinearLayout) mainView.findViewById(R.id.location_layout);
        user = (FontTextView) mainView.findViewById(R.id.user);
    }
}
