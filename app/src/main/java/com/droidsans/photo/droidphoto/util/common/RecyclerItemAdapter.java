package com.droidsans.photo.droidphoto.util.common;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Created by Xenocide93 on 6/19/16.
 */
public class RecyclerItemAdapter<I> extends RecyclerView.Adapter<ItemViewHolder> {
    public static final int TYPE_0 = 0;
    public static final int TYPE_1 = 1;
    public static final int TYPE_2 = 2;
    public static final int TYPE_3 = 3;
    public static final int TYPE_4 = 4;
    public static final int TYPE_5 = 5;
    public static final int TYPE_6 = 6;
    public static final int TYPE_7 = 7;
    public static final int TYPE_8 = 8;
    public static final int TYPE_9 = 9;
    public static final int TYPE_10 = 10;
    public static final int TYPE_11 = 11;
    public static final int TYPE_12 = 12;
    public static final int TYPE_13 = 13;
    public static final int TYPE_14 = 14;
    public static final int TYPE_15 = 15;
    public static final int TYPE_16 = 16;
    public static final int TYPE_17 = 17;

    /**
     * The Map.
     */
    private final SparseArray<ViewHolderFactory> viewHolderFactories;
    private ArrayList<ObjectHolder<I>> items = new ArrayList<>();
    private ArrayList<ObjectHolder<I>> dynamicList = new ArrayList<>();
    private int headerSize = 0;
    private int footerSize = 0;

    /**
     * @param viewHolderItem
     *            viewHolderItem
     */
    public RecyclerItemAdapter(Integer viewHolderItem) {
        viewHolderFactories = new SparseArray<>(viewHolderItem);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return viewHolderFactories.get(viewType).create(parent);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        if (position < items.size()) {
            holder.fillData(items.get(position).getObject(), position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    /**
     * Notify all item type change.
     *
     * @param type
     *            type
     */
    public void notifyViewTypeChanged(int type) {
        ListIterator<ObjectHolder<I>> iter = new ArrayList<>(items).listIterator();
        while (iter.hasNext()) {
            int index = iter.nextIndex();
            ObjectHolder<I> holder = iter.next();
            if (holder.getType() == type) {
                notifyItemChanged(index);
            }
        }
    }

    /**
     * Gets dynamic list.
     *
     * @return dynamicList dynamicList
     */
    public ArrayList<I> getDynamicList() {
        ArrayList<I> list = new ArrayList<>();
        for (ObjectHolder<I> item : dynamicList) {
            list.add(item.object);
        }
        return list;
    }

    /**
     * Remove item.
     *
     * @param item
     *            the item to be removed
     */
    public void remove(I item) {
        int i = -1;
        for (ObjectHolder<I> oh : dynamicList) {
            i++;
            if (oh.object.equals(item)) {
                break;
            }
        }
        if (i >= 0) {
            dynamicList.remove(i);
            items.remove(headerSize + i);
            notifyItemRemoved(headerSize + i);
        }

    }

    /**
     * Add void.
     *
     * @param item
     *            the item
     * @param type
     *            type item
     */
    public void add(I item, int type) {
        if (viewHolderFactories.get(type) == null) {
            throw new NullPointerException("Register view holder factory before  type " + type);
        }

        ObjectHolder<I> objectHolder = new ObjectHolder<I>(item, type);
        dynamicList.add(objectHolder);
        int index = ((items.size()) - footerSize);
        items.add(index, objectHolder);
        notifyItemInserted(index);
    }

    /**
     * Add header item.
     *
     * @param type
     *            type
     */
    public void addHeader(int type) {
        addHeader(null, type);
    }

    /**
     * Add header item.
     *
     * @param o
     *            o
     * @param type
     *            type
     */
    public void addHeader(Object o, int type) {
        if (viewHolderFactories.get(type) == null) {
            throw new NullPointerException("Register view holder factory before  type " + type);
        }

        ObjectHolder objectHolder = new ObjectHolder(o, type);
        items.add(headerSize, objectHolder);
        headerSize++;
        notifyDataSetChanged();
    }

    /**
     * Add footer item.
     *
     * @param type
     *            type
     */
    public void addFooter(int type) {
        addFooter(null, type);
    }

    /**
     * Add footer item.
     *
     * @param o
     *            object
     * @param type
     *            type
     */
    public void addFooter(Object o, int type) {
        if (viewHolderFactories.get(type) == null) {
            throw new NullPointerException("Register view holder factory before  type " + type);
        }

        ObjectHolder objectHolder = new ObjectHolder(o, type);
        items.add(objectHolder);
        footerSize++;
        notifyDataSetChanged();
    }

    /**
     * @param type
     *            type
     */
    public void removeHeader(int type) {
        ObjectHolder obj = null;
        int removeIndex;
        for (ObjectHolder holder : new ArrayList<>(items)) {
            if (holder.getType() == type) {
                obj = holder;
            }
        }

        removeIndex = items.indexOf(obj);
        items.remove(obj);
        headerSize--;

        if (removeIndex != -1) {
            notifyItemRemoved(removeIndex);
        } else {
            notifyDataSetChanged();
        }
    }

    /**
     * @param type
     *            type
     */
    public void removeFooter(int type) {
        ObjectHolder obj = null;
        int removeIndex;
        for (ObjectHolder holder : new ArrayList<>(items)) {
            if (holder.getType() == type) {
                obj = holder;
            }
        }

        removeIndex = items.indexOf(obj);
        items.remove(obj);
        footerSize--;

        if (removeIndex != -1) {
            notifyItemRemoved(removeIndex);
        } else {
            notifyDataSetChanged();
        }
    }

    /**
     *
     */
    public void clear() {
        items.removeAll(dynamicList);
        dynamicList.clear();
        notifyDataSetChanged();
    }

    /**
     * Clear all void.
     */
    public void clearAll() {
        headerSize = 0;
        footerSize = 0;
        items.clear();
        dynamicList.clear();
        notifyDataSetChanged();
    }

    /**
     * Gets item at position.
     *
     * @param position
     *            position
     * @return item item
     */
    public I getItem(int position) {
        return items.get(position).getObject();
    }

    /**
     * Register view holder factory.
     *
     * @param type
     *            the type
     * @param viewHolderFactory
     *            the view holder factory
     */
    public void registerViewHolderFactory(Integer type, ViewHolderFactory viewHolderFactory) {
        viewHolderFactories.put(type, viewHolderFactory);
    }

    private class ObjectHolder<I> {
        private int type;
        private I object;

        private ObjectHolder(I object, int type) {
            this.object = object;
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public I getObject() {
            return object;
        }

        public void setObject(I object) {
            this.object = object;
        }
    }

}
