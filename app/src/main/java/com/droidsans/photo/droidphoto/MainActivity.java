package com.droidsans.photo.droidphoto;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.droidsans.photo.droidphoto.util.CircleTransform;
import com.droidsans.photo.droidphoto.util.FontTextView;


public class MainActivity extends AppCompatActivity {

    public static Context mContext;

    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private RelativeLayout navigationHeader;

    private FontTextView username;
    private FontTextView displayName;
    private ImageView profile;

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
        findAllById();
        setupUIFrame();
        attachFragment();
    }

    private void setupUIFrame() {
        setSupportActionBar(toolbar);

        displayName.setText(getUserdata().getString(getString(R.string.display_name), "no display name ??"));
        username.setText("@" + getUserdata().getString(getString(R.string.username), "... no username ?? must be a bug"));
        Glide.with(getApplicationContext())
                .load("https://pbs.twimg.com/profile_images/596106374725021696/r2zqUbK7_400x400.jpg")
                .centerCrop()
                .transform(new CircleTransform(getApplicationContext()))
                .into(profile);

        navigationHeader.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //TODO move to profile without losing touch anim
                return false;
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

//        previousMenuItem = navigationView.getMenu().getItem(0);
//        evaluateMenuItem.setVisible(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem menuItem) {
                if (previousMenuItem != null) previousMenuItem.setChecked(false);
//                menuItem.setChecked(true);
                String selectedMenu = menuItem.getTitle().toString();
                getSupportFragmentManager().popBackStack();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//                fragmentTransaction.setCustomAnimations(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
                if (selectedMenu.equals(getString(R.string.drawer_feed))) {
                    feedMenuItem.setChecked(true);
                    fragmentTransaction.replace(R.id.main_fragment, new FeedFragment());
                    fragmentTransaction.commit();
                    toolbar.setTitle("Feed");
                    previousMenuItem = feedMenuItem;
                } else if (selectedMenu.equals(getString(R.string.drawer_event))) {
                    eventMenuItem.setChecked(true);
                    fragmentTransaction.replace(R.id.main_fragment, new EventFragment());
                    fragmentTransaction.commit();
                    toolbar.setTitle("Events");
                    previousMenuItem = eventMenuItem;
                } else if (selectedMenu.equals(getString(R.string.drawer_help))) {
                    helpMenuItem.setChecked(true);
                    fragmentTransaction.replace(R.id.main_fragment, new ProfileFragment());
                    fragmentTransaction.commit();
                    toolbar.setTitle(getUserdata().getString(getString(R.string.username), "???"));
                    previousMenuItem = helpMenuItem;
                } else if (selectedMenu.equals(getString(R.string.drawer_about))) {
                    aboutMenuItem.setChecked(true);
                    fragmentTransaction.replace(R.id.main_fragment, new AboutFragment());
                    fragmentTransaction.commit();
                    toolbar.setTitle("About");
                    previousMenuItem = aboutMenuItem;
                } else if (selectedMenu.equals(getString(R.string.drawer_settings))) {
                    settingsMenuItem.setChecked(true);
                    evaluateMenuItem.setVisible(true);
                    fragmentTransaction.replace(R.id.main_fragment, new SettingsFragment());
                    fragmentTransaction.commit();
                    toolbar.setTitle("Settings");
                    previousMenuItem = menuItem;
                } else if (selectedMenu.equals(getString(R.string.drawer_logout))) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Logout ?")
                            .setMessage("are you sure ?")
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
                                    previousMenuItem.setChecked(true);
                                }
                            })
//                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(), "bug ?", Toast.LENGTH_SHORT).show();
                }
                drawerLayout.closeDrawers();
                return false;
            }
        });

        navigationView.getMenu().getItem(4).getSubMenu().removeItem(R.id.drawer_evaluate);
        drawerLayout.requestLayout();
        previousMenuItem = feedMenuItem;
        feedMenuItem.setChecked(true);

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
        getSupportFragmentManager().findFragmentById(R.id.main_fragment);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //outState.put(tag, data);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        finish();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
//        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void findAllById() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        navigationHeader = (RelativeLayout) findViewById(R.id.navigation_head);

        username = (FontTextView) findViewById(R.id.username);
        displayName = (FontTextView) findViewById(R.id.display_name);
        profile = (ImageView) findViewById(R.id.profile_image_circle);

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
