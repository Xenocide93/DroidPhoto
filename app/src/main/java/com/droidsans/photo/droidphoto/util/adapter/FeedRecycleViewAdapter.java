package com.droidsans.photo.droidphoto.util.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.droidsans.photo.droidphoto.FeedFragment;
import com.droidsans.photo.droidphoto.ImageViewerActivity;
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
	private Context context;
	private FeedFragment feedFragment;
	public static boolean isClickOnce = false;

	public FeedRecycleViewAdapter(Context context, FeedFragment feedFragment, ArrayList<PicturePack> packs) {
		this.packs = packs;
		this.context = context;
		this.feedFragment = feedFragment;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		switch (viewType) {
		case TYPE_CONTENT:
			View feedImageHolderView = inflater.inflate(R.layout.item_feed_pic_uploading, null);
			return new FeedImageHolder(feedImageHolderView);
		case TYPE_FOOTER:
			View loadMoreHolderView = inflater.inflate(R.layout.item_load_more, null);
			return new LoadMoreHolder(loadMoreHolderView);
		default:
			Log.e(getClass().getSimpleName(), "onCreateViewHolder fall into default case.");
			View defaultHolderView = inflater.inflate(R.layout.item_feed_pic_uploading, null);
			return new MainViewHolder(defaultHolderView);
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
		switch (holder.getItemViewType()) {
		case TYPE_CONTENT:
			final PicturePack pack = packs.get(position);
			FeedImageHolder myHolder = (FeedImageHolder) holder;

			if (pack.isUploading) {
				Glide.with(context).load(pack.localPicturePath).diskCacheStrategy(DiskCacheStrategy.NONE).centerCrop()
						.placeholder(R.drawable.picture_placeholder_500_center).into(myHolder.picture);
				myHolder.uploadLayout.setVisibility(View.VISIBLE);
			} else {
				Glide.with(context).load(GlobalSocket.serverURL + pack.baseURL + pack.photoURL).centerCrop()
						.placeholder(R.drawable.picture_placeholder_500_center).crossFade().into(myHolder.picture);
				myHolder.uploadLayout.setVisibility(View.GONE);

				myHolder.deviceName.setText(String.format("%s %s", pack.vendor, pack.model));
				myHolder.user.setText(pack.username);

				myHolder.itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!isClickOnce) {
							FeedRecycleViewAdapter.isClickOnce = true;
							Intent imageViewerIntent = new Intent(context, ImageViewerActivity.class);

							imageViewerIntent.putExtra("pack", pack);
							imageViewerIntent.putExtra("position", position);

							feedFragment.startActivityForResult(imageViewerIntent, FeedFragment.UPDATE_LIKE_STATE);
						}
					}
				});
			}

			break;
		case TYPE_FOOTER:
			FeedFragment.mFeedFragment.onUpdateRecyclerViewRequest();
			break;
		}
	}

	@Override
	public int getItemCount() {
		if (packs == null)
			return -1;
		return packs.size();
	}

	@Override
	public int getItemViewType(int position) {
		if (packs.get(position).vendor != null) {
			return TYPE_CONTENT;
		} else {
			return TYPE_FOOTER;
		}
	}
}
