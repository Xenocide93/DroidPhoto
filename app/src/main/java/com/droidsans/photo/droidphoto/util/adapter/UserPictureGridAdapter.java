package com.droidsans.photo.droidphoto.util.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.bumptech.glide.Glide;
import com.droidsans.photo.droidphoto.R;
import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.droidsans.photo.droidphoto.util.PicturePack;
import com.droidsans.photo.droidphoto.util.view.SquareImageView;

import java.util.ArrayList;

public class UserPictureGridAdapter extends ArrayAdapter<PicturePack> {
    private int resourceLayout;
    private LayoutInflater inflater;
    public boolean isEditMode;
    public boolean[] isMarkedAsRemove;

    public UserPictureGridAdapter(Context context, int resource, ArrayList<PicturePack> objects, boolean isEditMode) {
        super(context, resource, objects);
        this.resourceLayout = resource;
        this.isEditMode = isEditMode;

        isMarkedAsRemove = new boolean[objects.size()];
        for (int i=0; i<isMarkedAsRemove.length; i++){
            isMarkedAsRemove[i] = false;
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
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
        final SquareImageView squareImageView = (SquareImageView) row.findViewById(R.id.picture);
        final View checkBoxBg = row.findViewById(R.id.checkbox_bg);
        final View checkBox = row.findViewById(R.id.checkbox);
        final View selectView = row.findViewById(R.id.select_view);

        selectView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMarkedAsRemove[position] = !isMarkedAsRemove[position];
                if(isMarkedAsRemove[position]){
                    selectView.animate()
                            .alpha(1f)
                            .setDuration(150)
                            .start();
                    checkBox.animate()
                            .alpha(1f)
                            .setDuration(150)
                            .start();
                } else {
                    selectView.animate()
                            .alpha(0f)
                            .setDuration(150)
                            .start();
                    checkBox.animate()
                            .alpha(0f)
                            .setDuration(150)
                            .start();
                }
            }
        });

        PicturePack pack = getItem(position);
        Glide.with(getContext())
                .load(GlobalSocket.serverURL + pack.baseURL + pack.photoURL)
                .centerCrop()
                .placeholder(R.drawable.picture_placeholder_500_center)
                .crossFade()
                .into(squareImageView);

        //restore state when scrolling
        if(isEditMode) {
            checkBox.setVisibility(View.VISIBLE);
            checkBoxBg.setVisibility(View.VISIBLE);
            selectView.setVisibility(View.VISIBLE);
            if(isMarkedAsRemove[position]) {
                selectView.setAlpha(1f);
                checkBox.setAlpha(1f);
            }
            else {
                selectView.setAlpha(0f);
                checkBox.setAlpha(0f);
            }
        } else {
            checkBox.setVisibility(View.GONE);
            checkBoxBg.setVisibility(View.GONE);
            selectView.setVisibility(View.GONE);
        }

        //holder.shutterSpeed.setText(pack.shutterSpeed);
        //holder.aperture.setText(pack.aperture);
        //holder.iso.setText("ISO"+pack.iso);

        return row;
    }

    @Override
    public void notifyDataSetChanged() {
        //reset remove mark array
        isMarkedAsRemove = new boolean[getCount()];
        for (int i=0; i<isMarkedAsRemove.length; i++){
            isMarkedAsRemove[i] = false;
        }
        super.notifyDataSetChanged();
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