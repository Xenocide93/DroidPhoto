package com.droidsans.photo.droidphoto;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
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

import com.droidsans.photo.droidphoto.util.view.FontTextView;

import java.io.File;
import java.io.IOException;


public class DeviceCameraCalibrationActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA = 8192;

    private Toolbar toolbar;

    private LinearLayout initCalibrateLayout;
    private Button startCalibrate, doneCalibrate;
    private ImageView topIcon;
    private ProgressBar working;
    private FontTextView title, description;

    private File calibrateFile;

    private Handler delayAction = new Handler();
    private Runnable changeState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_camera_calibration);

        findAllById();
        setupListener();

        setSupportActionBar(toolbar);
        showInit();
    }

    private void showInit() {
        doneCalibrate.setVisibility(Button.GONE);
        startCalibrate.setVisibility(Button.VISIBLE);
        startCalibrate.setClickable(true);
        working.setVisibility(ProgressBar.GONE);
        topIcon.setImageResource(R.drawable.ic_camera_alt_black_48dp);
        topIcon.setColorFilter(getResources().getColor(R.color.light_gray));
        topIcon.setVisibility(ImageView.VISIBLE);
        title.setText("Welcome !");
        description.setText("Please calibrate before using our app.\nYou just have to take a photo and\nour application will do the rest.\n\nDon't worry, the taken photo will be automatically deleted about seconds after.\nThe image quality isn't even matter,\njust launch a camera and take a shot !");
    }

    private void showRetry() {
        doneCalibrate.setVisibility(Button.GONE);
        startCalibrate.setText("Retry");
        startCalibrate.setVisibility(Button.VISIBLE);
        startCalibrate.setClickable(true);
        working.setVisibility(ProgressBar.GONE);
        topIcon.setImageResource(R.drawable.ic_cancel_black_48dp);
        topIcon.setColorFilter(getResources().getColor(R.color.primary_dark_color));
        topIcon.setVisibility(ImageView.VISIBLE);
        title.setText("Oops !");
        description.setText("");
    }

    private void showWorking() {
        doneCalibrate.setVisibility(Button.GONE);
        startCalibrate.setVisibility(Button.INVISIBLE);
        startCalibrate.setClickable(false);
        topIcon.setVisibility(ImageView.GONE);
        working.setVisibility(ProgressBar.VISIBLE);
        title.setText("Working ...");
        description.setText("\nPlease wait just a second.\nWe are working on configuring our application.");
    }

    private void showSuccess() {
        startCalibrate.setVisibility(Button.GONE);
        doneCalibrate.setVisibility(Button.VISIBLE);
        working.setVisibility(ProgressBar.GONE);
        topIcon.setImageResource(R.drawable.ic_check_circle_white_48dp);
        topIcon.setColorFilter(getResources().getColor(R.color.accent_color));
        topIcon.setVisibility(ImageView.VISIBLE);
        title.setText("Completed");
        description.setText("We have successfully calibrated our application to match your device camera configuration.\nThe taken photo is also removed just now.\nPlease enjoy !");
    }

    private void setupListener() {
        startCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calibrateFile = new File(getExternalCacheDir(), "calibrate.tmp");
                removeTemp();
                try {
                    if(calibrateFile.createNewFile()) {
                        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        camera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(calibrateFile));
                        startActivityForResult(camera, REQUEST_CAMERA);
                    } else {
                        Snackbar.make(startCalibrate, "cannot create temp file.", Snackbar.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        doneCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                    showWorking();
                    getExifData();
                    break;
                default:
                    showRetry();
                    break;
            }
        } else {
            Snackbar.make(startCalibrate, "cancel.", Snackbar.LENGTH_SHORT).show();
            removeTemp();
        }
//        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getExifData() {
        try {
            ExifInterface mExif = new ExifInterface(calibrateFile.getAbsolutePath());
            String make = mExif.getAttribute(ExifInterface.TAG_MAKE).trim().replaceAll("[ -]", "");
            String model = mExif.getAttribute(ExifInterface.TAG_MODEL).trim().replaceAll("[ -]", "");

            Log.d("droidphoto", "calibrate make: " + make);
            Log.d("droidphoto", "calibrate model: " + model);

            getSharedPreferences(getString(R.string.device_data), MODE_PRIVATE).edit()
                    .putString(getString(R.string.camera_make), make)
                    .putString(getString(R.string.camera_model), model)
                    .apply();

            removeTemp();
            delayAction.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showSuccess();
                }
            }, 1000);
        } catch (IOException e) {
            e.printStackTrace();
            delayAction.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showRetry();
                }
            }, 1000);

        }
    }

    private void removeTemp() {
        if(calibrateFile != null && calibrateFile.exists()) {
            if(!calibrateFile.delete()) {
                Log.d("droidphoto", "cannot delete calibrate file ??");
            }
        }
    }

    private void findAllById() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        initCalibrateLayout = (LinearLayout) findViewById(R.id.calibration_layout);
        startCalibrate = (Button) findViewById(R.id.button_camera_launch);
        doneCalibrate = (Button) findViewById(R.id.button_done);
        topIcon = (ImageView) findViewById(R.id.calibrate_icon);
        working = (ProgressBar) findViewById(R.id.calibrate_working);
        title = (FontTextView) findViewById(R.id.calibrate_title);
        description = (FontTextView) findViewById(R.id.calibrate_description);
    }

    @Override
    protected void onDestroy() {
        removeTemp();
        super.onDestroy();
    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_device_camera_calibration, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }
}
