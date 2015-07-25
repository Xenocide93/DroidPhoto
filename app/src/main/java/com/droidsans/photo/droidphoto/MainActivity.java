package com.droidsans.photo.droidphoto;

import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.droidsans.photo.droidphoto.util.transform.CircleTransform;
import com.droidsans.photo.droidphoto.util.view.FontTextView;
import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private static final int ACTIVITY_SETTINGS = 1024;
    public static Context mContext;
    public static int snackString;

    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    public static ActionBarDrawerToggle actionBarDrawerToggle;

    private RelativeLayout navigationHeader;

    private FontTextView username;
    private FontTextView displayName;
    private ImageView profile;

    private Handler delayAction = new Handler();

    private MenuItem previousMenuItem;
    private MenuItem feedMenuItem, eventMenuItem, helpMenuItem, aboutMenuItem, settingsMenuItem, evaluateMenuItem, logoutMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();

        initialize();
    }

    private void initialize() {
        GlobalSocket.initializeSocket();
        findAllById();
        setupUIFrame();
        attachFragment();
        setupListener();
        getUserInfo();
        printDeviceInfo();
        makeSnack();
    }

    public void makeSnack() {
        if(snackString != 0) {
            Snackbar.make(findViewById(R.id.main_fragment), getString(snackString), Snackbar.LENGTH_SHORT).show();
            snackString = 0;
        }
    }

    public void makeSnack(String s) {
        Snackbar.make(findViewById(R.id.main_fragment), s, Snackbar.LENGTH_SHORT).show();
    }

    private void setupListener() {
        if(!GlobalSocket.mSocket.hasListeners("get_user_info")) {
            GlobalSocket.mSocket.on("get_user_info", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject data = (JSONObject) args[0];
                            if (data.optBoolean("success")) {
                                // happy
                                JSONObject userObj = data.optJSONObject("userObj");
                                getUserdata().edit()
                                        .putString(getString(R.string.display_name), userObj.optString("disp_name"))
                                        .putString(getString(R.string.avatar_url), userObj.optString("avatar_url"))
                                        .putInt(getString(R.string.user_priviledge), userObj.optInt("priviledge"))
                                        .apply();
                                displayName.setText(userObj.optString("disp_name", ""));
                                Glide.with(getApplicationContext())
                                        .load(GlobalSocket.serverURL + ProfileFragment.baseURL + userObj.optString("avatar_url"))
//                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .placeholder(R.drawable.avatar_placeholder_300)
                                        .centerCrop()
                                        .transform(new CircleTransform(getApplicationContext()))
                                        .into(profile);
                                if (userObj.optInt("priviledge") == 2) {
//                                    if(previousMenuItem != null) previousMenuItem.setChecked(false);
//                                    navigationView.getMenu().clear();
//                                    navigationView.inflateMenu(R.menu.menu_drawer_mod);
//                                    findModMenuById();
                                    evaluateMenuItem.setVisible(true);
                                }
                            } else {
                                switch (data.optString("msg")) {
                                    case "db error":
                                        //sad
                                        break;
                                    default:
                                        break;
                                }
                                Toast.makeText(getApplicationContext(), data.optString("msg"), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        }
    }

    public void getUserInfo() {
        final JSONObject data = new JSONObject();
        try {
            data.put("_event", "get_user_info");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(!GlobalSocket.globalEmit("user.getuserinfo", data)) {
            delayAction.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!GlobalSocket.globalEmit("user.getuserinfo", data)) {
                        //sad
                    }
                }
            }, 2500);
        };
    }

    private void printDeviceInfo(){
        String s =  "brand:" + Build.BRAND
                + "\n model:" + Build.MODEL
                + "\n manufacture:" + Build.MANUFACTURER
                + "\n device:" + Build.DEVICE
                + "\n fingerprint:" + Build.FINGERPRINT
                + "\n id:" + Build.ID;
        Log.d("droidphoto", s);
    }

    private void setupUIFrame() {
        setSupportActionBar(toolbar);
//        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                return false;
//            }
//        });
//
//        toolbar.inflateMenu(R.menu.menu_main);

        username.setText("@" + getUserdata().getString(getString(R.string.username), "... no username ?? must be a bug"));

        String disp = getUserdata().getString(getString(R.string.display_name), "");
        displayName.setText(disp);
//        if(!disp.equals("")) {
        Glide.with(getApplicationContext())
                .load(GlobalSocket.serverURL + ProfileFragment.baseURL + getUserdata().getString(getString(R.string.avatar_url), ""))
                .placeholder(R.drawable.avatar_placeholder_300)
                .centerCrop()
                .transform(new CircleTransform(getApplicationContext()))
                .into(profile);
//        } else {
//        }

        switch (getUserdata().getInt(getString(R.string.user_priviledge), 1)) {
            case 1:
//                if(previousMenuItem != null) previousMenuItem.setChecked(false);
//                navigationView.getMenu().clear();
//                navigationView.inflateMenu(R.menu.menu_drawer);
//                navigationView.requestLayout();
//                findUserMenuById();
                findModMenuById();
                break;
            case 2:
                if(previousMenuItem != null) previousMenuItem.setChecked(false);
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.menu_drawer_mod);
                navigationView.requestLayout();
                findModMenuById();
                break;
        }



        navigationHeader.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                Snackbar.make(findViewById(R.id.main_fragment), "onCreateContextMenu", Snackbar.LENGTH_SHORT)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                            }
                        })
                        .setActionTextColor(getResources().getColor(R.color.accent_color))
                        .show();
                Log.d("droidphoto", "oncreatecontextmenu");
            }
        });

        navigationHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_fragment, new ProfileFragment());
                fragmentTransaction.commit();
                toolbar.setTitle(getUserdata().getString(getString(R.string.username), "???"));
                drawerLayout.closeDrawers();
                if (previousMenuItem != null) previousMenuItem.setChecked(false);
                previousMenuItem = null;

            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(new android.support.v4.app.FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                boolean canback = getSupportFragmentManager().getBackStackEntryCount() > 0;
//                getSupportActionBar().setDisplayHomeAsUpEnabled(canback);
            }



        });

