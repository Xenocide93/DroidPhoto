package com.droidsans.photo.droidphoto.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.droidsans.photo.droidphoto.R;

import java.util.ArrayList;

public class PictureGridAdapter extends ArrayAdapter<PicturePack> {
    private int resourceLayout;
    private LayoutInflater inflater;
    private Bitmap imageBitmap;

    public PictureGridAdapter(Context context, int resource, ArrayList<PicturePack> objects) {
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


        PicturePack pack = getItem(position);

        //        check has URL        check is start load         check has bitmap          check is done loading
        if((pack.photoURL != null) && (pack.isLoaded) && (pack.imageBitmap != null) && (pack.isDoneLoading)) {
            Log.d("droidphoto", "set image bitmap :" + position);
            holder.picture.setImageBitmap(pack.imageBitmap);
        } else {
            Log.d("droidphoto", "set placeholder :" + position);
            holder.picture.setImageDrawable(getContext().getResources().getDrawable(R.drawable.droidsans_logo));
        }
        holder.deviceName.setText(pack.vendor + " " + pack.model);
        holder.user.setText(pack.username + "");
        //holder.shutterSpeed.setText(pack.shutterSpeed);
        //holder.aperture.setText(pack.aperture);
        //holder.iso.setText("ISO"+pack.iso);

        return row;
    }

    private class ItemHolder {
        FontTextView deviceName, user, shutterSpeed, aperture, iso;
        ImageView picture;
        public ItemHolder(View view){
            picture = (ImageView) view.findViewById(R.id.picture);
            deviceName = (FontTextView) view.findViewById(R.id.device_name);
            user = (FontTextView) view.findViewById(R.id.user);
            shutterSpeed = (FontTextView) view.findViewById(R.id.shutter_speed);
            aperture = (FontTextView) view.findViewById(R.id.aperture);
            iso = (FontTextView) view.findViewById(R.id.iso);
        }
    }
}