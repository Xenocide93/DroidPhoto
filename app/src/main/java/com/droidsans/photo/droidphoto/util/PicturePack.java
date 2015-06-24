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
    public String photoURL, caption, vendor, model, eventId, shutterSpeed, aperture, iso, username, submitDate;
    public int userId, rank, width, height;
    public double gpsLat, gpsLong;
    public boolean isEnhanced, isFlash;
    public Drawable pictureDrawable;
    public boolean isLoaded = false;
    public Bitmap imageBitmap = null;
    public boolean isDoneLoading = false;
    public int percentage = 0;
    private ImageLoader2 imageLoader2;
    private final String baseUrl = "http://209.208.65.102/data/photo/500px/";

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

    public Drawable getPictureDrawable(){
        if(pictureDrawable==null){
        }
        return pictureDrawable;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public void setLoad() {
        if(!this.isLoaded) {
            //new ImageLoader().start();
            imageLoader2 = new ImageLoader2();
            imageLoader2.execute("");
            this.isLoaded = true;
        }
    }

    public void resetPackBitmap() {
        if(this.imageBitmap != null) {
            this.imageBitmap.recycle();
            //Log.d("droidphoto", "recycled");
        }
        if(imageLoader2 != null) {
            imageLoader2.cancel(true);
        }
        this.isLoaded = false;
        this.isDoneLoading = false;
    }

    /**<p>ImageLoader2 is a version2 of ImageLoader which use AyncTask (instead of Thread)</p>
     *
     * <p>params: string[] as follows
     * string[0] = baseURL
     * string[1] = photoURL</p>
     *
     * <p>return:
     * </p>
     *
     */

    public class ImageLoader2 extends AsyncTask<String,Integer,String> {

        @Override
        protected String doInBackground(String... params) {
            InputStream in = null;
            ByteArrayOutputStream outputStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(baseUrl + photoURL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.getDoInput();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Accept-Encoding", "");
                urlConnection.connect();
                if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return urlConnection.getResponseCode() + "";
                }
                int fileLength = urlConnection.getContentLength();

                //in = new BufferedInputStream(urlConnection.getInputStream());
                in = urlConnection.getInputStream();

                outputStream = new ByteArrayOutputStream();

                byte buffer[] = new byte[8192];
                int total = 0;
                int count;
                while((count = in.read(buffer)) != -1) {
                    total += count;
                    outputStream.write(buffer,0,count);
                    if(fileLength > 0) {
                        publishProgress((int) (total * 100 / fileLength));
                    }
                }
                in.close();
                outputStream.close();
                Log.d("droidphoto", "total:" + total + "|filelength:" + fileLength);
                if(total == fileLength) {
                    //imageBitmap = BitmapFactory.decodeStream(new BufferedInputStream(in));
                    if(imageBitmap != null) imageBitmap.recycle();
                    imageBitmap = BitmapFactory.decodeByteArray(outputStream.toByteArray(),0,total);
                    isDoneLoading = true;
                } else {
                    isDoneLoading = false;
                    isLoaded = false;
                }
            } catch (IOException e) {
                isDoneLoading = false;
                isLoaded = false;
                e.printStackTrace();
            } finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }
                if(in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
                if(outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                    }
                }
            }
            return "OK";
        }

        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            percentage = progress[0];
            Log.d("droidphoto","downloaded: " + progress[0] + "%");
        }

        @Override
        protected void onPostExecute(String result) {
            if(imageBitmap == null) {
                isLoaded = false;
                isDoneLoading = false;
            }
        }

    }
}