//        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
//            @Override
//            public void onBackStackChanged() {
//                if(getSupportFragmentManager().getBackStackEntryCount() == 0) {
//                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//                    actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
//                    actionBarDrawerToggle.syncState();
//                } else {
//                    actionBarDrawerToggle.setDrawerIndicatorEnabled(false);
//                    actionBarDrawerToggle.syncState();
//                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//                }
//            }
//        });

//        previousMenuItem = feedMenuItem;
//        evaluateMenuItem.setVisible(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem menuItem) {
                if(previousMenuItem != null) previousMenuItem.setChecked(false);
//                menuItem.setChecked(true);
                String selectedMenu = menuItem.getTitle().toString();

//                fragmentTransaction.setCustomAnimations(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
                if (selectedMenu.equals(getString(R.string.drawer_feed))) {
//                    GlobalSocket.reconnect();
                    feedMenuItem.setChecked(true);
                    getSupportFragmentManager().popBackStack();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_fragment, new FeedFragment());
                    fragmentTransaction.commit();
                    toolbar.setTitle(getString(R.string.drawer_feed));
                    previousMenuItem = feedMenuItem;
                } else if (selectedMenu.equals(getString(R.string.drawer_event))) {
//                    GlobalSocket.reconnect();
                    eventMenuItem.setChecked(true);
                    getSupportFragmentManager().popBackStack();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_fragment, new EventFragment());
                    fragmentTransaction.commit();
                    toolbar.setTitle(getString(R.string.drawer_event));
                    previousMenuItem = eventMenuItem;
                } else if (selectedMenu.equals(getString(R.string.drawer_help))) {
//                GlobalSocket.reconnect();
                    helpMenuItem.setChecked(true);
                    getSupportFragmentManager().popBackStack();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_fragment, new PlaceholderFragment());
                    fragmentTransaction.commit();
                    toolbar.setTitle(getString(R.string.drawer_help));
                    previousMenuItem = helpMenuItem;
                } else if (selectedMenu.equals(getString(R.string.drawer_about))) {
                    aboutMenuItem.setChecked(true);
                    getSupportFragmentManager().popBackStack();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_fragment, new AboutFragment());
                    fragmentTransaction.commit();
                    toolbar.setTitle(getString(R.string.drawer_about));
                    previousMenuItem = aboutMenuItem;
                } else if (selectedMenu.equals(getString(R.string.drawer_settings))) {
                    settingsMenuItem.setChecked(true);
//                    getSupportFragmentManager().popBackStack();
//                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//                    fragmentTransaction.replace(R.id.main_fragment, new SettingsFragment());
//                    fragmentTransaction.commit();
//                    toolbar.setTitle(getString(R.string.drawer_settings));
                    startActivityForResult(new Intent(getApplicationContext(), SettingsActivity.class), ACTIVITY_SETTINGS);
//                    previousMenuItem = settingsMenuItem;
                } else if (selectedMenu.equals(getString(R.string.drawer_evaluate))) {
                    evaluateMenuItem.setChecked(true);
                    getSupportFragmentManager().popBackStack();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_fragment, new PlaceholderFragment());
                    fragmentTransaction.commit();
                    toolbar.setTitle(getString(R.string.drawer_evaluate));
                    previousMenuItem = evaluateMenuItem;
                } else if (selectedMenu.equals(getString(R.string.drawer_logout))) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(getString(R.string.drawer_logout) + " ?")
                            .setMessage(getString(R.string.drawer_logout_confirm))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getUserdata().edit().clear().apply(); //clear userdata from sharedprefs.
                                    Intent login = new Intent(getApplicationContext(), SplashLoginActivity.class);
                                    startActivity(login);
                                    finish();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    logoutMenuItem.setChecked(false);
                                    if (previousMenuItem != null) previousMenuItem.setChecked(true);
                                }
                            })
