package com.droidsans.photo.droidphoto.util.adapter;

import android.view.View;
import android.widget.ProgressBar;

import com.droidsans.photo.droidphoto.R;

/**
 * Created by Froztic on 7/23/2015.
 */
public class LoadMoreHolder extends MainViewHolder {
    ProgressBar progress;

    public LoadMoreHolder(View itemView) {
        super(itemView);
        progress = (ProgressBar) itemView.findViewById(R.id.progress);
    }
}
