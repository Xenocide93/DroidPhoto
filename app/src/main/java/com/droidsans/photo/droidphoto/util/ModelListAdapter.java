package com.droidsans.photo.droidphoto.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.droidsans.photo.droidphoto.R;

public class ModelListAdapter extends ArrayAdapter<String> {
    private int resourceLayout;
    private LayoutInflater inflater;

    public ModelListAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
        this.resourceLayout = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ItemHolder holder;

        if(convertView==null){
            if(inflater==null){
                inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            row = inflater.inflate(resourceLayout, parent, false);
            holder = new ItemHolder(row);
            row.setTag(holder);
        } else {
            holder = (ItemHolder) row.getTag();
        }

        holder.modelName.setText(getItem(position));

        return row;
    }

    private class ItemHolder {
        FontTextView modelName;
        public ItemHolder(View view){
            modelName = (FontTextView) view.findViewById(R.id.model_name);
        }
    }
}
