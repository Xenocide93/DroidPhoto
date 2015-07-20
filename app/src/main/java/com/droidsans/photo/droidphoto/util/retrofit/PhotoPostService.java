package com.droidsans.photo.droidphoto.util.retrofit;

import retrofit.Callback;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

/**
 * Created by Froztic on 7/17/2015.
 */
public interface PhotoPostService {
    @Multipart
    @POST("/photo")
    void postPhoto(@Part("image") TypedFile file, @Part("_token") String token, Callback<UploadResponseModel> callback);

//    public String post(@Body String body, Callback<JSONObject> repsonse);
}
