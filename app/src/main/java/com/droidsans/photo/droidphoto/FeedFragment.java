package com.droidsans.photo.droidphoto;


import android.animation.Animator;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.droidsans.photo.droidphoto.util.FlowLayout;
import com.droidsans.photo.droidphoto.util.FontTextView;
import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.droidsans.photo.droidphoto.util.PictureGridAdapter;
import com.droidsans.photo.droidphoto.util.PicturePack;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

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

    private View logoLayout, dimView;
    private GridView feedGridView;
    private PictureGridAdapter adapter;
    private ArrayList<PicturePack> feedPicturePack;
    private FloatingActionsMenu fam;
    private FloatingActionButton fabChoosePic, fabCamera;

    private FontTextView reloadText;
    private Button reloadButton;

    private FrameLayout frameLayout;
    private LinearLayout reloadLayout;
    private ProgressBar loadingCircle;
    private FrameLayout imageViewLayout;

    private FlowLayout tagLayout;
    private ImageButton removeTagBtn;
    private boolean isRemoveTagActive = false;

    private static String staticPhotoPath;
    private boolean hasImageInPhotoPath;

    private boolean isLoaded;
    private int filterCount;
//    private NotifyAdapter packreload[];

//    private int firstAtPause;
//    private int lastAtPause;
//    private boolean notActive = false;

    private Emitter.Listener onGetFeedRespond;
    private Emitter.Listener onDisconnect;

    private Handler delayAction = new Handler();

    private ArrayList<TagView> tagViewArray;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_feed, container, false);
        frameLayout = (FrameLayout) rootView.findViewById(R.id.main_view);
        reloadLayout = (LinearLayout) rootView.findViewById(R.id.reload_view);
        loadingCircle = (ProgressBar) rootView.findViewById(R.id.loading_circle);
        dimView = rootView.findViewById(R.id.dim_view);
//        imageViewLayout = (FrameLayout) rootView.findViewById(R.id.image_viewer);
        initialize();
        Toast.makeText(getActivity().getApplicationContext(), "onCreateView", Toast.LENGTH_SHORT).show();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setupFeedAdapter();
        Toast.makeText(getActivity().getApplicationContext(), "onActivityCreated", Toast.LENGTH_SHORT).show();
        super.onActivityCreated(savedInstanceState);
    }

    private void initialize() {
        GlobalSocket.initializeSocket();
        findAllById();
        setupListener();
        initializeVendorModelLsit();
    }

    private void initializeVendorModelLsit() {
        tagViewArray = new ArrayList<TagView>();
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
        if(!GlobalSocket.globalEmit("photo.getfeed", filter)) {
            Toast.makeText(getActivity().getApplicationContext(),"cannot fire getfeed: retry in 3s", Toast.LENGTH_SHORT).show();
//            wait 4 sec and try globalemit again
            final JSONObject delayedfilter = filter;
            delayAction.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!GlobalSocket.globalEmit("photo.getfeed", delayedfilter)) {
                        initReload(); //if fail twice
                    } else {
                        GlobalSocket.mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
                    }
                }
            }, 4000);
        } else {
            //can emit: detect loss on the way
            GlobalSocket.mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        }

    }

    private void setupListener() {
        removeTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRemoveTagActive = !isRemoveTagActive;
                if(isRemoveTagActive){
                    removeTagBtn.setImageResource(R.drawable.remove_tag_pressed);
                    removeTagBtn.animate().scaleX(1.3f).scaleY(1.3f).setDuration(200).start();
                } else {
                    removeTagBtn.setImageResource(R.drawable.remove_tag_normal);
                    removeTagBtn.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
                    removeTag();
                    updateTagView();
                    updateFeed();
                }
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

        onDisconnect = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GlobalSocket.mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
                        initReload();
                    }
                });

            }
        };

        onGetFeedRespond = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GlobalSocket.mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
                        JSONObject data = (JSONObject) args[0];
                        if (data.optBoolean("success")) {
                            ArrayList<PicturePack> pack = new ArrayList<>();
                            JSONArray photoList = data.optJSONArray("photoList");
                            for (int i = 0; i < photoList.length(); i++) {
//                                    Log.d("droidphoto", "photoList(" + i + "):" + ((JSONObject) photoList.get(i)));
                                JSONObject jsonPack = photoList.optJSONObject(i);
                                PicturePack picturePack = new PicturePack();

                                picturePack.setPhotoId(jsonPack.optString("_id"));
                                picturePack.setPhotoURL(jsonPack.optString("photo_url"));
                                picturePack.setUserId(jsonPack.optString("user_id"));
                                picturePack.setUsername(jsonPack.optString("username"));
                                picturePack.setCaption(jsonPack.optString("caption", ""));
                                picturePack.setVendor(jsonPack.optString("vendor"));
                                picturePack.setModel(jsonPack.optString("model"));
                                picturePack.setEventId(jsonPack.optString("event_id"));
                                picturePack.setRank(jsonPack.optInt("ranking"));
                                picturePack.setShutterSpeed(jsonPack.optString("exp_time"));
                                picturePack.setAperture(jsonPack.optString("aperture"));
                                picturePack.setIso(jsonPack.optString("iso"));
                                picturePack.setWidth(jsonPack.optInt("width"));
                                picturePack.setHeight(jsonPack.optInt("height"));
//                                picturePack.setGpsLat(jsonPack.optDouble("gps_lat"));
//                                picturePack.setGpsLong(jsonPack.optDouble("gps_long"));
                                picturePack.setGpsLocation(jsonPack.optString("gps_location"));
                                picturePack.setGpsLocalizedLocation(jsonPack.optString("gps_localized"));
                                picturePack.setIsEnhanced(jsonPack.optBoolean("is_enhanced"));
                                picturePack.setIsFlash(jsonPack.optBoolean("is_flash"));
                                picturePack.setSubmitDate(jsonPack.optString("submit_date"));

                                pack.add(picturePack);
                            }

                            adapter = new PictureGridAdapter(getActivity(), R.layout.item_pic, pack);

                            if (loadingCircle.getVisibility() == ProgressBar.VISIBLE) {
                                loadingCircle.setVisibility(ProgressBar.GONE);
                                frameLayout.setVisibility(FrameLayout.VISIBLE);
                            }

                            feedGridView.setAdapter(adapter);
//                            adapter.notifyDataSetChanged();
//                                feedGridView.setAdapter(new PictureGridAdapter(getActivity(), R.layout.item_pic, pack));
//                                feedGridView.requestLayout();
                        } else {
                            Log.d("droidphoto", "Feed error: " + data.optString("msg"));
                            initReload();
                        }
                    }
                });
            }
        };
        if(!GlobalSocket.mSocket.hasListeners("get_feed")) {
            GlobalSocket.mSocket.on("get_feed", onGetFeedRespond);
        }
