package com.droidsans.photo.droidphoto.util.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.droidsans.photo.droidphoto.ImageViewerActivity;
import com.droidsans.photo.droidphoto.ProfileFragment;
import com.droidsans.photo.droidphoto.ProfileViewerActivity;
import com.droidsans.photo.droidphoto.R;
import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.droidsans.photo.droidphoto.util.PicturePack;
import com.droidsans.photo.droidphoto.util.view.SquareImageView;

import java.util.ArrayList;

/**
 * Created by Ong on 14/7/2015.
 */
public class ProfileFeedRecycleViewAdapter extends RecyclerView.Adapter {
    private ArrayList<PicturePack> packs;
    private LayoutInflater inflater;
    private Context context;
    public static boolean isClickOnce = false;
    public boolean isInEditMode = false;
    public boolean[] isMarkedAsRemove;
    private Object activityObject;

    public ProfileFeedRecycleViewAdapter(Context context, Object activityObject, ArrayList<PicturePack> packs){
        this.packs = packs;
        this.context = context;
        this.activityObject = activityObject;
        isMarkedAsRemove = new boolean[packs.size()];
        for (int i=0; i<isMarkedAsRemove.length; i++){
            isMarkedAsRemove[i] = false;
        }
    }

    @Override
    public ProfileFeedRecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(inflater==null) inflater = LayoutInflater.from(parent.getContext());
        View itemHolderView = inflater.inflate(R.layout.item_user_pic, null);

        ViewHolder holder = new ViewHolder(itemHolderView);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final PicturePack pack = packs.get(position);
        final ViewHolder myHolder = (ViewHolder) holder;

        Glide.with(context)
                .load(GlobalSocket.serverURL + pack.baseURL + pack.photoURL)
                .centerCrop()
                .placeholder(R.drawable.picture_placeholder_500_center)
                .crossFade()
                .into(myHolder.picture);

        if(isInEditMode){
            //set edit mode item visible
            myHolder.checkBox.setVisibility(View.VISIBLE);
            myHolder.checkBoxBg.setVisibility(View.VISIBLE);
            myHolder.selectView.setVisibility(View.VISIBLE);

            //set select view the correct mark state (while scrolling)
            if(isMarkedAsRemove[position]) {
                myHolder.selectView.setAlpha(1f);
                myHolder.checkBox.setAlpha(1f);
            } else {
                myHolder.selectView.setAlpha(0f);
                myHolder.checkBox.setAlpha(0f);
            }
        } else {
            //not in edit mode, all gone
            myHolder.checkBox.setVisibility(View.GONE);
            myHolder.checkBoxBg.setVisibility(View.GONE);
            myHolder.selectView.setVisibility(View.GONE);
        }

        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isInEditMode){
                    //toggle select state
                    isMarkedAsRemove[position] = !isMarkedAsRemove[position];
                    //toggle animation
                    if(isMarkedAsRemove[position]){
                        myHolder.selectView.animate()
                                .alpha(1f)
                                .setDuration(150)
                                .start();
                        myHolder.checkBox.animate()
                                .alpha(1f)
                                .setDuration(150)
                                .start();
                    } else {
                        myHolder.selectView.animate()
                                .alpha(0f)
                                .setDuration(150)
                                .start();
                        myHolder.checkBox.animate()
                                .alpha(0f)
                                .setDuration(150)
                                .start();
                    }
                } else { //not is edit mode, launch picture viewer normally
                    if(!isClickOnce){
                        ProfileFeedRecycleViewAdapter.isClickOnce = true;
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
                        imageViewerIntent.putExtra("submitDate", pack.submitDate);
                        imageViewerIntent.putExtra("avatarURL", pack.avatarURL);
                        imageViewerIntent.putExtra("is_enhanced", pack.isEnhanced);
                        imageViewerIntent.putExtra("is_like", pack.isLike);
                        imageViewerIntent.putExtra("like_count", pack.likeCount);

                        imageViewerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if(activityObject instanceof ProfileFragment){
                            ((ProfileFragment) activityObject).startActivityForResult(imageViewerIntent, ProfileFragment.UPDATE_LIKE_STATE);
                        } else if(activityObject instanceof ProfileViewerActivity) {
                            ((ProfileViewerActivity) activityObject).startActivityForResult(imageViewerIntent, ProfileViewerActivity.UPDATE_LIKE_STATE);
                        }
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return packs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        SquareImageView picture;
        View checkBox, checkBoxBg, selectView;

        public ViewHolder(View itemView) {
            super(itemView);
            picture = (SquareImageView) itemView.findViewById(R.id.picture);
            checkBoxBg = itemView.findViewById(R.id.checkbox_bg);
            checkBox = itemView.findViewById(R.id.checkbox);
            selectView = itemView.findViewById(R.id.select_view);
        }
    }
}
