package com.droidsans.photo.droidphoto.util;

import android.graphics.drawable.Drawable;

public class PicturePack {
    public String photoURL, caption, vendor, model, eventId, shutterSpeed, aperture, iso;
    public int userId, rank, width, height, gpsLat, gpsLong;
    public boolean isEnhanced, isFlash;
    public Drawable pictureDrawable;

    public PicturePack(int userId, String vendor, String model,
                       String shutterSpeed, String aperture, String iso) {
        this.vendor = vendor;
        this.model = model;
        this.userId = userId;
        this.shutterSpeed = shutterSpeed;
        this.aperture = aperture;
        this.iso = iso;
    }

    public PicturePack(String photoURL, int userId, String caption,
                       String vendor, String model,
                       String eventId, int rank,
                       String shutterSpeed, String aperture, String iso,
                       int width, int height, int gpsLat, int gpsLong,
                       boolean isEnhanced, boolean isFlash) {
        this.photoURL = photoURL;
        this.caption = caption;
        this.vendor = vendor;
        this.model = model;
        this.eventId = eventId;
        this.shutterSpeed = shutterSpeed;
        this.aperture = aperture;
        this.iso = iso;
        this.userId = userId;
        this.rank = rank;
        this.width = width;
        this.height = height;
        this.gpsLat = gpsLat;
        this.gpsLong = gpsLong;
        this.isEnhanced = isEnhanced;
        this.isFlash = isFlash;
    }

    public Drawable getPictureDrawable(){
        if(pictureDrawable==null){
            //TODO get picture from server
        }

        return pictureDrawable;
    }
}
