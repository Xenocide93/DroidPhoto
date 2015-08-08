package com.droidsans.photo.droidphoto.util.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.droidsans.photo.droidphoto.R;

/**
 * Created by Froztic on 7/23/2015.
 */
public class ReportDeviceHolder extends MainViewHolder implements View.OnClickListener {
    TextView buildDevice, buildModel, retailVendor, retailModel;
    View itemView;
    public DeviceReportClickListener mListener;

    public ReportDeviceHolder(View itemView, DeviceReportClickListener listener) {
        super(itemView);
        mListener = listener;
        buildDevice = (TextView) itemView.findViewById(R.id.build_device);
        buildModel = (TextView) itemView.findViewById(R.id.build_model);
        retailVendor = (TextView) itemView.findViewById(R.id.retail_vendor);
        retailModel = (TextView) itemView.findViewById(R.id.retail_model);
        itemView.setOnClickListener(this);
        this.itemView = itemView;
    }

    @Override
    public void onClick(View v) {
        this.mListener.onItemClick(v);
    }

    public interface DeviceReportClickListener {
        void onItemClick(View caller);
    }
}
