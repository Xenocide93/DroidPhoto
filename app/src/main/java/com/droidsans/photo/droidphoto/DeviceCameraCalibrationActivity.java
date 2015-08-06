package com.droidsans.photo.droidphoto;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.droidsans.photo.droidphoto.util.StringUtil;
import com.droidsans.photo.droidphoto.util.view.FontTextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class DeviceCameraCalibrationActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA = 8192;

    private Toolbar toolbar;

    //private SurfaceView surfaceCamera;
    private LinearLayout initCalibrateLayout;
    private Button startCalibrate, doneCalibrate;
    private ImageView topIcon;
    private ProgressBar working;
    private FontTextView title, description;

    private File calibrateFile;
    private boolean isInitial;
    private String packageName = "no package";
    //private Camera mCamera;

    private Handler delayAction = new Handler();
    private Runnable delaySuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_camera_calibration);

        findAllById();
        setupListener();

        setSupportActionBar(toolbar);

        if(
//                getSharedPreferences(getString(R.string.device_data), MODE_PRIVATE).getString(getString(R.string.camera_app_name), null) == null ||
                getSharedPreferences(getString(R.string.device_data), MODE_PRIVATE).getString(getString(R.string.camera_make), null) == null ||
                getSharedPreferences(getString(R.string.device_data), MODE_PRIVATE).getString(getString(R.string.camera_model), null) == null) {
            showInit();
            isInitial = true;
        } else {
            showAddMore();
            isInitial = false;
            if(getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }
    }

    private void showInit() {
        doneCalibrate.setVisibility(View.GONE);
        startCalibrate.setVisibility(View.VISIBLE);
        startCalibrate.setClickable(true);
        working.setVisibility(View.GONE);
//        surfaceCamera.setVisibility(View.VISIBLE);
        topIcon.setImageResource(R.drawable.ic_camera_alt_black_48dp);
        topIcon.setColorFilter(getResources().getColor(R.color.global_text_black));
        topIcon.setVisibility(View.VISIBLE);
        title.setText(getString(R.string.calibrate_text_title_welcome));
        description.setText(getString(R.string.calibrate_text_desc_welcome));

        removeTemp();
    }

    private void showAddMore() {
        showInit();
        startCalibrate.setText(R.string.calibrate_button_add);
        description.setText(R.string.calibrate_text_desc_add);
//        String applist[] = getSharedPreferences(getString(R.string.device_data), MODE_PRIVATE).getString(getString(R.string.camera_app_name), "").split(",");
//        for(String app : applist) {
//            description.append("\r\n- " + app);
//        }
    }

    private void showRetry() {
        doneCalibrate.setVisibility(View.GONE);
        startCalibrate.setText(R.string.calibrate_button_retry);
        startCalibrate.setVisibility(View.VISIBLE);
        startCalibrate.setClickable(true);
        working.setVisibility(View.GONE);
//        surfaceCamera.setVisibility(View.GONE);
        topIcon.setImageResource(R.drawable.ic_cancel_black_48dp);
        topIcon.setColorFilter(getResources().getColor(R.color.primary_dark_color));
        topIcon.setVisibility(View.VISIBLE);
        title.setText(getString(R.string.calibrate_text_title_error));
        description.setText(getString(R.string.calibrate_text_desc_error));

        removeTemp();
    }

    private void showHasData() {
        showSuccess();
        description.setText("already has data");
    }

    private void showWorking() {
        doneCalibrate.setVisibility(View.GONE);
        startCalibrate.setVisibility(View.INVISIBLE);
        startCalibrate.setClickable(false);
//        surfaceCamera.setVisibility(View.GONE);
        topIcon.setVisibility(View.GONE);
        working.setVisibility(View.VISIBLE);
        title.setText(getString(R.string.calibrate_text_title_working));
        description.setText(getString(R.string.calibrate_text_desc_working));
    }

    private void showSuccess() {
        startCalibrate.setVisibility(View.VISIBLE);
        startCalibrate.setText(R.string.calibrate_button_addmore);
        startCalibrate.setClickable(true);
        working.setVisibility(View.GONE);
//        surfaceCamera.setVisibility(View.GONE);
        topIcon.setImageResource(R.drawable.ic_check_circle_white_48dp);
        topIcon.setColorFilter(getResources().getColor(R.color.accent_color));
        topIcon.setVisibility(View.VISIBLE);
        title.setText(getString(R.string.calibrate_text_title_done));
        description.setText(R.string.calibrate_text_desc_added);

        removeTemp();
    }

    private void showInitSuccess() {
        showSuccess();
        startCalibrate.setVisibility(View.GONE);
        doneCalibrate.setVisibility(View.VISIBLE);
        description.setText(getString(R.string.calibrate_text_desc_done));
    }

    private void setupListener() {
        startCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calibrateFile = new File(getExternalCacheDir(), "calibrate.tmp");
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                camera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(calibrateFile));
//                startActivityForResult(camera, REQUEST_CAMERA);

