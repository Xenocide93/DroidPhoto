package com.droidsans.photo.droidphoto.util.common;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Xenocide93 on 6/19/16.
 */
public abstract class ItemViewHolder<T> extends RecyclerView.ViewHolder {

    /**
     * Instantiates a view holder.
     *
     * @param itemView
     *            the item view
     */
    public ItemViewHolder(View itemView) {
        super(itemView);
    }

    /**
     * Find view by id.
     *
     * @param id
     *            the id
     * @return the view
     */
    protected View findViewById(int id) {
        return itemView.findViewById(id);
    }

    /**
     * Gets context form item view.
     *
     * @return context
     */
    protected Context getContext() {
        return itemView.getContext();
    }

    /**
     * Fill data.
     *
     * @param data
     *            the data
     * @param position
     *            position
     */
    public void fillData(T data, int position) {
    }
}