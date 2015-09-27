package com.droidsans.photo.droidphoto;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
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

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
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
    private Button startCalibrate, doneCalibrate, skipCalibrate;
    private ImageView topIcon;
    private ProgressBar working;
    private FontTextView title, description;

    private File calibrateFile;
    private boolean isInitial;
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

        skipCalibrate.setVisibility(View.VISIBLE);
    }

    private void showAddMore() {
        showInit();
        startCalibrate.setText(R.string.calibrate_button_add);
        description.setText(R.string.calibrate_text_desc_add);
//        String applist[] = getSharedPreferences(getString(R.string.device_data), MODE_PRIVATE).getString(getString(R.string.camera_app_name), "").split(",");
//        for(String app : applist) {
//            description.append("\r\n- " + app);
//        }

        skipCalibrate.setVisibility(View.GONE);
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

        if(isInitial) skipCalibrate.setVisibility(View.VISIBLE);
        else skipCalibrate.setVisibility(View.GONE);
    }

//    private void showHasData() {
//        showSuccess();
//        description.setText("already has data");
//    }

    private void showWorking() {
        doneCalibrate.setVisibility(View.GONE);
        startCalibrate.setVisibility(View.INVISIBLE);
        startCalibrate.setClickable(false);
//        surfaceCamera.setVisibility(View.GONE);
        topIcon.setVisibility(View.GONE);
        working.setVisibility(View.VISIBLE);
        title.setText(getString(R.string.calibrate_text_title_working));
        description.setText(getString(R.string.calibrate_text_desc_working));

        skipCalibrate.setVisibility(View.GONE);
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

        skipCalibrate.setVisibility(View.GONE);

        removeTemp();
    }

    private void showSkipped() {
        showSuccess();
        startCalibrate.setVisibility(View.GONE);
        doneCalibrate.setVisibility(View.VISIBLE);
        description.setText(R.string.calibrate_text_desc_skip);

//        skipCalibrate.setVisibility(View.GONE);
    }

    private void showInitSuccess() {
        showSuccess();
        startCalibrate.setVisibility(View.GONE);
        doneCalibrate.setVisibility(View.VISIBLE);
        description.setText(getString(R.string.calibrate_text_desc_done));

        skipCalibrate.setVisibility(View.GONE);
    }

    private void setupListener() {
        startCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calibrateFile = new File(getExternalCacheDir(), "calibrate.tmp");
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                camera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(calibrateFile));
                startActivityForResult(camera, REQUEST_CAMERA);
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

//                final Intent chooserIntent = Intent.createChooser(camera, "Select Camera");

//                startActivityForResult(chooserIntent, REQUEST_CAMERA);

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

        skipCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSharedPreferences(getString(R.string.device_data), MODE_PRIVATE).edit()
//                                .putString(getString(R.string.camera_app_name), prefAppName)
                        .putString(getString(R.string.camera_make), Build.MANUFACTURER)
                        .putString(getString(R.string.camera_model), Build.MODEL)
                        .apply();

                showSkipped();
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
                    calibrateFile = new File(getExternalCacheDir(), "calibrate.tmp");

//                    checkDrewnoakesExif();
                    checkExif();

                    break;
                default:
                    break;
            }
        }
        removeTemp();
