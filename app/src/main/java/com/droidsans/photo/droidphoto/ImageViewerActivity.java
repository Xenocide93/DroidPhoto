package com.droidsans.photo.droidphoto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.util.LruCache;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.disklrucache.DiskLruCache;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.diegocarloslima.byakugallery.lib.TileBitmapDrawable;
import com.diegocarloslima.byakugallery.lib.TouchImageView;
import com.droidsans.photo.droidphoto.util.FontTextView;
import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.droidsans.photo.droidphoto.util.PicturePack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;


public class ImageViewerActivity extends AppCompatActivity {
    private static final int MAX_CACHE_SIZE = 1024*1024*50; //50MB
    private TouchImageView picture;
    private Bitmap imageBitmap;
    private byte imageByte[];
    private FontTextView deviceName, exposureTime, aperture, iso, location, user, caption;
    private LinearLayout locationLayout, captionLayout, reloadLayout;
    private String photoURL;
    private final String baseURL = "/data/photo/original/";
    private ImageLoader loader;
    private ProgressBar progressBar;
    private FontTextView progressText;
    private FontTextView reloadText;
    private Button reloadBtn;

    private Toolbar toolbar;
    private int percentage = 0;

    HttpURLConnection urlConnection = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        findAllById();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(setup()) {
//            Glide.with(getApplicationContext())
//                    .load(GlobalSocket.serverURL + baseURL + photoURL)
//                    .crossFade()
//                    .into(picture);
            File cachedFile = new File(getCacheDir(), photoURL.split("\\.")[0]);
            if(cachedFile.exists()) {
                //load image from cache
//                Glide.with(getApplicationContext())
//                        .load(cachedFile)
//                        .diskCacheStrategy(DiskCacheStrategy.NONE)
////                        .crossFade()
//                        .into(picture);
                TileBitmapDrawable.attachTileBitmapDrawable(picture, getCacheDir() + "/" + photoURL.split("\\.")[0], null, null);
            } else {
                //download image
                setupTouchListener();
                initImageLoader();
            }
        } else {
            Toast.makeText(getApplicationContext(),"cannot initialize imageviewer (bug ?)",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
//                Toast.makeText(getApplicationContext(),"back",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupTouchListener() {
        if(!picture.hasOnClickListeners()) {
            picture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(),"image clicked !!",Toast.LENGTH_SHORT).show();
                }
            });
        }
        if(!reloadBtn.hasOnClickListeners()) {
            reloadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    initImageLoader();
                }
            });
        }
    }

    private boolean setup() {
        Intent previousIntent = getIntent();
        photoURL = previousIntent.getStringExtra("photoURL");
        caption.setText(previousIntent.getStringExtra("caption"));
        if(caption.getText().equals("")) captionLayout.setVisibility(LinearLayout.GONE);
        deviceName.setText(previousIntent.getStringExtra("vendor") + " " + previousIntent.getStringExtra("model"));
        if(!previousIntent.getStringExtra("exposureTime").equals("")) {
            if(previousIntent.getStringExtra("exposureTime").contains("1/")) {
                exposureTime.setText(previousIntent.getStringExtra("exposureTime"));
            } else if(previousIntent.getStringExtra("exposureTime").contains("/")) {
                String expTime = previousIntent.getStringExtra("exposureTime");
                exposureTime.setText("1/" + (int) (Double.parseDouble(expTime.substring(expTime.indexOf("/") + 1, expTime.length())) / Double.parseDouble(expTime.substring(0, expTime.indexOf("/")))));
            } else {
                exposureTime.setText("1/" + (int) (1.0 / Double.parseDouble(previousIntent.getStringExtra("exposureTime"))));
            }
            if(previousIntent.getStringExtra("aperture").contains("f/")) {
                aperture.setText(previousIntent.getStringExtra("aperture"));
            } else {
                aperture.setText("f/" + previousIntent.getStringExtra("aperture"));
            }
            iso.setText(previousIntent.getStringExtra("iso"));
        }
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.settings_eng_location), true)) {
            if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.settings_local_location), true)) {
                //eng + local
                location.setText(previousIntent.getStringExtra("gpsLocation"));
                if(!previousIntent.getStringExtra("gpsLocalized").equals("")) location.append(" (" + previousIntent.getStringExtra("gpsLocalized") + ")");
            } else {
                //only eng
                location.setText(previousIntent.getStringExtra("gpsLocation"));
            }
        } else {
            //only local
            if(!previousIntent.getStringExtra("gpsLocalized").equals("")) {
                //if any
                location.setText(previousIntent.getStringExtra("gpsLocalized"));
            } else {
                //if none
                location.setText(previousIntent.getStringExtra("gpsLocation"));
            }
        }
        //if no location at all
        if(location.getText().equals("")) locationLayout.setVisibility(LinearLayout.GONE);

        user.setText(previousIntent.getStringExtra("username"));
        return true;
    }

    private void initImageLoader() {
        progressText.setText("connecting...");
        progressBar.setProgress(0);
        reloadBtn.setClickable(false);

        reloadLayout.setVisibility(LinearLayout.GONE);
        picture.setVisibility(TouchImageView.GONE);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        progressText.setVisibility(FontTextView.VISIBLE);

        loader = new ImageLoader();
        loader.execute(baseURL, photoURL, "");
    }

    private void initReload() {
        reloadText.setText("connection error :(");
        reloadBtn.setClickable(true);

        progressBar.setVisibility(ProgressBar.GONE);
        progressText.setVisibility(FontTextView.GONE);
        picture.setVisibility(TouchImageView.GONE);
        reloadLayout.setVisibility(LinearLayout.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        if(loader != null && loader.getStatus() != AsyncTask.Status.FINISHED) loader.cancel(true);
        if(imageBitmap != null) imageBitmap.recycle();
        super.onDestroy();
    }

    private void findAllById() {
        caption = (FontTextView) findViewById(R.id.caption);
        captionLayout = (LinearLayout) findViewById(R.id.caption_layout);
        picture = (TouchImageView) findViewById(R.id.picture);
        deviceName = (FontTextView) findViewById(R.id.device_name);
        exposureTime = (FontTextView) findViewById(R.id.shutter_speed);
        aperture = (FontTextView) findViewById(R.id.aperture);
        iso = (FontTextView) findViewById(R.id.iso);
        location = (FontTextView) findViewById(R.id.location);
        locationLayout = (LinearLayout) findViewById(R.id.location_layout);
        user = (FontTextView) findViewById(R.id.user);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        progressText = (FontTextView) findViewById(R.id.progress_text);

        reloadLayout = (LinearLayout) findViewById(R.id.reload_view);
        reloadText = (FontTextView) findViewById(R.id.reload_text);
        reloadBtn = (Button) findViewById(R.id.reload_button);
    }

    /**<p>ImageLoader is a version3 of ImageLoader which use AyncTask (instead of Thread)</p>
     *
     * <p>params: string[] as follows
     * string[0] = baseURL
     * string[1] = photoURL
     * string[2] = query params (eg. token)</p>
     *
     * <p>return:
     * </p>
     *
     */

    private class ImageLoader extends AsyncTask<String, Integer, String> {
        int fileLength;

        @Override
        protected String doInBackground(String... params) {
            InputStream in = null;
            ByteArrayOutputStream outputStream = null;
            try {
//                URL url = new URL(params[0] + "?_token=" + getSharedPreferences(getString(R.string.userdata), MODE_PRIVATE).getString(getString(R.string.token), ""));
                URL url = new URL(GlobalSocket.serverURL + params[0] + params[1] + params[2]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Accept-Encoding", "gzip");
                urlConnection.connect();
                if(urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return urlConnection.getResponseCode() + "";
                }

                fileLength = urlConnection.getContentLength();

                in = urlConnection.getInputStream();
                outputStream = new ByteArrayOutputStream();

                byte buffer[] = new byte[8192];
                int total = 0;
                int count;
                while((count = in.read(buffer)) != -1) {
                    if(isCancelled()) {
                        urlConnection.disconnect();
                        return "abort";
                    }
                    total += count;
                    outputStream.write(buffer, 0, count);
                    if(fileLength > 0) {
                        publishProgress((int) (total * 100 / fileLength));
                    }
                }
                in.close();
                outputStream.close();
                Log.d("droidphoto", "total:" + total + "|filelength:" + fileLength);
                if(total == fileLength) {
                    //imageBitmap = BitmapFactory.decodeStream(new BufferedInputStream(in));
//                    if(imageBitmap != null) imageBitmap.recycle();
//                    imageBitmap = BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, total);
                    imageByte = outputStream.toByteArray();
                } else {
                }

            } catch (IOException e) {
                e.printStackTrace();
                if(e.toString().contains("ETIMEDOUT")) {
                    return "timeout";
                }
            } finally {
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
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return "ok";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            if(percentage < progress[0]) {
                percentage = progress[0];
                progressBar.setProgress(percentage);
                progressText.setText(percentage + " %");
//                Log.d("droidphoto", "downloaded: " + progress[0] + "%");
            }
        }

        @Override
        protected void onPostExecute(String s) {
            switch (s) {
                case "ok":
                    Log.d("droidphoto", "init cache task for " + photoURL);
//                    String filename = photoURL.substring(0, photoURL.indexOf("."));
                    String filename = photoURL.split("\\.")[0];
                    String cachetablename = "test.e";
                    String loadedImagePath = getCacheDir() + "/" + filename;

                    /*  format
                        1 cacheSize -- total cache size exclude this table file
                        2 filename|size
                        3 filename|size
                        ...
                     */

                    //check cache size
                    int cacheSize = -1;
                    try {
                        File ccf = new File(getCacheDir(), cachetablename);
                        if(ccf.exists()) {
                            BufferedReader br = new BufferedReader(new FileReader(ccf));
                            String read = br.readLine();
                            if(read.isEmpty()) {
                                cacheSize = 0;
                                br.close();
                            } else {
                                cacheSize = Integer.parseInt(read);

                                while(cacheSize + fileLength > ImageViewerActivity.MAX_CACHE_SIZE) {
                                    Log.d("droidphoto", "clearing cache...");
                                    String line = br.readLine();
                                    if(line == null) {
                                        //no more to remove
                                        return;
                                    }
                                    String line2[] = line.split("\\|");
                                    Log.d("droidphoto", "removed size: " + line2[1]);
                                    cacheSize -= Integer.parseInt(line2[1]);
                                    File deleted = new File(getCacheDir(), line2[0]);
                                    if(!deleted.delete()) {
                                        Log.e("droidphoto", "error removing cached image");
                                    }
                                }
                                cacheSize += fileLength;
                                BufferedWriter bw = new BufferedWriter(new FileWriter(new File(getCacheDir(), cachetablename + ".tmp")));
                                bw.write("" + cacheSize);
                                String value;
                                while((value = br.readLine()) != null) {
                                    bw.write("\n" + value);
                                }
                                bw.close();
                                br.close();

                                File oldf = new File (getCacheDir(), cachetablename);
                                File newf = new File (getCacheDir(), cachetablename + ".tmp");
                                if(!oldf.delete()) {
                                    Log.e("droidphoto", "error removing old table");
                                }
                                if(!newf.renameTo(oldf)) {
                                    Log.e("droidphoto", "error replacing new table");
                                }
                            }
                        } else {
                            cacheSize = fileLength;
                            FileOutputStream fos = new FileOutputStream(ccf);
                            fos.write((cacheSize + "").getBytes());
                            fos.close();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if(cacheSize == -1) {
                        Toast.makeText(getApplicationContext(), "bug!? please report", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    //save file to cache
                    try {
                        FileOutputStream fos = new FileOutputStream(loadedImagePath);
                        fos.write(imageByte);
                        fos.close();
                        //update table
                        BufferedWriter fw = new BufferedWriter(new FileWriter(new File(getCacheDir(), cachetablename), true));
                        //if new file
                        if(cacheSize == 0) {
                            cacheSize += fileLength;
                            fw.write("" + cacheSize);
                        }
                        fw.write("\n" + filename + "|" + fileLength);
                        fw.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(ProgressBar.GONE);
                    progressText.setVisibility(FontTextView.GONE);
//                    Glide.with(getApplicationContext())
//                            .load(imageByte)
//                            .diskCacheStrategy(DiskCacheStrategy.NONE)
//                            .crossFade()
//                            .into(picture);

                    TileBitmapDrawable.attachTileBitmapDrawable(picture, getCacheDir() + "/" + photoURL.split("\\.")[0], null, null);
                    picture.setVisibility(TouchImageView.VISIBLE);
                    break;
                case "timeout":
                    initReload();
                    break;
                case "abort":
                    break;
            }
        }
    }
}

