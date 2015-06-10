package com.droidsans.photo.droidphoto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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


public class SplashLoginActivity extends Activity {
    public static final String APP_LOG = "droidphoto";
    public static Context mContext;

    private View logoLayout, loginLayout;
    private EditText username, password;
    private Button loginBtn, registerBtn, bypassLoginBtn;

    private Emitter.Listener onLoginRespond;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_login);
        mContext = getApplicationContext();


        initialize();

        //startActivity(new Intent(this, FillPostActivity.class));
        //finish();
    }

    private void initialize() {
        findAllById();
        setupSocket();
        setupListener();
        autoLogin();
        setupSplashAnimation();
    }

    private void autoLogin() {
        if(GlobalSocket.getToken()!=""){
            Log.d("droidphoto", "Token: "+GlobalSocket.getToken());
            Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainIntent);
            username.setText("");
            password.setText("");
            finish();
        }
    }

    private void setupSocket() {
        GlobalSocket.initializeSocket();
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
                        try {
                            isSuccess = data.getBoolean("success");
                            message = data.getString("msg");
                            userObject = data.getJSONObject("userObj");
                            token = data.getString("_token");
                            Log.d(APP_LOG, "isSuccess: "+isSuccess);

                            if(isSuccess){
                                Log.d(APP_LOG, "Token: "+token);
                                GlobalSocket.initializeToken(token);
                                GlobalSocket.writeStringToFile(GlobalSocket.USERNAME, ((String) userObject.get("username")));
                                GlobalSocket.writeStringToFile(GlobalSocket.DISPLAY_NAME, ((String)userObject.get("disp_name")));
                                Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_SHORT).show();
                                Intent mainActIntent = new Intent(getApplicationContext(), MainActivity.class);
                                //TODO putExtra data return from server
                                startActivity(mainActIntent);
                                username.setText("");
                                password.setText("");
                                finish();
                            } else {
                                String toastString = "";
                                switch (message){
                                    case "authen failed":
                                        toastString = "Login failed, please check you username and password and try again";
                                        break;
                                    case "db error":
                                        toastString = "Database error, please try again later";
                                        break;
                                }
                                Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {return;}
                    }
                });
            }
        };

        if(!GlobalSocket.mSocket.hasListeners("login_respond")) GlobalSocket.mSocket.on("login_respond", onLoginRespond);
    }

    private void setupListener() {
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject loginStuff = new JSONObject();
                String hexPassword = "";

                try {
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    md.update(password.getText().toString().getBytes());
                    hexPassword = bytesToHex(md.digest());

                } catch (NoSuchAlgorithmException e) {
                }

                try {
                    loginStuff.put("login", username.getText().toString());
                    loginStuff.put("password", hexPassword);
                    loginStuff.put("_event", "login_respond");
                } catch (JSONException e) {}

                Log.d(APP_LOG, hexPassword);

                GlobalSocket.globalEmit("user.login", loginStuff);
            }

        });
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(registerIntent);
            }
        });
        bypassLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent bypass = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(bypass);
                finish();
            }
        });
    }

    private void setupSplashAnimation() {
        logoLayout.animate().yBy(-280).setDuration(1200).setStartDelay(1500).start();
        loginLayout.setVisibility(View.VISIBLE);
        loginLayout.animate().alpha(1).setDuration(700).setStartDelay(2000).start();
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

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

}
