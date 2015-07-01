package com.droidsans.photo.droidphoto;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.droidsans.photo.droidphoto.util.Devices;
import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.TimeZone;

public class FillPostActivity extends AppCompatActivity {
    private ImageView photo;
    private Bitmap imageBitmap;
    private EditText caption, vendor, model;
    private Button uploadBtn;
    private CheckBox isEnhanced, isAccept;
//    private ExifInterface mExif;
    private String mCurrentPhotoPath;

    Metadata metadata;
    ExifSubIFDDirectory exifDirectory;
    ExifIFD0Directory orientationDirectory;
    GpsDirectory gpsDirectory;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_post);

        initialize();
    }

    private void initialize() {
        findAllById();
        setupToolbar();
        setupListener();
        setThumbnailImage();
        setVendorAndModel();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setVendorAndModel() {
        vendor.setText(Devices.getDeviceVendor());
        model.setText(Devices.getDeviceModel());
    }

    private void setThumbnailImage() {
        Intent previousIntent = getIntent();
        mCurrentPhotoPath = previousIntent.getStringExtra("photoPath");

        try {
            metadata = ImageMetadataReader.readMetadata(new File(mCurrentPhotoPath));
            gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            exifDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            orientationDirectory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
//            mExif = new ExifInterface(mCurrentPhotoPath);
        } catch (ImageProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(exifDirectory == null) {
            Toast.makeText(getApplicationContext(), "Error: selected image must have exif", Toast.LENGTH_LONG).show();
//            Snackbar.make(null, "Error: selected image must have exif", Snackbar.LENGTH_LONG).show();
            finish();
        }

//        try {
//            mExif = new ExifInterface(mCurrentPhotoPath);
//            Log.d("droidphoto", "exp_time = " + mExif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME));
//            Log.d("droidphoto", "iso = " + mExif.getAttributeInt(ExifInterface.TAG_ISO, -1));
//            Log.d("droidphoto", "lat = " + mExif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
//                    + "|long = " + mExif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
//                    + "ref:" + mExif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
//                    + "|" + mExif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF));
//            float[] f = new float[2];
//            mExif.getLatLong(f);
//            Log.d("droidphoto", "latlong:" + f[0] + "|" + f[1]);
//        } catch (IOException e) {
//            Log.e("droidphoto", "cannot read exif", e);
//        }

        boolean isSampledDown = false;
        //get width/height without load bitmap into memory
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, opt);

        //downsamples
        if(opt.outWidth > 5000 || opt.outHeight > 5000) {
            opt.inSampleSize = 4;
            isSampledDown = true;
        } else if(opt.outWidth > 2500 || opt.outHeight > 2500) {
            opt.inSampleSize = 2;
            isSampledDown = true;
        }
        opt.inJustDecodeBounds = false;
        imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, opt);
        Log.d("droidphoto", "outwidth: " + opt.outWidth + "|outheight: " + opt.outHeight);
        Log.d("droidphoto", "file size: " + imageBitmap.getByteCount() + " bytes");

        if(isSampledDown) {
            boolean isNormal = false;
            android.graphics.Matrix matrix = new android.graphics.Matrix();

            //old (will be deprecated)
//            switch (mExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)) {
//                case ExifInterface.ORIENTATION_NORMAL:
//                    isNormal = true;
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_90:
//                    matrix.postRotate(90);
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_180:
//                    matrix.postRotate(180);
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_270:
//                    matrix.postRotate(270);
//                    break;
//                default:
//                    Log.d("droidphoto", "what!!?? NO ROTATION!!?");
//                    break;
//            }
            //new
            if(orientationDirectory != null) {
                try {
                    Log.d("droidphoto", "orientation:" + orientationDirectory.getInt(ExifIFD0Directory.TAG_ORIENTATION));
                    switch (orientationDirectory.getInt(ExifIFD0Directory.TAG_ORIENTATION)) {
                        case 1:                     isNormal = true;                        break; //ExifInterface.ORIENTATION_NORMAL
                        case 2:                                 matrix.postScale(-1, 1);    break; //ExifInterface.ORIENTATION_FLIP_HORIZONTAL
                        case 3:     matrix.postRotate(180);                                 break; //ExifInterface.ORIENTATION_ROTATE_180
                        case 4:     matrix.postRotate(180);     matrix.postScale(-1, 1);    break; //ExifInterface.ORIENTATION_FLIP_VERTICAL
                        case 5:     matrix.postRotate(90);      matrix.postScale(-1, 1);    break; //ExifInterface.ORIENTATION_TRANSPOSE
                        case 6:     matrix.postRotate(90);                                  break; //ExifInterface.ORIENTATION_ROTATE_90
                        case 7:     matrix.postRotate(270);     matrix.postScale(-1, 1);    break; //ExifInterface.ORIENTATION_TRANSVERSE
                        case 8:     matrix.postRotate(270);                                 break; //ExifInterface.ORIENTATION_ROTATE_270

                        default:
                            Log.d("droidphoto", "what!!?? NO ROTATION!!?");
                            break;
                    }
                } catch (MetadataException e) {
                    e.printStackTrace();
                }
                if(!isNormal) {
                    imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);
                }
            }
        }
        photo.setImageBitmap(imageBitmap);
    }

    private void setupListener() {
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAccept.isChecked()) {
                    Toast.makeText(getApplicationContext(), "Please accpet our term of service", Toast.LENGTH_LONG).show();
                    return;
                }
