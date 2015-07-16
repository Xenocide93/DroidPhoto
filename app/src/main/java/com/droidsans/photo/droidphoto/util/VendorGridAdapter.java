package com.droidsans.photo.droidphoto.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.view.LayoutInflater;
import android.widget.ImageView;

import com.droidsans.photo.droidphoto.BrowseVendorActivity;
import com.droidsans.photo.droidphoto.R;

public class VendorGridAdapter extends ArrayAdapter<Integer> {
    private int resourceLayout;
    private LayoutInflater inflater;

    public VendorGridAdapter(Context context, int resource, Integer[] objects) {
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

        holder.vendorPic.setImageResource(getItem(position));
        if(getItem(position) == R.drawable.curve_white) {
            holder.vendorText.setText(BrowseVendorActivity.vendorName[position]);
        }

        return row;
    }

    private class ItemHolder {
        ImageView vendorPic;
        FontTextView vendorText;
        public ItemHolder(View view){
            vendorPic = (ImageView) view.findViewById(R.id.vendor_pic);
            vendorText = (FontTextView) view.findViewById(R.id.vendor_text);
        }
    }
}