//                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(), "bug ?", Toast.LENGTH_SHORT).show();
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,toolbar, R.string.drawer_open, R.string.drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
//                actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
//                actionBarDrawerToggle.setDrawerIndicatorEnabled(getSupportFragmentManager().getBackStackEntryCount() == 0);
            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);

        actionBarDrawerToggle.syncState();
    }



    private void attachFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_fragment, new FeedFragment());
        fragmentTransaction.commit();
        toolbar.setTitle("Feed");
        previousMenuItem = feedMenuItem;
        feedMenuItem.setChecked(true);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        actionBarDrawerToggle.syncState();
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(requestCode == ACTIVITY_SETTINGS) {
//            settingsMenuItem.setChecked(false);
//            if(previousMenuItem != null) previousMenuItem.setChecked(true);
//        }
//    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //outState.put(tag, data);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
//        if(GlobalSocket.mSocket.hasListeners("get_csv")) {
//            GlobalSocket.mSocket.off("get_csv");
//        }
        if(GlobalSocket.mSocket.hasListeners("get_user_info")) {
            GlobalSocket.mSocket.off("get_user_info");
        }
        finish();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(ProfileFragment.mProfileFragment!=null){
            if(ProfileFragment.mProfileFragment.adapter.isInEditMode){
                ProfileFragment.mProfileFragment.cancelEditMode();
                return;
            } else {
                super.onBackPressed();
            }
        }
        super.onBackPressed();
        if(getSupportFragmentManager().getBackStackEntryCount() == 0) {
            actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getSupportFragmentManager().popBackStack();
        actionBarDrawerToggle.syncState();
        return true;
    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
////        return super.onCreateOptionsMenu(menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
//        switch(item.getItemId()) {
//            case R.id.action_settings:
//                return true;
//            case R.id.action_search:
//                Toast.makeText(getApplicationContext(), "Search", Toast.LENGTH_SHORT).show();
//                return true;
//            case R.id.action_remove_picture:
//                if(fragment instanceof ProfileFragment){
//                    ((ProfileFragment)fragment).toggleEditMode();
//                }
//                return true;
//            case R.id.action_filter:
//                if(fragment instanceof FeedFragment){
//                    ((FeedFragment)fragment).launchAddFilterPopup();
//                }
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private void findAllById() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        navigationHeader = (RelativeLayout) findViewById(R.id.navigation_head);

        username = (FontTextView) findViewById(R.id.username);
        displayName = (FontTextView) findViewById(R.id.display_name);
        profile = (ImageView) findViewById(R.id.profile_image_circle);
    }

    private void findUserMenuById() {
        feedMenuItem = navigationView.getMenu().getItem(0);
        eventMenuItem = navigationView.getMenu().getItem(1);
        helpMenuItem = navigationView.getMenu().getItem(2);
        aboutMenuItem = navigationView.getMenu().getItem(3);
        settingsMenuItem = navigationView.getMenu().getItem(4).getSubMenu().getItem(0);
        logoutMenuItem = navigationView.getMenu().getItem(4).getSubMenu().getItem(1);
    }

    private void findModMenuById() {
        feedMenuItem = navigationView.getMenu().getItem(0);
        eventMenuItem = navigationView.getMenu().getItem(1);
        helpMenuItem = navigationView.getMenu().getItem(2);
        aboutMenuItem = navigationView.getMenu().getItem(3);
        settingsMenuItem = navigationView.getMenu().getItem(4).getSubMenu().getItem(0);
        evaluateMenuItem = navigationView.getMenu().getItem(4).getSubMenu().getItem(1);
        logoutMenuItem = navigationView.getMenu().getItem(4).getSubMenu().getItem(2);
    }

    private SharedPreferences getUserdata() {
        return getSharedPreferences(getString(R.string.userdata), Context.MODE_PRIVATE);
    }
}
