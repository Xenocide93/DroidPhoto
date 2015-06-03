package com.droidsans.photo.droidphoto;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

import com.droidsans.photo.droidphoto.com.droidsans.photo.droidphoto.util.VendorGridAdapter;

public class BrowseVendorActivity extends ActionBarActivity {
    private GridView vendorGridView;
    private VendorGridAdapter vendorGridAdapter;
    private Integer[] vendorPicResource = {
            R.drawable.curve_primary,
            R.drawable.curve_primary,
            R.drawable.curve_primary,
            R.drawable.curve_primary,
            R.drawable.curve_primary,
            R.drawable.curve_primary,
            R.drawable.curve_primary,
            R.drawable.curve_primary,
            R.drawable.curve_primary,
            R.drawable.curve_primary,
            R.drawable.curve_primary,
            R.drawable.curve_primary,
            R.drawable.curve_primary,
            R.drawable.curve_primary
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_vendor);
        initialize();
    }

    private void initialize() {
        findAllById();
        setupVendorGridView();
    }

    private void setupVendorGridView() {
        vendorGridAdapter = new VendorGridAdapter(getApplicationContext(), R.layout.vendor_item, vendorPicResource);
        vendorGridView.setAdapter(vendorGridAdapter);
    }

    private void findAllById() {
        vendorGridView = (GridView) findViewById(R.id.vendor_gridview);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_browse, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
