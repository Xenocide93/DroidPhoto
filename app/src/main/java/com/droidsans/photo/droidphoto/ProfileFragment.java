package com.droidsans.photo.droidphoto;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.droidsans.photo.droidphoto.util.CircleTransform;
import com.droidsans.photo.droidphoto.util.FontTextView;
import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private ProgressBar loadingCircle;
    private LinearLayout reloadLayout;
    private FrameLayout mainLayout;

    private ImageView profilePic;
    private FontTextView profileName;
    private FontTextView username;

    private FontTextView reloadText;
    private Button reloadButton;

    private String baseURL = "avatar/";

    private Handler delayAction = new Handler();

    private Emitter.Listener onGetUserInfoRespond;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        loadingCircle = (ProgressBar) rootView.findViewById(R.id.loading_circle);
        reloadLayout = (LinearLayout) rootView.findViewById(R.id.reload_view);
        mainLayout = (FrameLayout) rootView.findViewById(R.id.main_view);
        initialize();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        requestUserinfo();
    }

    private void initialize() {
        findAllById();
        setupListener();
    }

    private void setupListener() {
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
                            username.setText(userObj.optString("username"));
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

    private void initReload() {
        loadingCircle.setVisibility(ProgressBar.GONE);
        reloadLayout.setVisibility(LinearLayout.VISIBLE);
        reloadText.setText("Error loading user profile :(");
        if (!reloadButton.hasOnClickListeners()) {
            reloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reloadButton.setClickable(false);
                    reloadLayout.setVisibility(LinearLayout.GONE);
                    loadingCircle.setVisibility(ProgressBar.VISIBLE);
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
        username = (FontTextView) mainLayout.findViewById(R.id.username);

        reloadText = (FontTextView) reloadLayout.findViewById(R.id.reload_text);
        reloadButton = (Button) reloadLayout.findViewById(R.id.reload_button);
    }
}
