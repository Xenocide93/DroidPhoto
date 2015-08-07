package com.droidsans.photo.droidphoto.util.adapter;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.droidsans.photo.droidphoto.R;

/**
 * Created by Froztic on 7/23/2015.
 */
public class ReportDeviceHolder extends MainViewHolder {
    TextView buildDevice, buildModel, retailVendor, retailModel;

    public ReportDeviceHolder(View itemView) {
        super(itemView);
        buildDevice = (TextView) itemView.findViewById(R.id.build_device);
        buildModel = (TextView) itemView.findViewById(R.id.build_model);
        retailVendor = (TextView) itemView.findViewById(R.id.retail_vendor);
        retailModel = (TextView) itemView.findViewById(R.id.retail_model);
    }
}
