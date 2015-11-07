package com.droidsans.photo.droidphoto;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.droidsans.photo.droidphoto.util.PicturePack;
import com.droidsans.photo.droidphoto.util.SpacesItemDecoration;
import com.droidsans.photo.droidphoto.util.adapter.ProfileFeedRecycleViewAdapter;
import com.droidsans.photo.droidphoto.util.transform.CircleTransform;
import com.droidsans.photo.droidphoto.util.view.FontTextView;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class ProfileViewerActivity extends AppCompatActivity {
    public static final String DISPLAY_NAME = "displayName";
    public static final String PROFILE_DESCRIPTION = "profileDesc";
    public static final String AVATAR_URL = "avatarURL";
    public static final int TARGET_PROFILE_FRAGMENT = 12345;

    private ProgressBar loadingCircle;
    private LinearLayout reloadLayout;
    private RelativeLayout mainLayout;

    private ImageView profilePic;
    private FontTextView displayNameTv, usernameTV, profileDescTV;
    private RecyclerView profileFeedPicRecyclerview;

    private FontTextView reloadText;
    private Button reloadButton;

    private Toolbar toolbar;

    public static final String baseURL = "/data/avatar/";
    private String userId;
    private String username;
    private String avatarURL;

    private Handler delayAction = new Handler();

    private Emitter.Listener onGetProfileRespond;
    private Emitter.Listener onGetUserFeedRespond;
    private Emitter.Listener onDisconnect;

    public ProfileFeedRecycleViewAdapter adapter;
    ArrayList<PicturePack> packs;

    private MenuInflater menuInflater;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_viewer);

        initialize();
    }

    private void initialize() {
        findAllById();

        userId = getIntent().getStringExtra("user_id");
        Log.d(getString(R.string.app_name), "init profile viewer userId: " + userId);

        setupProfileFeedRecyclerView();
        setupListener();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        requestUserinfo();
        requestUserPhoto();
    }

    private void setupListener() {
        onGetProfileRespond = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        if(data.optBoolean("success")) {
                            loadingCircle.setVisibility(ProgressBar.GONE);
                            mainLayout.setVisibility(FrameLayout.VISIBLE);

                            JSONObject userObj = data.optJSONObject("userObj");

                            Log.d("droidphoto", userObj.optString("username") + " | " + userObj.optString("disp_name"));
                            username = userObj.optString("username");
                            toolbar.setTitle(username);
                            usernameTV.setText("@" + username);
                            displayNameTv.setText(userObj.optString("disp_name"));
                            avatarURL = userObj.optString("avatar_url");
                            Glide.with(getApplicationContext())
                                    .load(GlobalSocket.serverURL + baseURL + avatarURL)
//                                    .load(GlobalSocket.serverURL + baseURL + "test.jpg")
                                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                    .placeholder(R.drawable.avatar_placeholder_300)
                                    .centerCrop()
                                    .transform(new CircleTransform(getApplicationContext()))
//                                    .dontAnimate()
                                    .into(profilePic);
                            profileDescTV.setText(userObj.optString("profile_desc"));
                        } else {
                            switch (data.optString("msg")) {
                                case "db error":
//                                    Toast.makeText(getActivity().getApplicationContext(), "db error, please try again", Toast.LENGTH_SHORT).show();
                                    Snackbar.make(mainLayout, "db error, please try again", Snackbar.LENGTH_SHORT)
//                                            .setAction("OK", null)
                                            .show();
                                    break;
                                case "token error":
//                                    Toast.makeText(getActivity().getApplicationContext(), "what the fuck !!? how can you invalid your f*cking token ??", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                            initReload();
                        }
                    }
                });
            }
        };
        GlobalSocket.mSocket.on("get_profile", onGetProfileRespond);

        onGetUserFeedRespond = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GlobalSocket.mSocket.off(Socket.EVENT_DISCONNECT);
                        JSONObject data = (JSONObject) args[0];
                        if (data.optBoolean("success")) {
                            GlobalSocket.mSocket.off("get_user_feed");
                            packs = new ArrayList<>();
                            JSONArray photoList = data.optJSONArray("photoList");
                            for (int i = 0; i < photoList.length(); i++) {
                                JSONObject jsonPhoto = photoList.optJSONObject(i);
                                PicturePack pack = new PicturePack();
                                pack.setPhotoId(jsonPhoto.optString("_id"));
                                pack.setPhotoURL(jsonPhoto.optString("photo_url"));
                                pack.setUserId(jsonPhoto.optString("user_id"));
                                pack.setUsername(username);
                                pack.setCaption(jsonPhoto.optString("caption", ""));
                                pack.setVendor(jsonPhoto.optString("vendor"));
                                pack.setModel(jsonPhoto.optString("model"));
                                pack.setEventId(jsonPhoto.optString("event_id"));
                                pack.setRank(jsonPhoto.optInt("ranking"));
                                pack.setShutterSpeed(jsonPhoto.optString("exp_time"));
                                pack.setAperture(jsonPhoto.optString("aperture"));
                                pack.setIso(jsonPhoto.optString("iso"));
                                pack.setWidth(jsonPhoto.optInt("width"));
                                pack.setHeight(jsonPhoto.optInt("height"));
                                pack.setGpsLocation(jsonPhoto.optString("gps_location"));
                                pack.setGpsLocalizedLocation(jsonPhoto.optString("gps_localized"));
                                pack.setIsEnhanced(jsonPhoto.optBoolean("is_enhanced"));
                                pack.setIsFlash(jsonPhoto.optBoolean("is_flash"));
                                pack.setSubmitDate(jsonPhoto.optString("submit_date"));
                                pack.setAvatarURL(avatarURL);
                                pack.setIsLike(jsonPhoto.optBoolean("is_like"));
                                pack.setLikeCount(jsonPhoto.optInt("like_count"));

                                packs.add(pack);
                            }

                            Log.d("droidphoto", "set adapter");
                            adapter = new ProfileFeedRecycleViewAdapter(getApplicationContext(), packs);
                            profileFeedPicRecyclerview.setAdapter(adapter);
                        } else {
                            Log.d("droidphoto", "User Feed error: " + data.optString("msg"));
                            initReload();
                        }
                    }
                });
            }
        };
        if(!GlobalSocket.mSocket.hasListeners("get_user_feed")){
            GlobalSocket.mSocket.on("get_user_feed", onGetUserFeedRespond);
        }

        onDisconnect = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if(this != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("droidphoto", "ProfileFragment: disconnected");
                            GlobalSocket.mSocket.off(Socket.EVENT_DISCONNECT);
                            initReload();
                        }
                    });
                }
            }
        };
    }

    private void setupProfileFeedRecyclerView() {
        profileFeedPicRecyclerview.addItemDecoration(new SpacesItemDecoration(
                this,
                getResources().getInteger(R.integer.profile_feed_col_num),
                (int) getResources().getDimension(R.dimen.profile_recycleview_item_space),
                false, false, false, false
        ));
        profileFeedPicRecyclerview.setLayoutManager(
                new GridLayoutManager(this,
                        getResources().getInteger(R.integer.profile_feed_col_num)));
    }

    private void initReload() {
        loadingCircle.setVisibility(View.GONE);
        reloadLayout.setVisibility(View.VISIBLE);
        reloadText.setText("Error loading user profile :(");
        if (!reloadButton.hasOnClickListeners()) {
            reloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GlobalSocket.reconnect();
                    requestUserinfo();
                }
            });
        }
        reloadButton.setClickable(true);
    }

    private void requestUserinfo() {
        reloadButton.setClickable(false);
        mainLayout.setVisibility(View.GONE);
        reloadLayout.setVisibility(View.GONE);
        loadingCircle.setVisibility(View.VISIBLE);

        JSONObject data = new JSONObject();
        try {
            data.put("_event", "get_profile");
            data.put("user_id", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(!GlobalSocket.globalEmit("user.getprofile", data)) {
            //retry in 2 sec
            final JSONObject finalData = data;
            delayAction.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!GlobalSocket.globalEmit("user.getprofile", finalData)) {
                        //reload
                        initReload();
                    }
                }
            }, 2000);
        }
    }

    private void requestUserPhoto() {
        JSONObject data = new JSONObject();

        try {
            data.put("skip", 0);
            data.put("limit", 21);
            data.put("user_id", userId);
            data.put("_event", "get_user_feed");
        } catch (JSONException e){e.printStackTrace();}

        if(!GlobalSocket.globalEmit("photo.getuserphoto", data)) {
            final JSONObject delayedData = data;
            delayAction.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!GlobalSocket.globalEmit("photo.getuserphoto", delayedData)) {
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

    @Override
    public void onDestroy() {
        GlobalSocket.mSocket.off(Socket.EVENT_DISCONNECT);
        GlobalSocket.mSocket.off("get_user_info", onGetProfileRespond);

        if(GlobalSocket.mSocket.hasListeners("get_user_feed")) {
            GlobalSocket.mSocket.off("get_user_feed");
        }

        super.onDestroy();
    }

    @Override
    public void onStart() {
        ProfileFeedRecycleViewAdapter.isClickOnce = false;
        super.onStart();
    }

    @Override
    protected void onResume() {
        ProfileFeedRecycleViewAdapter.isClickOnce = false;
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuInflater = getMenuInflater();
        this.menu = menu;

        menuInflater.inflate(R.menu.menu_profile_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void findAllById(){

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        loadingCircle = (ProgressBar) findViewById(R.id.loading_circle);
        reloadLayout = (LinearLayout) findViewById(R.id.reload_view);
        mainLayout = (RelativeLayout) findViewById(R.id.main_view);

        profilePic = (ImageView) mainLayout.findViewById(R.id.profile_image_circle);
        displayNameTv = (FontTextView) mainLayout.findViewById(R.id.display_name);
        profileDescTV = (FontTextView) mainLayout.findViewById(R.id.profile_desc);

        usernameTV = (FontTextView) mainLayout.findViewById(R.id.username);
        profileFeedPicRecyclerview = (RecyclerView) mainLayout.findViewById(R.id.recyclerview_profile_feed_picture);

        reloadText = (FontTextView) reloadLayout.findViewById(R.id.reload_text);
        reloadButton = (Button) reloadLayout.findViewById(R.id.reload_button);
    }

    public static void launchProfileViewer(Context context, String userId){
        Intent profileViewerIntent = new Intent(context, ProfileViewerActivity.class);
        profileViewerIntent.putExtra("user_id", userId);
        profileViewerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(profileViewerIntent);
    }
}

