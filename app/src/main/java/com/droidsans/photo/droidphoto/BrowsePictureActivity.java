package com.droidsans.photo.droidphoto;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.droidsans.photo.droidphoto.util.PictureGridAdapter;
import com.droidsans.photo.droidphoto.util.PicturePack;

import java.util.ArrayList;


public class BrowsePictureActivity extends ActionBarActivity {
    private GridView pictureGridView;
    private LinearLayout tagLayout;
    private PictureGridAdapter pictureGridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_picture);

        findAllById();
        setupAdapter();
    }

    private void setupAdapter() {
        pictureGridAdapter = new PictureGridAdapter(getApplicationContext(), R.layout.item_pic, getTestPicturePackArray());
        pictureGridView.setAdapter(pictureGridAdapter);
    }

    private ArrayList<PicturePack> getTestPicturePackArray(){
        ArrayList<PicturePack> testPack = new ArrayList<PicturePack>();
        testPack.add(new PicturePack(1, "Sony", "Xperia Z3+", "1/60s", "f2.0", "400"));
        testPack.add(new PicturePack(1, "Samsug", "Galaxy S6", "1/200s", "f1.9", "100"));
        testPack.add(new PicturePack(1, "LG", "G4", "1/10s", "f1.8", "50"));
        testPack.add(new PicturePack(1, "ASUS", "Zenfone 2", "1/250s", "f2.0", "800"));
        testPack.add(new PicturePack(1, "OPPO", "Find 7a", "1/30s", "f2.0", "200"));
        testPack.add(new PicturePack(1, "HTC", "M9+", "1/100s", "f2.0", "200"));
        testPack.add(new PicturePack(1, "Xiaomi", "Mi Note Pro", "1/30s", "f1.8", "200"));
        testPack.add(new PicturePack(1, "Meizu", "M2 Note", "1/20s", "f1.8", "800"));

        return testPack;
    }

    private void findAllById() {
        pictureGridView = (GridView) findViewById(R.id.pic_gridview);
        tagLayout = (LinearLayout) findViewById(R.id.tag_layout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_browse_picture, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