//                List<Intent> intentList = new ArrayList<Intent>();
//
//                List<ResolveInfo> listCamera = getPackageManager().queryIntentActivities(camera, REQUEST_CAMERA);
//                for(ResolveInfo res : listCamera) {
//                    Intent intent = new Intent(camera);
//                    intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
//                    intent.setPackage(res.activityInfo.packageName);
//                    intent.setClassName(res.activityInfo.packageName, res.activityInfo.getClass().getName());
//                    intent.putExtra("name", res.activityInfo.packageName);
//                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(calibrateFile));
//                    intentList.add(intent);
//                }

                final Intent chooserIntent = Intent.createChooser(camera, "Select Camera");

                startActivityForResult(chooserIntent, REQUEST_CAMERA);

//                mCamera.takePicture(null, null, new Camera.PictureCallback() {
//                    @Override
//                    public void onPictureTaken(byte[] data, Camera camera) {
//                        showWorking();
//                        OutputStream os;
//                        calibrateFile = new File(getCacheDir(), "calibrate.tmp");
//                        Uri uri = Uri.fromFile(calibrateFile);
//                        try {
//                            os = getContentResolver().openOutputStream(uri);
//                            os.write(data);
//                            os.flush();
//                            os.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//
//                        try {
//                            ExifInterface mExif = new ExifInterface(calibrateFile.getAbsolutePath());
//                            if (mExif.getAttribute(ExifInterface.TAG_MAKE) == null || mExif.getAttribute(ExifInterface.TAG_MODEL) == null) {
//                                showRetry();
//                                return;
//                            }
//                            String make = StringUtil.wrapBlank(mExif.getAttribute(ExifInterface.TAG_MAKE)).replaceAll("[ -]", "");
//                            String model = StringUtil.wrapBlank(mExif.getAttribute(ExifInterface.TAG_MODEL)).replaceAll("[ -]", "");
//
//                            Log.d("droidphoto", "calibrate make: " + make);
//                            Log.d("droidphoto", "calibrate model: " + model);
//
//                            getSharedPreferences(getString(R.string.device_data), MODE_PRIVATE).edit()
//                                    .putString(getString(R.string.camera_make), make)
//                                    .putString(getString(R.string.camera_model), model)
//                                    .apply();
//
//                            delayAction.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    showSuccess();
//                                }
//                            }, 2000);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            delayAction.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    showRetry();
//                                }
//                            }, 2000);
//
//                        }
//                    }
//                });
            }
        });

        doneCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });

