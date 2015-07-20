package com.droidsans.photo.droidphoto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.droidsans.photo.droidphoto.util.CircleTransform;
import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

public class EditProfileActivity extends AppCompatActivity {
    public static final String IS_UPDATE_PROFILE_PIC = "isUpdateProfilePic";

    private Toolbar toolbar;
    private ImageView profilePic;
    private ImageButton editProfilePic;
    private Button changePass;
    private EditText displayName, profileDescription;

    private Handler delayAction = new Handler();

    private boolean isUpdateProfilePic = false;

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
                //TODO upload new profile pic

                isUpdateProfilePic = true;
            }
        });

        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent changePassIntent = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                startActivity(changePassIntent);
            }
        });
    }

    private void loadProfileData() {
        Intent oldIntent = getIntent();
        displayName.setText(oldIntent.getStringExtra(ProfileFragment.DISPLAY_NAME));
        profileDescription.setText(oldIntent.getStringExtra(ProfileFragment.PROFILE_DESCRIPTION));
        if(ProfileFragment.profilePictureDrawable!=null){
            profilePic.setImageDrawable(ProfileFragment.profilePictureDrawable);
        } else {
            JSONObject data = new JSONObject();
            try {
                data.put("_token", getApplicationContext().getSharedPreferences(getString(R.string.userdata), Context.MODE_PRIVATE).getString(getString(R.string.token), ""));
                data.put("_event", "userinfo_respond_edit_profile");
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
                        }
                    }
                }, 2000);
            }

            GlobalSocket.mSocket.on("userinfo_respond_edit_profile", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            GlobalSocket.mSocket.off("userinfo_respond");
                            Glide.with(getApplicationContext().getApplicationContext())
                                    .load(GlobalSocket.serverURL + ProfileFragment.baseURL + ((JSONObject)args[0]).optString("avatar_url"))
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .placeholder(R.drawable.ic_account_circle_black_48dp)
                                    .centerCrop()
                                    .transform(new CircleTransform(getApplicationContext()))
                                    .into(profilePic);
                        }
                    });
                }
            });
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setTitle(getString(R.string.title_activity_edit_profile));
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
                //TODO return profile data to MainActivity
                Intent returnData = new Intent();
                returnData.putExtra(ProfileFragment.DISPLAY_NAME, displayName.getText().toString());
                returnData.putExtra(ProfileFragment.PROFILE_DESCRIPTION, profileDescription.getText().toString());
                returnData.putExtra(EditProfileActivity.IS_UPDATE_PROFILE_PIC, isUpdateProfilePic);

                setResult(Activity.RESULT_OK, returnData);
                finish();
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

    private void FindAllById() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        profilePic = (ImageView) findViewById(R.id.profile_pic);
        editProfilePic  = (ImageButton) findViewById(R.id.edit_profile_pic);
        displayName = (EditText) findViewById(R.id.display_name);
        profileDescription = (EditText) findViewById(R.id.profile_desc);
        changePass = (Button) findViewById(R.id.change_pass_btn);
    }
}