//        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkExif() {
        try {
            ExifInterface mExif = new ExifInterface(calibrateFile.getAbsolutePath());

            if (mExif.getAttribute(ExifInterface.TAG_MAKE) == null || mExif.getAttribute(ExifInterface.TAG_MODEL) == null) {
                showRetry();
//                Toast.makeText(getApplicationContext(), "retry at null", Toast.LENGTH_SHORT).show();
                return;
            }
//            String name = "app name";
            String exifMake = StringUtil.wrapBlank(mExif.getAttribute(ExifInterface.TAG_MAKE)).replaceAll("[ -]", "");
            String exifModel = StringUtil.wrapBlank(mExif.getAttribute(ExifInterface.TAG_MODEL)).replaceAll("[ -]", "");

            Log.d("droidphoto", "calibrate android make: " + exifMake);
            Log.d("droidphoto", "calibrate android model: " + exifModel);

//            String prefAppName = getSharedPreferences(getString(R.string.device_data), MODE_PRIVATE).getString(getString(R.string.camera_app_name), null);
//            if (prefAppName == null) {
//                prefAppName = name;
//            } else {
//                String appnames[] = prefAppName.split(",");
//                for (String prefname : appnames) {
//                    if(prefname.equals(name)) {
//                        showHasData();
//                        return;
//                    }
//                }
//
//                prefAppName += "," + name;
//            }
            String prefMake = getSharedPreferences(getString(R.string.device_data), MODE_PRIVATE).getString(getString(R.string.camera_app_name), null);
            if (prefMake == null) {
                if(!exifMake.equals("")) {
                    prefMake = exifMake;
                }
            } else {
                boolean hasData = false;
                String lists[] = prefMake.split(",");
                for (String eachmake : lists) {
                    if(eachmake.equals(exifMake)) {
//                        showHasData();
//                        return;
                        hasData = true;
                        break;
                    }
                }
                if(!hasData && !exifMake.equals("")) {
                    prefMake += "," + exifMake;
                }
            }
            String prefModel = getSharedPreferences(getString(R.string.device_data), MODE_PRIVATE).getString(getString(R.string.camera_app_name), null);
            if (prefModel == null) {
                if(!exifModel.equals("")) { //if exifmake not empty
                    prefModel = exifModel;
                }
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
                if(!hasData && !exifModel.equals("")) {
                    prefModel += "," + exifModel;
                }
            }

//            Toast.makeText(getApplicationContext(), "prefmake : " + prefMake, Toast.LENGTH_SHORT).show();
//            Toast.makeText(getApplicationContext(), "prefmodel : " + prefModel, Toast.LENGTH_SHORT).show();

            getSharedPreferences(getString(R.string.device_data), MODE_PRIVATE).edit()
//                                .putString(getString(R.string.camera_app_name), prefAppName)
                    .putString(getString(R.string.camera_make), prefMake)
                    .putString(getString(R.string.camera_model), prefModel)
                    .apply();
            delayAction.postDelayed(delaySuccess, 1123);
        } catch (IOException e) {
            e.printStackTrace();
            showRetry();
//            Toast.makeText(getApplicationContext(), "retry at catch", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkDrewnoakesExif() {
        try {
            ExifInterface mExif = new ExifInterface(calibrateFile.getAbsolutePath());
            Metadata metadata = ImageMetadataReader.readMetadata(calibrateFile);
            ExifIFD0Directory mainDirectory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

            if(mainDirectory == null) {
                showRetry();
//                Toast.makeText(getApplicationContext(), "retry at no exif", Toast.LENGTH_SHORT).show();
                return;
            }
            if (mExif.getAttribute(ExifInterface.TAG_MAKE) == null || mExif.getAttribute(ExifInterface.TAG_MODEL) == null) {
                if(mainDirectory.getString(ExifIFD0Directory.TAG_MAKE) == null || mainDirectory.getString(ExifIFD0Directory.TAG_MODEL) == null) {
                    showRetry();
//                    Toast.makeText(getApplicationContext(), "retry at null", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
//            String name = "app name";
            String exifMake = StringUtil.wrapBlank(mExif.getAttribute(ExifInterface.TAG_MAKE)).replaceAll("[ -]", "");
            String exifModel = StringUtil.wrapBlank(mExif.getAttribute(ExifInterface.TAG_MODEL)).replaceAll("[ -]", "");

            String exifIFD0make = StringUtil.wrapBlank(mainDirectory.getString(ExifIFD0Directory.TAG_MAKE)).replaceAll("[ -]", "");
            String exifIFD0model = StringUtil.wrapBlank(mainDirectory.getString(ExifIFD0Directory.TAG_MODEL)).replaceAll("[ -]", "");

            Log.d("droidphoto", "calibrate android make: " + exifMake);
            Log.d("droidphoto", "calibrate android model: " + exifModel);
            Log.d("droidphoto", "calibrate drew make: " + exifIFD0make);
            Log.d("droidphoto", "calibrate drew model: " + exifIFD0model);

//            String prefAppName = getSharedPreferences(getString(R.string.device_data), MODE_PRIVATE).getString(getString(R.string.camera_app_name), null);
//            if (prefAppName == null) {
//                prefAppName = name;
//            } else {
//                String appnames[] = prefAppName.split(",");
//                for (String prefname : appnames) {
//                    if(prefname.equals(name)) {
//                        showHasData();
//                        return;
//                    }
//                }
//
//                prefAppName += "," + name;
//            }
            String prefMake = getSharedPreferences(getString(R.string.device_data), MODE_PRIVATE).getString(getString(R.string.camera_app_name), null);
            if (prefMake == null) {
                if (!exifMake.equals("")) { //if exifmake not empty
                    prefMake = exifMake;
                    if (!exifMake.equalsIgnoreCase(exifIFD0make)) {
                        prefMake += "," + exifIFD0make;
                    }
                } else if (!exifIFD0make.equals("")) { //if exifIFD0make not empty
                    prefMake = exifIFD0make;
                } else {
                    showRetry();
//                    Toast.makeText(getApplicationContext(), "retry at empty make", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                boolean hasData = false;
                boolean hasIFD0Data = false;
                String lists[] = prefMake.split(",");
                for (String eachmake : lists) {
                    if(eachmake.equals(exifMake)) {
//                                    showHasData();
//                                    return;
                        hasData = true;
                    }
                    if(eachmake.equals(exifIFD0make)) {
                        hasIFD0Data = true;
                    }
                    if(hasData && hasIFD0Data) {
                        break;
                    }
                }
                if(!hasData && !exifMake.equals("")) {
                    prefMake += "," + exifMake;
                }
                if(!hasIFD0Data && !exifIFD0make.equals("")) {
                    prefMake += "," + exifIFD0make;
                }
            }
            String prefModel = getSharedPreferences(getString(R.string.device_data), MODE_PRIVATE).getString(getString(R.string.camera_app_name), null);
            if (prefModel == null) {
                if(!exifModel.equals("")) { //if exifmake not empty
                    prefModel = exifModel;
                    if(!exifModel.equalsIgnoreCase(exifIFD0model)) {
                        prefModel += "," + exifIFD0model;
                    }
                } else if(!exifIFD0model.equals("")) { //if exifIFD0make not empty
                    prefModel = exifIFD0model;
                } else {
                    showRetry();
//                    Toast.makeText(getApplicationContext(), "retry at empty model", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                boolean hasData = false;
                boolean hasIFD0Data = false;
                String lists[] = prefModel.split(",");
                for (String eachmodel : lists) {
                    if(eachmodel.equals(exifModel)) {
//                                    showHasData();
//                                    return;
                        hasData = true;
                    }
                    if(eachmodel.equals(exifIFD0model)) {
                        hasIFD0Data = true;
                    }
                    if(hasData && hasIFD0Data) {
                        break;
                    }
                }
                if(!hasData && !exifModel.equals("")) {
                    prefModel += "," + exifModel;
                }
                if(!hasIFD0Data && !exifIFD0model.equals("")) {
                    prefModel += "," + exifIFD0model;
                }
            }

//            Toast.makeText(getApplicationContext(), "prefmake : " + prefMake, Toast.LENGTH_SHORT).show();
//            Toast.makeText(getApplicationContext(), "prefmodel : " + prefModel, Toast.LENGTH_SHORT).show();

            getSharedPreferences(getString(R.string.device_data), MODE_PRIVATE).edit()
//                                .putString(getString(R.string.camera_app_name), prefAppName)
                    .putString(getString(R.string.camera_make), prefMake)
                    .putString(getString(R.string.camera_model), prefModel)
                    .apply();
            delayAction.postDelayed(delaySuccess, 1084);
        } catch (ImageProcessingException | IOException e) {
            e.printStackTrace();
            showRetry();
//            Toast.makeText(getApplicationContext(), "retry at catch", Toast.LENGTH_SHORT).show();
        }
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
        startCalibrate = (Button) findViewById(R.id.button_camera_launch);
        doneCalibrate = (Button) findViewById(R.id.button_done);
        skipCalibrate = (Button) findViewById(R.id.button_skip);
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
