package com.droidsans.photo.droidphoto;


import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.droidsans.photo.droidphoto.util.FeedRecycleViewAdapter;
import com.droidsans.photo.droidphoto.util.FlowLayout;
import com.droidsans.photo.droidphoto.util.FontTextView;
import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.droidsans.photo.droidphoto.util.PicturePack;
import com.droidsans.photo.droidphoto.util.SpacesItemDecoration;
import com.droidsans.photo.droidphoto.util.SquareImageView;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Target;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import tourguide.tourguide.Overlay;
import tourguide.tourguide.Pointer;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;


/**
 * A simple {@link Fragment} subclass.
 */
public class FeedFragment extends Fragment {
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int FILTER_FEED = 2;
    public static final int FILL_POST = 4;
    public static final int SELECT_PHOTO = 8;
    public static final String FIRST_TIME_FEED_FRAGMENT = "firstTimeFeedFragment";

    private View logoLayout, dimView;
//    private GridView feedGridView;
    private RecyclerView feedRecycleView;
//    private PictureGridAdapter adapter;
    private FeedRecycleViewAdapter recycleAdapter;
    private ArrayList<PicturePack> feedPicturePack;
    private FloatingActionMenu fam;
    private FloatingActionButton fabChoosePic, fabCamera;

    private FontTextView reloadText;
    private Button reloadButton;

    private FrameLayout frameLayout;
    private LinearLayout reloadLayout;
    private ProgressBar loadingCircle;
    private FrameLayout imageViewLayout;

    private RelativeLayout tagField;
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
    private Emitter.Listener onGetDeviceListRespond;

    private Handler delayAction = new Handler();
    private Runnable timeout;
    private Runnable loop;

    private ArrayList<TagView> tagViewArray;

    public static FeedFragment mFeedFragment;

    private TourGuide tutorialHandler;
    private ArrayList<View> tutorialViewList;
    private ArrayList<String> tutorialStringList;
    private int nextTutorial = 1;

    public static int percentage = 0;
    public static boolean isFailedToUpload = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_feed_recycle, container, false);

        //set toolbar to be overwrite by this fragment toolbar
        setHasOptionsMenu(true);

        frameLayout = (FrameLayout) rootView.findViewById(R.id.main_view);
        reloadLayout = (LinearLayout) rootView.findViewById(R.id.reload_view);
        loadingCircle = (ProgressBar) rootView.findViewById(R.id.loading_circle);
