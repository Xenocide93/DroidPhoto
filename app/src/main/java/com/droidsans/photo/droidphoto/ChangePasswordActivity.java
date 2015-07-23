package com.droidsans.photo.droidphoto;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.droidsans.photo.droidphoto.util.view.FontTextView;
import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class ChangePasswordActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FontTextView username;
    private EditText oldPassword, newPassword, confirmPassword;
    private Button changeButton;
    private Emitter.Listener onPasswordChangeRespond;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        username = (FontTextView) findViewById(R.id.change_password_username);
        oldPassword = (EditText) findViewById(R.id.change_password_old);
        newPassword = (EditText) findViewById(R.id.change_password_new);
        confirmPassword = (EditText) findViewById(R.id.change_password_confirm);
        changeButton = (Button) findViewById(R.id.button_confirm);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        username.append(getSharedPreferences(getString(R.string.userdata), MODE_PRIVATE).getString(getString(R.string.username), ""));
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
                        changePassObj.put("_event", "change_password");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    Log.d(APP_LOG, hexPassword);
                    if (!GlobalSocket.globalEmit("user.changepass", changePassObj)) {
                        Toast.makeText(getApplicationContext(), "cannot connect to server, please try again", Toast.LENGTH_SHORT).show();
                        changeButton.setClickable(true);
                        changeButton.setTextColor(getResources().getColor(R.color.black));
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
                            GlobalSocket.mSocket.off("change_password");
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

        if(!GlobalSocket.mSocket.hasListeners("change_password")) GlobalSocket.mSocket.on("change_password", onPasswordChangeRespond);
    }

    private String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    @Override
    protected void onDestroy() {
        if(GlobalSocket.mSocket.hasListeners("change_password")) GlobalSocket.mSocket.off("change_password");
        super.onDestroy();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_change_password, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_done:

                return true;
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
