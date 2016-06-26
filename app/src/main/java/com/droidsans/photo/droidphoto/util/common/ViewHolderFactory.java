package com.droidsans.photo.droidphoto.util.common;

import android.view.ViewGroup;

/**
 * Created by Xenocide93 on 6/19/16.
 */
public interface ViewHolderFactory {
    /**
     * Create item view holder.
     *
     * @param parent
     *            the parent
     * @return the item view holder
     */
    ItemViewHolder create(ViewGroup parent);
}
