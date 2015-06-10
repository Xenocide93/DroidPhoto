package com.droidsans.photo.droidphoto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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

public class FillPostActivity extends ActionBarActivity {
    private ImageView photo;
    private Bitmap imageBitmap;
    private EditText caption, vendor, model;
    private Button uploadBtn;
    private CheckBox isEnhanced, isAccept;

    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_post);

        initialize();
    }

    private void initialize() {
        findAllById();
        setupListener();
        setThumbnailImage();
        setVendorAndModel();
    }

    private void setVendorAndModel() {
        vendor.setText(Devices.getDeviceVendor());
        model.setText(Devices.getDeviceModel());
    }

    private void setThumbnailImage() {
        Intent previousIntent = getIntent();
        mCurrentPhotoPath = previousIntent.getStringExtra("photoPath");
        File imageFile = new File(mCurrentPhotoPath);
        imageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        Log.d("droidphoto", "file size: " + imageBitmap.getByteCount() + " bytes");
        photo.setImageBitmap(imageBitmap);
    }

    private void setupListener() {
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isAccept.isChecked()){
                    Toast.makeText(getApplicationContext(), "Please accpet our term of service", Toast.LENGTH_LONG);
                    return;
                }

                new Thread(new Runnable() {
                    public void run() {
                        Log.d("droidphoto", "uploading...");
                        JSONObject respond = post("http://209.208.65.102:3000/photo", mCurrentPhotoPath);
                        Log.d("droidphoto", "respond: " + respond.toString());

                        ExifInterface mExif = null;
                        try {
                            if(respond.getBoolean("success")){
                                mExif = new ExifInterface(mCurrentPhotoPath);
                                JSONObject photoDetailStuff = new JSONObject();

                                photoDetailStuff.put("photo_name", respond.getString("filename"));
                                photoDetailStuff.put("caption", caption.getText().toString());
                                photoDetailStuff.put("model", model.getText().toString());
                                photoDetailStuff.put("vendor", vendor.getText().toString());
                                photoDetailStuff.put("is_flash", mExif.getAttribute(ExifInterface.TAG_FLASH));
                                photoDetailStuff.put("exp_time", mExif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME));
                                photoDetailStuff.put("tag_date", mExif.getAttribute(ExifInterface.TAG_DATETIME));
                                photoDetailStuff.put("width", mExif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
                                photoDetailStuff.put("height", mExif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
                                photoDetailStuff.put("iso", mExif.getAttribute(ExifInterface.TAG_ISO));
                                photoDetailStuff.put("aperture", mExif.getAttribute(ExifInterface.TAG_APERTURE));
                                photoDetailStuff.put("gps_lat", mExif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
                                photoDetailStuff.put("gps_long", mExif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
                                photoDetailStuff.put("gps_lat_ref", mExif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF));
                                photoDetailStuff.put("gps_long_ref", mExif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF));
                                photoDetailStuff.put("_event", "photoupload_respond");
                                photoDetailStuff.put("is_accept", isAccept.isChecked());
                                photoDetailStuff.put("is_enhanced", isEnhanced.isChecked());

                                GlobalSocket.globalEmit("photo.upload", photoDetailStuff);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (  IOException ex) {
                            Log.e("droidphoto", "cannot read exif", ex);
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
            request.writeBytes(lineEnd + GlobalSocket.getToken() + lineEnd);

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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
            httpUrlConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
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
        super.onDestroy();
        imageBitmap.recycle(); System.gc();
    }

    private byte[] fileToByteArray(String filePath){
        File file = new File(filePath);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}