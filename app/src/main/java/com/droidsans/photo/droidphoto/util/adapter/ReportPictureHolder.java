package com.droidsans.photo.droidphoto.util.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.droidsans.photo.droidphoto.R;

/**
 * Created by Froztic on 7/23/2015.
 */
public class ReportPictureHolder extends MainViewHolder {
    TextView reason;
    ImageView image;

    public ReportPictureHolder(View itemView) {
        super(itemView);
        reason = (TextView) itemView.findViewById(R.id.reason);
        image = (ImageView) itemView.findViewById(R.id.image);
    }
}
