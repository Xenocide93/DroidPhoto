package com.droidsans.photo.droidphoto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.droidsans.photo.droidphoto.util.FontTextView;
import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.droidsans.photo.droidphoto.util.PicturePack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class ImageViewerActivity extends AppCompatActivity {

    private ImageView picture;
    private Bitmap imageBitmap;
    private FontTextView deviceName, exposureTime, aperture, iso, location, user, caption;
    private LinearLayout locationLayout, captionLayout;
    private String photoURL;
    private final String baseURL = "http://209.208.65.102/data/photo/original/";

    private Toolbar toolbar;

    private int percentage = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        findAllById();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(setup()) {
            Glide.with(getApplicationContext())
                    .load(baseURL + photoURL)
//                    .placeholder(android.)
                    .crossFade()
                    .into(picture);
        } else {
            Toast.makeText(getApplicationContext(),"cannot initialize imageviewer",Toast.LENGTH_LONG).show();
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

    private boolean setup() {
        Intent previousIntent = getIntent();
        photoURL = previousIntent.getStringExtra("photoURL");
        caption.setText(previousIntent.getStringExtra("caption"));
        if(caption.getText().equals("")) captionLayout.setVisibility(LinearLayout.GONE);
        deviceName.setText(previousIntent.getStringExtra("vendor") + " " + previousIntent.getStringExtra("model"));
        if(!previousIntent.getStringExtra("exposureTime").equals("")) {
            if(previousIntent.getStringExtra("exposureTime").contains("1/")) {
                exposureTime.setText(previousIntent.getStringExtra("exposureTime"));
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
//        String gpsLat = previousIntent.getStringExtra("gpsLat");
//        String gpsLong = previousIntent.getStringExtra("gpsLong");
//        if(gpsLat == null || gpsLong == null) {
//            locationLayout.setVisibility(LinearLayout.GONE);
//        } else {
//            location.setText("TH");
//        }
        location.setText("TH"); //debug
        user.setText(previousIntent.getStringExtra("username"));
        return true;
    }

    @Override
    protected void onDestroy() {
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

        toolbar = (Toolbar) findViewById(R.id.toolbar);
    }

}