//
//        feedGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
//                    for (int visiblePosition = feedGridView.getFirstVisiblePosition(); visiblePosition <= feedGridView.getLastVisiblePosition(); visiblePosition++) {
////                        Log.d("droidphoto", "position: " + visiblePosition);
//                        PicturePack pp = (PicturePack) adapter.getItem(visiblePosition);
//                        if (!pp.isLoaded) {
//                            //((PicturePack) feedGridView.getItemAtPosition(visiblePosition)).setLoad();
////                            if (packreload[visiblePosition] == null) {
//                            pp.setLoad();
//                            packreload[visiblePosition] = new NotifyAdapter();
//                            packreload[visiblePosition].execute(visiblePosition);
////                            }
//                        }
//
//                    }
//                }
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//            }
//        });

        feedGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //reset all packreload -> code moved to onPause
//                PicturePack currentPack = adapter.getItem(position);
//                Fragment imageViewerFragment = new ImageViewerFragment();
//
//                Bundle args = new Bundle();
//                args.putString("photoId", currentPack.photoId);
//                args.putString("photoURL", currentPack.photoURL);
//                args.putString("caption", currentPack.caption);
//                args.putString("vendor", currentPack.vendor);
//                args.putString("model", currentPack.model);
//                args.putString("exposureTime", currentPack.shutterSpeed);
//                args.putString("aperture", currentPack.aperture);
//                args.putString("iso", currentPack.iso);
//                args.putString("userId", currentPack.userId);
//                args.putString("username", currentPack.username);
//                args.putString("gpsLocation", currentPack.gpsLocation);
//                args.putString("gpsLocalized", currentPack.gpsLocalizedLocation);
//
//                imageViewerFragment.setArguments(args);
//
//                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//                transaction.replace(R.id.main_fragment, imageViewerFragment, "IMAGE_VIEWER");
//                transaction.addToBackStack(null);
//                transaction.commit();
//

                Intent imageViewerIntent = new Intent(getActivity(), ImageViewerActivity.class);
                PicturePack currentPack = adapter.getItem(position);
                imageViewerIntent.putExtra("photoId", currentPack.photoId);
                imageViewerIntent.putExtra("photoURL", currentPack.photoURL);
                imageViewerIntent.putExtra("caption", currentPack.caption);
                imageViewerIntent.putExtra("vendor", currentPack.vendor);
                imageViewerIntent.putExtra("model", currentPack.model);
                imageViewerIntent.putExtra("exposureTime", currentPack.shutterSpeed);
                imageViewerIntent.putExtra("aperture", currentPack.aperture);
                imageViewerIntent.putExtra("iso", currentPack.iso);
                imageViewerIntent.putExtra("userId", currentPack.userId);
                imageViewerIntent.putExtra("username", currentPack.username);
                imageViewerIntent.putExtra("gpsLocation", currentPack.gpsLocation);
                imageViewerIntent.putExtra("gpsLocalized", currentPack.gpsLocalizedLocation);

                startActivity(imageViewerIntent);

