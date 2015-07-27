package com.droidsans.photo.droidphoto.util.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import com.droidsans.photo.droidphoto.R;
import com.droidsans.photo.droidphoto.util.view.FontTextView;

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

        holder.showModelInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO show model info dialog and report button
            }
        });

        holder.modelName.setText(getItem(position));

        return row;
    }

    private class ItemHolder {
        FontTextView modelName;
        ImageButton showModelInfo;
        public ItemHolder(View view){
            modelName = (FontTextView) view.findViewById(R.id.model_name);
            showModelInfo = (ImageButton) view.findViewById(R.id.model_show_info);
        }
    }
}
