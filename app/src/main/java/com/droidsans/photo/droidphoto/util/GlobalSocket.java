package com.droidsans.photo.droidphoto.util;

import android.os.Environment;
import android.util.Log;

import com.droidsans.photo.droidphoto.MainActivity;
import com.droidsans.photo.droidphoto.SplashLoginActivity;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Scanner;

public class GlobalSocket {
    public static final String TOKEN = "token";
    public static final String USERNAME = "username";
    public static final String DISPLAY_NAME = "displayName";

    public static Socket mSocket;
    private static String mToken;
    private static String mUsername;
    private static String mDisplayName;


    public static boolean initializeSocket(){
        if(mSocket==null){
            try {
                mSocket = IO.socket("http://209.208.65.102:3000");
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return false; //wrong URL
            }
        }

        if(!mSocket.connected()){
            mSocket.connect();
            if(!mSocket.connected()) return false; //cannot connect to server
        }

        return true;
    }

    public static boolean globalEmit(String event, JSONObject obj){
        initializeSocket();
        Log.d("droidphoto", "Emit: "+event);

        try {
            if(!event.equals("user.register") && !event.equals("user.login")) obj.put("_token", mToken==null? getToken(): mToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mSocket.emit(event, obj);
        return true;
    }

    public static void initializeToken(String token){
        mToken = token;

        File tokenFile = new File(SplashLoginActivity.mContext.getExternalFilesDir(null), "token");

        try {
            InputStream is = new ByteArrayInputStream(token.getBytes());
            OutputStream os = new FileOutputStream(tokenFile);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();

            Log.d("droidphoto", "from tokenFile: " + getToken());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getStringFromFile(String tag){
        switch (tag){
            case TOKEN: if(mToken!="")return mToken; break;
            case USERNAME: if(mUsername!="")return mUsername; break;
            case DISPLAY_NAME: if(mDisplayName!="")return mDisplayName; break;
        }

        File file = new File(SplashLoginActivity.mContext.getExternalFilesDir(null), tag);
        StringBuilder fileContents = new StringBuilder((int)file.length());
        Scanner scanner = null;

        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            while(scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine());
            }
            String s = fileContents.toString();
            switch (tag){
                case TOKEN: mToken=s; break;
                case USERNAME: mUsername=s; break;
                case DISPLAY_NAME: mDisplayName=s; break;
            }
            return s;
        } finally {
            scanner.close();
        }
    }

    public static void writeStringToFile(String tag, String value){
        switch (tag){
            case TOKEN: mToken = value; break;
            case USERNAME: mUsername = value; break;
            case DISPLAY_NAME: mDisplayName = value; break;
        }

        File file = new File(SplashLoginActivity.mContext.getExternalFilesDir(null), tag);

        try {
            InputStream is = new ByteArrayInputStream(value.getBytes());
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getToken(){
        File tokenFile = new File(SplashLoginActivity.mContext.getExternalFilesDir(null), "token");
        StringBuilder fileContents = new StringBuilder((int)tokenFile.length());
        Scanner scanner = null;

        try {
            scanner = new Scanner(tokenFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        }

        try {
            while(scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine());
            }
            mToken = fileContents.toString();
            return mToken;
        } finally {
            scanner.close();
        }

    }
}