package com.droidsans.photo.droidphoto;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;


public class MainActivity extends Activity {

    private ImageButton browseBtn, camraBtn, eventBtn;
    private View buttonsLayout, logoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
    }

    private void initialize(){
        findAllById();
        setListener();
        setAnimation();
    }

    private void setAnimation() {
        logoLayout.animate().yBy(-250).setDuration(1200).setStartDelay(2000).start();
        buttonsLayout.setVisibility(View.VISIBLE);
        buttonsLayout.animate().alpha(1).setDuration(700).setStartDelay(2500).start();
    }


    private void setListener(){
        browseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browseIntent = new Intent(getApplicationContext(), BrowseVendorActivity.class);
                startActivity(browseIntent);
            }
        });
        camraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Launch Picture Picker Intent", Toast.LENGTH_SHORT).show();
            }
        });
        eventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventIntent = new Intent(getApplicationContext(), EventActivity.class);
                startActivity(eventIntent);
            }
        });
    }

    private void findAllById(){
        browseBtn = (ImageButton) findViewById(R.id.browse_btn);
        camraBtn = (ImageButton) findViewById(R.id.cam_btn);
        eventBtn = (ImageButton) findViewById(R.id.event_btn);

        buttonsLayout = findViewById(R.id.btn_layout);
        logoLayout = findViewById(R.id.logo_layout);
    }
}
