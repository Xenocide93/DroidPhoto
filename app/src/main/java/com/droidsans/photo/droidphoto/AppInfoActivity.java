package com.droidsans.photo.droidphoto;

import android.content.Context;
import android.database.DataSetObserver;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AppInfoActivity extends AppCompatActivity {
    Toolbar toolbar;
    ListView mainList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);

        double cacheSize = 0;
        File ccf = new File(getCacheDir(), getString(R.string.cache_table_name));
        if(ccf.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(ccf));
                String read = br.readLine();
                if (read.isEmpty()) {
                    cacheSize = 0;
                    br.close();
                } else {
                    cacheSize = Integer.parseInt(read);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int launchCount = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(getString(R.string.launch_count), 0);

        final String[] name = new String[]{
                "Application Name",
                "Version",
                "Image Cache Size",
                "Launch Count"
        };
        final String[] value = new String[]{
                getString(R.string.app_name),
                getString(R.string.app_version),
                String.format("%.2f",(cacheSize / (1024.0*1024.0))) + " MB",
                launchCount + ((launchCount > 1)? " times":" time")
        };
        int length = name.length;
        final String[][] dataset = new String[][]{name, value};

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mainList = (ListView) findViewById(R.id.appinfo_list);

//        mainList.addHeaderView(((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_about_header, null, false));

        List<Map<String, String>> data = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            Map<String, String> datum = new HashMap<>(2);
            datum.put("name", name[i]);
            datum.put("value", value[i]);
            data.add(datum);
        }
//        SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), data, R.layout.item_info, new String[]{"name", "value"}, new int[]{R.id.text_1, R.id.text_2});
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.item_info, name){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row;
                if(convertView == null)
                {
                    row = getLayoutInflater().inflate(R.layout.item_info, null);
                }
                else
                {
                    row = convertView;
                }
                TextView v = (TextView) row.findViewById(R.id.text_1);
                v.setText(name[position]);
                v = (TextView) row.findViewById(R.id.text_2);
                v.setText(value[position]);
                return row;
//                return super.getView(position, convertView, parent);
            }
        };
        mainList.setAdapter(adapter);


        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_app_info, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
