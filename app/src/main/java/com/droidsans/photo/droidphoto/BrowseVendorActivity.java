package com.droidsans.photo.droidphoto;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.droidsans.photo.droidphoto.util.VendorGridAdapter;

public class BrowseVendorActivity extends ActionBarActivity {
    public static final int CHOOOSE_MODEL_REQUEST = 3;

    public static final String VENDOR_NUM = "vendor_num";

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
        vendorGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent chooseModelIntent = new Intent(getApplicationContext(), BrowseModelActivity.class);
                chooseModelIntent.putExtra(BrowseVendorActivity.VENDOR_NUM, position);
                startActivityForResult(chooseModelIntent, CHOOOSE_MODEL_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case CHOOOSE_MODEL_REQUEST:
                    setResult(RESULT_OK, data);
                    finish();
                    break;
            }
        } else {
            Toast.makeText(getApplicationContext(), "cancel", Toast.LENGTH_SHORT).show();
        }
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
