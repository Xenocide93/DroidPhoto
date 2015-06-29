package com.droidsans.photo.droidphoto;


import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class FeedFragment extends Fragment {
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int FILTER_FEED = 2;
    public static final int FILL_POST = 4;
    public static final int SELECT_PHOTO = 8;

    private ImageButton profileBtn, browseBtn, eventBtn, floatingSampleBtn;
    private View buttonsLayout, logoLayout, dimView;
    private GridView feedGridView;
    private PictureGridAdapter adapter;
    private ArrayList<PicturePack> feedPicturePack;
    private FloatingActionsMenu fam;
    private FloatingActionButton fabChoosePic, fabCamera;
    private ProgressBar loadingCircle;

    private static String staticPhotoPath;
    private boolean hasImageInPhotoPath;

    private int filterCount;
    private NotifyAdapter packreload[];

    private int firstAtPause;
    private int lastAtPause;
    private boolean notActive = false;

    private FrameLayout frameLayout;

    private Emitter.Listener onGetFeedRespond;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_feed, container, false);
        frameLayout = (FrameLayout) rootView.findViewById(R.id.main_view);
        loadingCircle = (ProgressBar) rootView.findViewById(R.id.loading_circle);
        initialize();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setupFeedAdapter();
        super.onActivityCreated(savedInstanceState);
    }

    private void initialize() {
        findAllById();
        setupListener();
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
        //TODO remove button and use navdrawer instead
        if(!profileBtn.hasOnClickListeners()) {
            profileBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "Profile : " + getActivity().getSharedPreferences(getString(R.string.userdata), Context.MODE_PRIVATE).getString(getString(R.string.display_name), "no display name ??"), Toast.LENGTH_SHORT).show();
                }
            });
        }

        if(!browseBtn.hasOnClickListeners()) {
            browseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browseIntent = new Intent(getActivity(), BrowseVendorActivity.class);
                    startActivityForResult(browseIntent, FILTER_FEED);
                }
            });
        }
        if(!eventBtn.hasOnClickListeners()) {
            eventBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "deprecated", Toast.LENGTH_SHORT).show();
                    Intent eventIntent = new Intent(getActivity(), EventActivity.class);
                    startActivity(eventIntent);
                }
            });
        }
        if(!floatingSampleBtn.hasOnClickListeners()) {
            floatingSampleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent eventIntent = new Intent(getActivity(), FloatingSampleActivity.class);
                    startActivity(eventIntent);
                }
            });
        }
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
                Toast.makeText(getActivity(), "Launch Camera Intent", Toast.LENGTH_SHORT).show();
            }
        });
        fabChoosePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchPicturePickerIntent();
                Toast.makeText(getActivity(), "Launch Picture Picker Intent", Toast.LENGTH_SHORT).show();
            }
        });

        onGetFeedRespond = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        try {
                            if (data.getBoolean("success")) {
                                ArrayList<PicturePack> pack = new ArrayList<PicturePack>();
                                JSONArray photoList = data.getJSONArray("photoList");
                                for (int i = 0; i < photoList.length(); i++) {
                                    Log.d("droidphoto", "photoList(" + i + "):" + ((JSONObject) photoList.get(i)));
                                    try {
                                        String caption = ((JSONObject) photoList.get(i)).has("caption") ?
                                                ((JSONObject) photoList.get(i)).getString("caption") : "";
                                        String eventId = ((JSONObject) photoList.get(i)).has("event_id") ?
                                                ((JSONObject) photoList.get(i)).getString("event_id") : null;
                                        Double gpsLat = ((JSONObject) photoList.get(i)).has("gps_lat") ?
                                                ((JSONObject) photoList.get(i)).getDouble("gps_lat") : Double.MIN_VALUE;
                                        Double gpsLong = ((JSONObject) photoList.get(i)).has("gps_long") ?
                                                ((JSONObject) photoList.get(i)).getDouble("gps_long") : Double.MIN_VALUE;
                                        pack.add(new PicturePack(
                                                ((JSONObject) photoList.get(i)).getString("photo_url"),
                                                ((JSONObject) photoList.get(i)).getString("username"),
                                                caption,
                                                ((JSONObject) photoList.get(i)).getString("vendor"),
                                                ((JSONObject) photoList.get(i)).getString("model"),
                                                eventId,
                                                ((JSONObject) photoList.get(i)).getInt("ranking"),
                                                ((JSONObject) photoList.get(i)).getString("exp_time"),
                                                ((JSONObject) photoList.get(i)).getString("aperture"),
                                                ((JSONObject) photoList.get(i)).getString("iso"),
                                                ((JSONObject) photoList.get(i)).getInt("width"),
                                                ((JSONObject) photoList.get(i)).getInt("height"),
                                                gpsLat,
                                                gpsLong,
                                                ((JSONObject) photoList.get(i)).getBoolean("is_enhanced"),
                                                ((JSONObject) photoList.get(i)).getBoolean("is_flash"),
                                                ((JSONObject) photoList.get(i)).getString("submit_date")));

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (adapter != null) {
                                    //recycle bitmap and reset load state
                                    Log.d("droidphoto", "count before change adapter :" + adapter.getCount());
                                    for (int i = 0; i < adapter.getCount(); i++) {
                                        adapter.getItem(i).resetPackBitmap();
                                        if (packreload[i] != null) packreload[i].cancel(true);
                                    }
                                }
                                adapter = new PictureGridAdapter(getActivity(), R.layout.item_pic, pack);
                                adapter.notifyDataSetChanged();
//                                feedGridView.invalidateViews();

                                loadingCircle.setVisibility(ProgressBar.GONE);
                                frameLayout.setVisibility(FrameLayout.VISIBLE);

                                feedGridView.setAdapter(adapter);
                                packreload = new NotifyAdapter[adapter.getCount()];
                                for (int i = 0; i < 4; i++) {
                                    ((PicturePack) feedGridView.getItemAtPosition(i)).setLoad();
                                    packreload[i] = new NotifyAdapter();
                                    packreload[i].execute(i);
                                }
                                adapter.notifyDataSetChanged();
//                                feedGridView.setAdapter(new PictureGridAdapter(getActivity(), R.layout.item_pic, pack));
//                                feedGridView.requestLayout();
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
        if(!GlobalSocket.mSocket.hasListeners("get_feed")) {
            GlobalSocket.mSocket.on("get_feed", onGetFeedRespond);
        }

        feedGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    for (int visiblePosition = feedGridView.getFirstVisiblePosition(); visiblePosition <= feedGridView.getLastVisiblePosition(); visiblePosition++) {
//                        Log.d("droidphoto", "position: " + visiblePosition);
                        PicturePack pp = (PicturePack) adapter.getItem(visiblePosition);
                        if (!pp.isLoaded) {
                            //((PicturePack) feedGridView.getItemAtPosition(visiblePosition)).setLoad();
//                            if (packreload[visiblePosition] == null) {
                            pp.setLoad();
                            packreload[visiblePosition] = new NotifyAdapter();
                            packreload[visiblePosition].execute(visiblePosition);
//                            }
                        }

                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        feedGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //reset all packreload -> code moved to onPause

                Intent imageViewerIntent = new Intent(getActivity(), ImageViewerActivity.class);
                PicturePack currentPack = adapter.getItem(position);
                imageViewerIntent.putExtra("photoURL", currentPack.photoURL);
                imageViewerIntent.putExtra("caption", currentPack.caption);
                imageViewerIntent.putExtra("vendor", currentPack.vendor);
                imageViewerIntent.putExtra("model", currentPack.model);
                imageViewerIntent.putExtra("exposureTime", currentPack.shutterSpeed);
                imageViewerIntent.putExtra("aperture", currentPack.aperture);
                imageViewerIntent.putExtra("iso", currentPack.iso);
                imageViewerIntent.putExtra("gpsLat", currentPack.gpsLat);
                imageViewerIntent.putExtra("gpsLong", currentPack.gpsLong);
                imageViewerIntent.putExtra("username", currentPack.username);

                startActivity(imageViewerIntent);
            }
        });
    }

    private class NotifyAdapter extends AsyncTask<Integer,Void,String> {
        @Override
        protected String doInBackground(Integer... params) {
            while(!((PicturePack)feedGridView.getItemAtPosition(params[0])).isDoneLoading && ((PicturePack)feedGridView.getItemAtPosition(params[0])).isLoaded) {
                //wait until done download
                //maybe download should be here ??
                //TODO move connection code and download from picturepack to here
            }
            return "done";
        }

        protected void onPostExecute(String result) {
            //tell the gridview to recall getView via adapter
            Log.d("droidphoto", "notify adapter");
            adapter.notifyDataSetChanged();
        }

    }

    private File createFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMDD_HHmmss").format(new Date());
        String imageFileName = "JPG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/droidphoto/");
        storageDir.mkdirs();
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        hasImageInPhotoPath = false;
        staticPhotoPath = image.getAbsolutePath();
        return image;
