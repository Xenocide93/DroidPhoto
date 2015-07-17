package com.droidsans.photo.droidphoto;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class SplashLoginActivity extends Activity {
    public static final String APP_LOG = "droidphoto";
    public static Context mContext;

    private View logoLayout, loginLayout;
    private EditText username, password;
    private Button loginBtn, registerBtn, bypassLoginBtn;

    private Emitter.Listener onLoginRespond;

    private Handler delayAction = new Handler();
    private Runnable timeout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_login);
        mContext = getApplicationContext();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        initialize();

        //startActivity(new Intent(this, FillPostActivity.class));
        //finish();
    }

    private void initialize() {
        findAllById();
        setupSocket();
        autoLogin();
        setupSplashAnimation();
        setupListener();
    }

    private void autoLogin() {
        String token = getSharedPreferences(getString(R.string.userdata), Context.MODE_PRIVATE).getString(getString(R.string.token), "");
        if(!token.equals("")) {
            Log.d("droidphoto", "Token: " + token);
//        if(GlobalSocket.getToken()!=""){
//            Log.d("droidphoto", "Token: "+GlobalSocket.getToken());
            Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainIntent);
            username.setText("");
            password.setText("");
            finish();
        }
    }

    private void setupSocket() {
        GlobalSocket.initializeSocket();
        if(onLoginRespond == null) {
            onLoginRespond = new Emitter.Listener() {
                @Override
                public void call(final Object[] args) {
                    SplashLoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject data = (JSONObject) args[0];
                            Boolean isSuccess;
                            String message;
                            JSONObject userObject;
                            String token;

                            isSuccess = data.optBoolean("success");
                            message = data.optString("msg");
                            Log.d(APP_LOG, "isSuccess: " + isSuccess);
                            if (isSuccess) {
                                userObject = data.optJSONObject("userObj");
                                token = data.optString("_token");

                                GlobalSocket.mSocket.off("login_respond");
                                Log.d(APP_LOG, "Token: " + token);
//                                SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
                                //TODO base64 encode username/disp_name
                                getSharedPreferences(getString(R.string.userdata), Context.MODE_PRIVATE).edit()
                                        .putString(getString(R.string.token), token)
                                        .putString(getString(R.string.user_id), userObject.optString("_id"))
                                        .putString(getString(R.string.username), userObject.optString("username"))
//                                        .putString(getString(R.string.display_name), userObject.optString("disp_name"))
                                        .apply();
//                                GlobalSocket.initializeToken(token);
//                                GlobalSocket.writeStringToFile(GlobalSocket.USERNAME, ((String) userObject.get("username")));
//                                GlobalSocket.writeStringToFile(GlobalSocket.DISPLAY_NAME, ((String)userObject.get("disp_name")));
//                                Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_SHORT).show();
                                Snackbar.make(loginLayout, getString(R.string.snackbar_login_success), Snackbar.LENGTH_SHORT).show();
                                Intent mainActIntent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(mainActIntent);
                                username.setText("");
                                password.setText("");
                                if(RegisterActivity.mRegisterActivity!=null){
                                    RegisterActivity.mRegisterActivity.finish();
                                }
                                finish();
                            } else {
                                String toastString = " ";
                                switch (message) {
                                    case "authen failed":
                                        toastString = getString(R.string.snackbar_login_authen_failed);
                                        break;
                                    case "db error":
                                        toastString = getString(R.string.snackbar_login_db_error);
                                        break;
                                }
//                                Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_SHORT).show();
                                Snackbar.make(loginLayout, toastString, Snackbar.LENGTH_LONG)
                                        .setAction("OK", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                            }
                                        })
//                                        .setActionTextColor(getResources().getColor(R.color.accent_color))
                                        .show();
                                loginBtn.setClickable(true);
                                loginBtn.setTextColor(getResources().getColor(R.color.primary_color));
                            }

                        }
                    });
                }
            };
        }

        if(!GlobalSocket.mSocket.hasListeners("login_respond")) GlobalSocket.mSocket.on("login_respond", onLoginRespond);
    }

    private void emitlogin() {
        final JSONObject loginStuff = new JSONObject();
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
//                    Log.d(APP_LOG, hexPassword);

        if(!GlobalSocket.globalEmit("user.login", loginStuff)) {
//                        Toast.makeText(getApplicationContext(), "cannot connect to server", Toast.LENGTH_SHORT).show();
            delayAction.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!GlobalSocket.globalEmit("user.login", loginStuff)) {
                        Snackbar.make(loginLayout, getString(R.string.snackbar_login_cannot_connect),Snackbar.LENGTH_LONG)
                                .setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                    }
                                })
                                .show();
                        loginBtn.setClickable(true);
                        loginBtn.setTextColor(getResources().getColor(R.color.primary_color));
                    } else {
                        delayAction.postDelayed(timeout, 5000);
                    }
                }
            }, 2000);
        } else {
            delayAction.postDelayed(timeout, 5000);
        }
    }

    private void setupListener() {
        if(!loginBtn.hasOnClickListeners()) {
            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loginBtn.setClickable(false);
                    loginBtn.setTextColor(getResources().getColor(R.color.light_gray));
                    emitlogin();
                }

            });
        }
        if(!registerBtn.hasOnClickListeners()) {
            registerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    registerBtn.setClickable(false);
                    Intent registerIntent = new Intent(getApplicationContext(), RegisterActivity.class);
                    startActivity(registerIntent);
                    registerBtn.setClickable(true);
                    loginBtn.setClickable(true);
//                    finish();
                }
            });
        }
        bypassLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent bypass = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(bypass);
                finish();
            }
        });

        timeout = new Runnable() {
            @Override
            public void run() {
                GlobalSocket.reconnect();
                loginBtn.setClickable(true);
                loginBtn.setTextColor(getResources().getColor(R.color.primary_color));
                Snackbar.make(loginLayout, "connection timeout. retry ?", Snackbar.LENGTH_LONG)
                        .setAction("retry", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loginBtn.setClickable(false);
                                loginBtn.setTextColor(getResources().getColor(R.color.light_gray));
                                emitlogin();
                            }
                        })
                        .show();
            }
        };
    }

    private void setupSplashAnimation() {
        logoLayout.animate()
                .yBy(-280 * getResources().getDisplayMetrics().densityDpi / 400)
                .setDuration(1200).setStartDelay(1500).start();
//        loginLayout.animate().translationY(0).setDuration(1200).setStartDelay(1500).start();
        loginLayout.setVisibility(View.VISIBLE);
        loginLayout.animate().alpha(1).setDuration(700).setStartDelay(2000).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                username.setEnabled(true);
                password.setEnabled(true);
                loginBtn.setClickable(true);
                registerBtn.setClickable(true);
                setupListener();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        username.setText("");
        password.setText("");

        if(GlobalSocket.mSocket.hasListeners("login_respond")) {
            GlobalSocket.mSocket.off("login_respond");
        }

        super.onDestroy();
    }

    private void findAllById() {
        logoLayout = findViewById(R.id.logo_layout);
        loginLayout = findViewById(R.id.login_layout);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        loginBtn = (Button) findViewById(R.id.login_btn);
        registerBtn = (Button) findViewById(R.id.register_btn);
        bypassLoginBtn = (Button) findViewById(R.id.bypass_login);
    }

    private String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

}
