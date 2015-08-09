package com.droidsans.photo.droidphoto;


import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
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
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.droidsans.photo.droidphoto.util.adapter.FeedRecycleViewAdapter;
import com.droidsans.photo.droidphoto.util.FlowLayout;
import com.droidsans.photo.droidphoto.util.view.FontTextView;
import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.droidsans.photo.droidphoto.util.PicturePack;
import com.droidsans.photo.droidphoto.util.SpacesItemDecoration;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tourguide.tourguide.Overlay;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;


/**
 * A simple {@link Fragment} subclass.
 */
public class FeedFragment extends Fragment {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int FILTER_FEED = 2;
    private static final int FILL_POST = 4;
    private static final int SELECT_PHOTO = 8;
    private static final String FIRST_TIME_FEED_FRAGMENT = "firstTimeFeedFragment";
    private static final int FEED_LIMIT_PER_REQUEST = 20;

    private View logoLayout, dimView;
//    private GridView feedGridView;
    private RecyclerView feedRecycleView;
//    private PictureGridAdapter adapter;
    private FeedRecycleViewAdapter recycleAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
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

    private static String staticPhotoPath = null;
    private static boolean hasImageInPhotoPath = false;

    private int filterCount;
    private String skipDate;
    private boolean isUpdate = false;
    private boolean noData = true;

    private boolean disconnected = false;

    private String resolvedVendor;
    private String resolvedModel;

//    private NotifyAdapter packreload[];

//    private int firstAtPause;
//    private int lastAtPause;
//    private boolean notActive = false;

    private Emitter.Listener onGetFeedRespond;
    private Emitter.Listener onDisconnect;
    private Emitter.Listener onGetDeviceListRespond;
    public Emitter.Listener onUpdateFeedRespond;

    private Handler delayAction = new Handler();
    private Runnable timeout;
    private Runnable loop;
    private int loopcount = 0;
    private Runnable update;

    private ArrayList<TagView> tagViewArray;

    public static FeedFragment mFeedFragment;

    private TourGuide tutorialHandler;
    private ArrayList<View> tutorialViewList;
    private ArrayList<String> tutorialStringList;
    private int nextTutorial = 1;

    public static int percentage = 0;
    public static boolean isFailedToUpload = false;
    public static boolean isCancelUpload = false;
    public static boolean isUploading = false;
    private RelativeLayout uploadProgressLayout;

    private static float normalFamPositionX, normalFamPositionY;
    private ImageView uploadImagePreview;
    private ProgressBar uploadProgressbar;
    private Button cancelUpload;
    private final int LOOP_DELAY = 250;

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
        tagViewArray = new ArrayList<>(); //setupTagViewArray
        initializeVendorModelList();
        updateTagView();
        initRequestFeed();
        initLoading();
        setupUploadProgress();
        super.onActivityCreated(savedInstanceState);
    }

    private void initialize() {
        findAllById();

        setupRecycleView();
        if(!isFirstTime()){
            setupListener();
        }
        setupEmitterListener();
    }

    private void setupUploadProgress() {
        if(isUploading){
            showUploadProgress(false);
            setFamEnable(false);
        } else {
            hideUploadProgress();
            setFamEnable(true);
        }
    }

    private void checkFirstTimeLaunch() { //call from onGetFeedRespond
        if(isFirstTime()){
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
                        .setDescription(getString(R.string.tutorial_touch_to_dismiss))
                        .setGravity(Gravity.LEFT | Gravity.TOP))
                    .setOverlay(new Overlay()
                        .setEnterAnimation(enterAnimation)
                        .setExitAnimation(exitAnimation)
                        .setBackgroundColor(getResources().getColor(R.color.black_transparent_overlay))
                        .setPadding(30))
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
//            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
//                    .putBoolean(FIRST_TIME_FEED_FRAGMENT, false).apply();p
            getActivity().getSharedPreferences(getString(R.string.tutorial_data), Context.MODE_PRIVATE).edit()
                    .putBoolean(getString(R.string.first_time_feed_fragment), false).apply();
            tutorialHandler.cleanUp();
            return;
        }

        Animation enterAnimation = new AlphaAnimation(0f, 1f);
        enterAnimation.setDuration(600);
        enterAnimation.setFillAfter(true);

        Animation exitAnimation = new AlphaAnimation(1f, 0f);
        exitAnimation.setDuration(600);
        exitAnimation.setFillAfter(true);

        tutorialHandler.cleanUp();
        tutorialHandler.setToolTip(new ToolTip()
                .setTitle(tutorialStringList.get(nextTutorial))
                .setDescription(getString(R.string.tutorial_touch_to_dismiss))
                .setGravity(Gravity.LEFT | Gravity.BOTTOM))
                .setOverlay(new Overlay()
                        .setEnterAnimation(enterAnimation)
                        .setExitAnimation(exitAnimation)
                        .setBackgroundColor(getResources().getColor(R.color.black_transparent_overlay))
                        .setPadding(20))
                .playOn(tutorialViewList.get(nextTutorial));

        nextTutorial++;
    }

    private void setupRecycleView() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFeed();
            }
        });

        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.accent_color), getResources().getColor(R.color.primary_color));

        feedRecycleView.addItemDecoration(new SpacesItemDecoration(
                getActivity(),
                getResources().getInteger(R.integer.main_feed_col_num),
                (int) getResources().getDimension(R.dimen.feed_recycleview_item_space),
                false, false, false, false
        ));
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.main_feed_col_num));
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                //set how much each type span how many cells (grids)
                switch (recycleAdapter.getItemViewType(position)) {
                    case FeedRecycleViewAdapter.TYPE_CONTENT:
                        return 1;
                    case FeedRecycleViewAdapter.TYPE_FOOTER:
                        return getResources().getInteger(R.integer.main_feed_col_num);
                    default:
                        return 0;
                }
