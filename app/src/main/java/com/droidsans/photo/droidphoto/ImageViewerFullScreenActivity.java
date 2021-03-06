package com.droidsans.photo.droidphoto;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.droidsans.photo.droidphoto.util.view.FontTextView;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ImageViewerFullScreenActivity extends AppCompatActivity {
    private String cacheFileName;
    private SubsamplingScaleImageView picture;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        super.setTheme(android.support.v7.appcompat.R.style.Theme_AppCompat_NoActionBar);
//        setTheme(android.support.v7.appcompat.R.style.Theme_AppCompat_Light_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer_full_screen);

        initialize();
    }

    private void initialize() {
        setupToolbar();
        setupPicture();
    }

    private void setupPicture() {
        picture = (SubsamplingScaleImageView) findViewById(R.id.picture);
//        View rootView = findViewById(R.id.root_view);

        Intent oldIntent = getIntent();
        cacheFileName = oldIntent.getStringExtra(ImageViewerActivity.CACHE_FILE_NAME);

        FontTextView credit = (FontTextView) findViewById(R.id.credit);
        credit.setText(getString(R.string.fullscreen_imageviewer_credit) + getIntent().getStringExtra("username"));

        String hash = cacheFileName;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(hash.getBytes());
            hash = bytesToHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        File cacheFile = new File(getCacheDir(), hash);
        if(cacheFile.exists()) {
            picture.setImage(ImageSource.uri(cacheFile.getAbsolutePath()));
            picture.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
        } else {
//            Snackbar.make(picture , "Error: picture is not finish loading", Snackbar.LENGTH_LONG)
//                    .setAction("Retry", new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            setupPicture();
//                        }
//                    })
//                    .show();
        }
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        for(int i = 0; i < toolbar.getChildCount(); i++) {
//            View v = toolbar.getChildAt(i);
//            if(v instanceof ImageButton) {
//                ((ImageButton) v).getDrawable().setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY));
//            }
//        }

        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setTitle("");
        getSupportActionBar().setTitle("");

        //hide status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LOW_PROFILE);
//        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LOW_PROFILE);
//        } else if(Build.VERSION.SDK_INT >= 19) {
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE);
//        } else {
//            Log.e("droidphoto", "SDK version bug ?? | SDK: " + Build.VERSION.SDK_INT);
//        }
    }

    private String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_no_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        picture.recycle();
        super.onDestroy();
    }
}