//        surfaceCamera.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//        surfaceCamera.getHolder().addCallback(new SurfaceHolder.Callback() {
//            @Override
//            public void surfaceCreated(SurfaceHolder holder) {
//                try {
//                    mCamera.setPreviewDisplay(surfaceCamera.getHolder());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//                try {
//                    mCamera.setPreviewDisplay(surfaceCamera.getHolder());
//                    mCamera.startPreview();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void surfaceDestroyed(SurfaceHolder holder) {
//            }
//        });

        delaySuccess = new Runnable() {
            @Override
            public void run() {
                if(isInitial) {
                    showInitSuccess();
                } else {
                    showSuccess();
                }
            }
        };
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                    showWorking();
//                    packageName = data.getStringExtra("name");
//                    Toast.makeText(getApplicationContext(), packageName, Toast.LENGTH_LONG).show();
                    calibrateFile = new File(getExternalCacheDir(), "calibrate.tmp");
                    try {
                        ExifInterface mExif = new ExifInterface(calibrateFile.getAbsolutePath());
                        if (mExif.getAttribute(ExifInterface.TAG_MAKE) == null || mExif.getAttribute(ExifInterface.TAG_MODEL) == null) {
                            showRetry();
                            return;
                        }
//                        String name = "app name";
                        String exifMake = StringUtil.wrapBlank(mExif.getAttribute(ExifInterface.TAG_MAKE)).replaceAll("[ -]", "");
                        String exifModel = StringUtil.wrapBlank(mExif.getAttribute(ExifInterface.TAG_MODEL)).replaceAll("[ -]", "");

                        Log.d("droidphoto", "calibrate make: " + exifMake);
                        Log.d("droidphoto", "calibrate model: " + exifModel);

//                        String prefAppName = getSharedPreferences(getString(R.string.device_data), MODE_PRIVATE).getString(getString(R.string.camera_app_name), null);
//                        if (prefAppName == null) {
//                            prefAppName = name;
//                        } else {
//                            String appnames[] = prefAppName.split(",");
//                            for (String prefname : appnames) {
//                                if(prefname.equals(name)) {
//                                    showHasData();
//                                    return;
//                                }
//                            }
//
//                            prefAppName += "," + name;
//                        }
                        String prefMake = getSharedPreferences(getString(R.string.device_data), MODE_PRIVATE).getString(getString(R.string.camera_app_name), null);
                        if (prefMake == null) {
                            prefMake = exifMake;
                        } else {
                            boolean hasData = false;
                            String lists[] = prefMake.split(",");
                            for (String eachmake : lists) {
                                if(eachmake.equals(exifMake)) {
//                                    showHasData();
//                                    return;
                                    hasData = true;
                                    break;
                                }
                            }
                            if(!hasData) {
                                prefMake += "," + exifMake;
                            }
                        }
                        String prefModel = getSharedPreferences(getString(R.string.device_data), MODE_PRIVATE).getString(getString(R.string.camera_app_name), null);
                        if (prefModel == null) {
                            prefModel = exifModel;
                        } else {
                            boolean hasData = false;
                            String lists[] = prefModel.split(",");
                            for (String eachmodel : lists) {
                                if(eachmodel.equals(exifModel)) {
//                                    showHasData();
//                                    return;
                                    hasData = true;
                                    break;
                                }
                            }
                            if(!hasData) {
                                prefModel += "," + exifModel;
                            }
                        }

                        getSharedPreferences(getString(R.string.device_data), MODE_PRIVATE).edit()
//                                .putString(getString(R.string.camera_app_name), prefAppName)
                                .putString(getString(R.string.camera_make), prefMake)
                                .putString(getString(R.string.camera_model), prefModel)
                                .apply();
                        delayAction.postDelayed(delaySuccess, 1123);
                    } catch (IOException e) {
                        e.printStackTrace();
                        showRetry();
                    }
                    break;
                default:
                    break;
            }
        } else {
            Snackbar.make(initCalibrateLayout, "cancel", Snackbar.LENGTH_SHORT).show();
            if(data != null) {
                Log.d("droidphoto", data.getPackage());
            }
        }
        removeTemp();
//        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mCamera.release();
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

//        surfaceCamera = (SurfaceView) findViewById(R.id.surface_camera);
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
        delayAction.removeCallbacks(delaySuccess);
//        surfaceCamera.destroyDrawingCache();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