//                if (mExif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME) == null ||
//                        mExif.getAttribute(ExifInterface.TAG_ISO) == null ||
//                        mExif.getAttribute(ExifInterface.TAG_APERTURE) == null) {
                if(exifDirectory == null) {
                    Toast.makeText(getApplicationContext(), "image has no exif", Toast.LENGTH_SHORT).show();
                    return;
                } else if(exifDirectory.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME) == null) {
                    Toast.makeText(getApplicationContext(), "image has no required exif", Toast.LENGTH_SHORT).show();
                    return;
                }


                new Thread(new Runnable() {
                    public void run() {
                        Log.d("droidphoto", "uploading...");
                        JSONObject respond = post("http://209.208.65.102:3000/photo", mCurrentPhotoPath);
                        Log.d("droidphoto", "respond: " + respond.toString());

                        try {
                            if (respond.getBoolean("success")) {
                                JSONObject photoDetailStuff = new JSONObject();
                                photoDetailStuff.put("photo_url", respond.getString("filename"));
                                photoDetailStuff.put("caption", caption.getText().toString());
                                photoDetailStuff.put("model", model.getText().toString());
                                photoDetailStuff.put("vendor", vendor.getText().toString());
//                                photoDetailStuff.put("is_flash", mExif.getAttribute(ExifInterface.TAG_FLASH));
//                                photoDetailStuff.put("exp_time", mExif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME));
//                                photoDetailStuff.put("tag_date", mExif.getAttribute(ExifInterface.TAG_DATETIME));
//                                photoDetailStuff.put("width", mExif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
//                                photoDetailStuff.put("height", mExif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
//                                photoDetailStuff.put("iso", mExif.getAttribute(ExifInterface.TAG_ISO));
//                                photoDetailStuff.put("aperture", mExif.getAttribute(ExifInterface.TAG_APERTURE));
//                                photoDetailStuff.put("gps_lat", mExif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
//                                photoDetailStuff.put("gps_long", mExif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
//                                photoDetailStuff.put("gps_lat_ref", mExif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF));
//                                photoDetailStuff.put("gps_long_ref", mExif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF));
                                photoDetailStuff.put("is_flash", exifDirectory.getString(ExifSubIFDDirectory.TAG_FLASH));
                                photoDetailStuff.put("exp_time", exifDirectory.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME));
                                photoDetailStuff.put("tag_date", exifDirectory.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL));
                                photoDetailStuff.put("width", exifDirectory.getString(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH));
                                photoDetailStuff.put("height", exifDirectory.getString(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT));
                                photoDetailStuff.put("iso", exifDirectory.getString(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
                                photoDetailStuff.put("aperture", exifDirectory.getString(ExifSubIFDDirectory.TAG_APERTURE));
                                if(gpsDirectory != null) {
                                    photoDetailStuff.put("gps_lat", gpsDirectory.getString(GpsDirectory.TAG_LATITUDE));
                                    photoDetailStuff.put("gps_long", gpsDirectory.getString(GpsDirectory.TAG_LONGITUDE));
                                    photoDetailStuff.put("gps_lat_ref", gpsDirectory.getString(GpsDirectory.TAG_LATITUDE_REF));
                                    photoDetailStuff.put("gps_long_ref", gpsDirectory.getString(GpsDirectory.TAG_LONGITUDE_REF));
                                }
                                photoDetailStuff.put("_event", "photoupload_respond");
                                photoDetailStuff.put("is_accept", isAccept.isChecked());
                                photoDetailStuff.put("is_enhanced", isEnhanced.isChecked());

                                GlobalSocket.globalEmit("photo.upload", photoDetailStuff);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();

                Intent returnIntent = new Intent();
                returnIntent.putExtra("caption", caption.getText().toString());
                returnIntent.putExtra("vendor", vendor.getText().toString());
                returnIntent.putExtra("model", model.getText().toString());
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

        Emitter.Listener onPhotoUploadRespond = new Emitter.Listener() {
            @Override
            public void call(final Object[] args) {
                FillPostActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        try {
                            if(data.getBoolean("success")){
                                //TODO notify main activity of successfully upload
                                Log.d("droidphoto", "upload success");
                            } else {
                                Log.d("droidphoto", "upload error: "+data.getString("msg"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        if(!GlobalSocket.mSocket.hasListeners("photoupload_respond")){
            GlobalSocket.mSocket.on("photoupload_respond", onPhotoUploadRespond);
        }
    }

    public JSONObject post(String url, String path) {
        byte[] data = fileToByteArray(path);
        String attachmentName = "image";
        String attachmentFileName = "image.jpg";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";

        HttpURLConnection httpUrlConnection = null;

        try {
            httpUrlConnection = (HttpURLConnection) (new URL(url)).openConnection();
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setDoOutput(true);

            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
            httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
            httpUrlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream request = new DataOutputStream(httpUrlConnection.getOutputStream());
            //start request section
            request.writeBytes(twoHyphens + boundary + lineEnd);

            /////////////////////////////////////////////////////////////////////////////////////////////

            //body heading
            request.writeBytes("Content-Disposition: form-data; name=\"_token\"" + lineEnd);
            request.writeBytes("Content-Type: text/plain" + lineEnd);
            request.writeBytes("Content-Transfer-Encoding: base64" + lineEnd);

            //request body
//            request.writeBytes(lineEnd + GlobalSocket.getToken() + lineEnd);
            request.writeBytes(lineEnd + getSharedPreferences(getString(R.string.userdata), Context.MODE_PRIVATE).getString(getString(R.string.token), "") + lineEnd);

            //end of body section
            request.writeBytes(twoHyphens + boundary + lineEnd);

            /////////////////////////////////////////////////////////////////////////////////////////////

            //heading
            request.writeBytes("Content-Disposition: form-data; name=\"" + attachmentName + "\";filename=\"" + attachmentFileName + "\"" + lineEnd);
            request.writeBytes("Content-Type: image/jpeg" + lineEnd);
            request.writeBytes("Content-Transfer-Encoding: base64" + lineEnd);

            //request file
            request.writeBytes(lineEnd);
            request.write(data);
            request.writeBytes(lineEnd);

            //end of file section
            request.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            request.flush();
            request.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }  catch (IOException e) {
            e.printStackTrace();
        }

        //get respond
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream responseStream = new BufferedInputStream(httpUrlConnection.getInputStream());
            BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
            String line;
            while ((line = responseStreamReader.readLine()) != null)
            {
                stringBuilder.append(line).append("\n");
            }
            responseStreamReader.close();
//            httpUrlConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            try {
                httpUrlConnection.disconnect();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        try {
            return new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        imageBitmap.recycle();
        super.onDestroy();
    }

    private byte[] fileToByteArray(String filePath){
        File file = new File(filePath);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    private void findAllById() {
        photo = (ImageView) findViewById(R.id.photo);
        caption = (EditText) findViewById(R.id.caption);
        vendor = (EditText) findViewById(R.id.vendor);
        model = (EditText) findViewById(R.id.model);
        uploadBtn = (Button) findViewById(R.id.upload_btn);
        isAccept = (CheckBox) findViewById(R.id.is_accept);
        isEnhanced = (CheckBox) findViewById(R.id.is_enhanced);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
     }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fill_post, menu);
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
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
