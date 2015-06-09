package com.droidsans.photo.droidphoto.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class Multipart2 {

    public static String post(String URLString, String imagePath){
        String urlToConnect = URLString;
        String paramToSend = "fubar";
        File fileToUpload = new File(imagePath);
        String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.

        URLConnection connection = null;
        try {
            connection = new URL(urlToConnect).openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.setDoOutput(true); // This sets request method to POST.
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));

            writer.println("--" + boundary);
            writer.println("Content-Disposition: form-data; name=\"paramToSend\"");
            writer.println("Content-Type: text/plain; charset=UTF-8");
            writer.println();
            writer.println(paramToSend);

            writer.println("--" + boundary);
            writer.println("Content-Disposition: form-data; name=\"fileToUpload\"; filename=\"file.jpeg\"");
            writer.println("Content-Type: image/jpeg; charset=UTF-8");
            writer.println();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileToUpload), "UTF-8"));
                for (String line; (line = reader.readLine()) != null;) {
                    Log.d("droidphoto", line);
                    writer.println(line);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) try { reader.close(); } catch (IOException logOrIgnore) {}
            }

            writer.println("--" + boundary + "--");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) writer.close();
        }

        // Connection is lazily executed whenever you request any status.
        String message = "";
        try {
            message = ((HttpURLConnection) connection).getResponseMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return message;
    }
}
