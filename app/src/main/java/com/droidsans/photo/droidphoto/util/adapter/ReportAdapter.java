package com.droidsans.photo.droidphoto.util.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * Created by Ong on 06/08/2015.
 */
public class ReportAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LayoutInflater layoutInflater;
    public static final int TYPE_DEVICE_REPORT = 1;
    public static final int TYPE_PHOTO_REPORT = 2;
    public static final int TYPE_USER_REPORT = 3;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(layoutInflater == null){layoutInflater = LayoutInflater.from(parent.getContext());}
        switch (viewType){
            case TYPE_DEVICE_REPORT:
                break;
            case TYPE_PHOTO_REPORT:
                break;
            case TYPE_USER_REPORT:
                break;
            default:
                break;
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
