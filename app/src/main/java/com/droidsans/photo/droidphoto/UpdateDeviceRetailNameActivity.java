package com.droidsans.photo.droidphoto;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

public class UpdateDeviceRetailNameActivity extends AppCompatActivity {
    private TextView buildManufacture, buildModel;
    private EditText retailVendor, retailModel;
    private Button search, pasteVendor, pasteModel, dismiss, update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_device_retail_name);
        
        setupToolbar();
        findAllById();
        setupData();
        setupButtonListener();
        setupUpdateButton();
    }

    private void setupUpdateButton() {
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject data = new JSONObject();
                try {
                    data.put("retail_vendor", retailVendor.getText());
                    data.put("retail_model", retailModel.getText());
                    data.put("build_device", buildManufacture.getText());
                    data.put("build_model", buildModel.getText());
                    data.put("_event", "onUpdateDeviceNameRespond");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                GlobalSocket.globalEmit("device.update", data);

                //setup emitter listener
                if(!GlobalSocket.mSocket.hasListeners("onUpdateDeviceNameRespond")){
                    GlobalSocket.mSocket.on("onUpdateDeviceNameRespond", new Emitter.Listener() {
                        @Override
                        public void call(final Object... args) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    GlobalSocket.mSocket.off("onUpdateDeviceNameRespond");
                                    JSONObject data = (JSONObject) args[0];
                                    if(data.optBoolean("success")){
                                        Toast.makeText(getApplicationContext(), "update device name success", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Error: "+data.optString("smg")+"\nPlease try again", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private void setupButtonListener() {
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.google.com/#q=" + buildManufacture.getText() + "+" + buildModel.getText());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        pasteVendor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                retailVendor.setText(clipboard.getPrimaryClip().getItemAt(0).getText().toString());
            }
        });

        pasteModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                retailModel.setText(clipboard.getPrimaryClip().getItemAt(0).getText().toString());
            }
        });
    }

    private void findAllById() {
        buildManufacture = (TextView) findViewById(R.id.raw_manufacture);
        buildModel = (TextView) findViewById(R.id.raw_model);
        retailVendor = (EditText) findViewById(R.id.correct_vendor);
        retailModel = (EditText) findViewById(R.id.correct_model);
        search = (Button) findViewById(R.id.search);
        pasteVendor = (Button) findViewById(R.id.paste_vendor);
        pasteModel = (Button) findViewById(R.id.paste_model);
        dismiss = (Button) findViewById(R.id.dismiss);
        update = (Button) findViewById(R.id.update);
    }

    private void setupData() {
        Intent oldIntent = getIntent();
        buildManufacture.setText(oldIntent.getStringExtra("build_manufacture"));
        buildModel.setText(oldIntent.getStringExtra("build_model"));
        retailVendor.setText(oldIntent.getStringExtra("retail_vendor"));
        retailModel.setText(oldIntent.getStringExtra("retail_model"));
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_update_device_retail_name, menu);
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
        } else if (id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