//                return 0;
            }
        });
//        feedRecycleView.setLayoutManager(new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.main_feed_col_num)));
        feedRecycleView.setLayoutManager(layoutManager);
    }

    private void initializeVendorModelList() {
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
        skipDate = null;
        isUpdate = false;
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
            if(skipDate != null) filter.put("skip", skipDate);
            filter.put("limit", FEED_LIMIT_PER_REQUEST);
            filter.put("sptag", null);
            if(isUpdate) filter.put("_event", "update_feed");
            else filter.put("_event", "get_feed");
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
                        delayAction.postDelayed(timeout, 10000);
                    }
                }
            }, 7000);
        } else {
            //can emit: detect loss on the way
            GlobalSocket.mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            //set timeout delay
            delayAction.postDelayed(timeout, 10000);
        }
    }

    private void setupListener() {
        removeTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                isRemoveTagActive = !isRemoveTagActive;
//                if (isRemoveTagActive) {
//                    removeTagBtn.setImageResource(R.drawable.remove_tag_pressed);
//                    removeTagBtn.animate().scaleX(1.15f).scaleY(1.15f).setDuration(150).start();
//                } else {
//                    removeTagBtn.setImageResource(R.drawable.remove_tag_normal);
//                    removeTagBtn.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
//                    if (removeTag()) {
//                        refreshFeed();
//                        initLoading();
//                    }
//                }
                for (int i = 0; i < tagViewArray.size(); i++) {
                    tagViewArray.get(i).selected = true;
                }
                removeTag();
                refreshFeed();
                initLoading();
            }
        });

        final int famAnimationDuration = 50;
        fam.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean open) {
                if (open) {
                    dimView.setVisibility(View.VISIBLE);
                    dimView.animate().alpha(0.7f).setDuration(300).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            fam.getMenuIconView().animate()
                                    .alpha(0f)
                                    .setDuration(famAnimationDuration)
                                    .start();
                            delayAction.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    fam.getMenuIconView().setImageResource(R.drawable.fab_add);
                                    fam.getMenuIconView().invalidate();
                                    fam.getMenuIconView().animate()
                                            .alpha(1f)
                                            .setDuration(famAnimationDuration)
                                            .start();
                                }
                            }, famAnimationDuration);
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
                            delayAction.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    fam.getMenuIconView().animate()
                                            .alpha(0f)
                                            .setDuration(famAnimationDuration)
                                            .start();
                                    delayAction.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            fam.getMenuIconView().setImageResource(R.drawable.ic_post_white_24px);
                                            fam.getMenuIconView().invalidate();
                                            fam.getMenuIconView().animate()
                                                    .alpha(1f)
                                                    .setDuration(famAnimationDuration)
                                                    .start();
                                        }
                                    }, famAnimationDuration);
                                }
                            }, 150);
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
                resolveDeviceName();
//                updateCSV();
                fam.close(true);
            }
        });
        fabChoosePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchPicturePickerIntent();
                resolveDeviceName();