//                String transitionName = getString(R.string.transition_image_view);
//                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), feedGridView, transitionName);
//                ActivityCompat.startActivity(getActivity(),imageViewerIntent, options.toBundle());
            }
        });
    }

    private void initReload() {
        loadingCircle.setVisibility(ProgressBar.GONE);
        reloadLayout.setVisibility(LinearLayout.VISIBLE);
        reloadText.setText("Error loading feed :(");
        if(!reloadButton.hasOnClickListeners()) reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadButton.setClickable(false);
                reloadLayout.setVisibility(LinearLayout.GONE);
                loadingCircle.setVisibility(ProgressBar.VISIBLE);
                GlobalSocket.reconnect(); //reconnect
                setupFeedAdapter();
            }
        });
        reloadButton.setClickable(true);
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
                    String vendorName = data.getStringExtra(BrowseVendorActivity.VENDOR_NAME);
                    String modelName = data.getStringExtra(BrowseModelActivity.MODEL_NAME);
                    Snackbar.make(frameLayout, "Vendor: " + vendorName + " Model: " + modelName, Snackbar.LENGTH_LONG).show();
                    if(!vendorName.equals("") && !modelName.equals("")){
                        //display existed filter tag
                        for(int i = 0; i < tagViewArray.size(); i++){
                            TagView view = tagViewArray.get(i);
                            if(vendorName.equals(view.vendorName) && modelName.equals(view.modelName)) return;
                        }
                        addTag(vendorName, modelName);
                        updateTagView();

                        //prepare data for refresh feed
                        updateFeed();
                    }
                    break;

                case SELECT_PHOTO:
                    String path = getImagePath(data.getData());
                    Toast.makeText(getActivity(), path, Toast.LENGTH_LONG).show();
                    Intent fillPostFromPicturePickerIntent = new Intent(getActivity(), FillPostActivity.class);
                    fillPostFromPicturePickerIntent.putExtra("photoPath", path);
                    fillPostFromPicturePickerIntent.putExtra("imageFrom", "Picture Picker");
                    startActivityForResult(fillPostFromPicturePickerIntent, FILL_POST);
                    break;

                case REQUEST_IMAGE_CAPTURE:
                    Toast.makeText(getActivity(), staticPhotoPath, Toast.LENGTH_LONG).show();
                    galleryAddPic();
                    hasImageInPhotoPath = true;
                    File f = new File(staticPhotoPath);
                    if(f.length() > 0) {
                        Intent fillPostIntent = new Intent(getActivity(), FillPostActivity.class);
                        fillPostIntent.putExtra("photoPath", staticPhotoPath);
                        fillPostIntent.putExtra("imageFrom", "Camera");
                        startActivityForResult(fillPostIntent, FILL_POST);
                        break;
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "invalid image", Toast.LENGTH_SHORT).show();
                        //TODO remove photo entry from mediastore or rescan
                        hasImageInPhotoPath = false;
                        staticPhotoPath = null;
                        break;
                    }

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