//        imageViewLayout = (FrameLayout) rootView.findViewById(R.id.image_viewer);
        initialize();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        initializeVendorModelList();
        updateTagView();
        initRequestFeed();
        initLoading();

        super.onActivityCreated(savedInstanceState);
    }

    private void initialize() {
        findAllById();
        setupRecycleView();
        setupListener();
        setupEmitterListener();
    }

    private void checkFirstTimeLaunch() { //call from onGetFeedRespond
        if(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(FIRST_TIME_FEED_FRAGMENT, true)){
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                    .putBoolean(FIRST_TIME_FEED_FRAGMENT, false).apply();

            // setup enter and exit animation
            Animation enterAnimation = new AlphaAnimation(0f, 1f);
            enterAnimation.setDuration(600);
            enterAnimation.setFillAfter(true);

            Animation exitAnimation = new AlphaAnimation(1f, 0f);
            exitAnimation.setDuration(600);
            exitAnimation.setFillAfter(true);

            //set tooltip list
            tutorialViewList = new ArrayList<>();
            tutorialStringList = new ArrayList<>();
            tutorialViewList.add(fam.getMenuIconView());
            tutorialStringList.add(getResources().getString(R.string.tutorial_string_fam));
            tutorialViewList.add(getActivity().getWindow().getDecorView().findViewById(R.id.action_filter));
            tutorialStringList.add(getResources().getString(R.string.tutorial_string_filter));

            //launch first tooltip (auto launch the rest)
            tutorialHandler = TourGuide.init(getActivity()).with(TourGuide.Technique.Click)
                    .setToolTip(new ToolTip()
                        .setTitle(tutorialStringList.get(0))
                        .setDescription("Touch to dismiss")
                        .setGravity(Gravity.LEFT|Gravity.TOP))
                    .setOverlay(new Overlay()
                        .setEnterAnimation(enterAnimation)
                        .setExitAnimation(exitAnimation))
                    .playOn(tutorialViewList.get(0));

            tutorialHandler.setOnToolTipClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showNextTutorial();
                }
            });

            setupListener();
        }
    }

    private void showNextTutorial(){
        if(tutorialHandler==null){
            tutorialHandler = TourGuide.init(getActivity()).with(TourGuide.Technique.Click);
        }

        if(nextTutorial>=tutorialViewList.size()){
            tutorialHandler.cleanUp();
            return;
        }

        tutorialHandler.cleanUp();
        tutorialHandler.setToolTip(new ToolTip()
                .setTitle(tutorialStringList.get(nextTutorial))
                .setDescription("Touch to dismiss")
                .setGravity(Gravity.LEFT | Gravity.BOTTOM))
                .playOn(tutorialViewList.get(nextTutorial));

        nextTutorial++;
    }

    private void setupRecycleView() {
        feedRecycleView.addItemDecoration(new SpacesItemDecoration(
                getActivity(),
                getResources().getInteger(R.integer.main_feed_col_num),
                (int) getResources().getDimension(R.dimen.feed_recycleview_item_space),
                false, false, false, false
        ));
        feedRecycleView.setLayoutManager(new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.main_feed_col_num)));
    }

    private void initializeVendorModelList() {
        tagViewArray = new ArrayList<>();
        final JSONObject requestStuff = new JSONObject();
        try {
            requestStuff.put("_event", "get_device_list");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(!GlobalSocket.globalEmit("db.getdevicelist", requestStuff)) {
            delayAction.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!GlobalSocket.globalEmit("db.getdevicelist", requestStuff)) {
                        // :(
                    }
                }
            }, 1500);
        }
    }

    private void initRequestFeed() {
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
        removeTagBtn.setVisibility(filterCount == 0 ? LinearLayout.GONE : LinearLayout.VISIBLE);

        try {
            filter.put("filter_count", filterCount);
            filter.put("skip", 0);
            filter.put("limit", 100);
            filter.put("sptag", null);
            filter.put("_event", "get_feed");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(!GlobalSocket.globalEmit("photo.getfeed", filter)) { //wait 4 sec and try globalemit again
            final JSONObject delayedfilter = filter;
            delayAction.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!GlobalSocket.globalEmit("photo.getfeed", delayedfilter)) { //if fail twice
                        initReload();
                    } else {
                        GlobalSocket.mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
                    }
                }
            }, 3000);
        } else {
            //can emit: detect loss on the way
            GlobalSocket.mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            //set timeout delay
            delayAction.postDelayed(timeout, 6000);
        }
    }

    private void setupListener() {
        removeTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRemoveTagActive = !isRemoveTagActive;
                if (isRemoveTagActive) {
                    removeTagBtn.setImageResource(R.drawable.remove_tag_pressed);
                    removeTagBtn.animate().scaleX(1.15f).scaleY(1.15f).setDuration(150).start();
                } else {
                    removeTagBtn.setImageResource(R.drawable.remove_tag_normal);
                    removeTagBtn.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
                    if(removeTag()){
                        updateTagView();
                        updateFeed();
                        initLoading();
                    }
                }
            }
        });

        fam.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean open) {
                if (open) {
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
                } else {
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
            }
        });
        dimView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam.close(true);
            }
        });

        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
//                Toast.makeText(getActivity(), "Launch Camera Intent", Toast.LENGTH_SHORT).show();
                fam.close(true);
            }
        });
        fabChoosePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchPicturePickerIntent();
