package com.droidsans.photo.droidphoto;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.droidsans.photo.droidphoto.util.PicturePack;
import com.droidsans.photo.droidphoto.util.transform.CircleTransform;
import com.droidsans.photo.droidphoto.util.view.FontTextView;
import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class ImageViewerActivity extends AppCompatActivity {
    private static final int MAX_CACHE_SIZE = 1024*1024*75; //75MB
    public static final String CACHE_FILE_NAME = "cacheFileName";

    private ImageView picture, avatar, zoom;
    private LinearLayout enhanced;
    private byte imageByte[];
    private FontTextView deviceName, exposureTime, aperture, iso, location, user, caption, submit;
    private LinearLayout locationLayout, captionLayout, reloadLayout;
    private String photoURL;
    private final String baseURL = "/data/photo/original/";
    private ImageLoader loader;
    private ProgressBar progressBar, loadingProgressBar;
    private FontTextView progressText;
    private FontTextView reloadText;
    private Button reloadBtn;
//    private Intent previousIntent;
    private PicturePack pack;
    private Boolean isLikeStateChange = false;
    private int likeCountInt;

    private LinearLayout likeLayout;
    private ImageView likeIcon;
    private TextView likeCount;

    private Toolbar toolbar;
    private int percentage = -1;

    HttpURLConnection urlConnection = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        findAllById();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(setup()) {
            loadImage();
        } else {
            Toast.makeText(getApplicationContext(), "cannot initialize imageviewer (bug ?)", Toast.LENGTH_LONG).show();
        }

        setupListener();
    }

    private void setupListener() {
        setupLikeButtonListener();

        View.OnClickListener launchProfileViewerOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileViewerActivity.launchProfileViewer(getApplicationContext(), pack.userId);
            }
        };

        user.setOnClickListener(launchProfileViewerOnClick);
        avatar.setOnClickListener(launchProfileViewerOnClick);
    }

    private void loadImage(){
//            Glide.with(getApplicationContext())
//                    .load(GlobalSocket.serverURL + baseURL + photoURL)
//                    .crossFade()
//                    .into(picture);
        Log.d(getString(R.string.app_name), "photoURL: " + photoURL);
        String hash = photoURL.substring(0, photoURL.lastIndexOf("."));
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(hash.getBytes());
            hash = bytesToHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
//            File cachedFile = new File(getCacheDir(), photoURL.substring(0, photoURL.lastIndexOf(".")));
        File cachedFile = new File(getCacheDir(), hash);
        if(cachedFile.exists()) {
            //load image from cache
            Glide.with(getApplicationContext())
                    .load(cachedFile)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                        .crossFade()
                    .into(picture);
//                TileBitmapDrawable.attachTileBitmapDrawable(picture, getCacheDir() + "/" + photoURL.split("\\.")[0], null, null);
            setupPictureClickListener();

            postImageLoaded();
        } else {
            //download image
            setupReloadButtonListener();
            initImageLoader();
        }
    }

    private void setupLikeButtonListener() {
        likeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pack.isLike) {
                    pack.isLike = true;
                    isLikeStateChange = true;
                    likeCountInt++;
                    setLikeButtonUI(true, likeCountInt);
                    JSONObject data = new JSONObject();
                    try {
                        data.put("photo_id", pack.photoId);
                        data.put("_event", "onLikeRespond");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    GlobalSocket.globalEmit("photo.like", data);
                    GlobalSocket.mSocket.on("onLikeRespond", new Emitter.Listener() {
                        @Override
                        public void call(final Object... args) {
                            GlobalSocket.mSocket.off("onLikeRespond");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    JSONObject returnData = (JSONObject) args[0];
                                    if (returnData.optBoolean("success")) {
                                        Toast.makeText(getApplicationContext(), "Like!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        likeCountInt--;
                                        isLikeStateChange = false;
                                        setLikeButtonUI(false, likeCountInt);
                                        Toast.makeText(getApplicationContext(), "Please try again later", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                } else {
                    pack.isLike = false;
                    isLikeStateChange = true;
                    likeCountInt--;
                    setLikeButtonUI(false, likeCountInt);
                    JSONObject data = new JSONObject();
                    try {
                        data.put("photo_id", pack.photoId);
                        data.put("_event", "onUnlikeRespond");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    GlobalSocket.globalEmit("photo.unlike", data);
                    GlobalSocket.mSocket.on("onUnlikeRespond", new Emitter.Listener() {
                        @Override
                        public void call(final Object... args) {
                            GlobalSocket.mSocket.off("onUnlikeRespond");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    JSONObject returnData = (JSONObject) args[0];
                                    if (returnData.optBoolean("success")) {
                                        Toast.makeText(getApplicationContext(), "Unlike!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        likeCountInt++;
                                        isLikeStateChange = false;
                                        setLikeButtonUI(false, likeCountInt);
                                        Toast.makeText(getApplicationContext(), "Please try again later", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(getSharedPreferences(getString(R.string.userdata), MODE_PRIVATE).getInt(getString(R.string.user_priviledge), 1) > 1) {
//        if(true){
            getMenuInflater().inflate(R.menu.menu_image_viewer_mod, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_no_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case android.R.id.home:
                if(isLikeStateChange){
                    Intent returnData = new Intent();
                    returnData.putExtra("isLike", pack.isLike);
                    returnData.putExtra("likeCount", likeCountInt);
                    returnData.putExtra("photo_id", pack.photoId);
                    returnData.putExtra("position", getIntent().getIntExtra("position", -1));
                    setResult(RESULT_OK, returnData);
                }
                finish();
//                Toast.makeText(getApplicationContext(),"back",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_settings:
                return true;
            case R.id.action_hide_pic:
                new AlertDialog.Builder(ImageViewerActivity.this)
                        .setTitle("Hide this photo")
                        .setMessage("are you sure ?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(!GlobalSocket.mSocket.hasListeners("hide_photo")) {
                                    GlobalSocket.mSocket.on("hide_photo", new Emitter.Listener() {
                                        @Override
                                        public void call(final Object... args) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    GlobalSocket.mSocket.off("hide_photo");
                                                    JSONObject data = (JSONObject) args[0];
                                                    if(data.optBoolean("success")) {
                                                        Log.d("droidphoto", "removed");
                                                        Toast.makeText(getApplicationContext(), "removed", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Log.d("droidphoto", "error " + data.optString("msg"));
                                                        Toast.makeText(getApplicationContext(), "error : " + data.optString("msg"), Toast.LENGTH_SHORT).show();
                                                    }
                                                    finish();
                                                }
                                            });
                                        }
                                    });
                                }
                                JSONObject send = new JSONObject();
                                try {
                                    send.put("photo_id", pack.photoId);
                                    send.put("user_id", pack.userId);
                                    send.put("_event", "hide_photo");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                GlobalSocket.globalEmit("photo.hide", send);
//                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
//                            .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Log.d("DroidShot", "I was here 1");
        if(isLikeStateChange){
            Log.d("DroidShot", "I was here 2");
            Intent returnData = new Intent();
            returnData.putExtra("isLike", pack.isLike);
            returnData.putExtra("likeCount", likeCountInt);
            returnData.putExtra("photo_id", pack.photoId);
            Log.d(getString(R.string.app_name), "photoId1: " + pack.photoId);
            returnData.putExtra("position", getIntent().getIntExtra("position", -1));
            setResult(RESULT_OK, returnData);
        }
        finish();
    }

    private void postImageLoaded() {
        //photo view count
        String photoId = pack.photoId;
        JSONObject send = new JSONObject();
        try {
            send.put("photo_id", photoId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        GlobalSocket.globalEmit("photo.view", send);

        //show zoom button after 3 sec
        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                zoom.animate().alpha(0.5f).setDuration(300).start();
            }
        }, 3000);
    }

    private void setupPictureClickListener() { //called nly when picture is finish downloaded
        if(!picture.hasOnClickListeners()) {
            picture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent fullscreenViewerIntent = new Intent(getApplicationContext(), ImageViewerFullScreenActivity.class);
                    fullscreenViewerIntent.putExtra(CACHE_FILE_NAME, photoURL.substring(0, photoURL.lastIndexOf(".")));
                    fullscreenViewerIntent.putExtra("username", getIntent().getStringExtra("username"));
                    startActivity(fullscreenViewerIntent);
                }
            });
        } 
    }

    private void setupReloadButtonListener(){
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
//        previousIntent = getIntent();
        pack = (PicturePack) getIntent().getSerializableExtra("pack");
        
        photoURL = pack.photoURL;
        caption.setText(pack.caption);
        if(caption.getText().equals("")) captionLayout.setVisibility(LinearLayout.GONE);
        deviceName.setText(pack.getDeviceName());
        if(!pack.shutterSpeed.equals("")) {
            Log.i(getClass().getSimpleName(), "shutterSpeed: " + pack.shutterSpeed);
            if(pack.shutterSpeed.equals("0")) {
                exposureTime.setText("---");
            } else if (!pack.shutterSpeed.contains("/") && !pack.shutterSpeed.substring(0,1).equals("0")) { // 4
                exposureTime.setText(pack.shutterSpeed + " s");
            } else if (pack.shutterSpeed.substring(0, 2).contains("1/")) { // 1/3
                exposureTime.setText(pack.shutterSpeed);
            } else if (pack.shutterSpeed.contains("/")) { //60001/100000
                String expTime = pack.shutterSpeed;
                exposureTime.setText("1/" + (int) (Double.parseDouble(expTime.substring(expTime.indexOf("/") + 1, expTime.length())) / Double.parseDouble(expTime.substring(0, expTime.indexOf("/")))));
            } else { // 0.2234235
                exposureTime.setText("1/" + (int) (1.0 / Double.parseDouble(pack.shutterSpeed)));
            }
        } else {
            exposureTime.setText("---");
        }
        if(!pack.aperture.equals("")) {
            if (pack.aperture.contains("f/")) {
                aperture.setText(pack.aperture);
            } else if(pack.aperture.length() == 1) {
                aperture.setText("f/" + pack.aperture + ".0");
            } else {
                aperture.setText("f/" + pack.aperture);
            }
        } else {
            aperture.setText("---");
        }
        if(pack.iso.equals("")) {
            iso.setText("---");
        } else if(!pack.iso.equals("0")) {
            iso.setText(pack.iso);
        } else {
            iso.setText("---");
        }

        if(pack.isEnhanced){
            enhanced.setVisibility(View.VISIBLE);
        } else {
            enhanced.setVisibility(View.GONE);
        }
        
        likeCountInt = pack.likeCount;
        setLikeButtonUI(pack.isLike, likeCountInt);

        Log.d("DroidShot", "ImageViewerActivity: previousIntent: isLike: " + pack.isLike);

        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.settings_eng_location), true)) {
            if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.settings_local_location), true)) {
                //eng + local
                location.setText(pack.gpsLocation);
                if(!pack.gpsLocalizedLocation.equals("")) location.append(" (" + pack.gpsLocalizedLocation + ")");
            } else {
                //only eng
                location.setText(pack.gpsLocation);
            }
        } else {
            //only local
            if(!pack.gpsLocalizedLocation.equals("")) {
                //if any
                location.setText(pack.gpsLocalizedLocation);
            } else {
                //if none
                location.setText(pack.gpsLocation);
            }
        }
        //if no location at all
        if(location.getText().equals("")) locationLayout.setVisibility(LinearLayout.GONE);

        user.setText(pack.username);
        Glide.with(getApplicationContext())
                .load(GlobalSocket.serverURL + ProfileFragment.baseURL + pack.avatarURL)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .fitCenter()
                .transform(new CircleTransform(getApplicationContext()))
                .placeholder(R.drawable.avatar_placeholder_300)
                .into(avatar);

        //date parsing
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date submitDate = format.parse(pack.submitDate);
            Date now = new Date();
            Log.d("droidphoto", "submit time : " + submitDate.toString());
            Log.d("droidphoto", "now : " + now.toString());
            long diff = now.getTime() - submitDate.getTime();
            String unit;
            if((diff/1000) < 60) {//seconds
                diff = diff/1000;
                unit = getString(R.string.imageviewer_unit_second); // วินาที
            } else if(diff/(1000*60) < 60) { //minutes
                diff = diff/(1000*60);
                unit = getString(R.string.imageviewer_unit_minute); // นาที
            } else if(diff/(1000*60*60) < 24) { //hours
                diff = diff/(1000*60*60);
                unit = getString(R.string.imageviewer_unit_hour); // ชม.
            } else {
                diff = diff/(1000*60*60*24);
                unit = getString(R.string.imageviewer_unit_day); // วัน
            }
            submit.setText(" " + diff + unit);
//            Log.d("droidphoto", "diff : " + diff + unit);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return true;
    }

    private void initImageLoader() {
        progressText.setText(getString(R.string.imageviewer_progressbar_text_connecting));
        progressBar.setProgress(0);
        loadingProgressBar.setVisibility(ProgressBar.VISIBLE);
        reloadBtn.setClickable(false);

        reloadLayout.setVisibility(LinearLayout.GONE);
        picture.setVisibility(ImageView.GONE);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        progressText.setVisibility(FontTextView.VISIBLE);

        loader = new ImageLoader();
        loader.execute(baseURL, photoURL, "");

    }

    private void initReload() {
        reloadText.setText("connection error :(");
        reloadBtn.setClickable(true);
        loadingProgressBar.setVisibility(ProgressBar.GONE);
        progressBar.setVisibility(ProgressBar.GONE);
        progressText.setVisibility(FontTextView.GONE);
        picture.setVisibility(ImageView.GONE);
        reloadLayout.setVisibility(LinearLayout.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        if(loader != null && loader.getStatus() != AsyncTask.Status.FINISHED) loader.cancel(true);
        GlobalSocket.mSocket.off("hide_photo");
        GlobalSocket.mSocket.off("onLikeRespond");
        GlobalSocket.mSocket.off("onUnlikeRespond");
        super.onDestroy();
    }

    private void findAllById() {
        caption = (FontTextView) findViewById(R.id.caption);
        captionLayout = (LinearLayout) findViewById(R.id.caption_layout);
        picture = (ImageView) findViewById(R.id.picture);
        deviceName = (FontTextView) findViewById(R.id.device_name);
        exposureTime = (FontTextView) findViewById(R.id.shutter_speed);
        aperture = (FontTextView) findViewById(R.id.aperture);
        iso = (FontTextView) findViewById(R.id.iso);
        location = (FontTextView) findViewById(R.id.location);
        locationLayout = (LinearLayout) findViewById(R.id.location_layout);
        user = (FontTextView) findViewById(R.id.user);
        avatar = (ImageView) findViewById(R.id.avatar);
        submit = (FontTextView) findViewById(R.id.submit_date);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        loadingProgressBar = (ProgressBar) findViewById(R.id.progress_loading);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        progressText = (FontTextView) findViewById(R.id.progress_text);

        reloadLayout = (LinearLayout) findViewById(R.id.reload_view);
        reloadText = (FontTextView) findViewById(R.id.reload_text);
        reloadBtn = (Button) findViewById(R.id.reload_button);
        enhanced = (LinearLayout) findViewById(R.id.enhanced);
        zoom = (ImageView) findViewById(R.id.zoom);

        likeLayout = (LinearLayout) findViewById(R.id.like_btn);
        likeIcon = (ImageView) findViewById(R.id.like_icon);
        likeCount = (TextView) findViewById(R.id.like_count);
    }

    private void setLikeButtonUI(boolean isLike, int count){
        if(isLike){
            likeIcon.setImageResource(R.drawable.ic_favorite_primary_24dp);
        } else {
            likeIcon.setImageResource(R.drawable.ic_favorite_outline_primary_24dp);
        }
        likeCount.setText(count + "");
    }

    private String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
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
                URL url = new URL(GlobalSocket.serverURL + params[0] + URLEncoder.encode(params[1] + params[2], "utf-8").replace("+", "%20"));
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Accept-Encoding", "gzip");
                try {
                    urlConnection.connect();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    return "no connection";
                }
                if(urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return urlConnection.getResponseCode() + "";
                }
                fileLength = urlConnection.getContentLength();

                in = urlConnection.getInputStream();
                outputStream = new ByteArrayOutputStream();
                publishProgress(-1);

                byte buffer[] = new byte[4096];
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
                } else if(fileLength == -1) {
                    fileLength = total;
                    imageByte = outputStream.toByteArray();
                } else {
                    //shouldn't be here
                }


            } catch (IOException e) {
                e.printStackTrace();
                if(e.toString().contains("ETIMEDOUT")) {
                    return "timeout";
                } else if(e.toString().contains("ECONNRESET")) {
                    return "connection reset";
                } else {
                    return "unknown error";
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
            if(progress[0] == -1) {
                loadingProgressBar.setVisibility(ProgressBar.GONE);
            } else if(percentage < progress[0]) {
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
//                    Log.d("droidphoto", "init cache task for " + photoURL);
                    String filename = photoURL.substring(0, photoURL.lastIndexOf("."));
//                    String filename = photoURL.split("\\.")[0];
                    String cachetablename = getString(R.string.cache_table_name);
                    //encoding to SHA1
                    try {
                        MessageDigest md = MessageDigest.getInstance("MD5");
                        md.update(filename.getBytes());
                        filename = bytesToHex(md.digest());
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }

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
                            if(read == null || read.isEmpty()) {
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
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(ProgressBar.GONE);
                    progressText.setVisibility(FontTextView.GONE);
                    Glide.with(getApplicationContext())
                            .load(getCacheDir() + "/" + filename)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .crossFade()
                            .into(picture);

//                    TileBitmapDrawable.attachTileBitmapDrawable(picture, getCacheDir() + "/" + photoURL.split("\\.")[0], null, null);

                    setupPictureClickListener();
                    picture.setVisibility(ImageView.VISIBLE);

                    postImageLoaded();

                    break;
                case "timeout":
                    initReload();
                    break;
                case "no connection":
                    initReload();
                    break;
                case "connection reset":
                    initReload();
                    break;
                case "unknown error":
                    Log.e("droidphoto", "unknown error: see printstacktrace above");
                    initReload();
                    break;
                case "abort":
                    break;
            }
        }
    }
}

