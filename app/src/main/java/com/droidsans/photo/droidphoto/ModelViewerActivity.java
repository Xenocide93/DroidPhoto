package com.droidsans.photo.droidphoto;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.droidsans.photo.droidphoto.util.view.FontTextView;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModelViewerActivity extends AppCompatActivity {
    LinearLayout mainLayout;
    Button dismiss;
    FontTextView title;
    ListView listView;

    LinearLayout reloadLayout;
    ProgressBar loadingCircle;
    Button reloadButton;
    FontTextView reloadText;

    int vendorNum, modelNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_model_viewer);
//        setFinishOnTouchOutside(false);

        findAllById();
        fromPreviousIntent();
        setupListener();
        showLoadingView();
        requestBuild();
    }

    private void fromPreviousIntent() {
        Intent previousIntent = getIntent();
        vendorNum = previousIntent.getIntExtra(BrowseVendorActivity.VENDOR_NUM, -1);
        modelNum = previousIntent.getIntExtra(BrowseModelActivity.MODEL_NUM, -1);
        if(vendorNum < 0 || modelNum < 0) {
            Toast.makeText(getApplicationContext(), "Error : -1", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showLoadingView() {
        mainLayout.setVisibility(View.GONE);
        reloadLayout.setVisibility(View.GONE);
        loadingCircle.setVisibility(View.VISIBLE);
    }

    private void showReloadView() {
        mainLayout.setVisibility(View.GONE);
        loadingCircle.setVisibility(View.GONE);
        reloadLayout.setVisibility(View.VISIBLE);
    }

    private void showNormalView() {
        loadingCircle.setVisibility(View.GONE);
        reloadLayout.setVisibility(View.GONE);
        mainLayout.setVisibility(View.VISIBLE);

        title.setText(BrowseVendorActivity.vendorName[vendorNum] + " " + BrowseModelActivity.modelName[vendorNum][modelNum]);
    }

    private void setupListener() {
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadingView();
                requestBuild();
            }
        });

        if(!GlobalSocket.mSocket.hasListeners("get_build")) {
            GlobalSocket.mSocket.on("get_build", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject data = (JSONObject) args[0];
                            if (data.optBoolean("success")) {
                                GlobalSocket.mSocket.off("get_build");
                                JSONArray buildList = data.optJSONArray("buildList");
                                int len = buildList.length();
                                final String[] bDevice = new String[len];
                                final String[] bModel = new String[len];
                                for(int i = 0; i < len; i++) {
                                    JSONObject build = buildList.optJSONObject(i);
                                    bDevice[i] = build.optString("build_device");
                                    bModel[i] = build.optString("build_model");
                                }

//                                Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_LONG).show();
                                showNormalView();

                                ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.item_build_info, bDevice){
                                    @Override
                                    public View getView(int position, View convertView, ViewGroup parent) {
                                        View row;
                                        if(convertView == null) {
                                            row = getLayoutInflater().inflate(R.layout.item_build_info, null);
                                        } else {
                                            row = convertView;
                                        }

                                        TextView v = (TextView) row.findViewById(R.id.build_device_text);
                                        v.setText(bDevice[position]);
//                                        v.setText("DEVICE (OS): " + bDevice[position]);
                                        v = (TextView) row.findViewById(R.id.build_model_text);
                                        v.setText(bModel[position]);
//                                        v.setText("MODEL (OS): " + bModel[position]);

                                        ImageView report = (ImageView) row.findViewById(R.id.model_show_info);
                                        report.setVisibility(getSharedPreferences(getString(R.string.userdata), MODE_PRIVATE)
                                                .getInt(getString(R.string.user_priviledge), 1) > 1? ImageView.VISIBLE: ImageView.GONE);

                                        return row;
//                                        return super.getView(position, convertView, parent);
                                    }
                                };

                                listView.setAdapter(adapter);

                            } else {
                                showReloadView();
                            }
                        }
                    });
                }
            });
        }
    }

    private void requestBuild() {
        JSONObject send = new JSONObject();

        try {
            send.put("retail_vendor", BrowseVendorActivity.vendorName[vendorNum]);
            send.put("retail_model", BrowseModelActivity.modelName[vendorNum][modelNum]);
            send.put("_event", "get_build");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(!GlobalSocket.globalEmit("device.getbuild", send)) {
            showReloadView();
        }
    }

    @Override
    protected void onDestroy() {
        GlobalSocket.mSocket.off("get_build");
        super.onDestroy();
    }

    private void findAllById() {
        mainLayout = (LinearLayout) findViewById(R.id.model_list_layout);
        dismiss = (Button) findViewById(R.id.button_dismiss);
        title = (FontTextView) findViewById(R.id.dialog_title);
        listView = (ListView) findViewById(R.id.model_list);

        loadingCircle = (ProgressBar) findViewById(R.id.loading_circle);

        reloadLayout = (LinearLayout) findViewById(R.id.reload_view);
        reloadButton = (Button) findViewById(R.id.reload_button);
        reloadText = (FontTextView) findViewById(R.id.reload_text);
    }

}