//                updateCSV();
                fam.close(true);
            }
        });

        cancelUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCancelUpload = true;
            }
        });

    }

    private void resolveDeviceName() {
        final JSONObject data = new JSONObject();
        try {
            data.put("build_device", Build.DEVICE);
            data.put("build_model", Build.MODEL);
            data.put("manufacturer", Build.MANUFACTURER.trim());
            data.put("_event", "get_device_name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(!GlobalSocket.globalEmit("device.resolve", data)) {
            delayAction.postDelayed(new Runnable() {
                @Override
                public void run() {
                    GlobalSocket.globalEmit("device.resolve", data);
                }
            }, 850);
        }
    }

    private void updateCSV(){
        final JSONObject data = new JSONObject();
        try {
            data.put("version", getVendorModelMapVersion());
            data.put("_event", "get_csv");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(!GlobalSocket.globalEmit("csv.get", data)){
            delayAction.postDelayed(new Runnable() {
                @Override
                public void run() {
                    GlobalSocket.globalEmit("csv.get", data);
                }
            }, 3000);
        }
    }

    private String getVendorModelMapVersion() {
//        return getSharedPreferences(getString(R.string.cvsMapPref), Context.MODE_PRIVATE)
//                .getString(getString(R.string.csvVersion), "0.0");
        return "0.9";
    }

    private void setupEmitterListener(){
        onDisconnect = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if(getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("droidphoto", "FeedFragment: disconnected");
                            GlobalSocket.mSocket.off(Socket.EVENT_DISCONNECT);
                            initReload();
                        }
                    });
                }
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
//                                Log.d("droidphoto", "photoList(" + i + "):" + ((JSONObject) photoList.opt(i)));
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
                                picturePack.setAvatarURL(jsonPack.optString("avatar_url"));
