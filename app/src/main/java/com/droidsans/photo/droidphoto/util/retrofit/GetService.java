package com.droidsans.photo.droidphoto.util.retrofit;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Streaming;
import retrofit.mime.TypedOutput;

/**
 * Created by Froztic on 7/24/2015.
 */
public interface GetService {
    @GET("/data/photo/original/{photoURL}")
    @Streaming
    void getOriginal(@Path("photoURL") String photoURL, Callback<Response> callback);
}
