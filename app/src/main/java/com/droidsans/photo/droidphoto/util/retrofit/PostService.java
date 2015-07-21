package com.droidsans.photo.droidphoto.util.retrofit;

import retrofit.Callback;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedFile;

/**
 * Created by Froztic on 7/17/2015.
 */
public interface PostService {
    @Multipart
    @POST("/photo")
    void postPhoto(@Part("image") TypedFile file, @Part("_token") String token, Callback<UploadResponseModel> callback);

    @Multipart
    @POST("/avatar")
    void postAvatar(@Part("avatar") TypedFile file, @Part("old_avatar_url") String oldAvatarURL, @Part("_token") String token, Callback<AvatarResponseModel> callback);

//    public String post(@Body String body, Callback<JSONObject> repsonse);
}