//                                Log.d("droidphoto", jsonPack.optString("submit_date"));

                                skipDate = jsonPack.optString("submit_date");

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
                            if(len == FEED_LIMIT_PER_REQUEST) {
                                PicturePack footer = new PicturePack();
                                feedPicturePack.add(footer);
                            }
                            recycleAdapter = new FeedRecycleViewAdapter(getActivity(), feedPicturePack);
                            feedRecycleView.setAdapter(recycleAdapter);

                            if(feedRecycleView.getAlpha() < 1f) {
                                feedRecycleView.animate()
                                        .alpha(1f)
                                        .setDuration(300)
                                        .start();
                                Log.d("droidphoto", "alpha:1");
                            }

                            swipeRefreshLayout.setRefreshing(false);

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
                                JSONArray bDevices = data.optJSONArray("bDeviceList");
                                JSONArray bModels = data.optJSONArray("bModelList");

                                //set model list
                                BrowseModelActivity.modelName = new String[models.length()][];
                                BrowseModelActivity.buildDevice = new String[bDevices.length()][];
                                BrowseModelActivity.buildModel = new String[bModels.length()][];
                                for(int i = 0; i < models.length(); i++) {
                                    BrowseModelActivity.modelName[i] = ((JSONArray) models.get(i)).join(",").replaceAll("\"", "").replaceAll("\\\\", "").split(",");
                                    BrowseModelActivity.buildDevice[i] = ((JSONArray) bDevices.get(i)).join(",").replaceAll("\"", "").split(",");
                                    BrowseModelActivity.buildModel[i] = ((JSONArray) bModels.get(i)).join(",").replaceAll("\"", "").split(",");

                                    Arrays.sort(BrowseModelActivity.modelName[i], new Comparator<String>() {
                                        @Override
                                        public int compare(String s1, String s2) {
                                            return s1.compareToIgnoreCase(s2);
                                        }
                                    });
                                }

                                if(models.length() > 0) {
                                    noData = false;
                                }

                                //set drawable for vendor
                                int vendorSize = BrowseVendorActivity.vendorName.length;
                                BrowseVendorActivity.vendorPicResource = new Integer[vendorSize];
                                for(int i = 0; i < vendorSize; i++) {
                                    switch (BrowseVendorActivity.vendorName[i]) {
                                        case "Acer":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_acer_300;
                                            break;
                                        case "Amazon":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_amazon_300;
                                            break;
                                        case "Asus":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_asus_300;
                                            break;
                                        case "Dell":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_dell_300;
                                            break;
                                        case "HP":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_hp_300;
                                            break;
                                        case "HTC":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_htc_300;
                                            break;
                                        case "Huawei":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_huawei_300;
                                            break;
                                        case "i-mobile":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_imobile_300;
                                            break;
                                        case "Intel":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_intel_300;
                                            break;
                                        case "LAVA":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_lava_300;
                                            break;
                                        case "Lenovo":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_lenovo_300;
                                            break;
                                        case "LG":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_lg_300;
                                            break;
                                        case "Meizu":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_meizu_300;
                                            break;
                                        case "Motorola":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_motorolar;
                                            break;
                                        case "Nikon":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_nikon_300;
                                            break;
                                        case "OnePlus":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_oneplus_300;
                                            break;
                                        case "OPPO":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_oppo_300;
                                            break;
                                        case "Panasonic":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_panasonic_300;
                                            break;
                                        case "Polaroid":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_polaroid_300;
                                            break;
                                        case "Samsung":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_samsung_300;
                                            break;
                                        case "Sony":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_sony_300;
                                            break;
                                        case "True":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_true_300;
                                            break;
                                        case "ViewSonic":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_viewsonic_300;
                                            break;
                                        case "Wiko":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_wiko_300;
                                            break;
                                        case "Xiaomi":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_xiaomi_300;
                                            break;
                                        case "ZTE":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_zte_300;
                                            break;
                                        case "Alcatel":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_alcatel_300;
                                            break;
                                        case "Doogee":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_doogee_300;
                                            break;
                                        case "Elephone":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_elephone_300;
                                            break;
                                        case "Fujitsu":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_fujitsu_300;
                                            break;
                                        case "Sharp":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_sharp_300;
                                            break;
                                        case "ThL":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_thl_300;
                                            break;
                                        case "vivo":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_vivo_300;
                                            break;
                                        case "Nubia":
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_nubia_300;
                                            break;
                                        default:
                                            BrowseVendorActivity.vendorPicResource[i] = R.drawable.vendor_logo_default_300;
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

        onUpdateFeedRespond = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GlobalSocket.mSocket.off("update_feed");
                        feedPicturePack.remove(feedPicturePack.size() - 1);
                        recycleAdapter.notifyDataSetChanged();
                        JSONObject data = (JSONObject) args[0];
                        if (data.optBoolean("success")) {
//                            String[] photoList = data.optJSONArray("photoList").join(",").replaceAll("\"", "").split(",");
                            JSONArray photoList = data.optJSONArray("photoList");
                            int len = photoList.length();
                            for (int i = 0; i < len; i++) {
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
                                picturePack.setAvatarURL(jsonPack.optString("avatar_url"));

                                skipDate = jsonPack.optString("submit_date");
                                feedPicturePack.add(picturePack);
                            }
                            recycleAdapter.notifyDataSetChanged();
                            if (len == FEED_LIMIT_PER_REQUEST) {
                                PicturePack footer = new PicturePack();
                                feedPicturePack.add(footer);
                            }
                        } else {
                            Snackbar.make(frameLayout, getString(R.string.snackbar_feed_cannot_refresh), Snackbar.LENGTH_LONG)
                                    .setAction(getString(R.string.snackbar_action_retry), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                        }
                                    })
                                    .show();
                        }
                    }
                });
            }
        };

        timeout = new Runnable() {
            @Override
            public void run() {
                GlobalSocket.mSocket.disconnect();
            }
        };

        Emitter.Listener onGetDeviceNameRespond = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                GlobalSocket.mSocket.off("get_device_name");
                JSONObject data = (JSONObject) args[0];
                if(data.optBoolean("success")) {
                    resolvedVendor = data.optString("retail_vendor");
                    resolvedModel = data.optString("retail_model");
                    Log.d("droidphoto", "resolved name : " + resolvedVendor + resolvedModel);
                } else {
                    Log.d("droidphoto", "error name resolve : " + data.optString("msg"));
                    //sad
                }
            }
        };

        if(!GlobalSocket.mSocket.hasListeners("get_device_name")) {
            GlobalSocket.mSocket.on("get_device_name", onGetDeviceNameRespond);
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

//        if(!GlobalSocket.mSocket.hasListeners("get_csv")){
//            GlobalSocket.mSocket.on("get_csv", new Emitter.Listener() {
//                @Override
//                public void call(final Object... args) {
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            GlobalSocket.mSocket.off("get_csv");
//                            Log.d("droidphoto", "csv.get: response");
//
//                            JSONObject data = (JSONObject) args[0];
//                            if (data.optBoolean("success")) {
//                                Log.d("droidphoto", "csv.get: success");
//                                Object csvObj = data.opt("csv");
//                                String version = data.optString("version");
//
//                                Log.d("droidphoto", "csv version: " + version);
//
//                                writeObjToInternalStorage(csvObj, getString(R.string.csvFileName));
//                                writeObjToInternalStorage(version, getString(R.string.csvVersion));
//                            } else {
//                                String msg = data.optString("msg");
//                                Log.d("droidphoto", "Error update csv: " + msg);
//                            }
//                        }
//                    });
//                }
//            });
//        }
    }

    private void initLoading() {
        loadingCircle.setVisibility(ProgressBar.VISIBLE);
        reloadLayout.setVisibility(LinearLayout.GONE);
        frameLayout.setVisibility(FrameLayout.GONE);
    }

    private void initReload() {
        loadingCircle.setVisibility(ProgressBar.GONE);
        reloadLayout.setVisibility(LinearLayout.VISIBLE);
        reloadText.setText(R.string.load_feed_error);
        if(!reloadButton.hasOnClickListeners()) reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadButton.setClickable(false);
                reloadLayout.setVisibility(LinearLayout.GONE);
                loadingCircle.setVisibility(ProgressBar.VISIBLE);
                GlobalSocket.reconnect(); //reconnect
                refreshFeed();
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
        picturePickerIntent.setType("image/jpeg");
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
                if(noData) {
//                    Snackbar.make(frameLayout, "fresh start. upload first image now !!", Snackbar.LENGTH_LONG).show();

                    return true;
                }
                if(isFirstTime()){
                    showNextTutorial();
                    return true;
                }
                if(filterCount >= 4){
                    Snackbar.make(getView(), getString(R.string.feed_filter_too_many_tag), Snackbar.LENGTH_LONG).show();
                } else {
                    launchAddFilterPopup();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == FragmentActivity.RESULT_OK)
            switch (requestCode) {
                case FILTER_FEED:
                    hasImageInPhotoPath = false;
                    staticPhotoPath = null;
//                    initializeVendorModelList();
                    String vendorName = data.getStringExtra(BrowseVendorActivity.VENDOR_NAME);
                    String modelName = data.getStringExtra(BrowseModelActivity.MODEL_NAME);
                    Snackbar.make(frameLayout, getString(R.string.snackbar_feed_selected_vendor) + vendorName + getString(R.string.snackbar_feed_selected_model) + modelName, Snackbar.LENGTH_SHORT).show();
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
                        refreshFeed();
                        initLoading();
                    }
                    break;

                case SELECT_PHOTO:
                    hasImageInPhotoPath = false;
                    staticPhotoPath = null;
//                    Log.d("droidphoto", "uri : " + data.getData());
//                    Log.d("droidphoto", "path : " + data.getData().getPath());
//                    Log.d("droidphoto", "type : " + getActivity().getApplicationContext().getContentResolver().getType(data.getData()));
                    String path = "";
                    if(data.getData().getAuthority().equals("com.google.android.apps.photos.contentprovider")) {
                        //clear old cache
                        clearUploadCache();

                        //download or copy them
                        Cursor cursor = getActivity().getContentResolver().query(data.getData(), null, null, null, null);
                        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        cursor.moveToFirst();
//                        Log.d("droidphoto", "filename: " + cursor.getString(nameIndex));
//                        path = getActivity().getCacheDir() + "/upload/" + "googlephoto_upload_temp";
                        path = getActivity().getCacheDir() + "/upload/" + cursor.getString(nameIndex);
                        cursor.close();

                        File temp = new File(path);
                        if (temp.exists()) {
                            if (!temp.delete()) {
                                //sad
                                Log.d("droidphoto", "cannot delete: why ?");
                            }
                        }
                        try {
                            InputStream in = getActivity().getContentResolver().openInputStream(data.getData());
                            FileOutputStream out = new FileOutputStream(new File(path));
                            byte buffer[] = new byte[4096];
//                            int total = 0;
                            int count;
                            while ((count = in.read(buffer)) != -1) {
//                                total += count;
                                out.write(buffer, 0, count);
                            }
                            in.close();
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//                    } else if(data.getData().toString().indexOf("content://media") == 0) {
                    } else {
                        //normal operation
                        path = getImagePath(data.getData());
                    }
                    Log.d("droidphoto", "resolved path : " + path);
                    if(path == null) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Error !!")
                                .setMessage("cannot resolve picture picker uri. please report us this bug.")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();
                        break;
                    }
                    File file = new File(path);
                    if(file.length() > 0) {
//                    Toast.makeText(getActivity(), path, Toast.LENGTH_LONG).show();
                        Intent fillPostFromPicturePickerIntent = new Intent(getActivity(), FillPostActivity.class);
                        fillPostFromPicturePickerIntent.putExtra("photoPath", path);
                        fillPostFromPicturePickerIntent.putExtra("imageFrom", "Picture Picker");
                        fillPostFromPicturePickerIntent.putExtra("vendor", resolvedVendor);
                        fillPostFromPicturePickerIntent.putExtra("model", resolvedModel);
                        startActivityForResult(fillPostFromPicturePickerIntent, FILL_POST);
                        break;
                    } else {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.alert_invalid_image))
                                .setMessage(getString(R.string.alert_invalid_image_message))
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();
                        //remove photo entry from mediastore or rescan | maybe it is impossible.. ?
                        hasImageInPhotoPath = false;
                        staticPhotoPath = null;
                        break;
                    }

                case REQUEST_IMAGE_CAPTURE:
