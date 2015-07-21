package com.droidsans.photo.droidphoto;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.droidsans.photo.droidphoto.util.CircleTransform;
import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.droidsans.photo.droidphoto.util.retrofit.AvatarResponseModel;
import com.droidsans.photo.droidphoto.util.retrofit.PostService;
import com.github.nkzawa.emitter.Emitter;
import com.squareup.okhttp.OkHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedFile;

public class EditProfileActivity extends AppCompatActivity {
    public static final String IS_UPDATE_PROFILE_PIC = "isUpdateProfilePic";
    public static final int SELECT_AVATAR = 32;
    private static final int MAX_AVATAR_SIZE = 300;

    private Toolbar toolbar;
    private ImageView profilePic;
    private ImageButton editProfilePic;
    private Button changePass;
    private EditText displayName, profileDescription;

    private Handler delayAction = new Handler();
    private Intent oldIntent;

    private boolean isUpdateProfilePic = false;
    private String profilePicPath;
    private Uri avatarURI;

    private Emitter.Listener onGetUserDataRespond;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initialize();
    }

    private void initialize() {
        FindAllById();
        setupToolbar();
        loadProfileData();
        setupListener();
    }

    private void setupListener() {
        editProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO update image
                dispatchPicturePickerIntent();
                Toast.makeText(getApplicationContext(), "Launch Picture Picker Intent", Toast.LENGTH_SHORT).show();
            }
        });

        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent changePassIntent = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                startActivity(changePassIntent);
            }
        });

        onGetUserDataRespond = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        };

        if(!GlobalSocket.mSocket.hasListeners("onUpdateProfileRespond")){
            GlobalSocket.mSocket.on("onUpdateProfileRespond", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            GlobalSocket.mSocket.off("onUpdateProfileRespond");
                            JSONObject data = (JSONObject) args[0];
                            if (data.optBoolean("success")) {
                                final JSONObject send = new JSONObject();
                                try {
                                    send.put("_event", "get_user_info");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if (!GlobalSocket.globalEmit("user.getuserinfo", send)) {
//                                    delayAction.postDelayed(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            if (!GlobalSocket.globalEmit("user.getuserinfo", send)) {
//                                                //sad
//                                            }
//                                        }
//                                    }, 2500);
                                }
                                ;
                                returnToPreviousActivity();
//                                Snackbar.make(getView(), "Profile information has been updated", Snackbar.LENGTH_LONG).show();
                            } else {
                                Snackbar.make(displayName, "Error: "+data.optString("msg"), Snackbar.LENGTH_LONG).show();
                                //reactivate button
                            }
                        }
                    });
                }
            });
        }
    }

    private void loadProfileData() {
        oldIntent = getIntent();
        if(oldIntent == null || !oldIntent.hasExtra(ProfileFragment.AVATAR_URL)) {
            Toast.makeText(getApplicationContext(), "from settings", Toast.LENGTH_SHORT).show();

            //TODO load data from server
            JSONObject data = new JSONObject();
            try {
                data.put("_event", "get_user_data");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(!GlobalSocket.globalEmit("user.getuserinfo", data)) {
                //retry
            } else {
                //detect data loss
            }
        } else {
            Toast.makeText(getApplicationContext(), "from profile", Toast.LENGTH_SHORT).show();
            displayName.setText(oldIntent.getStringExtra(ProfileFragment.DISPLAY_NAME));
            profileDescription.setText(oldIntent.getStringExtra(ProfileFragment.PROFILE_DESCRIPTION));
            Glide.with(getApplicationContext())
                    .load(GlobalSocket.serverURL + ProfileFragment.baseURL + oldIntent.getStringExtra(ProfileFragment.AVATAR_URL))
                    .centerCrop()
                    .transform(new CircleTransform(getApplicationContext()))
                    .placeholder(R.drawable.ic_account_circle_black_48dp)
                    .dontAnimate()
                    .into(profilePic);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setTitle(getString(R.string.title_activity_edit_profile));
    }

    private void returnToPreviousActivity() {
        if(oldIntent.hasExtra(ProfileFragment.AVATAR_URL)) {
            Intent returnData = new Intent();
            returnData.putExtra(ProfileFragment.DISPLAY_NAME, displayName.getText().toString());
            returnData.putExtra(ProfileFragment.PROFILE_DESCRIPTION, profileDescription.getText().toString());
            returnData.putExtra(EditProfileActivity.IS_UPDATE_PROFILE_PIC, isUpdateProfilePic);

            setResult(Activity.RESULT_OK, returnData);
            finish();
        } else {
            finish();
        }
    }

    private void dispatchPicturePickerIntent() {
        Intent picturePickerIntent = new Intent(Intent.ACTION_PICK);
        picturePickerIntent.setType("image/*");
        startActivityForResult(picturePickerIntent, SELECT_AVATAR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == AppCompatActivity.RESULT_OK) {
            switch (requestCode) {
                case SELECT_AVATAR:
                    profilePicPath = getImagePath(data.getData());
                    avatarURI = data.getData();
                    Toast.makeText(getApplicationContext(), profilePicPath, Toast.LENGTH_LONG).show();
                    Glide.with(getApplicationContext())
                            .load(profilePicPath)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
//                            .override(300,300)
                            .fitCenter()
                            .transform(new CircleTransform(getApplicationContext()))
                            .into(profilePic);
                    isUpdateProfilePic = true;
                    break;
                default:
                    Toast.makeText(getApplicationContext(), "bug ?????", Toast.LENGTH_SHORT).show();
                    break;
            }
        } else {
            //cancel
        }
//        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_done:
                if(isUpdateProfilePic) {
                    //resize image save to cache
                    File tempFile = new File(getCacheDir() + "/" + "avatartemp.jpg");
                    FileOutputStream out = null;
                    Bitmap bmp = null;
                    try {
                        InputStream in = new BufferedInputStream(getContentResolver().openInputStream(avatarURI));
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(in, null, options);
                        if(options.outHeight > MAX_AVATAR_SIZE || options.outWidth > MAX_AVATAR_SIZE) {
                            // Calculate ratios of height and width to requested height and width
                            final int heightRatio = Math.round((float) options.outHeight / MAX_AVATAR_SIZE);
                            final int widthRatio = Math.round((float) options.outWidth / MAX_AVATAR_SIZE);

                            // Choose the smallest ratio as inSampleSize value, this will guarantee
                            // a final image with both dimensions larger than or equal to the
                            // requested height and width.
                            options.inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
                        }
                        Metadata metadata;
                        ExifIFD0Directory orientationDirectory = null;
                        try {
                            metadata = ImageMetadataReader.readMetadata(new BufferedInputStream(getContentResolver().openInputStream(avatarURI)));
                            orientationDirectory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
                        } catch (ImageProcessingException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        android.graphics.Matrix matrix = null;
                        if(orientationDirectory != null && orientationDirectory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                            matrix = new android.graphics.Matrix();
                            Log.d("droidphoto", "rotating...");
                            try {
                                switch (orientationDirectory.getInt(ExifIFD0Directory.TAG_ORIENTATION)) {
                                    case 1:                                                             break; //ExifInterface.ORIENTATION_NORMAL
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
                        }

                        try {
                            in.close();
                            in = new BufferedInputStream(getContentResolver().openInputStream(avatarURI));
                            out = new FileOutputStream(tempFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        options.inJustDecodeBounds = false;
                        if(matrix == null) {
                            Log.d("droidphoto", "no matrix");
                            BitmapFactory.decodeStream(in, null, options).compress(Bitmap.CompressFormat.JPEG, 80, out);
                        } else {
                            Log.d("droidphoto", "has matrix");
                            Bitmap.createBitmap(BitmapFactory.decodeStream(in, null, options), 0, 0, options.outWidth, options.outHeight, matrix, true)
                                    .compress(Bitmap.CompressFormat.JPEG, 80, out);
                        }
                        if(out != null) {
                            try {
                                out.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Snackbar.make(displayName, "uploading...", Snackbar.LENGTH_LONG).show();

                    //create client
                    OkHttpClient okHttpClient = new OkHttpClient();
                    okHttpClient.setReadTimeout(5, TimeUnit.SECONDS);

                    //create rest adapter
                    RestAdapter restAdapter = new RestAdapter.Builder()
                            .setEndpoint(GlobalSocket.serverURL)
                            .setClient(new OkClient(okHttpClient))
                            .build();
                    PostService postService = restAdapter.create(PostService.class);
//                    postService.postAvatar(new TypedFile("image/jpeg", new File(profilePicPath)),
                    postService.postAvatar(new TypedFile("image/jpeg", tempFile),
                            oldIntent.getStringExtra(ProfileFragment.AVATAR_URL),
                            getSharedPreferences(getString(R.string.userdata), MODE_PRIVATE).getString(getString(R.string.token), ""),
                            new Callback<AvatarResponseModel>() {
                                @Override
                                public void success(AvatarResponseModel data, Response response) {
                                    if(data.success) {
                                        //update profile if any
                                        JSONObject emitData = new JSONObject();
                                        try {
                                            emitData.put("disp_name", displayName.getText().toString());
                                            emitData.put("profile_desc", profileDescription.getText().toString());
                                            emitData.put("_event", "onUpdateProfileRespond");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        if(!GlobalSocket.globalEmit("user.edit", emitData)) {
                                            //retry
                                        } else {
                                            //detect loss
                                        }
                                    } else {
                                        switch (data.msg) {
                                            case "token error":
                                                break;
                                            case "old image remove error":
                                                break;
                                            case "cannot write":
                                                break;
                                            case "db error":
                                                break;
                                            default:
                                                break;
                                        }
                                        Toast.makeText(getApplicationContext(), data.msg, Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void failure(RetrofitError error) {
                                    Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    JSONObject emitData = new JSONObject();
                    try {
                        emitData.put("disp_name", displayName.getText().toString());
                        emitData.put("profile_desc", profileDescription.getText().toString());
                        emitData.put("_event", "onUpdateProfileRespond");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(!GlobalSocket.globalEmit("user.edit", emitData)) {
                        //retry
                    } else {
                        //detect loss
                    }
                }
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private String getImagePath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    private void FindAllById() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        profilePic = (ImageView) findViewById(R.id.profile_pic);
        editProfilePic  = (ImageButton) findViewById(R.id.edit_profile_pic);
        displayName = (EditText) findViewById(R.id.display_name);
        profileDescription = (EditText) findViewById(R.id.profile_desc);
        changePass = (Button) findViewById(R.id.change_pass_btn);
    }


}
