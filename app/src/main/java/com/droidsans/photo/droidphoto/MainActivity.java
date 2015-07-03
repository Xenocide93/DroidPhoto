package com.droidsans.photo.droidphoto;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
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

    private MenuItem prevoiusMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "onCreate", Toast.LENGTH_LONG).show();
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

        prevoiusMenuItem = navigationView.getMenu().getItem(0);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem menuItem) {

                menuItem.setChecked(true);
                String selectedMenu = menuItem.getTitle().toString();

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//                fragmentTransaction.setCustomAnimations(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
                if(selectedMenu.equals(getString(R.string.drawer_feed))) {
                    fragmentTransaction.replace(R.id.main_fragment, new FeedFragment());
                    fragmentTransaction.commit();
                    toolbar.setTitle("Feed");
                    prevoiusMenuItem = menuItem;
                } else if(selectedMenu.equals(getString(R.string.drawer_event))) {
                    fragmentTransaction.replace(R.id.main_fragment, new EventFragment());
                    fragmentTransaction.commit();
                    toolbar.setTitle("Events");
                    prevoiusMenuItem = menuItem;
                } else if(selectedMenu.equals(getString(R.string.drawer_help))) {
                    fragmentTransaction.replace(R.id.main_fragment, new ProfileFragment());
                    fragmentTransaction.commit();
                    toolbar.setTitle(getUserdata().getString(getString(R.string.username),"???"));
                    prevoiusMenuItem = menuItem;
                } else if(selectedMenu.equals(getString(R.string.drawer_about))) {
                    fragmentTransaction.replace(R.id.main_fragment, new AboutFragment());
                    fragmentTransaction.commit();
                    toolbar.setTitle("About");
                    prevoiusMenuItem = menuItem;
                } else if(selectedMenu.equals(getString(R.string.drawer_settings))){
//                    fragmentTransaction.replace(R.id.main_fragment, new AboutFragment());
//                    fragmentTransaction.commit();
                    toolbar.setTitle("Settings");
                    prevoiusMenuItem = menuItem;
                } else if(selectedMenu.equals(getString(R.string.drawer_logout))) {
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
                                    menuItem.setChecked(false);
                                    prevoiusMenuItem.setChecked(true);
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

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,toolbar, R.string.drawer_open, R.string.drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    private void attachFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_fragment, new FeedFragment());
        fragmentTransaction.commit();
        toolbar.setTitle("Feed");
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        actionBarDrawerToggle.syncState();
        super.onPostCreate(savedInstanceState, persistentState);
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
    }

    private SharedPreferences getUserdata() {
        return getSharedPreferences(getString(R.string.userdata), Context.MODE_PRIVATE);
    }


}
