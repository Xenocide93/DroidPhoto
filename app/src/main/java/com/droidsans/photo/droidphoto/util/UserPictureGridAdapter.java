package com.droidsans.photo.droidphoto.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.droidsans.photo.droidphoto.R;

import java.util.ArrayList;

public class UserPictureGridAdapter extends ArrayAdapter<PicturePack> {
    private int resourceLayout;
    private LayoutInflater inflater;

    public UserPictureGridAdapter(Context context, int resource, ArrayList<PicturePack> objects) {
        super(context, resource, objects);
        this.resourceLayout = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        View row = convertView;
//        ItemHolder holder;
//
//        if(convertView==null){
//            if(inflater==null){
//                inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            }
//            row = inflater.inflate(resourceLayout, parent, false);
//            holder = new ItemHolder(row);
//            row.setTag(holder);
//        } else {
//            holder = (ItemHolder) row.getTag();
//        }

        if(inflater==null){
            inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        View row = inflater.inflate(resourceLayout, parent, false);
        SquareImageView squareImageView = (SquareImageView) row.findViewById(R.id.picture);

        PicturePack pack = getItem(position);
        Glide.with(getContext())
                .load(pack.baseURL + pack.photoURL)
                .centerCrop()
                .placeholder(R.drawable.droidsans_logo)
                .crossFade()
                .into(squareImageView);
        //holder.shutterSpeed.setText(pack.shutterSpeed);
        //holder.aperture.setText(pack.aperture);
        //holder.iso.setText("ISO"+pack.iso);

        return row;
    }

    private class ItemHolder {
//        FontTextView shutterSpeed, aperture, iso;
        SquareImageView picture;
        public ItemHolder(View view){
            picture = (SquareImageView) view.findViewById(R.id.picture);
//            shutterSpeed = (FontTextView) view.findViewById(R.id.shutter_speed);
//            aperture = (FontTextView) view.findViewById(R.id.aperture);
//            iso = (FontTextView) view.findViewById(R.id.iso);
        }
    }
}