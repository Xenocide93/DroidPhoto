package com.droidsans.photo.droidphoto.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class PicturePack implements Serializable {
	public String photoURL, caption, vendor, model, eventId, shutterSpeed, aperture, iso, username, submitDate,
			gpsLocation, photoId, userId, gpsLocalizedLocation, avatarURL, localPicturePath;
	public int rank, width, height, likeCount;
	public double gpsLat, gpsLong;
	public boolean isEnhanced, isFlash, isUploading, isLike;
	public int percentage = 0;
	public final String baseURL = "/data/photo/500px/";

	public static ArrayList<PicturePack> makePacksFromJSONArray(JSONArray photoList) {
		return PicturePack.addPacksFromJSONArray(photoList, null);
	}

	public static ArrayList<PicturePack> addPacksFromJSONArray(JSONArray photoList, ArrayList<PicturePack> packs) {
		if(packs == null){
			packs = new ArrayList<>();
		}

		for (int i = 0; i < photoList.length(); i++) {
			JSONObject jsonPack = photoList.optJSONObject(i);
			PicturePack picturePack = new PicturePack();

			picturePack.setPhotoId(jsonPack.optString("_id"));
			picturePack.setPhotoURL(jsonPack.optString("photo_url"));
			picturePack.setUserId(jsonPack.optString("user_id"));
			picturePack.setUsername(jsonPack.optString("username"));
			picturePack.setCaption(jsonPack.optString("caption", ""));
			picturePack.setVendor(jsonPack.optString("vendor"));
			picturePack.setModel(jsonPack.optString("model"));
			picturePack.setEventId(jsonPack.optString("event_id"));
			picturePack.setRank(jsonPack.optInt("ranking"));
			picturePack.setShutterSpeed(jsonPack.optString("exp_time"));
			picturePack.setAperture(jsonPack.optString("aperture"));
			picturePack.setIso(jsonPack.optString("iso"));
			picturePack.setWidth(jsonPack.optInt("width"));
			picturePack.setHeight(jsonPack.optInt("height"));
			picturePack.setGpsLocation(jsonPack.optString("gps_location"));
			picturePack.setGpsLocalizedLocation(jsonPack.optString("gps_localized"));
			picturePack.setIsEnhanced(jsonPack.optBoolean("is_enhanced"));
			picturePack.setIsFlash(jsonPack.optBoolean("is_flash"));
			picturePack.setSubmitDate(jsonPack.optString("submit_date"));
			picturePack.setAvatarURL(jsonPack.optString("avatar_url"));
			picturePack.setIsLike(jsonPack.optBoolean("is_like"));
			picturePack.setLikeCount(jsonPack.optInt("like_count"));

			packs.add(picturePack);
		}

		return packs;
	}

	public PicturePack() {
		//default constructor
	}

	public PicturePack(String username, String vendor, String model, String shutterSpeed, String aperture, String iso) {
		this.vendor = vendor;
		this.model = model;
		this.username = username;
		this.shutterSpeed = shutterSpeed;
		this.aperture = aperture;
		this.iso = iso;
		isUploading = false;
	}

	public void setIsUploading(boolean isUploading, String localPicturePath) {
		this.isUploading = isUploading;
		this.localPicturePath = localPicturePath;
	}

	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}

	public void setIsLike(Boolean isLike) {
		this.isLike = isLike;
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

	public String getDeviceName() {
		return vendor + " " + model;
	}
}