//        File image = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/droidphoto/", imageFileName + ".jpg");
//        if(image.createNewFile()) {
//            MainActivity.staticPhotoPath = image.getAbsolutePath();
//            return image;
//        }
//        return null;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createFile();
            } catch (IOException e) {
                Toast.makeText(getActivity(), "IOException" + e.toString(), Toast.LENGTH_SHORT).show();
            }

            if(photoFile !=null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void dispatchPicturePickerIntent() {
        Intent picturePickerIntent = new Intent(Intent.ACTION_PICK);
        picturePickerIntent.setType("image/*");
        startActivityForResult(picturePickerIntent, SELECT_PHOTO);
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(new File(staticPhotoPath));
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == getActivity().RESULT_OK)
            switch (requestCode) {
                case FILTER_FEED:
                    int vendorNum = data.getIntExtra(BrowseVendorActivity.VENDOR_NUM, -1);
                    int modelNum = data.getIntExtra(BrowseModelActivity.MODEL_NUM, -1);
                    Snackbar.make(frameLayout, "Vendor: " + vendorNum + " Model: " + modelNum, Snackbar.LENGTH_LONG).show();
                    if(vendorNum!=-1 && modelNum!=-1){
                        //TODO actually record tags and update filterCount
                        JSONObject filter = new JSONObject();
                        JSONArray filterData = new JSONArray();
                        String vendor[] = {"Asus", "WIKO"};
                        String model[] = {"Zenfone 5", "RIDGE"};
                        try {
                            //create filter data
                            filterCount = 2;
                            for(int i = 0; i < filterCount; i++) {
                                JSONObject value = new JSONObject();
                                value.put("vendor", vendor[i]);
                                value.put("model", model[i]);
                                filterData.put(i, value);
                            }
                            filter.put("data", filterData);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        requestFeedPicture(filter);

                        //update feed via socket.io onGetFeedResponse listener
                    }
                    break;

                case SELECT_PHOTO:
                    Toast.makeText(getActivity(), getImagePath(data.getData()), Toast.LENGTH_LONG).show();
                    Intent fillPostFromPicturePickerIntent = new Intent(getActivity(), FillPostActivity.class);
                    fillPostFromPicturePickerIntent.putExtra("photoPath", getImagePath(data.getData()));
                    startActivityForResult(fillPostFromPicturePickerIntent, FILL_POST);
                    break;

                case REQUEST_IMAGE_CAPTURE:
                    Toast.makeText(getActivity(), staticPhotoPath, Toast.LENGTH_LONG).show();
                    galleryAddPic();
                    hasImageInPhotoPath = true;
                    Intent fillPostIntent = new Intent(getActivity(), FillPostActivity.class);
                    fillPostIntent.putExtra("photoPath", staticPhotoPath);
                    startActivityForResult(fillPostIntent, FILL_POST);
                    break;

            }
        else if(resultCode == getActivity().RESULT_CANCELED) {
            Toast.makeText(getActivity(), "cancel",Toast.LENGTH_SHORT).show();
            if(!hasImageInPhotoPath && staticPhotoPath != null) {
                File image = new File(staticPhotoPath);
                if(image.delete()) {
                    hasImageInPhotoPath = false;
                    Toast.makeText(getActivity(),"temp file removed", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(),"cannot remove temp file", Toast.LENGTH_LONG).show();
                }
                staticPhotoPath = null;
            }
        }
    }


    @Override
    public void onPause() {
        //reset all packreload
        if(adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (packreload[i] != null && packreload[i].getStatus() == AsyncTask.Status.RUNNING) {
                    packreload[i].cancel(true);
                    adapter.getItem(i).resetPackBitmap();
                }
            }
            firstAtPause = feedGridView.getFirstVisiblePosition();
            lastAtPause = feedGridView.getLastVisiblePosition();
            notActive = true;
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(notActive) {
            for (int i = firstAtPause; i <= lastAtPause; i++) {
                if (!adapter.getItem(i).isLoaded) {
                    adapter.getItem(i).setLoad();
                    packreload[i] = new NotifyAdapter();
                    packreload[i].execute(i);
                }
            }
            notActive = false;
        }
    }

    @Override
    public void onDestroy() {
        if(GlobalSocket.mSocket.hasListeners("get_feed")) {
            GlobalSocket.mSocket.off("get_feed");
        }

//        if (adapter != null) {
//            //recycle bitmap and reset load state
//            Log.d("droidphoto", "count before destroy : " + adapter.getCount());
//            for (int i = 0; i < adapter.getCount(); i++) {
//                adapter.getItem(i).resetPackBitmap();
//                if (packreload[i] != null) packreload[i].cancel(true);
//            }
//        }

        super.onDestroy();
    }

    private String getImagePath(Uri uri) {
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    private void findAllById() {
        profileBtn = (ImageButton) frameLayout.findViewById(R.id.profile_btn);
        browseBtn = (ImageButton) frameLayout.findViewById(R.id.browse_btn);
        eventBtn = (ImageButton) frameLayout.findViewById(R.id.event_btn);
        floatingSampleBtn = (ImageButton) frameLayout.findViewById(R.id.floating_sample_btn);

        buttonsLayout = frameLayout.findViewById(R.id.btn_layout);
        logoLayout = frameLayout.findViewById(R.id.logo_layout);

        if(feedGridView != null) feedGridView.invalidateViews();
        feedGridView = (GridView) frameLayout.findViewById(R.id.feed_gridview);

        dimView = frameLayout.findViewById(R.id.dim_view);

        fam = (FloatingActionsMenu) frameLayout.findViewById(R.id.fam);
        fabCamera = (FloatingActionButton) frameLayout.findViewById(R.id.fab_camera);
        fabChoosePic = (FloatingActionButton) frameLayout.findViewById(R.id.fab_choosepic);
    }

}
