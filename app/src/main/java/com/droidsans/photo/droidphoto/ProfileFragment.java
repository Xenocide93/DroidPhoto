package com.droidsans.photo.droidphoto;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.droidsans.photo.droidphoto.util.CircleTransform;
import com.droidsans.photo.droidphoto.util.FontTextView;
import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.droidsans.photo.droidphoto.util.PicturePack;
import com.droidsans.photo.droidphoto.util.UserPictureGridAdapter;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private ProgressBar loadingCircle;
    private LinearLayout reloadLayout;
    private RelativeLayout mainLayout;

    private ImageView profilePic;
    private FontTextView profileName;
    private FontTextView usernameTV;
    private GridView userPicGridview;

    private FontTextView reloadText;
    private Button reloadButton;

    private String baseURL = "avatar/";
    private String username;

    private Handler delayAction = new Handler();

    private Emitter.Listener onGetUserInfoRespond;
    private Emitter.Listener onGetUserFeedRespond;
    private Emitter.Listener onDisconnect;

    private UserPictureGridAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        loadingCircle = (ProgressBar) rootView.findViewById(R.id.loading_circle);
        reloadLayout = (LinearLayout) rootView.findViewById(R.id.reload_view);
        mainLayout = (RelativeLayout) rootView.findViewById(R.id.main_view);
        initialize();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        requestUserinfo();
        setupFeedAdapter();
    }

    private void initialize() {
        findAllById();
        setupListener();
    }

    private void setupListener() {
        userPicGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
            }
        });

        onGetUserInfoRespond = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        if(data.optBoolean("success")) {
                            loadingCircle.setVisibility(ProgressBar.GONE);
                            mainLayout.setVisibility(FrameLayout.VISIBLE);

                            JSONObject userObj = data.optJSONObject("userObj");

                            Log.d("droidphoto", userObj.optString("username") + " | " + userObj.optString("disp_name"));
                            username = userObj.optString("username");
                            usernameTV.setText(username);
                            profileName.setText(userObj.optString("disp_name"));
//                            Glide.with(getActivity().getApplicationContext())
//                                    .load(GlobalSocket.serverURL + baseURL + data.optString("avatar_url"))
//                                    .placeholder(R.drawable.droidsans_logo)
//                                    .transform(new CircleTransform(getActivity().getApplicationContext()))
//                                    .into(profilePic);
                            if(data.has("avatar_url")) {
                                Glide.with(getActivity().getApplicationContext())
                                        .load(GlobalSocket.serverURL + baseURL + data.optString("avatar_url"))
                                        .transform(new CircleTransform(getActivity().getApplicationContext()))
                                        .into(profilePic);
                            }
                        } else {
                            switch (data.optString("msg")) {
                                case "db error":
                                    Toast.makeText(getActivity().getApplicationContext(), "db error, please try again", Toast.LENGTH_SHORT).show();
                                    break;
                                case "token error":
                                    Toast.makeText(getActivity().getApplicationContext(), "what the fuck !!? how can you invalid your f*cking token ??", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                            initReload();
                        }
                    }
                });
            }
        };

        if(!GlobalSocket.mSocket.hasListeners(getString(R.string.onGetUserInfoRespond))) {
            GlobalSocket.mSocket.on(getString(R.string.onGetUserInfoRespond), onGetUserInfoRespond);
        }

        onGetUserFeedRespond = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        if(data.optBoolean("success")){
                            ArrayList<PicturePack> packs = new ArrayList<>();
                            JSONArray photoList = data.optJSONArray("photoList");
                            for(int i=0; i<photoList.length(); i++){
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

                                packs.add(pack);
                            }

                            adapter = new UserPictureGridAdapter(getActivity(), R.layout.item_user_pic, packs);

                            userPicGridview.setAdapter(adapter);
                            //TODO add packs to adapter
                            //TODO add adapter th gridview
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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GlobalSocket.mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
                        initReload();
                    }
                });
            }
        };
    }

    private void requestUserinfo() {
        JSONObject data = new JSONObject();
        try {
            data.put("_token", getActivity().getSharedPreferences(getString(R.string.userdata), Context.MODE_PRIVATE).getString(getString(R.string.token), ""));
            data.put("_event", getString(R.string.onGetUserInfoRespond));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(!GlobalSocket.globalEmit("user.getuserinfo", data)) {
            //retry in 2 sec
            final JSONObject finalData = data;
            delayAction.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!GlobalSocket.globalEmit("user.getuserinfo", finalData)) {
                        //reload
                        initReload();
                    }
                }
            }, 2000);
        }
    }

    private void setupFeedAdapter() {
        JSONObject data = new JSONObject();

        try {
            data.put("skip", 0);
            data.put("limit", 21);
            data.put("_event", "get_user_feed");
        } catch (JSONException e){e.printStackTrace();}

        if(!GlobalSocket.globalEmit("photo.getuserphoto", data)) {
            Toast.makeText(getActivity().getApplicationContext(),"cannot fire getfeed: retry in 3s", Toast.LENGTH_SHORT).show();
//            wait 4 sec and try globalemit again
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

    private void initReload() {
        loadingCircle.setVisibility(View.GONE);
        reloadLayout.setVisibility(View.VISIBLE);
        reloadText.setText("Error loading user profile :(");
        if (!reloadButton.hasOnClickListeners()) {
            reloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reloadButton.setClickable(false);
                    reloadLayout.setVisibility(View.GONE);
                    loadingCircle.setVisibility(View.VISIBLE);
                    GlobalSocket.reconnect();
                    requestUserinfo();
                }
            });
        }
        reloadButton.setClickable(true);
    }


    @Override
    public void onDestroy() {
        if(GlobalSocket.mSocket.hasListeners(getString(R.string.onGetUserInfoRespond))) {
            GlobalSocket.mSocket.off(getString(R.string.onGetUserInfoRespond));
        }
        super.onDestroy();
    }

    private void findAllById() {
        profilePic = (ImageView) mainLayout.findViewById(R.id.profile_image_circle);
        profileName = (FontTextView) mainLayout.findViewById(R.id.display_name);
        usernameTV = (FontTextView) mainLayout.findViewById(R.id.username);
        userPicGridview = (GridView) mainLayout.findViewById(R.id.gridview_user_picture);

        reloadText = (FontTextView) reloadLayout.findViewById(R.id.reload_text);
        reloadButton = (Button) reloadLayout.findViewById(R.id.reload_button);
    }
}
