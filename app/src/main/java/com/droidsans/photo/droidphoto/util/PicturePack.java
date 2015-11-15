package com.droidsans.photo.droidphoto.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PicturePack {
    public String photoURL, caption, vendor, model,
            eventId, shutterSpeed, aperture, iso, username, submitDate,
            gpsLocation, photoId, userId, gpsLocalizedLocation, avatarURL,
            localPicturePath;
    public int rank, width, height, likeCount;
    public double gpsLat, gpsLong;
    public boolean isEnhanced, isFlash, isUploading, isLike;
    public int percentage = 0;
    public final String baseURL = "/data/photo/500px/";

    public PicturePack() {
        //default constructor
    }

    public PicturePack(String username, String vendor, String model,
                       String shutterSpeed, String aperture, String iso) {
        this.vendor = vendor;
        this.model = model;
        this.username = username;
        this.shutterSpeed = shutterSpeed;
        this.aperture = aperture;
        this.iso = iso;
        isUploading = false;
    }

    public void setIsUploading(boolean isUploading, String localPicturePath){
        this.isUploading = isUploading;
        this.localPicturePath = localPicturePath;
    }

    public void setLikeCount(int likeCount){this.likeCount = likeCount; }

    public void setIsLike(Boolean isLike){this.isLike = isLike; }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setShutterSpeed(String shutterSpeed) {
        this.shutterSpeed = shutterSpeed;
    }

    public void setAperture(String aperture) {
        this.aperture = aperture;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }

    public void setSubmitDate(String submitDate) {
        this.submitDate = submitDate;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setGpsLat(double gpsLat) {
        this.gpsLat = gpsLat;
    }

    public void setGpsLong(double gpsLong) {
        this.gpsLong = gpsLong;
    }

    public void setGpsLocation(String gpsLocation) {
        this.gpsLocation = gpsLocation;
    }

    public void setGpsLocalizedLocation(String gpsLocalizedLocation) {
        this.gpsLocalizedLocation = gpsLocalizedLocation;
    }

    public void setIsEnhanced(boolean isEnhanced) {
        this.isEnhanced = isEnhanced;
    }

    public void setIsFlash(boolean isFlash) {
        this.isFlash = isFlash;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }
}