//                    Toast.makeText(getActivity(), staticPhotoPath, Toast.LENGTH_LONG).show();
                    galleryAddPic();
                    File f = new File(staticPhotoPath);
                    hasImageInPhotoPath = false;
                    staticPhotoPath = null;
                    if(f.length() > 0) {
                        Intent fillPostIntent = new Intent(getActivity(), FillPostActivity.class);
                        fillPostIntent.putExtra("photoPath", f.getAbsolutePath());
                        fillPostIntent.putExtra("imageFrom", "Camera");
                        fillPostIntent.putExtra("vendor", resolvedVendor);
                        fillPostIntent.putExtra("model", resolvedModel);
                        startActivityForResult(fillPostIntent, FILL_POST);
                        break;
                    } else {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.alert_invalid_image))
                                .setMessage(getString(R.string.alert_invalid_image_message))
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();
                        //remove photo entry from mediastore or rescan | maybe it is impossible.. ?
                        hasImageInPhotoPath = false;
                        staticPhotoPath = null;
                        break;
                    }

                case FILL_POST:
//                    PicturePack uploadingPicturePack = new PicturePack(
//                            getActivity().getApplicationContext().getSharedPreferences(getString(R.string.userdata), Context.MODE_PRIVATE)
//                                    .getString(getString(R.string.username), ""),
//                            data.getStringExtra("vendor"),
//                            data.getStringExtra("model"),
//                            "", "", ""
//                    );
//                    uploadingPicturePack.setIsUploading(true, data.getStringExtra("path"));
//                    feedPicturePack.add(0, uploadingPicturePack);
//                    recycleAdapter.notifyDataSetChanged();

