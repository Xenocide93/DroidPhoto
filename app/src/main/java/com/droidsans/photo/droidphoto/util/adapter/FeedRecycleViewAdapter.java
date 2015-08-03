package com.droidsans.photo.droidphoto.util.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.droidsans.photo.droidphoto.FeedFragment;
import com.droidsans.photo.droidphoto.ImageViewerActivity;
import com.droidsans.photo.droidphoto.ImageViewerFragment;
import com.droidsans.photo.droidphoto.R;
import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.droidsans.photo.droidphoto.util.PicturePack;

import java.util.ArrayList;

/**
 * Created by Ong on 14/7/2015.
 */
public class FeedRecycleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_CONTENT = 64;
    public static final int TYPE_FOOTER = 128;
    public static final int TYPE_HEADER = 256;
    private ArrayList<PicturePack> packs;
//    private LayoutInflater inflater;
    private Context context;
    public static boolean isClickOnce = false;

    public FeedRecycleViewAdapter(Context context, ArrayList<PicturePack> packs){
        this.packs = packs;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        if(inflater==null) inflater = LayoutInflater.from(parent.getContext());
//        View itemHolderView = inflater.inflate(R.layout.item_feed_pic_uploading, null);
//
//        FeedImageHolder holder = new FeedImageHolder(itemHolderView);
//        return holder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_CONTENT:
                View feedImageHolderView = inflater.inflate(R.layout.item_feed_pic_uploading, null);
                FeedImageHolder feedImageHolder = new FeedImageHolder(feedImageHolderView);
                return feedImageHolder;
            case TYPE_FOOTER:
                View loadMoreHolderView = inflater.inflate(R.layout.item_load_more, null);
                LoadMoreHolder loadMoreHolder = new LoadMoreHolder(loadMoreHolderView);
                return loadMoreHolder;
            default:
                //bug ???
                View defaultHolderView = inflater.inflate(R.layout.item_feed_pic_uploading, null);
                MainViewHolder defaultHolder = new MainViewHolder(defaultHolderView);
                return defaultHolder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case TYPE_CONTENT:
                final PicturePack pack = packs.get(position);
                FeedImageHolder myHolder = (FeedImageHolder) holder;

                if (pack.isUploading) {
                    Glide.with(context)
                            .load(pack.localPicturePath)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .centerCrop()
                            .placeholder(R.drawable.picture_placeholder_500_center)
                            .into(myHolder.picture);
                    myHolder.uploadLayout.setVisibility(View.VISIBLE);
                } else {
                    Glide.with(context)
                            .load(GlobalSocket.serverURL + pack.baseURL + pack.photoURL)
                            .centerCrop()
                            .placeholder(R.drawable.picture_placeholder_500_center)
                            .crossFade()
                            .into(myHolder.picture);
                    myHolder.uploadLayout.setVisibility(View.GONE);


                    myHolder.deviceName.setText(pack.vendor + " " + pack.model);
                    myHolder.user.setText(pack.username + "");

                    myHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!isClickOnce) {
                                FeedRecycleViewAdapter.isClickOnce = true;
//                                Intent imageViewerIntent = new Intent(context, ImageViewerActivity.class);
//                                imageViewerIntent.putExtra("photoId", pack.photoId);
//                                imageViewerIntent.putExtra("photoURL", pack.photoURL);
//                                imageViewerIntent.putExtra("caption", pack.caption);
//                                imageViewerIntent.putExtra("vendor", pack.vendor);
//                                imageViewerIntent.putExtra("model", pack.model);
//                                imageViewerIntent.putExtra("exposureTime", pack.shutterSpeed);
//                                imageViewerIntent.putExtra("aperture", pack.aperture);
//                                imageViewerIntent.putExtra("iso", pack.iso);
//                                imageViewerIntent.putExtra("userId", pack.userId);
//                                imageViewerIntent.putExtra("username", pack.username);
//                                imageViewerIntent.putExtra("gpsLocation", pack.gpsLocation);
//                                imageViewerIntent.putExtra("gpsLocalized", pack.gpsLocalizedLocation);
//                                imageViewerIntent.putExtra("submitDate", pack.submitDate);
//                                imageViewerIntent.putExtra("avatarURL", pack.avatarURL);
//                                imageViewerIntent.putExtra("is_enhanced", pack.isEnhanced);
//
//                                context.startActivity(imageViewerIntent);

                                Bundle args = new Bundle();
                                args.putString("photoId", pack.photoId);
                                args.putString("photoURL", pack.photoURL);
                                args.putString("caption", pack.caption);
                                args.putString("vendor", pack.vendor);
                                args.putString("model", pack.model);
                                args.putString("exposureTime", pack.shutterSpeed);
                                args.putString("aperture", pack.aperture);
                                args.putString("iso", pack.iso);
                                args.putString("userId", pack.userId);
                                args.putString("username", pack.username);
                                args.putString("gpsLocation", pack.gpsLocation);
                                args.putString("gpsLocalized", pack.gpsLocalizedLocation);

                                Fragment imageViewerFragment = ImageViewerFragment.newInstance(args);

                                FragmentTransaction transaction = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.main_fragment, imageViewerFragment, "IMAGE_VIEWER");
                                transaction.addToBackStack(null);
                                transaction.commit();


                            }
                        }
                    });
                }

                //for test UI
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
                break;
            case TYPE_FOOTER:
                FeedFragment.mFeedFragment.updateFeed();
                GlobalSocket.mSocket.on("update_feed", FeedFragment.mFeedFragment.onUpdateFeedRespond);
                break;
        }
    }

    @Override
    public int getItemCount() {
        if(packs == null) return -1;
        return packs.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(packs.get(position).vendor != null) {
            return TYPE_CONTENT;
        } else {
            return TYPE_FOOTER;
        }
    }
}
