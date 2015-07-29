package com.droidsans.photo.droidphoto;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;

import org.json.JSONException;
import org.json.JSONObject;


import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;


public class RegisterActivity extends AppCompatActivity {
    EditText username, password, passwordConfirm, email, displayName;
    TextView tosLink;
    Button registerBtn;

    public static Activity mRegisterActivity;

    private Emitter.Listener onRegisterRespond;
    private Handler delayAction = new Handler();
    private Runnable timeout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initialize();
    }

    private void initialize() {
        RegisterActivity.mRegisterActivity = this;
        findAllById();
        initHint();
        setupListener();
        setupSocket();
    }

    private void initHint() {
        tosLink.append(" ");
        final Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
        intent.putExtra("URL", getString(R.string.url_terms_of_service));
        intent.putExtra("Title", getString(R.string.title_activity_terms_of_service));
        SpannableString linktext = new SpannableString(getString(R.string.register_accept_tos_link));
        linktext.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                widget.getContext().startActivity(intent);
            }
        }, 0, getString(R.string.register_accept_tos_link).length(), 0);
        tosLink.append(linktext);
        tosLink.setMovementMethod(new LinkMovementMethod());
        //tosLink.setText(Html.fromHtml(getString(R.string.register_accept_tos) + "<font color='" + getResources().getColor(R.color.link) + "'>" + getString(R.string.register_accept_tos_link) + "</font>"));
    }

    private void setupSocket() {
        GlobalSocket.initializeSocket();
        onRegisterRespond = new Emitter.Listener() {
            @Override
            public void call(final Object[] args) {
                RegisterActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        delayAction.removeCallbacks(timeout);
                        JSONObject data = (JSONObject) args[0];
                        Boolean isSuccess;
                        String message;
                        try {
                            isSuccess = data.getBoolean("success");
                            message = data.getString("msg");
                            Log.d("droidphoto", "isSuccess: " + isSuccess + " Message: " + message);
                            if(isSuccess){
//                                Toast.makeText(getApplicationContext(), "Register Successful", Toast.LENGTH_SHORT).show();
//                                Snackbar.make(null, "Register Success", Snackbar.LENGTH_SHORT).show();
//                                Intent mainActIntent = new Intent(getApplicationContext(), MainActivity.class);
//                                startActivity(mainActIntent);
//                                finish();
                                MainActivity.snackString = R.string.snackbar_register_success;
                                JSONObject loginStuff = new JSONObject();
                                String hexPassword = "";

                                try {
                                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                                    md.update(password.getText().toString().getBytes());
                                    hexPassword = bytesToHex(md.digest());
                                } catch (NoSuchAlgorithmException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    loginStuff.put("login", username.getText().toString());
                                    loginStuff.put("password", hexPassword);
                                    loginStuff.put("_event", "login_respond");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if(!GlobalSocket.mSocket.connected()) {
//                                    Toast.makeText(getApplicationContext(), "cannot connect to server", Toast.LENGTH_SHORT).show();
                                    Snackbar.make(displayName, getString(R.string.snackbar_login_cannot_connect), Snackbar.LENGTH_LONG)
                                            .setAction("OK", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                }
                                            })
                                            .show();
                                } else if(GlobalSocket.mSocket.hasListeners("login_respond")) {
                                    GlobalSocket.globalEmit("user.login", loginStuff); //this automatic finish() this activity
                                } else {
//                                    Toast.makeText(getApplicationContext(), "no login respond listener", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                String toastString = "";
                                switch (message){
                                    case "null required field":
                                        toastString = getString(R.string.snackbar_register_null_required);
                                        break;
                                    case "invalid email":
                                        toastString = getString(R.string.snackbar_register_email_invalid);
                                        break;
                                    case "email exist":
                                        toastString = getString(R.string.snackbar_register_email_exist);
                                        break;
                                    case "username exist":
                                        toastString = getString(R.string.snackbar_register_username_exist);
                                        break;
//                                    case "both exist": //deprecated
//                                        toastString = "Both username and email have already been used";
//                                        break;
                                    case "db error":
                                        toastString = getString(R.string.snackbar_register_db_error);
                                        break;
                                    case "cannot save":
                                        toastString = getString(R.string.snackbar_register_cannot_save);
                                        break;
                                    case "undefined":
                                        toastString = "Unidentified error, please try again later";
                                        break;
                                }
//                                Toast.makeText(getApplicationContext(),toastString, Toast.LENGTH_SHORT).show();
                                Snackbar.make(findViewById(R.id.register_layout), toastString, Snackbar.LENGTH_SHORT)
                                        .setAction("ok", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {}
                                        })
                                        .show();
                                registerBtn.setClickable(true);
                                registerBtn.setTextColor(getResources().getColor(R.color.primary_color));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        if(!GlobalSocket.mSocket.hasListeners("register_respond")) GlobalSocket.mSocket.on("register_respond", onRegisterRespond);
    }

    private void setupListener() {
        if(!registerBtn.hasOnClickListeners()) {
            registerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    emitregister();
                }
            });
        }

        timeout = new Runnable() {
            @Override
            public void run() {
                GlobalSocket.reconnect();
                registerBtn.setClickable(true);
                registerBtn.setTextColor(getResources().getColor(R.color.primary_color));
                Snackbar.make(registerBtn, getString(R.string.snackbar_connection_timeout), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.snackbar_action_retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                emitregister();
                            }
                        })
                        .show();
            }
        };
    }

    private void emitregister() {
        registerBtn.setClickable(false);
        registerBtn.setTextColor(getResources().getColor(R.color.light_gray));
        Log.d("droidphoto", "lastindexof : " + email.getText().toString().lastIndexOf(".") + "| length : " + email.getText().toString().length());
        if(username.getText().toString().length() == 0 || password.getText().toString().length() == 0 || passwordConfirm.getText().toString().length() == 0 || email.getText().toString().length() == 0 || displayName.getText().toString().length() == 0) {
            Snackbar.make(registerBtn, getString(R.string.snackbar_register_null_required), Snackbar.LENGTH_LONG).show();
            registerBtn.setClickable(true);
            registerBtn.setTextColor(getResources().getColor(R.color.primary_color));
        } else if (!password.getText().toString().equals(passwordConfirm.getText().toString())) {
//                        Toast.makeText(getApplicationContext(), "Mismatched comfirm password, please retype the password", Toast.LENGTH_SHORT).show();
            Snackbar.make(findViewById(R.id.register_layout), getString(R.string.snackbar_changepass_mismatch), Snackbar.LENGTH_LONG).show();
            password.setText("");
            passwordConfirm.setText("");
            registerBtn.setClickable(true);
            registerBtn.setTextColor(getResources().getColor(R.color.primary_color));
        } else if (username.getText().toString().length() > getResources().getInteger(R.integer.max_username_size)) {
            Snackbar.make(username, getString(R.string.snackbar_register_username_toolong), Snackbar.LENGTH_LONG).show();
            registerBtn.setClickable(true);
            registerBtn.setTextColor(getResources().getColor(R.color.primary_color));
        } else if (password.getText().toString().length() < getResources().getInteger(R.integer.min_password_size)) {
            Snackbar.make(password, getString(R.string.snackbar_changepass_tooshort), Snackbar.LENGTH_LONG).show();
            registerBtn.setClickable(true);
            registerBtn.setTextColor(getResources().getColor(R.color.primary_color));
//        } else if (!email.getText().toString().contains("@") ||
//                !email.getText().toString().contains(".") ||
//                email.getText().toString().lastIndexOf(".") < email.getText().toString().length() - 4) {
        } else if(!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            Snackbar.make(email, getString(R.string.snackbar_register_email_invalid), Snackbar.LENGTH_LONG).show();
            registerBtn.setClickable(true);
            registerBtn.setTextColor(getResources().getColor(R.color.primary_color));
        } else if (displayName.getText().toString().length() > getResources().getInteger(R.integer.max_profilename_size)) {
            Snackbar.make(username, getString(R.string.snackbar_register_profilename_toolong), Snackbar.LENGTH_LONG).show();
            registerBtn.setClickable(true);
            registerBtn.setTextColor(getResources().getColor(R.color.primary_color));
        } else {
            final JSONObject registerStuff = new JSONObject();
            String hexPassword = "";

            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(password.getText().toString().getBytes());
                hexPassword = bytesToHex(md.digest());
            } catch (NoSuchAlgorithmException e) {
            }

            try {
                registerStuff.put("username", username.getText().toString());
                registerStuff.put("password", hexPassword);
                registerStuff.put("email", email.getText().toString());
                registerStuff.put("disp_name", displayName.getText().toString());
                registerStuff.put("_event", "register_respond");
            } catch (JSONException e) {
            }

            if (!GlobalSocket.globalEmit("user.register", registerStuff)) {
                delayAction.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!GlobalSocket.globalEmit("user.register", registerStuff)) {
                            registerBtn.setClickable(true);
                            registerBtn.setTextColor(getResources().getColor(R.color.primary_color));
                        } else {
                            delayAction.postDelayed(timeout, 10000);
                        }
                    }
                }, 2000);
            } else {
                delayAction.postDelayed(timeout, 10000);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if(GlobalSocket.mSocket.hasListeners("register_respond")) {
            GlobalSocket.mSocket.off("register_respond");
        }
        delayAction.removeCallbacks(timeout);
        username.setText("");
        password.setText("");
        passwordConfirm.setText("");
        email.setText("");
        displayName.setText("");

        super.onDestroy();
    }

    private void findAllById() {
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        passwordConfirm = (EditText) findViewById(R.id.password_confirm);
        email = (EditText) findViewById(R.id.email);
        displayName = (EditText) findViewById(R.id.display_name);
        tosLink = (TextView) findViewById(R.id.tos_link);

        registerBtn = (Button) findViewById(R.id.register_btn);
    }

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

}