//                Toast.makeText(getActivity(), "Launch Picture Picker Intent", Toast.LENGTH_SHORT).show();
                fam.close(true);
            }
        });

    }

    private void setupEmitterListener(){

        onDisconnect = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("droidphoto", "FeedFragment: disconnected");
                        GlobalSocket.mSocket.off(Socket.EVENT_DISCONNECT);
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
                        GlobalSocket.mSocket.off(Socket.EVENT_DISCONNECT);
                        delayAction.removeCallbacks(timeout);
                        JSONObject data = (JSONObject) args[0];
                        if (data.optBoolean("success")) {
                            feedPicturePack = new ArrayList<>();
                            JSONArray photoList = data.optJSONArray("photoList");
                            int len = photoList.length();
                            for (int i = 0; i < len; i++) {
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

                                feedPicturePack.add(picturePack);
                            }

                            if (loadingCircle.getVisibility() == ProgressBar.VISIBLE) {
                                loadingCircle.setVisibility(ProgressBar.GONE);
                                frameLayout.setVisibility(FrameLayout.VISIBLE);
                            }

                            //setup tranditional recycle view adapter
//                            adapter = new PictureGridAdapter(getActivity(), R.layout.item_feed_pic, feedPicturePack);
//                            feedGridView.setAdapter(adapter);

                            //setup new recycle view adapter
                            recycleAdapter = new FeedRecycleViewAdapter(getActivity(), feedPicturePack);
                            feedRecycleView.setAdapter(recycleAdapter);

                            checkFirstTimeLaunch();

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

        onGetDeviceListRespond = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        if(data.optBoolean("success")) {
                            try {
                                //set vendor list
                                BrowseVendorActivity.vendorName = data.optJSONArray("vendorList").join(",").replaceAll("\"", "").split(",");
                                JSONArray models = data.optJSONArray("modelList");

                                //set model list
                                BrowseModelActivity.modelName = new String[models.length()][];
                                for(int i = 0; i < models.length(); i++) {
                                    BrowseModelActivity.modelName[i] = ((JSONArray) models.get(i)).join(",").replaceAll("\"", "").split(",");
                                }

                                //set drawable for vendor
                                int vendorSize = BrowseVendorActivity.vendorName.length;
                                BrowseVendorActivity.vendorPicResource = new Integer[vendorSize];
                                for(int i = 0; i < vendorSize; i++) {
                                    switch (BrowseVendorActivity.vendorName[i]) {
                                        case "Asus":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.asus_300;
                                            break;
                                        case "OPPO":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.oppo_300;
                                            break;
                                        case "WIKO":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.wiko_300;
                                            break;
                                        case "LG":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.lg_300;
                                            break;
                                        case "Sony":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.sony_300;
                                            break;
                                        case "Samsung":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.samsung_300;
                                            break;
                                        default:
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.default_300;
                                            break;
                                    }
                                }
                                Log.d("droidphoto", "done init vendor model list");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            switch (data.optString("msg")) {
                                case "db error":
                                    //sad
                                    break;
                                case "no list":
                                    //that's okay :o
                                    break;
                                default:
                                    //fuck you
                                    break;
                            }
                        }
                    }
                });
            }
        };

        if(!GlobalSocket.mSocket.hasListeners("get_device_list")) {
            GlobalSocket.mSocket.on("get_device_list", onGetDeviceListRespond);
        }

        timeout = new Runnable() {
            @Override
            public void run() {
                GlobalSocket.mSocket.disconnect();
            }
        };

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

//        feedGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                feedGridView.setClickable(false);
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

//                Intent imageViewerIntent = new Intent(getActivity(), ImageViewerActivity.class);
//                PicturePack currentPack = adapter.getItem(position);
//                imageViewerIntent.putExtra("photoId", currentPack.photoId);
//                imageViewerIntent.putExtra("photoURL", currentPack.photoURL);
//                imageViewerIntent.putExtra("caption", currentPack.caption);
//                imageViewerIntent.putExtra("vendor", currentPack.vendor);
//                imageViewerIntent.putExtra("model", currentPack.model);
//                imageViewerIntent.putExtra("exposureTime", currentPack.shutterSpeed);
//                imageViewerIntent.putExtra("aperture", currentPack.aperture);
//                imageViewerIntent.putExtra("iso", currentPack.iso);
//                imageViewerIntent.putExtra("userId", currentPack.userId);
//                imageViewerIntent.putExtra("username", currentPack.username);
//                imageViewerIntent.putExtra("gpsLocation", currentPack.gpsLocation);
//                imageViewerIntent.putExtra("gpsLocalized", currentPack.gpsLocalizedLocation);
//
//                startActivity(imageViewerIntent);

