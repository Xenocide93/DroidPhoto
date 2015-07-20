package com.droidsans.photo.droidphoto.util.retrofit;

import retrofit.Callback;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

/**
 * Created by Froztic on 7/20/2015.
 */
public interface AvatarPostService {
    @Multipart
    @POST("/avatar")
    void postAvatar(@Part("avatar") TypedFile file, @Part("_token") String token, Callback<UploadResponseModel> callback);
}