//                    View uploadingView = feedRecycleView.getChildAt(0);
//                    SquareImageView picture = (SquareImageView) uploadingView.findViewById(R.id.picture);
//                    final ProgressBar progressBar = (ProgressBar) uploadingView.findViewById(R.id.upload_progress);
//                    uploadingView.findViewById(R.id.upload_layout).setVisibility(RelativeLayout.VISIBLE);
//                    progressBar.setVisibility(ProgressBar.VISIBLE);
//                    FontTextView deviceName = (FontTextView) uploadingView.findViewById(R.id.device_name);
//                    FontTextView username = (FontTextView) uploadingView.findViewById(R.id.user);

                    isUploading = true;
                    staticPhotoPath = data.getStringExtra("path");
                    hasImageInPhotoPath = true;
                    setFamEnable(false);
                    showUploadProgress(true);

                    break;
            }
        else if(resultCode == FragmentActivity.RESULT_CANCELED) {
            if(data != null && data.hasExtra("return code")) {
                switch (data.getStringExtra("return code")) {
                    case "not your photo":
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.alert_not_your_photo_title))
                                .setMessage(getString(R.string.alert_not_your_photo_message))
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dalog, int which) {
                                    }
                                })
//                                .setIcon(R.drawable.ic_error_outline_black_24dp)
                                .show();
                        break;
                    case "no exif":
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.alert_not_from_camera_title))
                                .setMessage(getString(R.string.alert_not_from_camera_summary))
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();
                        break;
                    case "no required exif":
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.alert_no_required_exif_title))
                                .setMessage(getString(R.string.alert_no_required_exif_summary))
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

    private void clearUploadCache() {
        Log.d("droidphoto", "deleting file in dir: " + getActivity().getCacheDir() + "/upload/");
        File uploadCache = new File(getActivity().getCacheDir(), "upload");
        uploadCache.mkdir();
        if (!uploadCache.isDirectory()) {
            String[] cacheList = uploadCache.list();
            for (String cache : cacheList) {
                if (!new File(uploadCache, cache).delete()) {
                    Log.d("droidphoto", "what ??");
                }
            }
        }
    }

    private void removeTemp() {
        if ((!hasImageInPhotoPath) && (staticPhotoPath != null)) {
            File image = new File(staticPhotoPath);
            if(image.exists()) {
                try {
                    BufferedReader br = new BufferedReader(new FileReader(image));
                    String read = br.readLine();
                    if(read == null || read.isEmpty()) {
                        image.delete();
//                        if (image.delete()) {
////                Toast.makeText(getActivity(), "temp file removed", Toast.LENGTH_LONG).show();
//                            Snackbar.make(getView(), "temp file removed", Snackbar.LENGTH_LONG).show();
//                        } else {
////                Toast.makeText(getActivity(), "cannot remove temp file", Toast.LENGTH_LONG).show();
//                            Snackbar.make(getView(), "cannot remove temp file", Snackbar.LENGTH_LONG).show();
//                        }
                    }
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            hasImageInPhotoPath = false;
            staticPhotoPath = null;
        }
    }

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
    public void onDestroyView() {
        GlobalSocket.mSocket.off(Socket.EVENT_DISCONNECT);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
//        if(GlobalSocket.mSocket.hasListeners("get_feed")) {
//        GlobalSocket.mSocket.off(Socket.EVENT_DISCONNECT);
            GlobalSocket.mSocket.off("get_feed");
//        }
//        if(GlobalSocket.mSocket.hasListeners("get_device_list")) {
            GlobalSocket.mSocket.off("get_device_list");
//        }
        GlobalSocket.mSocket.off("update_feed");
        GlobalSocket.mSocket.off("get_device_name");
//        GlobalSocket.mSocket.off("get_csv");
//        if (adapter != null) {
//            //recycle bitmap and reset load state
//            Log.d("droidphoto", "count before destroy : " + adapter.getCount());
//            for (int i = 0; i < adapter.getCount(); i++) {
//                adapter.getItem(i).resetPackBitmap();
//                if (packreload[i] != null) packreload[i].cancel(true);
//            }
//        }

        if(isUploading){
            delayAction.removeCallbacks(loop);
        }

        super.onDestroy();
    }

    private void setFamEnable(final Boolean enable){
//        final boolean[] hasSetFamPositionOnce = {false};
//        if(normalFamPositionX == 0f || normalFamPositionY == 0f){
//            for(int i = 0; i <= 1000; i+=10){
//                final int finalI = i;
//                delayAction.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if(normalFamPositionX == 0f || normalFamPositionY == 0f){
//                            normalFamPositionX = fam.getX();
//                            normalFamPositionY = fam.getY();
//                        } else if(!hasSetFamPositionOnce[0]){
//                            Log.i("droidphoto","postDelay " + (finalI-10) + ": (" + normalFamPositionX + ", " + normalFamPositionY+")");
//                            if(enable){
//                                fam.animate()
//                                        .x(normalFamPositionX).y(normalFamPositionY)
//                                        .setDuration(300);
//                            } else {
//                                fam.animate()
//                                        .xBy(fam.getWidth()/2)
//                                        .setStartDelay(500)
//                                        .setDuration(400);
//                            }
//                            hasSetFamPositionOnce[0] = true;
//                        } else {
//                            Log.i("droidphoto","postDelay "+finalI+" already have, get now fam X,Y: (" + fam.getX() + ", " + fam.getY()+")");
//                        }
//                    }
//                }, finalI);
//            }
//        } else {
//            Log.i("droidphoto","already have X,Y: (" + normalFamPositionX + ", " + normalFamPositionY+")");
//            if(enable){
//                Log.i("droidphoto","show X,Y: (" + normalFamPositionX + ", " + normalFamPositionY+")");
//                fam.animate()
//                        .x(normalFamPositionX).y(normalFamPositionY)
//                        .setDuration(300);
//                delayAction.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.i("droidphoto","where are you, fam X,Y: (" + fam.getX() + ", " + fam.getY()+")");
//                    }
//                }, 500);
//            } else {
//                Log.i("droidphoto","hide X,Y: (" + normalFamPositionX + ", " + normalFamPositionY+")");
//                fam.animate()
//                        .xBy(fam.getWidth()/2)
//                        .setStartDelay(500)
//                        .setDuration(400);
//            }
//        }

        if (enable) {
            fam.setVisibility(View.VISIBLE);
            fam.animate()
                    .alpha(1f)
                    .setDuration(200);
        } else {
            fam.animate()
                    .alpha(0f)
                    .setStartDelay(500)
                    .setDuration(200);
            delayAction.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fam.setVisibility(View.GONE);
                }
            }, 900);
        }
    }

    private void showUploadProgress(boolean animate){
        Glide.with(getActivity().getApplicationContext())
                .load(staticPhotoPath)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .centerCrop()
                .placeholder(R.drawable.picture_placeholder_500_center)
                .into(uploadImagePreview);

        update = new Runnable() {
            @Override
            public void run() {
                uploadProgressbar.setProgress(percentage);
            }
        };

        loop = new Runnable() {
            @Override
            public void run() {
                if(isCancelUpload) { //show cancel
                    isUploading = false;
                    staticPhotoPath = null;
                    hasImageInPhotoPath = false;
                    clearUploadCache();
                    setFamEnable(true);
                    hideUploadProgress();
                    Snackbar.make(frameLayout, getString(R.string.snackbar_feed_upload_cancel), Snackbar.LENGTH_LONG).show();
                } else if(isFailedToUpload) { //show failed
                    isUploading = false;
                    staticPhotoPath = null;
                    hasImageInPhotoPath = false;
                    clearUploadCache();
                    setFamEnable(true);
                    hideUploadProgress();
                    refreshFeed();
                    Snackbar.make(frameLayout, getString(R.string.snackbar_feed_upload_failed), Snackbar.LENGTH_LONG).show();
                } else {
                    if (percentage < 100) {//update upload progress
                        Log.d("droidphoto", "uploaded : " + percentage + "%");
                        if(percentage > 97) {
                            loopcount++;
                        }
                        uploadProgressbar.setProgress(percentage);
                        if(getActivity() != null) getActivity().runOnUiThread(update);
                        if(loopcount > 60) isFailedToUpload = true;
                        delayAction.postDelayed(loop, LOOP_DELAY);
                    } else { //upload done
                        isUploading = false;
                        staticPhotoPath = null;
                        hasImageInPhotoPath = false;
                        clearUploadCache();
                        setFamEnable(true);
                        uploadProgressbar.setProgress(percentage);
                        if(getActivity() != null) getActivity().runOnUiThread(update);
                        refreshFeed();
                        initializeVendorModelList();

                        //animate
                        hideUploadProgress();
                    }
                }
            }
        };

        loopcount = 0;
        delayAction.postDelayed(loop, LOOP_DELAY);

        uploadProgressLayout.setVisibility(View.VISIBLE);
        if(animate){
            uploadProgressLayout.setY(frameLayout.getBottom());
            uploadProgressLayout.animate()
                    .y(frameLayout.getBottom() - getResources().getDimension(R.dimen.snackbar_height))
                    .setDuration(getResources().getInteger(R.integer.fake_snackbar_animation_speed))
                    .setStartDelay(500)
                    .start();
        } else {
            uploadProgressLayout.setBottom(frameLayout.getBottom());
        }

    }

    private void hideUploadProgress(){
        uploadProgressLayout.animate()
                .y(frameLayout.getBottom())
                .setDuration(getResources().getInteger(R.integer.fake_snackbar_animation_speed))
                .start();

        delayAction.postDelayed(new Runnable() {
            @Override
            public void run() {
                uploadProgressLayout.setVisibility(View.INVISIBLE);
            }
        }, getResources().getInteger(R.integer.fake_snackbar_animation_speed));
    }

    private String getImagePath(Uri uri) {
        Cursor cursor = null;
        try {
            Uri newUri = handleImageUri(uri);
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = getActivity().getContentResolver().query(newUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e){
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
//        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
//        cursor.moveToFirst();
//        String document_id = cursor.getString(0);
//        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
//        cursor.close();
//
//        cursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
//        cursor.moveToFirst();
//        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
//        cursor.close();
//
//        return path;
    }

    public Uri handleImageUri(Uri uri) {
        Pattern pattern = Pattern.compile("(content://media/.*\\d)");
        if (uri.getPath().contains("content")) {
            Matcher matcher = pattern.matcher(uri.getPath());
            if (matcher.find())
                return Uri.parse(matcher.group(1));
            else
                throw new IllegalArgumentException("Cannot handle this URI");
        } else
            return uri;
    }

    private void writeObjToInternalStorage(Object obj, String filename){
        File file = new File(getActivity().getApplicationContext().getExternalFilesDir(null), filename);

        try {
            InputStream is = new ByteArrayInputStream(serialize(obj));
            OutputStream os = new FileOutputStream(file);
            byte[] writeData = new byte[is.available()];
            is.read(writeData);
            os.write(writeData);
            is.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        return b.toByteArray();
    }

    private boolean isFirstTime(){
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.JELLY_BEAN){
            return getActivity().getSharedPreferences(getString(R.string.tutorial_data), Context.MODE_PRIVATE).getBoolean(getString(R.string.first_time_feed_fragment), true);
//            return PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(FIRST_TIME_FEED_FRAGMENT, true);
        } else {
            return false;
        }
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
        swipeRefreshLayout = (SwipeRefreshLayout) frameLayout.findViewById(R.id.swipe_refresh_layout);

        dimView = frameLayout.findViewById(R.id.dim_view);

        fam = (FloatingActionMenu) frameLayout.findViewById(R.id.fam);
        fabCamera = (FloatingActionButton) frameLayout.findViewById(R.id.fab_camera);
        fabChoosePic = (FloatingActionButton) frameLayout.findViewById(R.id.fab_choosepic);

        reloadText = (FontTextView) reloadLayout.findViewById(R.id.reload_text);
        reloadButton = (Button) reloadLayout.findViewById(R.id.reload_button);

        uploadProgressLayout = (RelativeLayout) frameLayout.findViewById(R.id.uploading_fake_snackbar_layout);
        uploadImagePreview = (ImageView) uploadProgressLayout.findViewById(R.id.uploading_imageview);
        uploadProgressbar = (ProgressBar) uploadProgressLayout.findViewById(R.id.upload_progressbar);
        cancelUpload = (Button) uploadProgressLayout.findViewById(R.id.cancel_pic_fake_snackbar);
    }

    public void refreshFeed(){
        feedRecycleView.animate()
                .alpha(0f)
                .setDuration(300)
                .start();

        skipDate = null;
        isUpdate = false;
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

    public void updateFeed(){
        JSONObject filter = new JSONObject();
        JSONArray filterData = new JSONArray();
        isUpdate = true;

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
        private ImageView deleteTagButton;

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
            deleteTagButton = (ImageView) tagView.findViewById(R.id.delete_tag_button);

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

            deleteTagButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selected = true;
                    removeTag();
                    refreshFeed();
                    initLoading();
                }
            });
        }
    }
}
