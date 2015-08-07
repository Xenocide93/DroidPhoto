package com.droidsans.photo.droidphoto.util.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.droidsans.photo.droidphoto.R;
import com.droidsans.photo.droidphoto.util.ReportPack;

import java.util.ArrayList;

/**
 * Created by Ong on 06/08/2015.
 */
public class ReportAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LayoutInflater inflater;
    private Context context;
    private ReportPack[] packs;
    public static final int TYPE_DEVICE_REPORT = 1;
    public static final int TYPE_PHOTO_REPORT = 2;
    public static final int TYPE_USER_REPORT = 3;

    public ReportAdapter (Context context, ReportPack[] packs){
        this.context = context;
        this.packs = packs;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(inflater == null){inflater = LayoutInflater.from(parent.getContext());}
        View view;
        RecyclerView.ViewHolder holder;
        switch (viewType){
            case TYPE_DEVICE_REPORT:
                view = inflater.inflate(R.layout.item_report_device, null);
                holder = new ReportDeviceHolder(view);
                return holder;
            case TYPE_PHOTO_REPORT:
                view = inflater.inflate(R.layout.item_report_photo, null);
                holder = new ReportPictureHolder(view);
                return holder;
            case TYPE_USER_REPORT:
                view = inflater.inflate(R.layout.item_report_user, null);
                holder = new ReportUserHolder(view);
                return holder;
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ReportPack pack = packs[position];

        switch (holder.getItemViewType()){
            case TYPE_DEVICE_REPORT:
                ReportDeviceHolder mHolder = (ReportDeviceHolder) holder;
                mHolder.buildDevice.setText(pack.build_device);
                mHolder.buildModel.setText(pack.build_model);
                mHolder.retailVendor.setText(pack.retail_vendor);
                mHolder.retailModel.setText(pack.retail_model);
                break;
            case TYPE_PHOTO_REPORT:
                ReportPictureHolder nHolder = (ReportPictureHolder) holder;
                Glide.with(context)
                        .load(pack.photo_id)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .centerCrop()
                        .placeholder(R.drawable.picture_placeholder_500_center)
                        .into(nHolder.image);
                nHolder.reason.setText(pack.reason);
                break;
            case TYPE_USER_REPORT:
                ReportUserHolder oHolder = (ReportUserHolder) holder;
                oHolder.username.setText(pack.user_id);
                oHolder.reason.setText(pack.reason);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return packs.length;
    }

    @Override
    public int getItemViewType(int position) {
        switch (packs[position].type){
            case "device": return TYPE_DEVICE_REPORT;
            case "photo": return TYPE_PHOTO_REPORT;
            case "user": return TYPE_USER_REPORT;
            default: return TYPE_DEVICE_REPORT;
        }
    }
}