//                String transitionName = getString(R.string.transition_image_view);
//                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), feedGridView, transitionName);
//                ActivityCompat.startActivity(getActivity(),imageViewerIntent, options.toBundle());
//            }
//        });
    }

    private void initLoading() {
        loadingCircle.setVisibility(ProgressBar.VISIBLE);
        reloadLayout.setVisibility(LinearLayout.GONE);
        frameLayout.setVisibility(FrameLayout.GONE);
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
                initRequestFeed();
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
                new AlertDialog.Builder(getActivity())
                        .setTitle("Cannot Launch Camera")
                        .setMessage("Please insert SD Card and then try again.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
//                            .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_feed, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_search:
//                Toast.makeText(getActivity(), "search", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_filter:
                launchAddFilterPopup();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == FragmentActivity.RESULT_OK)
            switch (requestCode) {
                case FILTER_FEED:
                    String vendorName = data.getStringExtra(BrowseVendorActivity.VENDOR_NAME);
                    String modelName = data.getStringExtra(BrowseModelActivity.MODEL_NAME);
                    Snackbar.make(frameLayout, "Vendor: " + vendorName + " Model: " + modelName, Snackbar.LENGTH_SHORT).show();
                    if(!vendorName.equals("") && !modelName.equals("")){
                        //display existed filter tag
                        //if already existed, return
                        for(TagView view : tagViewArray) {
//                        for(int i = 0; i < tagViewArray.size(); i++){
//                            TagView view = tagViewArray.get(i);
                            if(vendorName.equals(view.vendorName) && modelName.equals(view.modelName)) return;
                        }
                        addTag(vendorName, modelName);
                        updateTagView();

                        //prepare data for refresh feed
                        updateFeed();
                        initLoading();
                    }
                    break;

                case SELECT_PHOTO:
                    String path = getImagePath(data.getData());
//                    Toast.makeText(getActivity(), path, Toast.LENGTH_LONG).show();
                    Intent fillPostFromPicturePickerIntent = new Intent(getActivity(), FillPostActivity.class);
                    fillPostFromPicturePickerIntent.putExtra("photoPath", path);
                    fillPostFromPicturePickerIntent.putExtra("imageFrom", "Picture Picker");
                    startActivityForResult(fillPostFromPicturePickerIntent, FILL_POST);
                    break;

                case REQUEST_IMAGE_CAPTURE:
//                    Toast.makeText(getActivity(), staticPhotoPath, Toast.LENGTH_LONG).show();
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
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Invalid Image")
                                .setMessage("Selected image has no data.\nPlease select other image.")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();
                        //TODO remove photo entry from mediastore or rescan
                        hasImageInPhotoPath = false;
                        staticPhotoPath = null;
                        break;
                    }

                case FILL_POST:
                    PicturePack uploadingPicturePack = new PicturePack(
                            getActivity().getApplicationContext().getSharedPreferences(getString(R.string.userdata), Context.MODE_PRIVATE)
                                    .getString(getString(R.string.username), ""),
                            data.getStringExtra("vendor"),
                            data.getStringExtra("model"),
                            "", "", ""
                    );
                    uploadingPicturePack.setIsUploading(true, data.getStringExtra("path"));
                    feedPicturePack.add(0, uploadingPicturePack);
                    recycleAdapter.notifyDataSetChanged();

                    View uploadingView = feedRecycleView.getChildAt(0);
                    SquareImageView picture = (SquareImageView) uploadingView.findViewById(R.id.picture);
                    final ProgressBar progressBar = (ProgressBar) uploadingView.findViewById(R.id.upload_progress);
//                    FontTextView deviceName = (FontTextView) uploadingView.findViewById(R.id.device_name);
//                    FontTextView username = (FontTextView) uploadingView.findViewById(R.id.user);

                    Glide.with(getActivity().getApplicationContext())
                            .load(data.getStringExtra("path"))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .centerCrop()
                            .placeholder(R.drawable.droidsans_logo)
                            .into(picture);

                    final int loopdelay = 500;
                    loop = new Runnable() {
                        @Override
                        public void run() {
                            if(isFailedToUpload) { //show failed
                                feedPicturePack.remove(0);
                                recycleAdapter.notifyDataSetChanged();
                                Snackbar.make(frameLayout, "upload failed", Snackbar.LENGTH_LONG).show();
                            } else {
                                if (percentage < 100) {//update upload progress
//                                    Log.d("droidphoto", "uploaded : " + percentage + "%");
                                    progressBar.setProgress(percentage);
                                    delayAction.postDelayed(loop, loopdelay);
                                } else { //upload done
                                    progressBar.setProgress(percentage);
                                    updateFeed();
                                    Snackbar.make(frameLayout, "upload success", Snackbar.LENGTH_LONG).show();
                                }
                            }
                        }
                    };
                    delayAction.postDelayed(loop, loopdelay);
//                    Snackbar.make(frameLayout, "uploading...", Snackbar.LENGTH_LONG).show();
                    break;
            }
        else if(resultCode == FragmentActivity.RESULT_CANCELED) {
            if(data != null && data.hasExtra("return code")) {
                switch (data.getStringExtra("return code")) {
                    case "not your photo":
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Not Your Image")
                                .setMessage("Please select a photo taken by this phone.")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .setIcon(R.drawable.ic_error_outline_black_24dp)
                                .show();
                        break;
                    case "no exif":
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Not From Camera")
                                .setMessage("Selected image must have exif.")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();
                        break;
                    case "cannot detect photo owner":
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Error")
                                .setMessage("Cannot detect photo owner (bug ??).")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();
                        break;
                    default:
                        break;
                }
            } else {
                removeTemp();
            }
        }
    }

    private void removeTemp() {
        if (!hasImageInPhotoPath && staticPhotoPath != null) {
            File image = new File(staticPhotoPath);
            if (image.delete()) {
                hasImageInPhotoPath = false;
//                Toast.makeText(getActivity(), "temp file removed", Toast.LENGTH_LONG).show();
            } else {
//                Toast.makeText(getActivity(), "cannot remove temp file", Toast.LENGTH_LONG).show();
            }
            staticPhotoPath = null;
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
    public void onStop() {
//        feedGridView.setClickable(false);
        super.onStop();
    }

    @Override
    public void onStart() {
//        feedGridView.setClickable(true);
        FeedRecycleViewAdapter.isClickOnce = false;
        super.onStart();
    }

    @Override
    public void onAttach(Activity activity) {
        mFeedFragment = this;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mFeedFragment = null;
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        if(GlobalSocket.mSocket.hasListeners("get_feed")) {
            GlobalSocket.mSocket.off("get_feed");
        }
        if(GlobalSocket.mSocket.hasListeners("get_device_list")) {
            GlobalSocket.mSocket.off("get_device_list");
        }
        GlobalSocket.mSocket.off(Socket.EVENT_DISCONNECT);

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
        tagField = (RelativeLayout) frameLayout.findViewById(R.id.tag_field);

        tagLayout = (FlowLayout) frameLayout.findViewById(R.id.tag_layout);
        logoLayout = frameLayout.findViewById(R.id.logo_layout);
        removeTagBtn = (ImageButton) frameLayout.findViewById(R.id.remove_tag_button);

//        if(feedGridView != null) feedGridView.invalidateViews();
//        feedGridView = (GridView) frameLayout.findViewById(R.id.feed_gridview);

        if(feedRecycleView != null) feedRecycleView.invalidate();
        feedRecycleView = (RecyclerView) frameLayout.findViewById(R.id.feed_recycleview);

        dimView = frameLayout.findViewById(R.id.dim_view);

        fam = (FloatingActionMenu) frameLayout.findViewById(R.id.fam);
        fabCamera = (FloatingActionButton) frameLayout.findViewById(R.id.fab_camera);
        fabChoosePic = (FloatingActionButton) frameLayout.findViewById(R.id.fab_choosepic);

        reloadText = (FontTextView) reloadLayout.findViewById(R.id.reload_text);
        reloadButton = (Button) reloadLayout.findViewById(R.id.reload_button);
    }

    public void runUploadingAnimation(){ //will be called when emit upload
        //TODO get picture from path in parameter
        //TODO create placeholder view of the uploading photo with upload animation
    }

    public void updateFeed(){
        JSONObject filter = new JSONObject();
        JSONArray filterData = new JSONArray();

        try {
            //create filter data
//            filterCount = tagViewArray.size();
            for(TagView view : tagViewArray) {
                JSONObject value = new JSONObject();
                value.put("vendor", view.vendorName);
                value.put("model", view.modelName);
                filterData.put(value);
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
        for(TagView view : tagViewArray) {
            tagLayout.addView(view.getTagView());
        }
        if(filterCount==0){
            View recentTag = LayoutInflater.from(getActivity()).inflate(R.layout.recent_tag, null, false);
            tagLayout.addView(recentTag);
        }
    }

    private void addTag(String vendorName, String modelName){
        TagView tagView = new TagView(tagLayout, vendorName, modelName);
        tagView.tagIndex = tagViewArray.size();
        tagViewArray.add(tagView);
        filterCount++;
    }

    private boolean removeTag(){
        ArrayList<TagView> tempArray = new ArrayList<>();
        boolean someTagWasRemoved = false;
        for(TagView view : tagViewArray) {
            if(!view.selected) tempArray.add(view);
            else someTagWasRemoved = true;
        }
        filterCount = tempArray.size();
        tagViewArray = tempArray;
        if(someTagWasRemoved) updateTagView();

        return someTagWasRemoved;
    }

    public class TagView {
        public String vendorName, modelName;
        private TextView vendorTV, modelTV;
        private LinearLayout tagWrapper;
        private View tagView;
        private LayoutInflater inflater;
        private int tagIndex;
        private boolean selected = false;

        public TagView(FlowLayout tagLayout, String vendorName, String modelName) {
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
