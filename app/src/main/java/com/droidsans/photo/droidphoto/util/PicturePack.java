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
    public String photoURL, caption, vendor, model, eventId, shutterSpeed, aperture, iso, username, submitDate, gpsLocation, photoId;
    public int userId, rank, width, height;
    public double gpsLat, gpsLong;
    public boolean isEnhanced, isFlash;
    public int percentage = 0;
    public final String baseURL = "http://209.208.65.102/data/photo/500px/";

    public PicturePack() {
        //default constructor
    }

    public PicturePack(int userId, String vendor, String model,
                       String shutterSpeed, String aperture, String iso) {
        this.vendor = vendor;
        this.model = model;
        this.userId = userId;
        this.shutterSpeed = shutterSpeed;
        this.aperture = aperture;
        this.iso = iso;
    }

    public PicturePack(String photoURL, String username, String caption,
                       String vendor, String model,
                       String eventId, int rank,
                       String shutterSpeed, String aperture, String iso,
                       int width, int height, double gpsLat, double gpsLong,
                       boolean isEnhanced, boolean isFlash, String submitDate) {
        this.photoURL = photoURL;
        this.caption = caption;
        this.vendor = vendor;
        this.model = model;
        this.eventId = eventId;
        this.shutterSpeed = shutterSpeed;
        this.aperture = aperture;
        this.iso = iso;
        //this.userId = userId;
        this.username = username;
        this.rank = rank;
        this.width = width;
        this.height = height;
        this.gpsLat = gpsLat;
        this.gpsLong = gpsLong;
        this.isEnhanced = isEnhanced;
        this.isFlash = isFlash;
        this.submitDate = submitDate;
    }

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

    public void setSubmitDate(String submitDate) {
        this.submitDate = submitDate;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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
