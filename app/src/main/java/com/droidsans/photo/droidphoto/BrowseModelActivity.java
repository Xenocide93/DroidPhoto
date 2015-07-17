package com.droidsans.photo.droidphoto;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.droidsans.photo.droidphoto.util.ModelListAdapter;


public class BrowseModelActivity extends AppCompatActivity {
    public static final String MODEL_NUM = "model_num";
    public static final String MODEL_NAME = "model_name";

    private ListView modelListView;
    private ModelListAdapter modelListAdapter;
    public static String[][] modelName = {
            {"a11w"},
            {"Zenfone 2", "Zenfone 5"},
            {"RIDGE"},
            {"Nexus 5"},
            {"Xperia P"},
            {"Galaxy S4", "Galaxy S5"}
    };

    private int vendorNum;
    private String vendorName;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_model);
        initialize();
    }

    private void initialize() {
        findAllById();
        setupToolbar();
        retrieveIntentData();
        setupModelListView();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void retrieveIntentData() {
        Intent previousIntent = getIntent();
        vendorNum = previousIntent.getIntExtra(BrowseVendorActivity.VENDOR_NUM, -1);
        vendorName = previousIntent.getStringExtra(BrowseVendorActivity.VENDOR_NAME);
//        Toast.makeText(getApplicationContext(), "Vendor Num: "+vendorNum, Toast.LENGTH_SHORT).show();
    }

    private void setupModelListView() {
        modelListAdapter = new ModelListAdapter(getApplicationContext(), R.layout.item_model, modelName[vendorNum]);
        modelListView.setAdapter(modelListAdapter);
        modelListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(BrowseVendorActivity.VENDOR_NUM, vendorNum);
                resultIntent.putExtra(BrowseVendorActivity.VENDOR_NAME, vendorName);
                resultIntent.putExtra(BrowseModelActivity.MODEL_NUM, position);
                resultIntent.putExtra(BrowseModelActivity.MODEL_NAME, modelName[vendorNum][position]);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    private void findAllById() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        modelListView = (ListView) findViewById(R.id.model_listview);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_browse_model, menu);
//        return true;
//    }

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
