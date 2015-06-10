package com.droidsans.photo.droidphoto;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;

import org.json.JSONException;
import org.json.JSONObject;


import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class RegisterActivity extends Activity {
    EditText username, password, passwordConfirm, email, displayName;
    Button registerBtn;

    private Emitter.Listener onRegisterRespond;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initialize();
    }

    private void initialize() {
        findAllById();
        setupListener();
        setupSocket();
    }

    private void setupSocket() {
        GlobalSocket.initializeSocket();
        onRegisterRespond = new Emitter.Listener() {
            @Override
            public void call(final Object[] args) {
                RegisterActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        Boolean isSuccess;
                        String message;
                        try {
                            isSuccess = data.getBoolean("success");
                            message = data.getString("msg");
                            Log.d("droidphoto", "isSuccess: " + isSuccess + " Message: " + message);
                            if(isSuccess){
                                Toast.makeText(getApplicationContext(), "Register Successful", Toast.LENGTH_SHORT).show();
                                Intent mainActIntent = new Intent(getApplicationContext(), MainActivity.class);
                                //TODO putExtra data return from server
                                startActivity(mainActIntent);
                            } else {
                                String toastString = "";
                                switch (message){
                                    case "email exist":
                                        toastString = "This email address has already been used";
                                        break;
                                    case "username exist":
                                        toastString = "This username has already been used";
                                        break;
                                    case "both exist":
                                        toastString = "Both username and email have already been used";
                                        break;
                                    case "db error":
                                        toastString = "Database error, please try again later";
                                        break;
                                    case "cannot save":
                                        toastString = "Data cannot be save into database, please try again later";
                                        break;
                                    case "undefined":
                                        toastString = "Unidentified error, please try again later";
                                        break;
                                }
                                Toast.makeText(getApplicationContext(),toastString, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {return;}
                    }
                });
            }
        };

        if(!GlobalSocket.mSocket.hasListeners("register_respond")) GlobalSocket.mSocket.on("register_respond", onRegisterRespond);
    }

    private void setupListener() {
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!password.getText().toString().equals(passwordConfirm.getText().toString())) {
                    Toast.makeText(getApplicationContext(),
                            "Missmatched comfirm password, please retype the password",
                            Toast.LENGTH_SHORT).show();
                    password.setText("");
                    passwordConfirm.setText("");
                } else {
                    JSONObject registerStuff = new JSONObject();
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
                    } catch (JSONException e) {}

                    GlobalSocket.globalEmit("user.register", registerStuff);
                }
            }
        });
    }

    private void findAllById() {
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        passwordConfirm = (EditText) findViewById(R.id.password_confirm);
        email = (EditText) findViewById(R.id.email);
        displayName = (EditText) findViewById(R.id.display_name);

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