//
//    @Override
//    public void onPause() {
//        //reset all packreload
//        if(adapter != null) {
//            for (int i = 0; i < adapter.getCount(); i++) {
//                if (packreload[i] != null && packreload[i].getStatus() == AsyncTask.Status.RUNNING) {
//                    packreload[i].cancel(true);
//                    adapter.getItem(i).resetPackBitmap();
//                }
//            }
//            firstAtPause = feedGridView.getFirstVisiblePosition();
//            lastAtPause = feedGridView.getLastVisiblePosition();
//            notActive = true;
//        }
//        super.onPause();
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        if(notActive) {
//            for (int i = firstAtPause; i <= lastAtPause; i++) {
//                if (!adapter.getItem(i).isLoaded) {
//                    adapter.getItem(i).setLoad();
//                    packreload[i] = new NotifyAdapter();
//                    packreload[i].execute(i);
//                }
//            }
//            notActive = false;
//        }
//    }

    @Override
    public void onDestroy() {
        if(GlobalSocket.mSocket.hasListeners("get_feed")) {
            GlobalSocket.mSocket.off("get_feed");
        }
        GlobalSocket.mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);

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
        tagLayout = (FlowLayout) frameLayout.findViewById(R.id.tag_layout);
        logoLayout = frameLayout.findViewById(R.id.logo_layout);
        removeTagBtn = (ImageButton) frameLayout.findViewById(R.id.remove_tag_button);

        if(feedGridView != null) feedGridView.invalidateViews();
        feedGridView = (GridView) frameLayout.findViewById(R.id.feed_gridview);

        fam = (FloatingActionsMenu) frameLayout.findViewById(R.id.fam);
        fabCamera = (FloatingActionButton) frameLayout.findViewById(R.id.fab_camera);
        fabChoosePic = (FloatingActionButton) frameLayout.findViewById(R.id.fab_choosepic);

        reloadText = (FontTextView) reloadLayout.findViewById(R.id.reload_text);
        reloadButton = (Button) reloadLayout.findViewById(R.id.reload_button);
    }

    private void updateFeed(){
        JSONObject filter = new JSONObject();
        JSONArray filterData = new JSONArray();

        try {
            //create filter data
            filterCount = tagViewArray.size();
            for(int i = 0; i < filterCount; i++) {
                JSONObject value = new JSONObject();
                value.put("vendor", tagViewArray.get(i).vendorName);
                value.put("model", tagViewArray.get(i).modelName);
                filterData.put(i, value);
            }
            filter.put("data", filterData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        requestFeedPicture(filter);
    }

    public void launchAddFilterPopup(){
        Intent browseIntent = new Intent(getActivity(), BrowseVendorActivity.class);
        startActivityForResult(browseIntent, FILTER_FEED);
    }

    private void updateTagView(){
        tagLayout.removeAllViews();
        for(int i = 0; i < tagViewArray.size(); i++){
            tagLayout.addView(tagViewArray.get(i).getTagView());
        }
    }

    private void addTag(String vendorName, String modelName){
        TagView tagView = new TagView(tagLayout, vendorName, modelName);
        tagView.tagIndex = tagViewArray.size();
        tagViewArray.add(tagView);
    }

    private void removeTag(){
        ArrayList<TagView> tempArray = new ArrayList<TagView>();
        for(int i = 0; i < tagViewArray.size(); i++){
            if(!tagViewArray.get(i).selected){tempArray.add(tagViewArray.get(i));}
        }
        tagViewArray = tempArray;
        updateTagView();
    }

    public class TagView {
        public String vendorName, modelName;
        private TextView vendorTV, modelTV;
        private LinearLayout tagWrapper;
        private View tagView;
        private LayoutInflater inflater;
        private int tagIndex;
        private boolean selected = false;

        public TagView(FlowLayout tagLayout, String vendorName, String modelName){
            this.vendorName = vendorName;
            this.modelName = modelName;

            if(inflater==null){inflater = LayoutInflater.from(getActivity());}
            tagView = inflater.inflate(R.layout.filter_tag, null, false);

            tagWrapper = (LinearLayout) tagView.findViewById(R.id.tag_wrapper);
            vendorTV = (TextView) tagView.findViewById(R.id.vendor_tag);
            vendorTV.setText(vendorName);
            modelTV = (TextView) tagView.findViewById(R.id.model_tag);
            modelTV.setText(modelName);

            setOnClickListener();
        }

        public View getTagView(){
            return tagView;
        }

        public void setOnClickListener(){
            tagView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isRemoveTagActive){
                        selected = !selected;
                        if(selected){
                            tagWrapper.setBackgroundResource(R.drawable.curve_primary_normal);
                            vendorTV.setTextColor(getResources().getColor(R.color.white));
                            modelTV.setTextColor(getResources().getColor(R.color.white));
                        } else {
                            tagWrapper.setBackgroundResource(R.drawable.curve_primary_border);
                            vendorTV.setTextColor(getResources().getColor(R.color.primary_color));
                            modelTV.setTextColor(getResources().getColor(R.color.primary_color));
                        }
                    }
                }
            });
        }
    }

}
