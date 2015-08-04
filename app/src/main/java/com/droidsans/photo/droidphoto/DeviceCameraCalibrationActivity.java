package com.droidsans.photo.droidphoto;

import android.content.Intent;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.droidsans.photo.droidphoto.util.StringUtil;
import com.droidsans.photo.droidphoto.util.view.FontTextView;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;


public class DeviceCameraCalibrationActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA = 8192;

    private Toolbar toolbar;

    private SurfaceView surfaceCamera;
    private LinearLayout initCalibrateLayout;
    private Button startCalibrate, doneCalibrate;
    private ImageView topIcon;
    private ProgressBar working;
    private FontTextView title, description;

    private File calibrateFile;
    private Camera mCamera;

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
        doneCalibrate.setVisibility(View.GONE);
        startCalibrate.setVisibility(View.VISIBLE);
        startCalibrate.setClickable(true);
        working.setVisibility(View.GONE);
        surfaceCamera.setVisibility(View.VISIBLE);
        topIcon.setImageResource(R.drawable.ic_camera_alt_black_48dp);
        topIcon.setVisibility(View.VISIBLE);
        title.setText(getString(R.string.calibrate_text_title_welcome));
        description.setText(getString(R.string.calibrate_text_desc_welcome));

        removeTemp();
    }

    private void showRetry() {
        doneCalibrate.setVisibility(View.GONE);
        startCalibrate.setText("Retry");
        startCalibrate.setVisibility(View.VISIBLE);
        startCalibrate.setClickable(true);
        working.setVisibility(View.GONE);
        surfaceCamera.setVisibility(View.GONE);
        topIcon.setImageResource(R.drawable.ic_cancel_black_48dp);
        topIcon.setColorFilter(getResources().getColor(R.color.primary_dark_color));
        topIcon.setVisibility(View.VISIBLE);
        title.setText(getString(R.string.calibrate_text_title_error));
        description.setText(getString(R.string.calibrate_text_desc_error));

        removeTemp();
    }

    private void showWorking() {
        doneCalibrate.setVisibility(View.GONE);
        startCalibrate.setVisibility(View.INVISIBLE);
        startCalibrate.setClickable(false);
        surfaceCamera.setVisibility(View.GONE);
        topIcon.setVisibility(View.GONE);
        working.setVisibility(View.VISIBLE);
        title.setText(getString(R.string.calibrate_text_title_working));
        description.setText(getString(R.string.calibrate_text_desc_working));
    }

    private void showSuccess() {
        surfaceCamera.setVisibility(View.GONE);
        startCalibrate.setVisibility(View.GONE);
        doneCalibrate.setVisibility(View.VISIBLE);
        working.setVisibility(View.GONE);
        surfaceCamera.setVisibility(View.GONE);
        topIcon.setImageResource(R.drawable.ic_check_circle_white_48dp);
        topIcon.setColorFilter(getResources().getColor(R.color.accent_color));
        topIcon.setVisibility(View.VISIBLE);
        title.setText(getString(R.string.calibrate_text_title_done));
        description.setText(getString(R.string.calibrate_text_desc_done));

        removeTemp();
    }

    private void setupListener() {
        startCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        showWorking();
                        OutputStream os;
                        calibrateFile = new File(getCacheDir(), "calibrate.tmp");
                        Uri uri = Uri.fromFile(calibrateFile);
                        try {
                            os = getContentResolver().openOutputStream(uri);
                            os.write(data);
                            os.flush();
                            os.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            ExifInterface mExif = new ExifInterface(calibrateFile.getAbsolutePath());
                            if (mExif.getAttribute(ExifInterface.TAG_MAKE) == null || mExif.getAttribute(ExifInterface.TAG_MODEL) == null) {
                                showRetry();
                                return;
                            }
                            String make = StringUtil.wrapBlank(mExif.getAttribute(ExifInterface.TAG_MAKE)).replaceAll("[ -]", "");
                            String model = StringUtil.wrapBlank(mExif.getAttribute(ExifInterface.TAG_MODEL)).replaceAll("[ -]", "");

                            Log.d("droidphoto", "calibrate make: " + make);
                            Log.d("droidphoto", "calibrate model: " + model);

                            getSharedPreferences(getString(R.string.device_data), MODE_PRIVATE).edit()
                                    .putString(getString(R.string.camera_make), make)
                                    .putString(getString(R.string.camera_model), model)
                                    .apply();

                            delayAction.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    showSuccess();
                                }
                            }, 2000);
                        } catch (IOException e) {
                            e.printStackTrace();
                            delayAction.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    showRetry();
                                }
                            }, 2000);

                        }
                    }
                });
            }
        });

        doneCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });

        surfaceCamera.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceCamera.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    mCamera.setPreviewDisplay(surfaceCamera.getHolder());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//                Camera.Parameters params = mCamera.getParameters();
//                List<Camera.Size> previewSize = params.getSupportedPreviewSizes();
//                List<Camera.Size> pictureSize = params.getSupportedPictureSizes();
//                params.setJpegQuality(2);
//                mCamera.setParameters(params);
                try {
                    mCamera.setPreviewDisplay(surfaceCamera.getHolder());
                    mCamera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera.release();
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

        surfaceCamera = (SurfaceView) findViewById(R.id.surface_camera);
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
        surfaceCamera.destroyDrawingCache();
        super.onDestroy();
    }
}
