package com.droidsans.photo.droidphoto.util.adapter;

import android.view.View;
import android.widget.TextView;

import com.droidsans.photo.droidphoto.R;

/**
 * Created by Froztic on 7/23/2015.
 */
public class ReportUserHolder extends MainViewHolder {
    TextView username, reason;

    public ReportUserHolder(View itemView) {
        super(itemView);
        username = (TextView) itemView.findViewById(R.id.username);
        reason = (TextView) itemView.findViewById(R.id.reason);
    }
}
