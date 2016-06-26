package com.droidsans.photo.droidphoto.util.ViewHolderFactory;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.droidsans.photo.droidphoto.FeedFragment;
import com.droidsans.photo.droidphoto.ImageViewerActivity;
import com.droidsans.photo.droidphoto.R;
import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.droidsans.photo.droidphoto.util.PicturePack;
import com.droidsans.photo.droidphoto.util.adapter.ProfileFeedRecycleViewAdapter;
import com.droidsans.photo.droidphoto.util.common.ItemViewHolder;
import com.droidsans.photo.droidphoto.util.common.ViewHolderFactory;
import com.droidsans.photo.droidphoto.util.view.FontTextView;
import com.droidsans.photo.droidphoto.util.view.SquareImageView;

/**
 * Created by Xenocide93 on 6/19/16.
 */
public class PictureViewHolderFactory implements ViewHolderFactory {
	private View.OnClickListener onPictureClickListener;

	public PictureViewHolderFactory(View.OnClickListener onPictureClickListener) {
		this.onPictureClickListener = onPictureClickListener;
	}

	@Override
	public ItemViewHolder create(ViewGroup parent) {
		return new PictureViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_feed_pic_uploading,
				parent,
                false
        ));
	}

	private class PictureViewHolder extends ItemViewHolder<PicturePack> {
		private FontTextView deviceName, user;
		private SquareImageView picture;
		private RelativeLayout uploadLayout;

        private PicturePack pack;
        private int position;

		/**
		 * Instantiates a view holder.
		 *
		 * @param itemView
		 *            the item view
		 */
		public PictureViewHolder(View itemView) {
			super(itemView);

			picture = (SquareImageView) itemView.findViewById(R.id.picture);
			deviceName = (FontTextView) itemView.findViewById(R.id.device_name);
			user = (FontTextView) itemView.findViewById(R.id.user);
			uploadLayout = (RelativeLayout) itemView.findViewById(R.id.upload_layout);
            
            itemView.setOnClickListener(onPictureClickListener);
		}

		@Override
		public void fillData(PicturePack pack, int position) {

            this.pack = pack;
            this.position = position;

            if (pack.isUploading) {
                Glide.with(getContext()).load(pack.localPicturePath).diskCacheStrategy(DiskCacheStrategy.NONE).centerCrop()
                        .placeholder(R.drawable.picture_placeholder_500_center).into(picture);
                uploadLayout.setVisibility(View.VISIBLE);
            } else {
                Glide.with(getContext()).load(GlobalSocket.serverURL + pack.baseURL + pack.photoURL).centerCrop()
                        .placeholder(R.drawable.picture_placeholder_500_center).crossFade().into(picture);
                uploadLayout.setVisibility(View.GONE);

                deviceName.setText(String.format("%s %s", pack.vendor, pack.model));
                user.setText(pack.username);
            }
        }
	}
}
