package com.droidsans.photo.droidphoto;

import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class ChangePasswordActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText oldPassword, newPassword, confirmPassword;
    private Button changeButton;
    private Emitter.Listener onPasswordChangeRespond;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        oldPassword = (EditText) findViewById(R.id.change_password_old);
        newPassword = (EditText) findViewById(R.id.change_password_new);
        confirmPassword = (EditText) findViewById(R.id.change_password_confirm);
        changeButton = (Button) findViewById(R.id.button_confirm);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(newPassword.getText().toString().equals(confirmPassword.getText().toString())) {
                    changeButton.setClickable(false);
                    changeButton.setTextColor(getResources().getColor(R.color.light_gray));

                    JSONObject changePassObj = new JSONObject();
                    String hexOldPassword = "";
                    String hexNewPassword = "";

                    try {
                        MessageDigest md = MessageDigest.getInstance("SHA-256");
                        md.update(oldPassword.getText().toString().getBytes());
                        hexOldPassword = bytesToHex(md.digest());
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }

                    try {
                        MessageDigest md = MessageDigest.getInstance("SHA-256");
                        md.update(newPassword.getText().toString().getBytes());
                        hexNewPassword = bytesToHex(md.digest());
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }

                    try {
                        changePassObj.put("old_pass", hexOldPassword);
                        changePassObj.put("new_pass", hexNewPassword);
                        changePassObj.put("_event", "change_password_respond");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    Log.d(APP_LOG, hexPassword);
                    if (!GlobalSocket.mSocket.connected()) {
                        GlobalSocket.mSocket.connect();
                        Toast.makeText(getApplicationContext(), "cannot connect to server", Toast.LENGTH_SHORT).show();
                        changeButton.setClickable(true);
                        changeButton.setTextColor(getResources().getColor(R.color.black));
//                        GlobalSocket.reconnect();
                    } else {
                        GlobalSocket.globalEmit("user.changepass", changePassObj);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "newpass != confirm", Toast.LENGTH_SHORT).show();
                }
            }
        });
        onPasswordChangeRespond = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        if(data.optBoolean("success")) {
                            GlobalSocket.mSocket.off("change_password_respond");
                            getSharedPreferences(getString(R.string.userdata), MODE_PRIVATE).edit()
                                    .remove(getString(R.string.token))
                                    .putString(getString(R.string.token), data.optString("_token"))
                                    .apply();
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), data.optString("msg"), Toast.LENGTH_SHORT).show();
                            switch (data.optString("msg")) {
                                case "undefined":
                                    break;
                                case "db error":
                                    break;
                                case "not found":
                                    break;
                                default:
                                    break;
                            }
                            changeButton.setClickable(true);
                            changeButton.setTextColor(getResources().getColor(R.color.black));
                        }

                    }
                });
            }
        };

        if(!GlobalSocket.mSocket.hasListeners("change_password_respond")) GlobalSocket.mSocket.on("change_password_respond", onPasswordChangeRespond);
    }

    private String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_change_password, menu);
//        return true;
//    }
//
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
