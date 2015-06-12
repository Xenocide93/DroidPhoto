package com.droidsans.photo.droidphoto;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.droidsans.photo.droidphoto.util.PictureGridAdapter;
import com.droidsans.photo.droidphoto.util.PicturePack;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends Activity {
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int FILTER_FEED = 2;
    public static final int FILL_POST = 4;

    public static Context mContext;

    private ImageButton browseBtn, eventBtn, floatingSampleBtn;
    private View buttonsLayout, logoLayout, dimView;
    private GridView feedGridView;
    private PictureGridAdapter adapter;
    private ArrayList<PicturePack> feedPicturePack;
    private FloatingActionsMenu fam;
    private FloatingActionButton fabChoosePic, fabCamera;

    public static String staticPhotoPath;

    private int filterCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();

        initialize();
    }

    private void initialize() {
        findAllById();
        setupListener();
        setupFeedAdapter();
    }

    private void setupFeedAdapter() {
        filterCount = 0;
        JSONObject filter = new JSONObject();
        JSONObject[] data = new JSONObject[0];

        try {
            filter.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        requestFeedPicture(filter);
    }

    private void requestFeedPicture(JSONObject filter) {
        try {
            filter.put("filter_count", filterCount);
            filter.put("skip", 0);
            filter.put("limit", 40);
            filter.put("sptag", null);
            filter.put("_event", "get_feed");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        GlobalSocket.globalEmit("photo.getfeed", filter);
    }

    private void setupListener() {
        browseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browseIntent = new Intent(getApplicationContext(), BrowseVendorActivity.class);
                startActivityForResult(browseIntent, FILTER_FEED);
            }
        });
        eventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventIntent = new Intent(getApplicationContext(), EventActivity.class);
                startActivity(eventIntent);
            }
        });
        floatingSampleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventIntent = new Intent(getApplicationContext(), FloatingSampleActivity.class);
                startActivity(eventIntent);
            }
        });
        fam.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                dimView.animate().alpha(0.7f).setDuration(300).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        dimView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                }).start();
            }

            @Override
            public void onMenuCollapsed() {
                dimView.animate().alpha(0.0f).setDuration(300).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        dimView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                }).start();

            }
        });
        dimView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam.collapse();
            }
        });


        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();

                Toast.makeText(getApplicationContext(), "Launch Camera Intent", Toast.LENGTH_SHORT).show();
            }
        });
        fabChoosePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO launch choose picture intent

                Toast.makeText(getApplicationContext(), "Launch Picture Picker Intent", Toast.LENGTH_SHORT).show();
            }
        });

        Emitter.Listener onGetFeedRespond = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        try {
                            if(data.getBoolean("success")){
                                ArrayList<PicturePack> pack = new ArrayList<PicturePack>();
                                JSONArray photoList = data.getJSONArray("photoList");
                                for(int i = 0; i < photoList.length(); i++) {
                                    Log.d("droidphoto", "photoList(" + i + "):" + ((JSONObject)photoList.get(i)));
                                    try {
                                        String caption = ((JSONObject)photoList.get(i)).has("caption")?
                                                ((JSONObject)photoList.get(i)).getString("caption"):"";
                                        String eventId = ((JSONObject)photoList.get(i)).has("event_id")?
                                                ((JSONObject)photoList.get(i)).getString("event_id"):null;
                                        Double gpsLat = ((JSONObject)photoList.get(i)).has("gps_lat")?
                                                ((JSONObject)photoList.get(i)).getDouble("gps_lat"): Double.MIN_VALUE;
                                        Double gpsLong = ((JSONObject)photoList.get(i)).has("gps_long")?
                                                ((JSONObject)photoList.get(i)).getDouble("gps_long"):Double.MIN_VALUE;
                                        pack.add(new PicturePack(
                                                ((JSONObject)photoList.get(i)).getString("photo_url"),
                                                ((JSONObject)photoList.get(i)).getString("username"),
                                                caption,
                                                ((JSONObject)photoList.get(i)).getString("vendor"),
                                                ((JSONObject)photoList.get(i)).getString("model"),
                                                eventId,
                                                ((JSONObject)photoList.get(i)).getInt("ranking"),
                                                ((JSONObject)photoList.get(i)).getString("exp_time"),
                                                ((JSONObject)photoList.get(i)).getString("aperture"),
                                                ((JSONObject)photoList.get(i)).getString("iso"),
                                                ((JSONObject)photoList.get(i)).getInt("width"),
                                                ((JSONObject)photoList.get(i)).getInt("height"),
                                                gpsLat,
                                                gpsLong,
                                                ((JSONObject)photoList.get(i)).getBoolean("is_enhanced"),
                                                ((JSONObject)photoList.get(i)).getBoolean("is_flash"),
                                                ((JSONObject)photoList.get(i)).getString("submit_date")));

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                PictureGridAdapter adapter = new PictureGridAdapter(getApplicationContext(), R.layout.item_pic, pack);
                                feedGridView.invalidateViews();
                                feedGridView.setAdapter(adapter);
                                //feedGridView.requestLayout();

                                /*
                                feedGridView.setAdapter(new PictureGridAdapter(
                                        getApplicationContext(),
                                        R.layout.item_pic,
                                        getTestPicturePackArray()));
                                */
                            } else {
                                Log.d("droidphoto", "Feed error: " + data.getString("msg"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        if(!GlobalSocket.mSocket.hasListeners("get_feed")){GlobalSocket.mSocket.on("get_feed", onGetFeedRespond);}
    }

    private File createFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMDD_HHmmss").format(new Date());
        String imageFileName = "JPG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        MainActivity.staticPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createFile();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "IOException", Toast.LENGTH_SHORT).show();
            }

            if(photoFile !=null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(new File(MainActivity.staticPhotoPath));
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK)
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    galleryAddPic();
                    Intent fillPostIntent = new Intent(getApplicationContext(), FillPostActivity.class);
                    fillPostIntent.putExtra("photoPath", MainActivity.staticPhotoPath);
                    startActivityForResult(fillPostIntent, FILL_POST);
                    break;

                case FILTER_FEED:
                    int vendorNum = data.getIntExtra(BrowseVendorActivity.VENDOR_NUM, -1);
                    int modelNum = data.getIntExtra(BrowseModelActivity.MODEL_NUM, -1);
                    if(vendorNum!=-1 && modelNum!=-1){
                        Snackbar.make(findViewById(R.id.main_view), "Vendor: " + vendorNum + " Model: " + modelNum, Snackbar.LENGTH_LONG).show();
                        //TODO get filtered picture feed from server



                        //TODO set feed picture to feed view

                        feedGridView.setAdapter(new PictureGridAdapter(
                                getApplicationContext(),
                                R.layout.item_pic,
                                getTestPicturePackArray()));
                    }
                    break;
            }
        else if(resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "cancel",Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<PicturePack> getTestPicturePackArray(){
        //TODO example
        ArrayList<PicturePack> testPack = new ArrayList<PicturePack>();
        testPack.add(new PicturePack(1, "Sony", "Xperia Z3+", "1/60s", "f2.0", "400"));
        testPack.add(new PicturePack(1, "Samsug", "Galaxy S6", "1/200s", "f1.9", "100"));
        testPack.add(new PicturePack(1, "LG", "G4", "1/10s", "f1.8", "50"));
        testPack.add(new PicturePack(1, "ASUS", "Zenfone 2", "1/250s", "f2.0", "800"));
        testPack.add(new PicturePack(1, "OPPO", "Find 7a", "1/30s", "f2.0", "200"));
        testPack.add(new PicturePack(1, "HTC", "M9+", "1/100s", "f2.0", "200"));
        testPack.add(new PicturePack(1, "Xiaomi", "Mi Note Pro", "1/30s", "f1.8", "200"));
        testPack.add(new PicturePack(1, "Meizu", "M2 Note", "1/20s", "f1.8", "800"));

        return testPack;
    }

    private void findAllById() {
        browseBtn = (ImageButton) findViewById(R.id.browse_btn);
        eventBtn = (ImageButton) findViewById(R.id.event_btn);
        floatingSampleBtn = (ImageButton) findViewById(R.id.floating_sample_btn);

        buttonsLayout = findViewById(R.id.btn_layout);
        logoLayout = findViewById(R.id.logo_layout);

        feedGridView = (GridView) findViewById(R.id.feed_gridview);

        dimView = findViewById(R.id.dim_view);

        fam = (FloatingActionsMenu) findViewById(R.id.fam);
        fabCamera = (FloatingActionButton) findViewById(R.id.fab_camera);
        fabChoosePic = (FloatingActionButton) findViewById(R.id.fab_choosepic);
    }
}
