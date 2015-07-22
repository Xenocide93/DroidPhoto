package com.droidsans.photo.droidphoto.util;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.droidsans.photo.droidphoto.ImageViewerActivity;
import com.droidsans.photo.droidphoto.R;

import java.util.ArrayList;

/**
 * Created by Ong on 14/7/2015.
 */
public class FeedRecycleViewAdapter extends RecyclerView.Adapter {
    private ArrayList<PicturePack> packs;
    private LayoutInflater inflater;
    private Context context;
    public static boolean isClickOnce = false;

    public FeedRecycleViewAdapter(Context context, ArrayList<PicturePack> packs){
        this.packs = packs;
        this.context = context;
    }

    @Override
    public FeedRecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(inflater==null) inflater = LayoutInflater.from(parent.getContext());
        View itemHolderView = inflater.inflate(R.layout.item_feed_pic_uploading, null);

        ViewHolder holder = new ViewHolder(itemHolderView);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final PicturePack pack = packs.get(position);
        ViewHolder myHolder = (ViewHolder) holder;

        if(pack.isUploading){
            Glide.with(context)
                    .load(pack.localPicturePath)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .centerCrop()
                    .placeholder(R.drawable.droidsans_logo)
                    .into(myHolder.picture);
            myHolder.uploadLayout.setVisibility(View.VISIBLE);
        } else {
            Glide.with(context)
                    .load(GlobalSocket.serverURL + pack.baseURL + pack.photoURL)
                    .centerCrop()
                    .placeholder(R.drawable.droidsans_logo)
                    .crossFade()
                    .into(myHolder.picture);
            myHolder.uploadLayout.setVisibility(View.GONE);
        }

        myHolder.deviceName.setText(pack.vendor + " " + pack.model);
        myHolder.user.setText(pack.username + "");

        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isClickOnce){
                    FeedRecycleViewAdapter.isClickOnce = true;
                    Intent imageViewerIntent = new Intent(context, ImageViewerActivity.class);
                    imageViewerIntent.putExtra("photoId", pack.photoId);
                    imageViewerIntent.putExtra("photoURL", pack.photoURL);
                    imageViewerIntent.putExtra("caption", pack.caption);
                    imageViewerIntent.putExtra("vendor", pack.vendor);
                    imageViewerIntent.putExtra("model", pack.model);
                    imageViewerIntent.putExtra("exposureTime", pack.shutterSpeed);
                    imageViewerIntent.putExtra("aperture", pack.aperture);
                    imageViewerIntent.putExtra("iso", pack.iso);
                    imageViewerIntent.putExtra("userId", pack.userId);
                    imageViewerIntent.putExtra("username", pack.username);
                    imageViewerIntent.putExtra("gpsLocation", pack.gpsLocation);
                    imageViewerIntent.putExtra("gpsLocalized", pack.gpsLocalizedLocation);

                    context.startActivity(imageViewerIntent);
                }
            }
        });

        //TODO for test UI
//        if(position == 0){
//            FeedRecycleViewAdapter.isClickOnce = true;
//            Intent imageViewerIntent = new Intent(context, ImageViewerActivity.class);
//            imageViewerIntent.putExtra("photoId", pack.photoId);
//            imageViewerIntent.putExtra("photoURL", pack.photoURL);
//            imageViewerIntent.putExtra("caption", pack.caption);
//            imageViewerIntent.putExtra("vendor", pack.vendor);
//            imageViewerIntent.putExtra("model", pack.model);
//            imageViewerIntent.putExtra("exposureTime", pack.shutterSpeed);
//            imageViewerIntent.putExtra("aperture", pack.aperture);
//            imageViewerIntent.putExtra("iso", pack.iso);
//            imageViewerIntent.putExtra("userId", pack.userId);
//            imageViewerIntent.putExtra("username", pack.username);
//            imageViewerIntent.putExtra("gpsLocation", pack.gpsLocation);
//            imageViewerIntent.putExtra("gpsLocalized", pack.gpsLocalizedLocation);
//
//            context.startActivity(imageViewerIntent);
//        }
    }

    @Override
    public int getItemCount() {
        return packs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        FontTextView deviceName, user;
        SquareImageView picture;
        RelativeLayout uploadLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            picture = (SquareImageView) itemView.findViewById(R.id.picture);
            deviceName = (FontTextView) itemView.findViewById(R.id.device_name);
            user = (FontTextView) itemView.findViewById(R.id.user);
            uploadLayout = (RelativeLayout) itemView.findViewById(R.id.upload_layout);
        }
    }
}
