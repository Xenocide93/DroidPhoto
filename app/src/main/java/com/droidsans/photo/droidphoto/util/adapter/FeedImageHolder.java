package com.droidsans.photo.droidphoto.util.adapter;

import android.view.View;
import android.widget.RelativeLayout;

import com.droidsans.photo.droidphoto.R;
import com.droidsans.photo.droidphoto.util.view.FontTextView;
import com.droidsans.photo.droidphoto.util.view.SquareImageView;

/**
 * Created by Froztic on 7/23/2015.
 */
public class FeedImageHolder extends MainViewHolder {
    FontTextView deviceName, user;
    SquareImageView picture;
    RelativeLayout uploadLayout;

    public FeedImageHolder(View itemView) {
        super(itemView);
        picture = (SquareImageView) itemView.findViewById(R.id.picture);
        deviceName = (FontTextView) itemView.findViewById(R.id.device_name);
        user = (FontTextView) itemView.findViewById(R.id.user);
        uploadLayout = (RelativeLayout) itemView.findViewById(R.id.upload_layout);
    }
}
